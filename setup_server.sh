#!/bin/bash

echo Running "git clone https://github.com/jos0003/Chat.git"
git clone https://github.com/jos0003/Chat.git
echo Running "cd Chat"
cd Chat

# Install these dependencies
echo Running "sudo apt-get install sqlite python-pip"
sudo apt-get install sqlite python-pip
echo Running "sudo pip install flask bcrypt flask-bcrypt flask-socketio sqlalchemy flask-sqlalchemy"
sudo pip install flask bcrypt flask-bcrypt flask-socketio sqlalchemy flask-sqlalchemy

# Run Chat server on port 5000.
# This will create config.py, which will store the configuration
# settings in a function.
echo Running "python server_app"
python server_app
