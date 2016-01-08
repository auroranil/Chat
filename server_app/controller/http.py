import logging

from app import main, app
from model import *

import sqlalchemy
from flask import request, render_template, jsonify

from datetime import datetime, timedelta
import functools
import json

def authenticated_only_http(f):
    @functools.wraps(f)
    def wrapped(*args, **kwargs):
        data = request.data
        user_id = data.get("user_id")
        session = data.get("session")
        
        if user_id is None or session is None:
            logging.info("Failed auth: Did not supply credentials")
            return
        elif not User.session_in_user(user_id, session, UserSession):
            logging.info("Failed auth: Not in session")
            return
        return f(*args, **kwargs)
    
    return wrapped

def render_template_custom(path,**kwargs):
    return render_template(path, session=request.cookies.get("session"), **kwargs)

@main.route('/', methods=['GET'])
def welcome():
    logging.info(str(request.cookies))
    return render_template_custom('welcome.html')
        
## The password is checked here
@main.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'GET':
        return render_template_custom('login.html')
    elif request.method == 'POST':
        auth = json.loads(request.data)

        if auth is not None:
            username, password = auth['username'], auth['password']
            user = User.query.filter_by(username=username).first()
            
            if user is not None:
                session = user.authenticate(password, UserSession)
                if session != None:
                    logging.info("%r has logged in." % user)
                    return jsonify({"authenticated": True, "user_id": user.id, "session": session})
                return jsonify({"authenticated": False, "reason": "Either session is invalid or session has expired."})
            return jsonify({"authenticated": False, "reason": "Username does not exist."})
        return jsonify({"authenticated": False, "reason": "Failed to collect credentials."})
    
@main.route('/signup', methods=['GET', 'POST'])
def signup():
    if request.method == 'GET':
        return render_template_custom('signup.html')
    elif request.method == 'POST':
        cred = json.loads(request.data)
        
        if cred is not None:
            username, password = cred['username'], cred['password']
            user = User(username)
            session = user.register(password, UserSession)
            if session != None:
                logging.info("%r has signed up." % user)
                return jsonify({"registered": True, "user_id": user.id, "session": session})
        return jsonify({"registered": False})

@main.route('/fetchrooms', methods=['POST'])
@authenticated_only_http
def fetch_rooms():
    global Room, User
    
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

    return jsonify({"rooms": rooms})

@main.route('/createroom', methods=['POST'])
@authenticated_only_http
def create_room():
    data = json.loads(request.data)
    
    if data is not None:
        global Room, User
        logging.info("%r is creating room '%r'" % (data.get("username"), data.get("room_name")))
        try:
            room = Room(data.get('room_name'), data.get("user_id"))
            room.commit()
        except sqlalchemy.exc.IntegrityError as e:
            # code, msg = e.orig
            c = {"created": False}
            return c
        return jsonify({"created": True, "room_id": room.id})
    return jsonify({"created": False})

@main.route('/fetchfriends', methods=['POST'])
@authenticated_only_http
def fetch_friends():
    global User, Friend
    
    data = json.loads(request.data)
    
    user_id = data.get("user_id")
    
    friends = []
    for friend in Friend.query.filter(sqlalchemy.or_(Friend.req_user_id == user_id, Friend.res_user_id == user_id)).all():
        friend_user_id = -1
        if friend.req_user_id != user_id:
            friend_user_id = friend.req_user_id
        elif friend.res_user_id != user_id:
            friend_user_id = friend.res_user_id
            
        friend_user = User.query.get(friend_user_id)
        if friend_user is None:
            created_by_user = {"id": -1, "username": "deleted"}
        friends.append({
            "user_id": friend_user.id,
            "username": friend_user.username,
            "request": friend.request,
            "date": str(friend.created_date)
        })

    return jsonify({"friends": friends})

@main.route('/user/<other_user_id>', methods=['POST'])
@authenticated_only_http
def query_user(other_user_id):
    global User, Friend
    user = User.query.get(other_user_id)
    data = json.loads(request.data)
    user_id = data.get("user_id")
    
    friend = Friend.query.filter(
        sqlalchemy.or_(
            sqlalchemy.and_(
                Friend.req_user_id == user_id, 
                Friend.res_user_id == int(other_user_id)
            ), 
            sqlalchemy.and_(
                Friend.req_user_id == int(other_user_id),
                Friend.res_user_id == user_id
            )
        )
    ).first()
    
    if user is not None:
        outputDict = {
                "username": user.username, 
                "created_date": str(user.created_date), 
                "last_active_date": str(user.last_active_date), 
                "online": datetime.utcnow() - user.last_active_date < timedelta(minutes=15)
        }
        
        outputDict["is_friend"] = friend is not None and not friend.request
        outputDict["has_requested_to_be_friends"] = friend is not None and friend.request and friend.req_user_id == int(other_user_id)
        outputDict["has_sent_friend_request"] = friend is not None and friend.request and friend.res_user_id == int(other_user_id)
    
        return jsonify(outputDict)
    return jsonify({"error": "User ID %r does not exist." % other_user_id})

@main.route('/friend', methods=['POST'])
@authenticated_only_http
def friend():
    global User, Friend
    data = json.loads(request.data)
    # If they are not the same user
    user_id = data.get("user_id")
    friend_user_id = data.get("friend_user_id")
    if user_id != friend_user_id:
        friend = Friend.query.filter(
            sqlalchemy.or_(
                sqlalchemy.and_(
                    Friend.req_user_id == friend_user_id, 
                    Friend.res_user_id == user_id
                ), 
                sqlalchemy.and_(
                    Friend.req_user_id == user_id,
                    Friend.res_user_id == friend_user_id
                )
            )
        ).first()
        if friend is not None:
            if friend.request:
                if friend.req_user_id == user_id:
                    # Remove friend request
                    logging.debug("Removing friend request...")
                    friend.remove()
                    return jsonify({"success": True})
                elif friend.res_user_id == user_id:
                    # Accept friend request
                    logging.debug("Accepting friend request...")
                    friend.request = False
                    friend.update()
                    return jsonify({"success": True})
            else:
                # Remove friend
                logging.debug("Removing friend...")
                friend.remove()
                return jsonify({"success": True})
        else:
            logging.debug("Adding friend request...")
            friend = Friend(user_id, friend_user_id)
            friend.addAndCommit()
            return jsonify({"success": True})
    return jsonify({"error": "Same User ID %r when trying to send friend request, accept friend request, or remove friends." % user_id})

@main.route('/logout', methods=['POST'])
@authenticated_only_http
def logout():
    cred = json.loads(request.data)    
    if cred is not None:
        username, user_id, session = cred.get('username'), cred.get('user_id'), cred.get('session')
        if username is not None and user_id is not None and session is not None:
            user = User.query.get(user_id)
            if user is not None:
                logging.info("%s has logged out." % user)
                user.remove_session(session, UserSession)
                return jsonify({"logged out": True})
    return jsonify({"logged out": False})

@app.errorhandler(404)
def page_not_found(e):
    return render_template_custom('404.html'), 404
