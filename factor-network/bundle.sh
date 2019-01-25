# export ORG="factor"
# myNetWorkFolder="$(pwd)"
# export STARTER=~/Blockhain/HLF/fabric-starter
# cd $STARTER
export FABRIC_VERSION=1.3.0
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
./chaincode-install.sh factor_scala 1.0 /opt/chaincode/java/factor  java
./chaincode-instantiate.sh org1-org2 factor_scala '["init", "{\"id\": \"aasd\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"org12\",\"mspId\":\"org1\",\"role\": \"Factor\"}"]' 1.0
./chaincode-instantiate.sh org1-org3 factor_scala '["init", "{\"id\": \"3159123\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"3123123\",\"mspId\":\"org3\",\"role\": \"Buyer\"}"]' 1.0
./chaincode-instantiate.sh common factor_scala '["init", "{\"id\": \"fasf\",\"mspId\":\"org1\",\"role\": \"Factor\",\"name\": \"Сбербанк\"}", "{\"id\": \"qdqwds\",\"mspId\":\"org2\",\"role\": \"Buyer\",\"name\": \"Мвидео\"}"]' 1.0
# ./chaincode-reload.sh org1-org2 factor_scala '["init", "{\"id\": \"org1_1\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"org1_2\",\"mspId\":\"org1\",\"role\": \"Factor\"}"]' /opt/chaincode/java/factor java
# cp -rf chaincode $STARTER
# ./chaincode-install.sh factor 1.0 factor  golang
# ./chaincode-instantiate.sh common factor '[]'
# cd $myNetWorkFolder
# cd ..