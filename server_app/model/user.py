import logging

from app import db, bcrypt
import datetime

class User(db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True)
    created_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    last_active_date = db.Column(db.DateTime, default=datetime.datetime.utcnow)
    password_hash = db.Column(db.String(60))

    def __init__(self, username):
        self.username = username
        
    def __repr__(self):
        return '<User(id=%r, username=%r)>' % (self.id, self.username)
        
    def authenticate(self, password, UserSession):
        u = User.query.filter_by(username=self.username).first()
        if(u == None):
            return None
            
        if bcrypt.check_password_hash(u.password_hash, password):
            return self.generate_session(UserSession)
        
        return None
    
    def generate_session(self, UserSession):
        session = UserSession(self.id)
        session.commit()
        return session.hash
    
    @classmethod
    def session_in_user(cls, user_id, session_hash, UserSession):
        user = cls.query.get(user_id)
        if user is not None:
            s = UserSession.query.filter_by(user_id=user.id, hash=session_hash).first()
            if s is not None:
                user.update_last_active_date()
                return True
        else:
            logging.info("Username with id=%r does not exist.") % user_id
        return False
        
    def remove_session(self, hash, UserSession):
        s = UserSession.query.filter_by(hash=hash).first()
        if s is not None:
            db.session.delete(s)
            db.session.commit()
            return True
        return False
            
    def register(self, password, UserSession):
        self.password_hash = bcrypt.generate_password_hash(password)
        db.session.add(self)
        db.session.commit()
        return self.generate_session(UserSession)
        
    def update_last_active_date(self):
        self.last_active_date = datetime.datetime.utcnow()
        db.session.commit()
