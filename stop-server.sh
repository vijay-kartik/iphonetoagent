#!/bin/bash

# Script to stop the running API server

echo "Looking for running API server processes..."

# Find Java processes related to this apiserver project
SERVER_PIDS=$(ps aux | grep java | grep "apiserver/app/build" | grep -v grep | awk '{print $2}')

if [ -z "$SERVER_PIDS" ]; then
    echo "No API server processes found running."
else
    echo "Found API server process(es) with PID(s): $SERVER_PIDS"
    
    for PID in $SERVER_PIDS; do
        echo "Stopping server with PID: $PID"
        kill -TERM $PID
        
        # Wait a few seconds for graceful shutdown
        sleep 3
        
        # Check if process is still running
        if kill -0 $PID 2>/dev/null; then
            echo "Process $PID still running, sending SIGKILL..."
            kill -KILL $PID
            sleep 1
        fi
        
        # Verify process is stopped
        if kill -0 $PID 2>/dev/null; then
            echo "Failed to stop process $PID"
        else
            echo "Successfully stopped server with PID: $PID"
        fi
    done
fi

# Also kill any Kotlin daemon processes that might be hanging around
echo "Checking for Kotlin daemon processes..."
KOTLIN_PIDS=$(ps aux | grep "kotlin.*daemon" | grep -v grep | awk '{print $2}')

if [ ! -z "$KOTLIN_PIDS" ]; then
    echo "Found Kotlin daemon process(es) with PID(s): $KOTLIN_PIDS"
    for PID in $KOTLIN_PIDS; do
        echo "Stopping Kotlin daemon with PID: $PID"
        kill -TERM $PID
    done
fi

echo "Server stop script completed."