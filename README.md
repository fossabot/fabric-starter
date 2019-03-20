# Инструкция по подключению к сети Факторинг

## Цель документа

* обеспечить возможность самостоятельного развертывания узла для участия в децентрализованной сети на платформе HyperLedger Fabric
* обеспечить возможность по выполнению специальных настроек для использования смарт-контрактов ( ChainCode) по автоматизации процессов факторинга

## Термины и определения

* Канал - сущность в терминологии Hyperledger Fabric, соответствующая отдельной цепочке блоков транзакций, которая доступна и распространяется только между ограниченным кругом организаций и упорядочивающих узлов. Каждый канал характеризуется набором полномочий подключенных участников и правилами изменения полномочий
* Консорциум - перечень полноправных участников Сети. С технической точки зрения смарт-контракты Факторинг настроены таким образом, что не являющиеся членами консорциума организации имеет доступ только для просмотра данных в каналах

## Компонентный состав решения

* узел (`peer`) - обязательный компонент, docker-container, внутри которого исполняет код, реализующий базовую бизнес-логику платформы: сетевое взаимодействие с другими участниками, взаимодействие с хранилищем (`ledger`), развертывание смарт-контрактов (`ChainCode`)
* Смарт-контракт (`ChainCode`) - массив компонент, представляющих собой docker-container, содержащие исполняемый код, реализующий бизнес-логику по изменению и получению данных в ledger
* упорядочивающий узел (`Orderer`) - опциональный компонент, docker-container, внутри которого исполняется код получения транзакций от peer, проверка их корректности, упорядочивание и формирование блоков
* удостоверяющий цент (`CA`) - обязательный компонент, docker-container, содержащий в себе хранилище доверенных сертификатов других участников, а также выпускающий сертификаты для дополнительных узлов внутри предприятия
* вспомогательные сервисы (`Backend`) - опциональный компонент, docker-container, внутри которого исполняется код, предоставляющий возможность пользователям просматривать данные из ledger и исполнять смарт-контракты в браузере
* API сервер (`API Node`) -

## Этапы подключения участника

Можно выделить два основных этапа развертывания решения.

### Подключение к сети

- [Installation.](#install)
- [Network with 1 organization (and orderer) for development.](#example1org)
- [Several organizations on one (local) host in multiple docker containers.](#example3org)
- [REST API to query and invoke chaincodes.](#restapi)
- [Getting closer to production. Multiple hosts deployment with `docker-machine`. Deployment to clouds.](#multihost)
- [Join to an External Network](#joinexternal)
- [Consortium Types. Invite-based and Majority-based Governance](#consortiumtypes)
- [Development\Release cycle](#releasecycle)

#### Инфраструктура

1. Выделить в сети предприятия или в облачной инфраструктуре сервер со следующими параметрами:
    * Ресурсы не менее чем 2 CPU, 2 Gb RAM, 10 GB HDD
    * Рекомендуемая ОС: Ubuntu LTS
    * Установленное ПО: Docker, Docker-compose

2. Обеспечить сетевой доступ по следующим портам и протоколам:

    |Откуда|Куда|Протокол|Порт|
    |--|--|--|--|
    |*|localhost|TCP|7053|
    |*|localhost|TCP|7051|
    |*|localhost|TCP|80|
    |localhost|52.174.22.75|TCP|7050-7054,80|
    |localhost|104.40.205.94|TCP|7050-7054,80|

3. Указать адреса других участников сети в списке `/etc/hosts` :

```
52.174.22.75 www.mvideo.factoring
52.174.22.75 peer0.mvideo.factoring
52.174.22.75 peer1.mvideo.factoring

104.40.205.94 www.factoring
104.40.205.94 orderer.factoring

<a name="example1org"></a>
## Create a network with 1 organization for development
See [One Org Network](docs/network-one-org.md)



<a name="example3org"></a>
## Create a local network of 3 organizations
See [Three local Orgs Network](docs/network-three-org.md)


<a name="restapi"></a>
## Use REST API to query and invoke chaincodes
See [Use REST Api](docs/rest-api.md)

<a name="multihost"></a>
## Multi host deployment
See [Multi host deployment](docs/multihost.md)

Создать конфигурационный файл для bridge сети

<a name="joinexternal"></a>
## Join to an External Network
For `invite-based` blockchain-networks (see next chapter) new organization can be added to the consortium by a member of this network.
The new organization need to obtain the BOOTSRAP_IP (currently it's the IP of the _orderer_ node) and deploy its own node with this IP.  
```bash
export BOOTSTRAP_IP=192.168.0.1
#ORG=... DOMAIN=... docker-compose up
```
Then the new organization passes the ip address of the newly deployed node to the network's member and this member adds the organization to Consortium by it's administration dashboard.
After that the new organization can create own channels, add other organizations to the own channels and even invite more organizations to the network itself.     

<a name="consortiumtypes"></a>
## Consortium Types. Invite-based and Majority-based Governance

So now our network can be governed by itself (or to say it right by the netwrk's members). 
The first type of network-governance is `Invite-based`. With this type of deployment 
any organization ((and not a central system administrator)) - member of the blockchain network can add new organization to consortium.

To deploy such type of network export environment variable
```bash
export CONSORTIUM_CONFIG=InviteConsortiumPolicy
``` 

`Majority` type of governance is coming.       


<a name="releasecycle"></a>
## Releases\Snapshots cycle

 После настройки и проверки сетевого соединения, необходимо обеспечить включение организации (т.е. ее публичного ключа) в список доверенных участников. Для участия в сети необходимо явным образом  выраженное согласие других участников, которое с технической точки зрения выражается в следующем:

 1. Организация, обладающая упорядочивающим узлом (`Orderer`) должна пригласить нового участника в круг участников ( консорциум), выполнив команду: `./consortium-add-org.sh "$ORG"` . Кроме того, информация об адресах компонентов новой организации должны быть переданы и зафиксированы в настройках узлов других участников Сети.*Эта команда сработает только в соответствующем контуре уполномоченной организации, где развернут упорядочивающий узел*
 2. Организация, создавшая канал, должна пригласить нового участника в канал, выполнив команду для каждого канала: `./channel-add-org.sh $CHANNEL_NAME $ORG`. *Эта команда сработает только у уполномоченной организации - создателя канала (контракта)*
 3. Необходимо от лица подключаемой организации выполнить комманду для каждого планируемого к использованию канала: `./channel-join.sh $CHANNEL_NAME `  

 **Примечание**:

* `$ORG` - название новой организации, указанное при развертывании узлов
* `$CHANNEL_NAME` - название канала, которое можно получить от его создателя

### Развертывание веб-приложения:
Для удобства работы со смарт-контрактом Факторинг было разработано небольшое приложение, обеспечивающее формирование отчетности и работу с файлами. Поскольку, оно не относится к типовой конфигурации, то инструкция по развертыванию вынесена в отдельный раздел, а установка выполняется отдельной командой

        docker-compose -f factor-network/docker/backend-compose.yaml up -d
### Развертывание смарт-контрактов

- master(development)
- snapshot-0.4-1.4
    - auto-generate crypto configuration
    - Invite type consortium
    - BOOTSTRAP_IP for new node joining
- snapshot-0.3-1.4
    - use _fabric-starter-rest:snapshot-0.3-1.4_
- snapshot-0.2-1.4
    - use _fabric-starter-rest:snapshot-0.2-1.4_
- snapshot-0.1-1.4
    - start snapshot branching

```

### Проверить работоспособность приложения
Зайти на страницу http://hostname:5500/login