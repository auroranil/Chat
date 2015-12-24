# Chat
A simple chat application for android with a python flask server as the backend.

## Setup
* Run these commands to clone the repository and get the Chat server up and running

        # cd to the directory where you want the Chat directory to be,
        # then run these commands
        git clone https://github.com/jos0003/Chat.git
        cd Chat
        
        # Install these dependencies
        sudo apt-get install sqlite python-pip
        sudo pip install flask bcrypt flask-bcrypt flask-socketio sqlalchemy flask-sqlalchemy
        
        # Run server on port 5000 as specified in the first argument
        python server_app 5000
* Open `android_app` gradle project in either Android Studio or Eclipse with the gradle plugin.
* Install android application on your phone and run the app.

## Dependencies
* pip packages
 * flask
 * flask-bcrypt
 * flask-socketio
 * sqlalchemy
 * flask-sqlalchemy
* bcrypt (flask-bcrypt needs this)
* sqlite for portable database (easier to setup than mysql)

## ALWAYS TODO:
* Refactor code
* Clean up bugs
* Constantly improve UI

## DONE:
* Store session locally (using shared preferences with flag Context.MODE_PRIVATE)
* Resume session automatically when remember me checkbox is checked and user returns to app
* Public rooms (display a list of rooms to join, and a edittext and button to host a room)
* Tabs in MenuActivity to display which fragment the user is on (rooms or friends)
* Friends list structure in database

## TODO:
* Ability to add (via requests and accepting requests) and remove friends
* Show friend requests in listview
* Show friends in listview by fetching it from database and storing them in an adapter
* Ability to delete room
* Ability to delete account
* Message caching (to reduce load on central server)
* Private rooms (request to join, join with password)
* User profile (username, status, joined date, send friend request)
* Private message chat to a single friend
* Block list
* Show where people have read up to (like in google+ hangouts)
