#!/usr/bin/env bash
source lib.sh
usageMsg="$0 channelName newOrg"
exampleMsg="$0 common org2"

IFS=
channelName=${1:?`printUsage "$usageMsg" "$exampleMsg"`}
newOrg=${2:?`printUsage "$usageMsg" "$exampleMsg"`}
port1=${3:-80}
port2=${4:-7051}
downloadMSP ${newOrg} ${port1}
addOrgToChannel $channelName $newOrg $port2