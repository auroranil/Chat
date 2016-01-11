import logging

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
    
        user_id = args[0].get("user_id")
        session = args[0].get("session")
        
        if not User.session_in_user(user_id, session, UserSession):
            logging.info("Failed auth: Not in session")
            disconnect()
        else:
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
    username = data.get('username')
    room_name = data.get('room_name')
    room_id = data.get('room_id')
    leave_room("room" + str(room_id))
    logging.info(username + " has left room '%s' (id=%r)." % (room_name, room_id))

@socketio.on('fetch messages')
@authenticated_only_socketio
def fetch_messages(data):
    logging.debug("fetching messages...")
    global Message
    global Room
    global User
    messages = []
    
    room_id = data.get('room_id')
    db_msg_list = Message.fetch(int(data.get('type')), room_id, int(data.get('before_msg_id', -1)))
    
    for message in db_msg_list:
        messages.append({
            "username": User.query.get(message.user_id).username,
            "user_id": message.user_id,
            "message_id": message.id,
            "message": message.message,
            "datetimeutc": str(message.date)
        })
    
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
    data["message_id"] = message.id
    data["datetimeutc"] = str(message.date)
    
    emit("received message", data, room="room" + str(data.get('room_id')))
    return "Success!: Server successfully received message."
