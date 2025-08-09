#!/bin/bash

# Start the More Outlines test server

SERVER_DIR="test-server"
MEMORY="2G"

echo "🚀 Starting More Outlines test server..."

if [ ! -d "$SERVER_DIR" ]; then
    echo "❌ Test server not found! Please run ./setup-test-server.sh first"
    exit 1
fi

cd "$SERVER_DIR"

if [ ! -f "fabric-server-launch.jar" ]; then
    echo "❌ Server jar not found! Please run ./setup-test-server.sh first"
    exit 1
fi

echo "🎮 Server starting on localhost:25565"
echo "📝 Check logs in $SERVER_DIR/logs/"
echo "🛑 Press Ctrl+C to stop the server"
echo ""

java -Xmx$MEMORY -Xms$MEMORY -jar fabric-server-launch.jar nogui