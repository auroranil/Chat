import sys
import os
import logging
import time
import argparse

from app import app, db, main, socketio

def setup_logging():
    if not os.path.exists(os.path.join(sys.path[0], "log")):
        os.makedirs(os.path.join(sys.path[0], "log"))

    logFormatter = logging.Formatter('%(asctime)s - [%(levelname)s] %(name)s: %(message)s')

    root = logging.getLogger()
    root.setLevel(logging.DEBUG)

    fileHandler = logging.FileHandler(os.path.join(sys.path[0], "log", "chat-"+time.strftime("%d-%m-%Y.log")))
    fileHandler.setFormatter(logFormatter)
    root.addHandler(fileHandler)

    consoleHandler = logging.StreamHandler(sys.stdout)
    consoleHandler.setFormatter(logFormatter)
    root.addHandler(consoleHandler)

def setup_db():
    db.create_all()

def parse_arguments():
    parser = argparse.ArgumentParser(description='Chat server <https://github.com/jos0003/Chat>')
    parser.add_argument('-p', '--port', help="Choose port to run chat server on. If not set, Chat server will use the port from config.py file.", required=False, default=app.config['PORT'])
    
    return parser.parse_args(sys.argv[1:])

def run_server():
    setup_logging()
    setup_db()
    
    results = parse_arguments()

    logging.info("Chat server is now running on 0.0.0.0:%r" % results.port)
    socketio.run(app, host="0.0.0.0", port=results.port)
    
run_server()
