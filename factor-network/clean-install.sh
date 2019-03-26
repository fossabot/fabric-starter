#!/bin/bash
export ORG=$1
export DOMAIN=$2
export WORK_DIR=${pwd}
./clean.sh
docker system prune
rm -rf crypto-config/
./clean.sh
docker-compose -f docker-compose.yaml -f docker-compose-ports.yaml up -d
