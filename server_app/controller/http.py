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
        data = json.loads(request.data)
        user_id = data.get("user_id")
        session = data.get("session")
        
        if user_id is None or session is None:
            logging.info("Failed auth: Did not supply credentials")
            return
        
        if not User.session_in_user(user_id, session, UserSession):
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

        if auth is None:
            return jsonify({"authenticated": False, "error": "Failed to collect credentials."})
        
        username, password = auth.get('username'), auth.get('password')
        
        # Must be non-empty strings.
        if not username or not password:
            return jsonify({"authenticated": False, "error": "Failed to collect credentials."})
        
        username = username.strip() # Never trim passwords
        
        user = User.query.filter_by(username=username).first()
        
        if user is None:
            return jsonify({"authenticated": False, "error": "Username does not exist."})
            
        session = user.authenticate(password, UserSession)
        if session == None:
            return jsonify({"authenticated": False, "error": "Either session is invalid or session has expired."})

        logging.info("%r has logged in." % user)
        return jsonify({"authenticated": True, "user_id": user.id, "session": session})
    
@main.route('/signup', methods=['GET', 'POST'])
def signup():
    if request.method == 'GET':
        return render_template_custom('signup.html')
    elif request.method == 'POST':
        cred = json.loads(request.data)
        
        if cred is None:
            return jsonify({"registered": False, "error": "Unable to fetch JSON input."})
            
        username, password = cred.get('username'), cred.get('password')
        
        # Must be non-empty strings.
        if not username or not password:
            return jsonify({"registered": False, "error": "Username and password fields not set."})
            
        username = username.strip() # Never trim passwords
        user = User(username)
        session = user.register(password, UserSession)
        if session is None:
            return jsonify({"registered": False, "error": "Database error when trying to register."})
            
        logging.info("%r has signed up." % user)
        return jsonify({"registered": True, "user_id": user.id, "session": session})
        
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
    
    if data is None:
        return jsonify({"created": False, "error": "Unable to fetch JSON input."})
    
    global Room, User
    user_id, username, room_name = data.get("user_id"), data.get("username"), data.get("room_name")
    if not username or not room_name:
        return jsonify({"created": False, "error": "Username and room name must not be empty strings."})
    
    username, room_name = username.strip(), room_name.strip()
    
    if(room_name > 20):
        return jsonify({"created": False, "error": "Room name cannot be longer than 20 characters."})
    
    logging.info("%r is creating room '%r'" % (username, room_name))
    
    try:
        room = Room(room_name, user_id)
        room.commit()
    except sqlalchemy.exc.IntegrityError as e:
        return {"created": False, "error": "Database error when trying to create room."}
    
    return jsonify({"created": True, "room_id": room.id})

@main.route('/fetchfriends', methods=['POST'])
@authenticated_only_http
def fetch_friends():
    global User, Friend
    
    data = json.loads(request.data)
    
    # If this function has been reached,
    # then it is known that user_id is not None
    # as it has passed the authentication stage.
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
    data = json.loads(request.data)
    # If this function has been reached,
    # then it is known that user_id is not None
    # as it has passed the authentication stage.
    user_id = data.get("user_id")
    
    user = User.query.get(other_user_id)
    if user is None:
        return jsonify({"success": False, "error": "User ID %r does not exist." % other_user_id})
    
    try:
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
    except sqlalchemy.exc.IntegrityError as e:
        return jsonify({"success": False, "error": "Database error."})       
        
    output_dict = {
            "success": True,
            "username": user.username, 
            "created_date": str(user.created_date), 
            "last_active_date": str(user.last_active_date), 
            "online": datetime.utcnow() - user.last_active_date < timedelta(minutes=15),
            "is_friend": friend is not None and not friend.request,
            "has_requested_to_be_friends": friend is not None and friend.request and friend.req_user_id == int(other_user_id),
            "has_sent_friend_request": friend is not None and friend.request and friend.res_user_id == int(other_user_id)
    }

    return jsonify(output_dict)

@main.route('/friend', methods=['POST'])
@authenticated_only_http
def friend():
    global User, Friend
    data = json.loads(request.data)
    user_id, friend_user_id = data.get("user_id"), data.get("friend_user_id")
    
    if friend_user_id is None:
        return jsonify({"success": False, "error": "Field 'friend_user_id' not set. It must be set to user ID other than the user trying to change status of being friends."})
        
    if friend_user_id < 0:
        return jsonify({"success": False, "error": "Field 'friend_user_id' has to be >= 0. It must be set to user ID other than the user trying to change status of being friends."})
    
    # A user cannot be friends with themselves
    if user_id is friend_user_id:
        return jsonify({"success": False, "error": "Same User ID %r when trying to send friend request, accept friend request, or remove friends." % user_id})
    
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
    
    # Friend logic
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

@main.route('/logout', methods=['POST'])
@authenticated_only_http
def logout():
    cred = json.loads(request.data)    
    if cred is None:
        return jsonify({"success": False, "error": "Unable to fetch JSON input."})
    
    username, user_id, session = cred.get('username'), cred.get('user_id'), cred.get('session')
    
    # user_id is authenticated.
    if not username or not session:
        return jsonify({"success": False, "error": "Username and session must be non-empty strings."})
    
    user = User.query.get(user_id)
    user.remove_session(session, UserSession)
    
    logging.info("%s has logged out." % user)
    return jsonify({"success": True})

@app.errorhandler(404)
def page_not_found(e):
    return render_template_custom('404.html'), 404
