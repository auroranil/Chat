# Chat
A simple chat application for android with a python flask server as the backend.

## Setup
### Chat server: Ubuntu (quick and easy)
Run this command to clone the repository, install dependencies, and get the Chat server up and running in daemon mode.
```
curl https://raw.githubusercontent.com/jos0003/Chat/master/setup_server.sh | sh
```

### Chat server: All operating systems (more challenging)
* Clone the repository
```
git clone https://github.com/jos0003/Chat
```
* Install these following dependencies:
  * python
  * bcrypt (flask-bcrypt needs this)
  * eventlet (flask-socketio prefers this the most; "gevent" is an alternative)
  * sqlite (for portable database; easier to setup than mysql)
  * pip packages
    * flask
    * flask-bcrypt
    * flask-socketio
    * sqlalchemy
    * flask-sqlalchemy
* Run `python /path/to/Chat/server_app -d` to start the server in daemon mode.

### Chat client: Web
(Currently work in progress)

Once Chat server is running, Chat client is accessible via a web browser by heading to `http://localhost:<port>`

### Chat client: Android 
* Open `android_app` gradle project in either Android Studio or Eclipse with the gradle plugin.
* Install android application on your phone and run the app.

## Chat server

### Run
* Run `python /path/to/Chat/server_app -d` to start the server in daemon mode, or
* Run `python /path/to/Chat/server_app -t` to start the server within a terminal.

### Help
* Run `python /path/to/Chat/server_app -h` to view help details.

### Stop daemon
* Run `sh /path/to/Chat/server_app/stop_daemon_server.sh` to stop the daemon for Chat server.

## Features
* Able to remember user when logging in/signing up
* Rooms
  * Public rooms (displays a list of rooms to join, and a edittext and button to host a room)
  * Display rooms in a list
* User
  * User profile (username, joined date, last active date, is online)
* Friends
  * Ability to add (via requests) and remove friends
  * Display friends (and friend requests) in a list
  * Private message chat to a friend
* Server can run in daemon mode.

## TODO:

### At all stages of development
* Refactor code
* Remove bugs
* Refine UI constantly

### Friends
* In friends listview, make ignore request button work
* In friends listview, add menu features
* When a friend is removed, manage their conversations (currently its left alone in the database but inaccessible in the client app)
* Block list
* Ordering
* Able to search a friend

### Delete access
* Ability to delete room
* Ability to delete account

### Caching 
* Messages
* Rooms
* Friends

### Communication
* Replace HTTP POST API requests with RESTful HTTP API requests
* Isolate controller code from activities within android application
* Ensure message delivery

### Rooms
* Private rooms (request to join, join with password)
* Able to search a room
* Vote rooms
* Ordering (most recent, most voted)

### Chat
* Show where people have read up to (like in google+ hangouts)
* Notifications
* End to end encryption (via HTTPS + Let's Encrypt; will sign public keys if requested)
* Able to search chat messages

### Web
* Replicate android features onto the web application

### User
* Able to edit and save user bio
* Able to recover by using email address (optional)
* Show user activity in user profile

### Welcome screen
* Dynamically form-validate welcomeActivity as credentials are being entered.

### Testing
* Write test cases
