import functools
import json
import sys
import datetime

import flask
from flask import request, Blueprint

from flask.ext.bcrypt import Bcrypt
from flask_socketio import SocketIO, send, emit, join_room, leave_room, disconnect
from flask.ext.sqlalchemy import SQLAlchemy
import sqlalchemy

from flask import Flask
app = Flask(__name__)
main = Blueprint('main', __name__)
app.debug = True

# credentials
import credentials

app.config['SECRET_KEY'] = credentials.SECRET_KEY
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+mysqldb://{}:{}@{}/{}'.format(credentials.mysql["user"], credentials.mysql["password"], credentials.mysql["host"], credentials.mysql["dbname"])

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
socketio = SocketIO(app)

#from model import *
from model import *

clients = []
users_typing = []

def authenticated_only_http(f):
    @functools.wraps(f)
    def wrapped(*args, **kwargs):
        data = json.loads(request.data)
        user_id = data.get("user_id")
        session = data.get("session")
        
        if user_id is None or session is None:
            print "Failed auth: Did not supply credentials"
            return
        elif not User.session_in_user(user_id, session, UserSession):
            print "Failed auth: Not in session"
            return
        return f(*args, **kwargs)
    
    return wrapped
    
## The password is checked here
@main.route('/login', methods=['POST'])
def login():
    print "Someone is logging in..."
    auth = json.loads(request.data)

    if auth is not None:
        username, password = auth['username'], auth['password']
        user = User.query.filter_by(username=username).first()
        if user is not None:
            session = user.authenticate(password, UserSession)
            if session != None:
                return json.dumps({"authenticated": True, "user_id": user.id, "session": session})
            return json.dumps({"authenticated": False, "reason": "Either session is invalid or session has expired."})
        return json.dumps({"authenticated": False, "reason": "Username does not exist."})
    return json.dumps({"authenticated": False, "reason": "Failed to collect credentials."})
    
@main.route('/signup', methods=['POST'])
def signup():
    print "Someone is signing up..."
    cred = json.loads(request.data)
    
    if cred is not None:
        username, password = cred['username'], cred['password']
        user = User(username)
        session = user.register(password, UserSession)
        if session != None:
            return json.dumps({"registered": True, "user_id": user.id, "session": session})
    return json.dumps({"registered": False})

@main.route('/fetchrooms', methods=['POST'])
@authenticated_only_http
def fetchrooms():
    global Room
    global User
    global db
    rooms = []
    for room in Room.query.all():
        created_by_user = User.query.get(room.created_by_user_id)
        if created_by_user is None:
            created_by_user = {"id": -1, "username": "deleted"}
        rooms.append({
            "room_id": room.id,
            "room_name": room.room_name,
            "user_id": created_by_user.id,
            "username": created_by_user.username,
            "date": str(room.created_date)
        })

    return json.dumps({"rooms": rooms})

@main.route('/createroom', methods=['POST'])
@authenticated_only_http
def createroom():
    data = json.loads(request.data)
    
    if data is not None:
        global Room, User, db
        print "%r is creating room '%r'" % (data.get("username"), data.get("room_name"))
        try:
            room = Room(data.get('room_name'), data.get("user_id"))
            db.session.add(room)
            db.session.commit()
        except sqlalchemy.exc.IntegrityError as e:
            code, msg = e.orig
            c = {"created": False}
            if code == 1062:
                c["error"] = "Room already exists."
            return c
        return json.dumps({"created": True, "room_id": room.id})
    return json.dumps({"created": False})

@main.route('/logout', methods=['POST'])
@authenticated_only_http
def logout():
    cred = json.loads(request.data)    
    if cred is not None:
        username, user_id, session = cred.get('username'), cred.get('user_id'), cred.get('session')
        if username is not None and user_id is not None and session is not None:
            print "%r is logging out..." % username
            user = User.query.get(user_id)
            if user is not None:
                print "Removing %s " % user
                user.remove_session(session, UserSession)
                return json.dumps({"logged out": True})
    return json.dumps({"logged out": False})
 
def authenticated_only(f):
    @functools.wraps(f)
    def wrapped(*args, **kwargs): 
        if(len(args) == 0):
            print "Failed auth: Did not supply credentials"
            disconnect()
    
        user_id = args[0].get("user_id")
        session = args[0].get("session")
        
        if not User.session_in_user(user_id, session, UserSession):
            print "Failed auth: Not in session"
            disconnect()
        else:
            return f(*args, **kwargs)
    return wrapped
    
@socketio.on('join')
@authenticated_only
def connected(data):
    username = data.get("username")
    room_name = data.get("room_name")
    room_id = data.get('room_id')
    if Room.query.get(room_id) is None:
        emit("error", "Room '%s' does not exist" % room_name)
        disconnect()
    else:
        join_room("room" + str(room_id))
        print(username + " has entered room '%s' (id=%r)." % (room_name, room_id))

@socketio.on('leave')
@authenticated_only
def disconnected(data):
    stopped_typing_handler(data.get("username"), data.get("room_id"))
    username = data.get('username')
    room_name = data.get('room_name')
    room_id = data.get('room_id')
    leave_room("room" + str(room_id))
    print(username + " has left room '%s' (id=%r)." % (room_name, room_id))

@socketio.on('fetch messages')
@authenticated_only
def fetch_messages(data):
    print "fetching messages..."
    global Message
    global Room
    global User
    messages = []
    
    room_id = data.get('room_id')
    db_msg_list = Message.query.order_by(Message.id.desc()).filter_by(other_id=room_id, type=int(data.get('type')))
    
    if data.get("datetimeutc") is not None:
        utc_time = datetime.datetime.strptime(data.get("datetimeutc"), "%Y-%m-%d %H:%M:%S")
        db_msg_list = db_msg_list.filter(Message.date < utc_time)
    
    db_msg_list = db_msg_list.limit(25).all()
    
    for message in db_msg_list:
        messages.append({
            "username": User.query.get(message.user_id).username,
            "user_id": message.user_id,
            "message": message.message,
            "datetimeutc": str(message.date)
        })
    
    messages.reverse()
    history = {"history": messages}
    if(len(messages) > 0):
         history["earliest_datetimeutc"] = messages[0]["datetimeutc"]
    
    emit("history", history)

@socketio.on('typing')
@authenticated_only
def started_typing(data):
    users_typing.append(data.get('username'))
    #emit("typing", data.get('username'), broadcast=True)
    emit("typing", data.get('username'), room="room" + str(data.get('room_id')), include_self=False)

@socketio.on('stop typing')
@authenticated_only
def stopped_typing(data):
    stopped_typing_handler(data.get("username"), data.get("room_id"))

def stopped_typing_handler(username, room_id):
    if username in users_typing:
        users_typing.remove(username)
        emit("stop typing", username, room="room" + str(room_id), include_self=False)

@socketio.on('send message')
@authenticated_only
def handle_sent_message(data):
    print('received message: ' + str(data))
    
    global User
    global Message
    global Room
    message = Message(data.get('user_id'), data.get('message'), data.get('type'), data.get('room_id'))
    data["datetimeutc"] = str(message.date)
    global db
    db.session.add(message)
    db.session.commit()
    
    emit("received message", data, room="room" + str(data.get('room_id')))
    return "Success!: Server successfully received message."
