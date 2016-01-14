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
    
@socketio.on('join')
@authenticated_only_socketio
def connected(data):
    username = data.get("username")
    room_name = data.get("room_name")
    room_id = data.get('room_id')
    if Room.query.get(room_id) is None:
        emit("error", "Room '%s' does not exist" % room_name)
        disconnect()
    else:
        join_room("room" + str(room_id))
        logging.info(username + " has entered room '%s' (id=%r)." % (room_name, room_id))

@socketio.on('leave')
@authenticated_only_socketio
def disconnected(data):
    stopped_typing_handler(data.get("username"), data.get("room_id"))
    username, room_name, room_id = data.get('username'), data.get('room_name'), data.get('room_id')
    leave_room("room" + str(room_id))
    logging.info(username + " has left room '%s' (id=%r)." % (room_name, room_id))

@socketio.on('fetch messages')
@authenticated_only_socketio
def fetch_messages(data):
    logging.debug("Fetching messages...")
    global Message, Room, User
    messages = []
    
    room_id = data.get('room_id')
    db_msg_list = Message.fetch(int(data.get('type')), room_id, int(data.get('before_msg_id', -1)))
    
    for message in db_msg_list:
        messages.append(message.serialize(User))
    
    messages.reverse()
    history = {"history": messages}
    
    emit("history", history)

@socketio.on('typing')
@authenticated_only_socketio
def started_typing(data):
    emit("typing", data.get('username'), room="room" + str(data.get('room_id')), include_self=False)

@socketio.on('stop typing')
@authenticated_only_socketio
def stopped_typing(data):
    stopped_typing_handler(data.get("username"), data.get("room_id"))
    
def stopped_typing_handler(username, room_id):
    emit("stop typing", username, room="room" + str(room_id), include_self=False)

@socketio.on('send message')
@authenticated_only_socketio
def handle_sent_message(data):
    logging.debug('received message: ' + str(data))
    
    global User, Message, Room
    message = Message(data.get('user_id'), data.get('message'), data.get('type'), data.get('room_id'))
    message.commit()
    
    emit("received message", message.serialize(User), room="room" + str(data.get('room_id')))

