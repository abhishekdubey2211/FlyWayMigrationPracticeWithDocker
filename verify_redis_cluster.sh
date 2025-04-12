#!/bin/bash

# Configuration
MASTER_IP="172.21.0.3"
REDIS_PORT=6379
SENTINEL_PORT=26379
REDIS_PASSWORD="abhi123"
NETWORK_NAME="jodo-network"
MASTER_CONTAINER="redis-master"
SLAVE1_CONTAINER="redis-slave-1"
SLAVE2_CONTAINER="redis-slave-2"
SENTINEL1="sentinel-1"
SENTINEL2="sentinel-2"
SENTINEL3="sentinel-3"

echo "🚀 Verifying Docker Containers..."

containers=($MASTER_CONTAINER $SLAVE1_CONTAINER $SLAVE2_CONTAINER $SENTINEL1 $SENTINEL2 $SENTINEL3)

for container in "${containers[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "$container"; then
        echo "✅ $container is running."
    else
        echo "❌ $container is NOT running!"
        exit 1
    fi
done

echo ""
echo "🔄 Checking container network connectivity to $MASTER_IP..."

for container in $SENTINEL1 $SLAVE1_CONTAINER $SLAVE2_CONTAINER; do
    echo "➡️  Pinging master from $container..."
    docker exec "$container" ping -c 2 "$MASTER_IP" > /dev/null
    if [ $? -eq 0 ]; then
        echo "✅ $container can reach Redis master."
    else
        echo "❌ $container cannot reach Redis master!"
        exit 1
    fi
done

echo ""
echo "🔐 Testing Redis AUTH from Sentinel..."
docker exec "$SENTINEL1" redis-cli -h "$MASTER_IP" -p $REDIS_PORT -a "$REDIS_PASSWORD" PING | grep -q "PONG"
if [ $? -eq 0 ]; then
    echo "✅ Redis master is accessible and authenticated successfully."
else
    echo "❌ Redis master AUTH failed!"
    exit 1
fi

echo ""
echo "🔁 Checking slave replication status..."
for slave in $SLAVE1_CONTAINER $SLAVE2_CONTAINER; do
    echo "➡️  $slave replication info:"
    docker exec "$slave" redis-cli -a "$REDIS_PASSWORD" INFO replication | grep -E "role:|master_host"
done

echo ""
echo "🧭 Verifying Sentinel monitoring..."

docker exec "$SENTINEL1" redis-cli -p $SENTINEL_PORT SENTINEL get-master-addr-by-name mymaster | grep -q "$MASTER_IP"
if [ $? -eq 0 ]; then
    echo "✅ Sentinel is correctly monitoring 'mymaster' with IP $MASTER_IP."
else
    echo "❌ Sentinel is NOT monitoring 'mymaster' correctly!"
    exit 1
fi

echo ""
echo "✅✅✅ All services are running and verified successfully!"
