#!/usr/bin/env bash
source lib.sh
usageMsg="$0 channelName newOrg"
exampleMsg="$0 common org2"

IFS=
channelName=${1:?`printUsage "$usageMsg" "$exampleMsg"`}
newOrg=${2:?`printUsage "$usageMsg" "$exampleMsg"`}
port = ${3}
downloadMSP ${newOrg} ${port}
addOrgToChannel $channelName $newOrg