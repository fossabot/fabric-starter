#!/usr/bin/env bash
source lib.sh
usageMsg="$0 newOrg [consortiumName=SampleConsortium]"
exampleMsg="$0 org1"

IFS=
NEWORG=${1:?`printUsage "$usageMsg" "$exampleMsg"`}
consortiumName=${2:-"SampleConsortium"}
port=${3}
echo "Add $NEWORG with port $port to consortium $consortiumName"
EXECUTE_BY_ORDERER=1 downloadMSP ${NEWORG} ${port}
#EXECUTE_BY_ORDERER=1 txTranslateChannelConfigBlock "orderer-system-channel"
EXECUTE_BY_ORDERER=1 updateConsortium $NEWORG "orderer-system-channel"
