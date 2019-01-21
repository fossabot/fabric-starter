
#FACTOR
eval "$(docker-machine env factoringdev-factor)"
docker-machine scp -r chaincode factoringdev-factor:.
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export ORG="factor"
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./chaincode-reload.sh factor-buyer factor_scala '["init", "{\"id\": \"1\",\"mspId\":\"factor\",\"role\": \"Factor\"}", "{\"id\": \"2\",\"mspId\":\"buyer\",\"role\": \"Buyer\"}"]' /opt/chaincode/java/factor java

#BUYER
eval "$(docker-machine env factoringdev-buyer)"
#docker-machine scp -r chaincode factoringdev-buyer:.
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export COMPOSE_PROJECT_NAME="factoring"
export ORG="buyer"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./chaincode-install.sh factor_scala 1.12664 /opt/chaincode/java/factor  java