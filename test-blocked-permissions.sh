#!/bin/bash

# Test the permission system with server blocking the mod

SERVER_DIR="test-server"

echo "🚫 Testing BLOCKED permissions..."

if [ ! -d "$SERVER_DIR" ]; then
    echo "❌ Test server not found! Please run ./setup-test-server.sh first"
    exit 1
fi

# Set server config to block mod
cat > "$SERVER_DIR/config/more-outlines-server.json" << EOF
{
  "allowMoreOutlinesMod": false,
  "reason": "Test server blocks More Outlines mod for testing"
}
EOF

echo "🚫 Server configured to BLOCK More Outlines mod"
echo "📝 Config: $SERVER_DIR/config/more-outlines-server.json"
echo ""
echo "🎮 Now start the server with: ./start-test-server.sh"
echo "🔗 Connect with client to localhost:25565"
echo "🧪 Expected behavior: Client should show 'mod not allowed' message, outlines disabled"