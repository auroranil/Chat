# Chat
A simple chat application for android with a python flask server as the backend.

## Setup
* Run this command to clone the repository and get the Chat server up and running.
        
        wget https://raw.githubusercontent.com/jos0003/Chat/master/setup_server.sh && chmod +x ./setup_server.sh && ./setup_server.sh
        
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
* User profile (username, status, joined date, last active date, is online)

## TODO:
* Ability to add (via requests and accepting requests) and remove friends
* Show friend requests in listview
* Show friends in listview by fetching it from database and storing them in an adapter
* Ability to delete room
* Ability to delete account
* Message caching (to reduce load on central server)
* Private rooms (request to join, join with password)
* Private message chat to a single friend
* Block list
* Show where people have read up to (like in google+ hangouts)
* Replicate android features onto the web application
* Able to edit and save user bio
* Show user activity in user profile
