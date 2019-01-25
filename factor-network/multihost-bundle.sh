# docker-machine scp -r templates factoringdev-orderer:templates
export FABRIC_VERSION=1.4.0
eval "$(docker-machine env factoringdev-orderer)"
./clean.sh
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-forderer-multihost.yaml"
export ORG=""
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./generate-orderer.sh
docker-compose -f docker-compose-orderer.yaml -f orderer-multihost.yaml up -d

# docker-machine scp -r templates factoringdev-factor:templates
# docker-machine scp -r chaincode factoringdev-factor:.
# docker-machine scp -r webapp factoringdev-factor:.
# docker-machine scp -r backend factoringdev-factor:.
eval "$(docker-machine env factoringdev-factor)"
./clean.sh
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export ORG="factor"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
# docker run -dit --name alpine --network fabric-overlay alpine
./generate-peer.sh
docker-compose -f docker-compose.yaml -f multihost.yaml up -d
docker-compose -f factor-network/docker/backend-compose.yaml up -d

eval "$(docker-machine env factoringdev-orderer)"
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-forderer-multihost.yaml"
export ORG=""
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./consortium-add-org.sh factor

eval "$(docker-machine env factoringdev-factor)"
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export ORG="factor"
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
# docker-machine scp -r chaincode factoringdev-factor:.
./channel-create.sh common
./channel-join.sh common
./channel-create.sh factor-buyer
./channel-join.sh factor-buyer
./channel-create.sh factor-buyer2
./channel-join.sh factor-buyer2
./chaincode-install.sh factor_scala 1.0 /opt/chaincode/java/factor  java
./chaincode-instantiate.sh factor-buyer factor_scala '["init", "{\"id\": \"1\",\"mspId\":\"factor\",\"role\": \"Factor\"}", "{\"id\": \"2\",\"mspId\":\"buyer\",\"role\": \"Buyer\"}"]'  1.0
./chaincode-instantiate.sh common factor_scala '["init", "{\"id\": \"factor1\",\"mspId\":\"factor\",\"role\": \"Factor\",\"name\": \"Сбербанк\"}", "{\"id\": \"buyer1\",\"mspId\":\"buyer\",\"role\": \"Buyer\",\"name\": \"Мвидео\"}"]' 1.0
./chaincode-instantiate.sh factor-buyer2 factor_scala '["init", "{\"id\": \"a1\",\"mspId\":\"factor\",\"role\": \"Factor\"}", "{\"id\": \"2c\",\"mspId\":\"buyer2\",\"role\": \"Buyer\"}"]'  1.0
# ./chaincode-reload.sh factor-buyer factor_scala '["init", "{\"id\": \"1\",\"mspId\":\"factor\",\"role\": \"Factor\"}", "{\"id\": \"2\",\"mspId\":\"buyer\",\"role\": \"Buyer\"}"]' /opt/chaincode/java/factor java

# docker-machine scp -r templates factoringdev-buyer:templates
# docker-machine scp -r webapp factoringdev-buyer:.
# docker-machine scp -r backend factoringdev-buyer:.
eval "$(docker-machine env factoringdev-buyer)"
./clean.sh
# docker-machine scp -r chaincode factoringdev-buyer:.
# docker run -dit --name alpine --network fabric-overlay alpine
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export COMPOSE_PROJECT_NAME="factoring"
export ORG="buyer"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./generate-peer.sh
docker-compose -f docker-compose.yaml -f multihost.yaml up -d
docker-compose -f factor-network/docker/backend-compose.yaml up -d

eval "$(docker-machine env factoringdev-factor)"
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export ORG="factor"
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./channel-add-org.sh factor-buyer buyer
./channel-add-org.sh common buyer


eval "$(docker-machine env factoringdev-buyer)"
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
export ORG="buyer"
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./channel-join.sh common
./channel-join.sh factor-buyer
./chaincode-install.sh factor_scala 1.0 /opt/chaincode/java/factor  java
# ./factor-network/upgrade.sh