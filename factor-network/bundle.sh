# export ORG="factor"
# myNetWorkFolder="$(pwd)"
# export STARTER=~/Blockhain/HLF/fabric-starter
# cd $STARTER
./clean.sh
./generate-orderer.sh
docker-compose -f  docker-compose-orderer.yaml up -d
./generate-peer.sh
docker-compose up -d
./consortium-add-org.sh org1
./channel-create.sh common
./channel-join.sh common
./channel-create.sh org1-org2
./channel-join.sh org1-org2
./channel-create.sh org1-org3
./channel-join.sh org1-org3
./chaincode-install.sh factor_scala 3.1 /opt/chaincode/java/factor  java
./chaincode-instantiate.sh org1-org2 factor_scala '["init", "{\"id\": \"123\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"123\",\"mspId\":\"org1\",\"role\": \"Factor\"}"]' 3.1
./chaincode-instantiate.sh org1-org3 factor_scala '["init", "{\"id\": \"3123123\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"3123123\",\"mspId\":\"org3\",\"role\": \"Buyer\"}"]' 3.1
./chaincode-instantiate.sh common factor_scala '["init", "{\"id\": \"org1\",\"mspId\":\"org1\",\"role\": \"Factor\",\"name\": \"Сбербанк\"}", "{\"id\": \"org2\",\"mspId\":\"org2\",\"role\": \"Buyer\",\"name\": \"Мвидео\"}"]' 3.1
# cp -rf chaincode $STARTER
# ./chaincode-install.sh factor 1.0 factor  golang
# ./chaincode-instantiate.sh common factor '[]'
# cd $myNetWorkFolder
# cd ..