import sys
from app import app, db, main, socketio
db.create_all()
app.register_blueprint(main)

port = 5000
if len(sys.argv) == 2:
    port = int(sys.argv[1])

print "Chat server is now running on 0.0.0.0:%r" % port
socketio.run(app, host="0.0.0.0", port=port)
