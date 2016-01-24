import sys
import os
import logging
import time
import argparse
import atexit
import signal

def check_directory():
    if not os.path.exists(os.path.expanduser("~/.chatserver")):
        os.makedirs(os.path.expanduser("~/.chatserver"))
    sys.path.append(os.path.expanduser("~/.chatserver"))
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))

def graceful_exit(signal, frame):
    logging.info("Process sent SIGTERM, shutting down.")
    sys.exit(0)

def daemonize():
    if os.path.exists(os.path.expanduser("~/.chatserver/pid")):
        sys.stderr.write("[FATAL] The daemon seems to be already running.\nTo kill the server, run 'sh server_app/stop_daemon_server.sh'.\nTo override this behaviour, delete ~/.chatserver/pid\n")
        sys.exit(1)

    try:
        pid = os.fork()
        if pid < 0:
            sys.stderr.write("[FATAL] First fork failed: pid < 0\n")
            sys.exit(1)
        if pid > 0:
            sys.exit(0)
    except OSError, e:
        sys.stderr.write("[FATAL] First fork failed: %d (%s)\n" % (e.errno, e.strerror))
        sys.exit(1)
    
    try:
        os.chdir("/")
    except:
        sys.stderr.write("Failed to change cwd to root. This application may prevent some partitions from unmounting.\n")
    try:
        os.setsid()
    except:
        sys.stderr.write("[FATAL] setsid() failed. Try running with the --no-daemon option.\n")
        sys.exit(1)
    try:
        os.umask(0)
    except:
        sys.stderr.write("umask(0) failed. Files created by this application may have unexpected properties.\n")

    try:
        pid = os.fork()
        if pid < 0:
            sys.stderr.write("[FATAL] Second fork failed: pid < 0")
            sys.exit(1)
        if pid > 0:
            sys.exit(0)
    except OSError, e:
        sys.stderr.write("[FATAL] Second fork failed: %d (%s)\n" % (e.errno, e.strerror))
        sys.exit(1)
        
    try:    
        atexit.register(lambda: os.remove(os.path.expanduser("~/.chatserver/pid")))
        pidfile = open(os.path.expanduser("~/.chatserver/pid"), "w")
        pidfile.write(str(os.getpid()))
        pidfile.close()
    except:
        sys.stderr.write("[FATAL] pid file writing failed. Try running with the --no-daemon option.\n")
    
    try:
        signal.signal(signal.SIGTERM, graceful_exit)
    except Exception, e:
        sys.stderr.write("[FATAL] Failed to register termination signal handler.\nTraceback:\n%s\n" %str(e))
        sys.exit(1)
        
    sys.stdout.write("[INFO] Now running server on daemon mode.\n")
    sys.stdout.write("[INFO] Kill server by running 'sh server_app/stop_daemon_server.sh'\n")
    sys.stdout.write("[INFO] To view logs for this server, navigate to ~/.chatserver directory and open the latest log. This is the last message that will not be logged to the file.\n")

def setup_logging(args):
    logFormatter = logging.Formatter('%(asctime)s - [%(levelname)s] %(name)s: %(message)s')

    root = logging.getLogger()
    root.setLevel(logging.DEBUG)

    fileHandler = logging.FileHandler(os.path.expanduser("~/.chatserver/chat-" + time.strftime("%d-%m-%Y.log")))
    fileHandler.setFormatter(logFormatter)
    root.addHandler(fileHandler)
    
    if not args.daemon:
        consoleHandler = logging.StreamHandler(sys.stdout)
        consoleHandler.setFormatter(logFormatter)
        root.addHandler(consoleHandler)

def redirect_std_to_logging():
    """
    Redirects stdout and stderr to log file.
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

def setup_db():
    try:
        db.create_all()
    except Exception, e:
        logging.critical("Database  creation failed: %s" %str(e))

def parse_arguments():
    parser = argparse.ArgumentParser(description='Chat server <https://github.com/jos0003/Chat>')
    parser.add_argument('-p', '--port', help="Choose port to run chat server on. If not set, Chat server will use the port from config.py file.", required=False, default=app.config['PORT'])
    parser.add_argument('-t', '--no-daemon', dest='daemon', action='store_false', help="Disable daemon/service creation. Process will be tied to the terminal and logging will be done to stdout and stderr.")
    parser.add_argument('-d', '--daemon', dest='daemon', action='store_true', help="Opposite of --no-daemon; creates a daemon or service. Enabled by default.")
    parser.set_defaults(daemon=True)
    return parser.parse_args(sys.argv[1:])

def run_server():
    results = parse_arguments()
    try:
        results.port = int(results.port)
    except:
        sys.stderr.write("[FATAL] port must be an integer\n")
        sys.exit(1)
    
    setup_logging(results)
    
    if results.daemon:
        daemonize()
        redirect_std_to_logging()
        logging.info("Daemon spawned successfully, pid is %d" % os.getpid())
    
    setup_db()

    logging.info("Chat server is now starting on 0.0.0.0:%r" % results.port)
    try:
        socketio.run(app, host="0.0.0.0", port=results.port, use_reloader=False)
    except Exception, e:
        logging.critical("SocketIO failed: %s" % str(e))
        sys.exit(1)
    
check_directory()
from app import app, db, main, socketio
run_server()
