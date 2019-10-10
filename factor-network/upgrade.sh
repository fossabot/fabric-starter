CHAINCODE_VERSION=${1}
cd $WORK_DIR/chaincode/java/factoring
gradle clean shadowJar
cp .build/libs/chaincode.jar ../fatjar
cd $WORK_DIR
./chaincode-create-package.sh factor_scala /opt/chaincode/java/fatjar java $CHAINCODE_VERSION /opt/chaincode/factor_scala_$CHAINCODE_VERSION
./chaincode-install-package.sh /opt/chaincode/factor_scala_${CHAINCODE_VERSION}
./chaincode-upgrade.sh common factor_scala '["init", "{\"id\": \"org1\",\"mspId\":\"org1\",\"role\": \"Factor\",\"name\": \"Сбербанк Факторинг\"}", "{\"id\": \"org2\",\"mspId\":\"org2\",\"role\": \"Buyer\",\"name\": \"Мвидео\"}"]' $CHAINCODE_VERSION
