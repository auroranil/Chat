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
        return '<Message(user_id=%r, message=%r, type=%r, other_id=%r)>' % (self.user_id, self.message, self.type, self.other_id)
        
    @classmethod
    def fetch(cls, type_num, other_id, before_id):
        result = cls.query.order_by(cls.id.desc()).filter_by(other_id=other_id, type=type_num)
        
        if not before_id < 0:
            result = result.filter(cls.id < before_id)
            
        result = result.limit(25).all()
        
        return result
        
    def serialize(self, User):
        output_dict = {
            "username": User.query.get(self.user_id).username,
            "user_id": self.user_id,
            "message_id": self.id,
            "message": self.message,
            "datetimeutc": str(self.date)
        }
        
        return output_dict
        
    def commit(self):
        db.session.add(self)
        db.session.commit()

