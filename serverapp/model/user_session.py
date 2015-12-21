from app import db

import datetime
import uuid

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
