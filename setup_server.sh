#!/bin/bash
git clone https://github.com/jos0003/Chat.git
cd Chat

# Install these dependencies
sudo apt-get install sqlite python-pip
sudo pip install flask bcrypt flask-bcrypt flask-socketio sqlalchemy flask-sqlalchemy

# Run server on port 5000 as specified in the second argument.
# This will create config.py, which will store the configuration
# settings in a function.
python server_app 5000
