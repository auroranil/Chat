import sys
import os
import logging
import time
import atexit

if not os.path.exists(os.path.expanduser("~/.chatserver")):
    os.makedirs(os.path.expanduser("~/.chatserver"))
if os.path.exists(os.path.expanduser("~/.chatserver/pid")):
    sys.stderr.write("The daemon seems to be already running. To override this behaviour, delete ~/.chatserver/pid\n")
    sys.exit(1)

try:
    pid = os.fork()
    if pid < 0:
        sys.stderr.write("First fork failed: pid < 0\n")
        sys.exit(1)
    if pid > 0:
        sys.exit(0)
except OSError, e:
    sys.stderr.write("First fork failed: %d (%s)\n" % (e.errno, e.strerror))
    sys.exit(1)
    
os.chdir("/")
os.setsid()
os.umask(0)

try:
    pid = os.fork()
    if pid < 0:
        sys.stderr.write("Second fork failed: pid < 0")
        sys.exit(1)
    if pid > 0:
        sys.exit(0)
except OSError, e:
    sys.stderr.write("Second fork failed: %d (%s)\n" % (e.errno, e.strerror))
    sys.exit(1)

logging.basicConfig(filename=os.path.expanduser("~/.chatserver/chat-"+time.strftime("%d-%m-%Y.log")), level=logging.DEBUG)

atexit.register(lambda: os.remove(os.path.expanduser("~/.chatserver/pid")))
pidfile = open(os.path.expanduser("~/.chatserver/pid"), "w")
pidfile.write(str(os.getpid()))
pidfile.close()

sys.stderr.close()
sys.stdout.close()
sys.stdin.close()

logging.info("Daemon spawned successfully, pid is %d" %os.getpid())

from app import app, db, main, socketio
db.create_all()
app.register_blueprint(main)

port = app.config['PORT']
if len(sys.argv) == 2:
    port = int(sys.argv[1])

logging.info("Chat server is now running on 0.0.0.0:%r" % port)
socketio.run(app, host="0.0.0.0", port=port)
