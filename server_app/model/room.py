from app import db

import datetime

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
        return '<Room(id=%r, room_name=%r, created_by_user_id=%r)>' % (self.id, self.room_name, self.created_by_user_id)
        
    def commit(self):
        db.session.add(self)
        db.session.commit()
