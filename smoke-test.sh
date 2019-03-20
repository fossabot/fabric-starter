#!/usr/bin/env bash

source lib.sh

export MULTIHOST=true
export DOMAIN=${DOMAIN-example.com}

first_org=${1:-org1}
ip=$(getMachineIp ${first_org})

info "Smoke test logs into $first_org at $ip and queries dns chaincode via rest api"

jwt=`(curl -d '{"username":"user1","password":"pass"}' -H "Content-Type: application/json" http://${ip}:4000/users | tr -d '"')`
curl -H "Authorization: Bearer $jwt" "http://$ip:4000/channels/common/chaincodes/dns?fcn=range"
echo

info "Query reference chaincode"
curl -H "Authorization: Bearer $jwt" "http://$ip:4000/channels/common/chaincodes/reference?fcn=range"
echo
