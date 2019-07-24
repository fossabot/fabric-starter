# Инструкция по подключению к сети Факторинг

1. Настроить DNS
2. Настроить окружение:

    ``` bash
    export ORG=alfa && export DOMAIN=factoring && export COMPOSE_PROJECT_NAME=alfa && export WWW_PORT=7062 && export PEER0_PORT=7061 && export CUSTOM_PORT_CONFIG=/etc/factor-network/custom_port_config.json
    ```

3. Запустить контейнеры:

    ```bash
    docker-compose -f docker-compose.yaml -f docker-compose-ports.yaml -f factor-network/factoring.yaml up -d
    ```

4. Присоединиться к каналу common:

    ```bash
    ./channel-join.sh common
    ```

5. Создать канал с Мвидео

    ```bash
    ./channel-create.sh alfa-mvideo
    ```

6. Присоединиться к созданному каналу

    ```bash
    ./channel-join.sh alfa-mvideo
    ```

7. Установить chaincode

    ```bash
    ./chaincode-install.sh /opt/chaincode/fatjar
    ```

8. Создать контейнер с chaincode

    ```bash
    ./chaincode-instantiate.sh alfabank-mvm factor_scala '["init", "{\"id\": \"alfa\",\"mspId\":\"alfa\",\"role\": \"Factor\",\"name\": \"Альфа Банк\"}", "{\"id\": \"mvm\",\"mspId\":\"mvm\",\"role\": \"Buyer\",\"name\": \"Мвидео\"}"]' 2.42
    ```

9. Добавить в канал МВидео

    ```bash
    ./channel-add-org.sh mvm
    ```
