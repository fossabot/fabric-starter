# export ORG="factor"
myNetWorkFolder="$(pwd)"
export STARTER=~/Blockhain/HLF/fabric-starter
cd $STARTER
./clean.sh
./generate-orderer.sh
docker-compose -f  docker-compose-orderer.yaml up -d
./generate-peer.sh
docker-compose up -d
./consortium-add-org.sh org1
./channel-create.sh common
./channel-join.sh common
cp -rf chaincode $STARTER
./chaincode-install.sh factor 1.0 factor  golang
./chaincode-instantiate.sh common factor '[]'
# cd $myNetWorkFolder
# cd ..