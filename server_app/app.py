import os
import sys
import logging

import flask
from flask import Flask, Blueprint

from flask_socketio import SocketIO
from flask.ext.bcrypt import Bcrypt
from flask.ext.sqlalchemy import SQLAlchemy

app = Flask(__name__)
main = Blueprint('main', __name__)

# configuration
try:
    import config
    config.configure(app)
except ImportError as e:
    # If config.py doesn't exist, create one
    config_file = open(os.path.expanduser("~/.chatserver/config.py"), "w")
    config_file.write("def configure(app):\n")
    config_file.write("    app.config['SECRET_KEY'] = %r\n" % os.urandom(24))
    config_file.write("    app.config['SQLALCHEMY_DATABASE_URI'] = '%s'\n" % ("sqlite:///"+os.path.expanduser("~/.chatserver/Chat.db")))
    config_file.write("    # Disable track modifications to suppress warning\n")
    config_file.write("    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False\n")
    config_file.write("    # If port is not set in the second argument, set default port in the variable below.\n")
    config_file.write("    app.config['PORT'] = 5000\n")
    config_file.write("    app.debug = True\n")
    config_file.close()
    
    # set chmod permissions
    os.chmod(os.path.expanduser("~/.chatserver/config.py"), 0600)
    
    import config
    config.configure(app)

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
socketio = SocketIO(app)

from controller import *
app.register_blueprint(main)
