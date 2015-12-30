import sys
import os
import logging

if not os.path.exists(os.path.expanduser("~/.chatserver")):
    os.makedirs(os.path.expanduser("~/.chatserver"))
logging.basicConfig(filename=os.path.expanduser("~/.chatserver/chat.log"), level=logging.DEBUG)

from app import app, db, main, socketio
db.create_all()
app.register_blueprint(main)

port = app.config['PORT']
if len(sys.argv) == 2:
    port = int(sys.argv[1])

logging.info("Chat server is now running on 0.0.0.0:%r" % port)
socketio.run(app, host="0.0.0.0", port=port)
