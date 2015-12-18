#!/usr/bin/python

import functools
import json
import sys
import uuid
import datetime

import flask
from flask import request, Blueprint

from flask.ext.bcrypt import Bcrypt
from flask.ext.sqlalchemy import SQLAlchemy
import sqlalchemy
from flask_socketio import SocketIO, send, emit, join_room, leave_room, disconnect

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
clients = []
users_typing = []

class User(db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    last_active_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    password_hash = db.Column(db.String(60))

    def __init__(self, username):
        self.username = username
        
    def __repr__(self):
        return '<User(id=%r, username=%s)>' % (self.id, self.username)
        
    def authenticate(self, password):
        u = User.query.filter_by(username=self.username).first()
        if(u == None):
            return None
            
        if bcrypt.check_password_hash(u.password_hash, password):
            return self.generate_session()
        
        return None
    
    def generate_session(self):
        session = UserSession(self.id)
        db.session.add(session)
        db.session.commit()
        return session.hash
    
    @classmethod
    def session_in_user(cls, user_id, session_hash):
        user = cls.query.get(user_id)
        if user is not None:
            s = UserSession.query.filter_by(user_id=user.id, hash=session_hash).first()
            if s is not None:
                return True
        else:
            print("Username with id=%r does not exist.") % user_id
        return False
        
    def remove_session(self, hash):
        s = UserSession.query.filter_by(hash=hash).first()
        if s is not None:
            db.session.delete(s)
            db.session.commit()
            return True
        return False
            
    def register(self, password):
        self.password_hash = bcrypt.generate_password_hash(password)
        db.session.add(self)
        db.session.commit()
        return self.generate_session()

class UserSession(db.Model):
    __tablename__ = 'user_sessions'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, nullable=False)
    hash = db.Column(db.String(32), unique=True, nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    
    def __init__(self, user_id):
        self.user_id = user_id
        self.hash = str(uuid.uuid4().get_hex())
        
    def __repr__(self):
        return '<UserSession(user_id=%r, hash=%s)>' % (self.user_id, self.hash)

class Friend(db.Model):
    __tablename__ = 'friends'

    id = db.Column(db.Integer, primary_key=True)
    req_user_id = db.Column(db.Integer, nullable=False)
    res_user_id = db.Column(db.Integer, nullable=False)
    # Is it a friend request? False if they are friends, True otherwise
    request = db.Column(db.Boolean, nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    
    def __init__(self, req_user_id, res_user_id, request=True):
        self.req_user_id = req_user_id
        self.res_user_id = res_user_id
        self.request = request
        
    def __repr__(self):
        return '<Friend(req_user_id=%r, res_user_id=%r, request=%r)>' % (self.req_user_id, self.res_user_id, self.request)

class Room(db.Model):
    __tablename__ = 'rooms'
    
    id = db.Column(db.Integer, primary_key=True)
    room_name = db.Column(db.String(20), nullable=False, unique=True)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    created_by_user_id = db.Column(db.Integer, nullable=False)
    
    def __init__(self, room_name, created_by_user_id):
        self.room_name = room_name
        self.created_by_user_id = created_by_user_id
        
    def __repr__(self):
        return '<Room(id=%r, room_name=%s, created_by_user_id=%r)>' % (self.id, self.room_name, self.created_by_user_id)

class Message(db.Model):
    __tablename__ = 'messages'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, nullable=False)
    
    # 0 for rooms, 1 for friends
    type = db.Column(db.Integer, nullable=False)
    # if type=0, id is the room's id
    # if type=1, id is the friend's user id
    other_id = db.Column(db.Integer, nullable=False)
    
    message = db.Column(db.String(2500), nullable=False)
    date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    
    def __init__(self, user_id, message, type, other_id):
        self.user_id = user_id
        self.message = message
        self.type = type
        self.other_id = other_id
        
    def __repr__(self):
        return '<Message(user_id=%r, message=%s, type=%r, other_id=%r)>' % (self.user_id, self.message, self.type, self.other_id)

def authenticated_only_http(f):
    @functools.wraps(f)
    def wrapped(*args, **kwargs):
        data = json.loads(request.data)
        user_id = data.get("user_id")
        session = data.get("session")
        
        if user_id is None or session is None:
            print "Failed auth: Did not supply credentials"
            return
        elif not User.session_in_user(user_id, session):
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
            session = user.authenticate(password)
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
        session = user.register(password)
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
                user.remove_session(session)
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
        
        if not User.session_in_user(user_id, session):
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

if __name__ == "__main__":
    db.create_all()
    app.register_blueprint(main)
    app.debug = True
    socketio.run(app, host="0.0.0.0")
