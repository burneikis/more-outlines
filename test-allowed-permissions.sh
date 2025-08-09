#!/bin/bash

# Test the permission system with server allowing the mod

SERVER_DIR="test-server"

echo "✅ Testing ALLOWED permissions..."

if [ ! -d "$SERVER_DIR" ]; then
    echo "❌ Test server not found! Please run ./setup-test-server.sh first"
    exit 1
fi

# Set server config to allow mod
cat > "$SERVER_DIR/config/more-outlines-server.json" << EOF
{
  "allowMoreOutlinesMod": true,
  "reason": "Test server allows More Outlines mod"
}
EOF

echo "✅ Server configured to ALLOW More Outlines mod"
echo "📝 Config: $SERVER_DIR/config/more-outlines-server.json"
echo ""
echo "🎮 Now start the server with: ./start-test-server.sh"
echo "🔗 Connect with client to localhost:25565"
echo "🧪 Expected behavior: Mod should work normally, outlines enabled"