# Chat
A simple chat application for android with a python flask server as the backend.

## Setup
### Chat server: Ubuntu
Run this command to clone the repository, install dependencies, and get the Chat server up and running.
```
curl https://raw.githubusercontent.com/jos0003/Chat/master/setup_server.sh | sh
```

### Chat server: Other operating systems
* Clone the repository
```
git clone https://github.com/jos0003/Chat
```
* Install these dependencies:
 * python
 * pip packages
  * flask
  * flask-bcrypt
  * flask-socketio
  * sqlalchemy
  * flask-sqlalchemy
 * bcrypt (flask-bcrypt needs this)
 * sqlite for portable database (easier to setup than mysql)
* Run `python /path/to/Chat/server_app` to start the server.
### Chat client: Web
(Currently work in progress)

Once Chat server is running, Chat client is accessible via a web browser by heading to `http://localhost:<port>`

### Chat client: Android 
* Open `android_app` gradle project in either Android Studio or Eclipse with the gradle plugin.
* Install android application on your phone and run the app.

## Run Chat server
* Run `python /path/to/Chat/server_app` to start the server.
 * Use `screen` if you want to run the server without staying logged in to a terminal.

## ALWAYS TODO:
* Refactor code
* Clean up bugs
* Constantly improve UI

## DONE:
* Caching 
 * Store session locally (using shared preferences with flag Context.MODE_PRIVATE)
 * Resume session automatically when remember me checkbox is checked and user returns to app
* Rooms
 * Public rooms (display a list of rooms to join, and a edittext and button to host a room)
* Menu
 * Tabs in MenuActivity to display which fragment the user is on (rooms or friends)
* Database
 * Friends list structure in database
* User
 * User profile (username, status, joined date, last active date, is online)
* Friends
 * Ability to add (via requests and accepting requests) and remove friends
 * Show friends in listview by fetching it from database and storing them in an adapter

## TODO:
* Friends
 * Show friend requests within the same listview
 * Private message chat to a single friend
 * Block list
* Delete access
 * Ability to delete room
 * Ability to delete account
* Caching 
 * Messages
 * Rooms
 * Friends
* Communication
 * Replace API HTTP POST requests with RESTful API requests
 * Create a class that abstracts code dealing with JSON with AsyncTask
* Rooms
 * Private rooms (request to join, join with password)
 * Able to search a room
 * Vote rooms
* Chat
 * Show where people have read up to (like in google+ hangouts)
* Web
 * Replicate android features onto the web application
* User
 * Able to edit and save user bio
 * Able to recover by using email address (optional)
 * Show user activity in user profile
* Welcome screen
 * Dynamically form-validate welcomeActivity as credentials are being entered.
* Server
 * Use daemon instead of using screen to run the server.
