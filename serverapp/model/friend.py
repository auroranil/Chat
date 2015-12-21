from app import db

import datetime

class Friend(db.Model):
    __tablename__ = 'friends'

    id = db.Column(db.Integer, primary_key=True)
    req_user_id = db.Column(db.Integer, nullable=False)
    res_user_id = db.Column(db.Integer, nullable=False)
    # Is it a friend request? False if they are friends, True otherwise
    request = db.Column(db.Boolean, nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    
    def __init__(self, req_user_id, res_user_id, request=True):
        self.req_user_id = req_user_id
        self.res_user_id = res_user_id
        self.request = request
        
    def __repr__(self):
        return '<Friend(req_user_id=%r, res_user_id=%r, request=%r)>' % (self.req_user_id, self.res_user_id, self.request)
