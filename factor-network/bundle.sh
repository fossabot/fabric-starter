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
./consortium-add-org.sh "$ORG"
./channel-create.sh common
./channel-join.sh common
./channel-create.sh "$ORG-org2"
./channel-join.sh "$ORG-org2"
./chaincode-install.sh factor_scala 1.0 /opt/chaincode/java/factoring  java
./chaincode-instantiate.sh "$ORG-org2" factor_scala '["init", "{\"id\": \"aasd\",\"mspId\":\"$ORG\",\"role\": \"Buyer\"}", "{\"id\": \"org12\",\"mspId\":\"$ORG\",\"role\": \"Factor\"}"]' 1.0
./chaincode-instantiate.sh common factor_scala '["init", "{\"id\": \"fasf\",\"mspId\":\"$ORG\",\"role\": \"Factor\",\"name\": \"Сбербанк\"}", "{\"id\": \"qdqwds\",\"mspId\":\"org2\",\"role\": \"Buyer\",\"name\": \"Мвидео\"}"]' 1.0
# ./chaincode-reload.sh $ORG-org2 factor_scala '["init", "{\"id\": \"org1_1\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"org1_2\",\"mspId\":\"org1\",\"role\": \"Factor\"}"]' /opt/chaincode/java/factor java
# cp -rf chaincode $STARTER
# ./chaincode-install.sh factor 1.0 factor  golang
# ./chaincode-instantiate.sh common factor '[]'
# cd $myNetWorkFolder
# cd ..