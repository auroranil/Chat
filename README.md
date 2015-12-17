# Chat
A simple chat application for android with a python flask server as the backend.

## Setup
* Using android studio, clone this git repository.
* Install these following dependencies via `sudo pip install <pip-package>`
 * flask
 * flask-bcrypt
 * flask-socketio
 * sqlalchemy
 * flask-sqlalchemy
* Install mysql and create a non-root user that has access to a single database.
* Create `serverapp/credentials.py` file with the following content:

        SECRET_KEY = 'type some random secret key here'

        mysql = {
          "user": "your-username-for-db-you-have-created",
          "host": "localhost",
          "password": "*******",
          "dbname": "name-of-database"
        }

* Run `serverapp/app.py` to start the server
* In `WelcomeActivity` java file, set `url` variable to point to where the server is (port=5000)
* Install android application on your phone and run the app.

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
