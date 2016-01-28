import logging
import json 

from app import socketio
from model import *

from flask_socketio import SocketIO, send, emit, join_room, leave_room, disconnect

import functools

def authenticated_only_socketio(f):
    @functools.wraps(f)
    def wrapped(*args, **kwargs): 
        if(len(args) == 0):
            logging.info("Failed auth: Did not supply credentials")
            disconnect()
            return
    
        user_id = args[0].get("user_id")
        session = args[0].get("session")
        
        if user_id is None or user_id < 0 or not session or not User.session_in_user(user_id, session, UserSession):
            logging.info("Failed to authenticate user ID %r" % user_id)
            disconnect()
            return
        
        return f(*args, **kwargs)
    return wrapped
   
def get_friend_conversation_string_id(u_id, v_id):
    return "friend-" + "-".join(map(str, sorted([u_id, v_id])))
 
@socketio.on('join')
@authenticated_only_socketio
def connected(data):
    user_id = data.get("user_id")
    username = data.get("username")
    chat_type = data.get("type")
    
    room_name = ""
    
    if chat_type == 0:
        room_display_name = data.get("room_name")
        room_id = data.get('room_id')
        
        if Room.query.get(room_id) is None:
            emit("error", "Room '%s' does not exist" % room_name)
            disconnect()
        else:
            join_room("room-" + str(room_id))
            logging.info(username + " has entered room '%s' (id=%r)." % (room_display_name, room_id))
    elif chat_type == 1:
        friend_username = data.get("friend_username")
        friend_user_id = data.get("friend_user_id")
        
        if User.query.get(friend_user_id) is None:
            emit("error", "Username %r with id %r does not exist." % (friend_username, friend_user_id))
            disconnect()
        else:
            join_room(get_friend_conversation_string_id(user_id, friend_user_id))
            logging.info("%r (id=%r) has entered a friend conservation with %r (id=%r)" % (username, user_id, friend_username, friend_user_id))
    else:
        emit("error", "Must specify 'type' to be either 0 or 1.")
        disconnect()

@socketio.on('leave')
@authenticated_only_socketio
def disconnected(data):
    room_name = ""
    
    chat_type = data.get("type")
    
    if chat_type == 0:
        room_name = "room-" + str(data.get('room_id'))
    elif chat_type == 1:
        room_name = get_friend_conversation_string_id(data.get('user_id'), data.get('friend_user_id'))

    stopped_typing_handler(data.get("username"), room_name)
    leave_room(room_name)
    
    username, room_display_name, room_id = data.get('username'), data.get('room_name'), data.get('room_id')
    logging.info(username + " has left room '%s' (id=%r)." % (room_display_name, room_id))

@socketio.on('fetch messages')
@authenticated_only_socketio
def fetch_messages(data):
    logging.debug("Fetching messages...")
    global Message, Room, User
    messages = []
    
    chat_type = data.get('type')

    user_id = data.get('user_id')

    other_id = -1    
    if chat_type == 0:
        other_id = data.get('room_id')
    elif chat_type == 1:
        other_id = data.get('friend_user_id')
    
    db_msg_list = Message.fetch(chat_type, user_id, other_id, data.get('before_msg_id', -1), data.get('after_msg_id', -1))
    
    for message in db_msg_list:
        messages.append(message.serialize(User))
    
    messages.reverse()
    history = {"messages": messages}
    
    emit("history", history)

@socketio.on('typing')
@authenticated_only_socketio
def started_typing(data):
    chat_type = data.get('type')
    room_name = ""
    
    if chat_type == 0:
        room_name = "room-" + str(data.get('room_id'))
    elif chat_type == 1:
        room_name = get_friend_conversation_string_id(data.get('user_id'), data.get('friend_user_id'))
    
    emit("typing", data.get('username'), room=room_name, include_self=False)

@socketio.on('stop typing')
@authenticated_only_socketio
def stopped_typing(data):
    room_name = ""
    
    chat_type = data.get('type')
    
    if chat_type == 0:
        room_name = "room-" + str(data.get('room_id'))
    elif chat_type == 1:
        room_name = get_friend_conversation_string_id(data.get('user_id'), data.get('friend_user_id'))

    stopped_typing_handler(data.get("username"), room_name)
    
def stopped_typing_handler(username, room_name):
    emit("stop typing", username, room=room_name, include_self=False)

@socketio.on('send message')
@authenticated_only_socketio
def handle_sent_message(data):
    logging.debug('received message: ' + str(data))
    
    global User, Message, Room
    if not data.get('message'):
        return
    
    chat_type = data.get('type')
    other_id = -1
    if chat_type == 0:
        other_id = data.get('room_id')
    elif chat_type == 1:
        other_id = data.get('friend_user_id')
    message = Message(data.get('user_id'), data.get('message').strip(), data.get('type'), other_id)
    message.commit()
    
    room_name = ""
    
    if chat_type == 0:
        room_name = "room-" + str(data.get('room_id'))
    elif chat_type == 1:
        room_name = get_friend_conversation_string_id(data.get('user_id'), data.get('friend_user_id'))
    
    emit("received message", message.serialize(User), room=room_name)

