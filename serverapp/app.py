import flask
from flask import Blueprint

from flask_socketio import SocketIO
from flask.ext.bcrypt import Bcrypt
from flask.ext.sqlalchemy import SQLAlchemy

from flask import Flask
app = Flask(__name__)
main = Blueprint('main', __name__)

# credentials
import credentials
app.config['SECRET_KEY'] = credentials.SECRET_KEY
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+mysqldb://{}:{}@{}/{}'.format(credentials.mysql["user"], credentials.mysql["password"], credentials.mysql["host"], credentials.mysql["dbname"])

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
socketio = SocketIO(app)

from controller import *
