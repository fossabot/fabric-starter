#!/usr/bin/env bash
source lib/util/util.sh

setDocker_LocalRegistryEnv

if [ -n "$DOCKER_REGISTRY" ]; then
    DOCKER_MACHINE_FLAGS="${DOCKER_MACHINE_FLAGS} --engine-insecure-registry $DOCKER_REGISTRY "
    echo "Using docker-registry: $DOCKER_REGISTRY"
fi

function info() {
    echo -e "************************************************************\n\033[1;33m${1}\033[m\n************************************************************"
}

info "Create Network with vm names prefix: $VM_NAME_PREFIX"
#read -n1 -r -p "Press any key to continue..." key

: ${DOMAIN:=example.com}

orgs=${@:-org1}

# Create orderer host machine

ordererMachineName="orderer.$DOMAIN"

info "Creating $ordererMachineName, Options: $DOCKER_MACHINE_FLAGS"

docker-machine rm ${ordererMachineName} --force
docker-machine create ${DOCKER_MACHINE_FLAGS} ${ordererMachineName}

# Create member organizations host machines

for org in ${orgs}
do
    orgMachineName="${org}.$DOMAIN"
    info "Creating member organization machine: $orgMachineName with flags: $DOCKER_MACHINE_FLAGS"
    docker-machine rm ${orgMachineName} --force
    docker-machine create ${DOCKER_MACHINE_FLAGS} ${orgMachineName}
done

