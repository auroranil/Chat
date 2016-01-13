#!/bin/sh

kill `cat ~/.chatserver/pid`
if [ "$?" = "0" ]; then
    rm ~/.chatserver/pid
    
    if [ "$?" = "0" ]; then
        echo "Daemon for Chat server has been stopped successfully."
    else
        echo "Error: failed to remove ~/.chatserver/pid. Please remove it manually."
    fi
else
    echo "Error: failed to kill process (maybe Chat server is not running on daemon or it already has been stopped?). If it is still running, please kill it manually and remove the pid file."
fi

