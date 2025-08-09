#!/bin/bash

# More Outlines Test Server Setup Script
# This script sets up a Fabric server for testing the permission system

set -e

SERVER_DIR="test-server"
MINECRAFT_VERSION="1.21.8"
FABRIC_VERSION="0.16.14"
FABRIC_API_VERSION="0.131.0+1.21.8"

echo "🚀 Setting up More Outlines test server..."

# Create server directory
mkdir -p "$SERVER_DIR"
cd "$SERVER_DIR"

# Create basic server structure
mkdir -p mods config logs

# Download Fabric server installer if not present
if [ ! -f "fabric-server-installer.jar" ]; then
    echo "📥 Downloading Fabric server installer..."
    curl -L -o fabric-server-installer.jar "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.1/fabric-installer-1.0.1.jar"
fi

# Install Fabric server if server jar doesn't exist
if [ ! -f "fabric-server-launch.jar" ]; then
    echo "🔧 Installing Fabric server..."
    java -jar fabric-server-installer.jar server -mcversion "$MINECRAFT_VERSION" -loader "$FABRIC_VERSION" -downloadMinecraft
fi

# Download Fabric API if not present
if [ ! -f "mods/fabric-api-$FABRIC_API_VERSION.jar" ]; then
    echo "📥 Downloading Fabric API..."
    curl -L -o "mods/fabric-api-$FABRIC_API_VERSION.jar" "https://github.com/FabricMC/fabric/releases/download/$FABRIC_API_VERSION/fabric-api-$FABRIC_API_VERSION.jar"
fi

# Copy our mod to the server (main jar only, not sources)
echo "📦 Copying More Outlines mod to server..."
cp ../build/libs/more-outlines-1.0.0.jar mods/ 2>/dev/null || {
    echo "❌ Mod jar not found! Please build the mod first with: ./gradlew build"
    exit 1
}

# Create server.properties
cat > server.properties << EOF
enable-jmx-monitoring=false
rcon.port=25575
level-seed=
gamemode=creative
enable-command-block=true
enable-query=false
generator-settings={}
enforce-secure-profile=true
level-name=world
motd=More Outlines Test Server
query.port=25565
pvp=true
generate-structures=true
max-chained-neighbor-updates=1000000
difficulty=peaceful
network-compression-threshold=256
max-tick-time=60000
require-resource-pack=false
use-native-transport=true
max-players=20
online-mode=false
enable-status=true
allow-flight=true
initial-disabled-packs=
broadcast-rcon-to-ops=true
view-distance=10
server-ip=
resource-pack-prompt=
allow-nether=true
server-port=25565
enable-rcon=false
sync-chunk-writes=true
op-permission-level=4
prevent-proxy-connections=false
hide-online-players=false
resource-pack=
entity-broadcast-range-percentage=100
simulation-distance=10
rcon.password=
player-idle-timeout=0
debug=false
force-gamemode=false
rate-limit=0
hardcore=false
white-list=false
broadcast-console-to-ops=true
spawn-npcs=true
spawn-animals=true
log-ips=true
function-permission-level=2
initial-enabled-packs=vanilla
level-type=minecraft\:normal
text-filtering-config=
spawn-monsters=true
enforce-whitelist=false
spawn-protection=0
resource-pack-sha1=
max-world-size=29999984
EOF

# Accept EULA
echo "eula=true" > eula.txt

# Create default server permission config (allowing mod)
mkdir -p config
cat > config/more-outlines-server.json << EOF
{
  "allowMoreOutlinesMod": true,
  "reason": "Test server allows More Outlines mod"
}
EOF

echo "✅ Test server setup complete!"
echo ""
echo "📁 Server directory: $SERVER_DIR/"
echo "🎮 To start the server: ./start-test-server.sh"
echo "🚫 To test blocked permissions: ./test-blocked-permissions.sh"
echo "✅ To test allowed permissions: ./test-allowed-permissions.sh"

cd ..