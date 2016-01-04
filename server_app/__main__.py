import sys
import os
import logging
import time
import atexit

if not os.path.exists(os.path.expanduser("~/.chatserver")):
    os.makedirs(os.path.expanduser("~/.chatserver"))
sys.path.append(os.path.expanduser("~/.chatserver"))
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
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

"""
Workaround to get socket.io logging the main logs
Based on http://www.electricmonk.nl/log/2011/08/14/redirect-stdout-and-stderr-to-a-logger-in-python/
"""
class StreamToLogger(object):
   def __init__(self, logger, log_level=logging.INFO):
      self.logger = logger
      self.log_level = log_level
      self.linebuf = ''
 
   def write(self, buf):
      for line in buf.rstrip().splitlines():
         self.logger.log(self.log_level, line.rstrip())

sys.stdin.close()
sys.stdout.close()
sys.stderr.close()

stdout_logger = logging.getLogger('STDOUT')
sys.stdout = StreamToLogger(stdout_logger, logging.INFO)
stderr_logger = logging.getLogger('STDERR')
sys.stderr = StreamToLogger(stderr_logger, logging.ERROR)

logging.info("Daemon spawned successfully, pid is %d" %os.getpid())

from app import app, db, main, socketio
logging.debug("app import OK")
try:
    db.create_all()
except Exception, e:
    logging.critical("Database loading failed: %s" %str(e))
    sys.exit(1)

logging.debug("db OK")
app.register_blueprint(main)
logging.debug("blueprint OK")

port = app.config['PORT']
if len(sys.argv) == 2:
    port = int(sys.argv[1])

logging.info("Chat server is now running on 0.0.0.0:%r" % port)
try:
    socketio.run(app, host="0.0.0.0", port=port, use_reloader=False)
except Exception, e:
    logging.critical("SocketIO failed: %s" %str(e))
