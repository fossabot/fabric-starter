# usefull stuff

## API Node

```bash
JWT=`(curl -d '{"username":"user1","password":"pass"}' --header "Content-Type: application/json" http://localhost:4000/users | tr -d '"')`
```

```bash  
curl -H "Authorization: Bearer $JWT" --header "Content-Type: application/json" -d '{"fcn":"set","args":["a","20"]}' http://localhost:4000/channels/common/chaincodes/sacc
```

## 1.Create Orderer

### 1.1 Create machine

```bash
docker-machine create --driver digitalocean --digitalocean-access-token=$DO_TOKEN orderer
# docker swarm init --advertise-addr 40.68.7.39 #CHANGE IP
docker swarm init --advertise-addr 192.168.0.6
```

### 1.2 Deploy

```bash
docker swarm join-token worker
docker-machine scp -r templates factoringdev-orderer:templates
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-forderer-multihost.yaml"
export ORG=""
export COMPOSE_PROJECT_NAME="factoring"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
./generate-orderer.sh
docker-compose -f docker-compose-orderer.yaml -f orderer-multihost.yaml up -d
```

## 2. Create Factor

### 2.1 Create machine

    ```bash
    docker-machine create --driver digitalocean --digitalocean-access-token=$DO_TOKEN factor
    docker swarm join --token SWMTKN-1-32fugck6qik1jn97xqaj7b2ls0i9d5foyri53jteyer3wzf4uw-7qvsxg0zy1apdgd2m62utt4j5 104.248.6.140:2377
    # export WORK_DIR=/root
    ```

### 2.2 Deploy

```bash
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
# docker-machine scp -r templates factoringdev-factor:templates
# docker-machine scp -r chaincode factoringdev-factor:.
# docker-machine scp -r webapp factoringdev-factor:.
# docker-machine scp -r backend factoringdev-factor:.
export ORG="factor"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring.ru
docker run -dit --name alpine --network fabric-overlay alpine
./generate-peer.sh
docker-compose -f docker-compose.yaml -f multihost.yaml up -d
```

### 2.3 Add factor to the consortium

```bash
eval "$(docker-machine env orderer)"
./consortium-add-org.sh factor
```

### 2.4 Create channel

```bash
eval "$(docker-machine env factor)"
./channel-create.sh common
./channel-join.sh common
```

### 2.5. Chaincode

```bash
./chaincode-install.sh factor 1.0 factor  golang
./chaincode-instantiate.sh common factor '[]' 1.0
```

## 3. Buyer

### 3.1 Create machine

```bash
docker-machine create --driver digitalocean --digitalocean-access-token=$DO_TOKEN buyer
# docker swarm join --token SWMTKN-1-32fugck6qik1jn97xqaj7b2ls0i9d5foyri53jteyer3wzf4uw-7qvsxg0zy1apdgd2m62utt4j5 104.248.6.140:2377
# docker swarm join --token SWMTKN-1-39i5jxsnmp0oqhn3thwcpoubt97qbb8hatukv3rte378gg2i21-6p0m1hemmcmncak5vh8fd6gq8 192.168.0.6:2377
```

### 3.2 Deploy

```bash
eval "$(docker-machine env factoringdev-buyer)"
export WORK_DIR=/home/factoring_admin
export COMPOSE_FLAGS="-fmultihost.yaml"
docker-machine scp -r templates factoringdev-buyer:templates
docker-machine scp -r chaincode factoringdev-buyer:.
docker-machine scp -r webapp factoringdev-buyer:.
docker-machine scp -r backend factoringdev-buyer:.
export ORG="buyer"
export ORGS='{"factor": "peer0.factor.factoring.ru:7051", "buyer": "peer0.buyer.factoring.ru:7051", "seller": "peer0.seller.factoring.ru:7051"}'
export CAS='{ "factor": "ca.factor.factoring.ru:7054", "buyer": "ca.buyer.factoring.ru:7054", "seller": "ca.seller.factoring.ru:7054" }'
export DOMAIN=factoring2.ru
docker run -dit --name alpine --network fabric-overlay alpine
./generate-peer.sh
docker-compose -f docker-compose.yaml -f multihost.yaml up -d
docker-compose -f factor-network/docker/backend-compose.yaml up -d
```

### 3.3 Add factor to the consortium

```bash
eval "$(docker-machine env orderer)"
./consortium-add-org.sh buyer
```

### 3.4 Join channel

```bash
eval "$(docker-machine env factor)"
./channel-add-org.sh common buyer
```

```bash
eval "$(docker-machine env buyer)"
./channel-join.sh common
```

### 3.5. Chaincode

```bash
./chaincode-install.sh factor 1.0 factor  golang
./chaincode-instantiate.sh common factor '[]' 1.0
```