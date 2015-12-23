from app import db

import datetime

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
        
    def commit(self):
        db.session.add(self)
        db.session.commit()
