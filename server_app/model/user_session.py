from app import db

from os import urandom
from base64 import b64encode
import datetime

class UserSession(db.Model):
    __tablename__ = 'user_sessions'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, nullable=False)
    hash = db.Column(db.String(32), unique=True, nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    
    def __init__(self, user_id):
        self.user_id = user_id
        self.hash = b64encode(urandom(24)).decode('utf-8')
        
    def __repr__(self):
        return '<UserSession(user_id=%r, hash=%s)>' % (self.user_id, self.hash)
    
    def commit(self):
        db.session.add(self)
        db.session.commit()
