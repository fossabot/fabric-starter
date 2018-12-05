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
./channel-create.sh sber0mvideo
./channel-join.sh sber0mvideo
./channel-create.sh sber0severstal
./channel-join.sh sber0severstal
./chaincode-install.sh factor_scala 3.1 /opt/chaincode/java/factor  java
./chaincode-instantiate.sh sber0mvideo factor_scala '["init", "{\"id\": \"org1b\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"org1\",\"mspId\":\"org1\",\"role\": \"Buyer\"}"]' 3.1
./chaincode-instantiate.sh sber0severstal factor_scala '["init", "{\"id\": \"org1b\",\"mspId\":\"org1\",\"role\": \"Buyer\"}", "{\"id\": \"org1\",\"mspId\":\"org1\",\"role\": \"Buyer\"}"]' 3.1
# cp -rf chaincode $STARTER
# ./chaincode-install.sh factor 1.0 factor  golang
# ./chaincode-instantiate.sh common factor '[]'
# cd $myNetWorkFolder
# cd ..