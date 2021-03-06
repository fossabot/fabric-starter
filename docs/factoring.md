# Инструкция по подключению к сети Факторинг

## Цель документа

* обеспечить возможность самостоятельного развертывания узла для участия в децентрализованной сети на платформе HyperLedger Fabric
* обеспечить возможность по выполнению специальных настроек для использования смарт-контрактов ( ChainCode) по автоматизации процессов факторинга

## Термины и определения

* Канал - сущность в терминологии Hyperledger Fabric, соответствующая отдельной цепочке блоков транзакций, которая доступна и распространяется только между ограниченным кругом организаций и упорядочивающих узлов. Каждый канал характеризуется набором полномочий подключенных участников и правилами изменения полномочий
* Консорциум - перечень полноправных участников Сети. С технической точки зрения смарт-контракты Факторинг настроены таким образом, что не являющиеся членами консорциума организации имеет доступ только для просмотра данных в каналах

## Компонентный состав решения

* узел (`Peer`) - обязательный компонент, docker-container, внутри которого исполняет код, реализующий базовую бизнес-логику платформы: сетевое взаимодействие с другими участниками, взаимодействие с хранилищем (`Ledger`), развертывание смарт-контрактов (`ChainCode`)
* канал (`channel`) - в терминологии Hyperledger сущность, соответствующая отдельной цепочке данных (`ledger`) с определенным набором полномочий для определенного набора участников Сети. В рамках проекта Факторинг является аналогом контракта между Фактором и Покупателем (и опционально Продавцом), за пределами которого данные об осуществленных сделках не распространяются. Для удобства участников в Сети создается канал `common` для хранения публичной информации (список наименований и идентификаторов всех участвующих в Сети организаций, позднее планируется DNS, статус голосования по приглашениям и тп) 
* Смарт-контракт (`ChainCode`) - массив компонент, представляющих собой docker-container, содержащие исполняемый код, реализующий бизнес-логику по изменению и получению данных в ledger
* упорядочивающий узел (`Orderer`) - опциональный компонент, docker-container, внутри которого исполняется код получения транзакций от peer, проверка их корректности, упорядочивание и формирование блоков
* удостоверяющий цент (`CA`) - обязательный компонент, docker-container, содержащий в себе хранилище доверенных сертификатов других участников, а также выпускающий сертификаты для дополнительных узлов внутри предприятия
* вспомогательные сервисы (`Backend`) - опциональный компонент, docker-container, внутри которого исполняется код, предоставляющий возможность пользователям просматривать данные из ledger и исполнять смарт-контракты в браузере
* API сервер (`API Node`) -

## Этапы подключения участника

Можно выделить два основных этапа развертывания решения.

### Подключение к сети

Данный этап является общим для всех решений с использованием HyperLedger Fabric. Для успешного функционирования узла предприятия и взаимодействия с другими узлами необходимо выполнить описанные ниже действия

#### Инфраструктура

1. Выделить в сети предприятия или в облачной инфраструктуре сервер со следующими параметрами:
    * Ресурсы не менее чем 2 CPU, 2 Gb RAM, 10 GB HDD
    * Рекомендуемая ОС: Ubuntu LTS
    * Установленное ПО: Docker, Docker-compose

2. Указать адреса других участников сети в списке `/etc/hosts` :

```
91.220.181.242 www.mvideo.factoring
91.220.181.242 peer0.mvideo.factoring
91.220.181.242 peer1.mvideo.factoring

194.85.99.38 www.factoring
194.85.99.38 orderer.factoring

89.175.56.229 www.sbf.factoring
89.175.56.229 peer0.sbf.factoring
89.175.56.229 peer1.sbf.factoring
```
3. Обеспечить двусторонний сетевой доступ по протоколку TCP и портам 7050, 7051, 80 для указанных в п.2 адресов 

4. Настроить DNS для контейнеров:
```bash
sudo apt install dnsmasq
```

Проверить статус службы:

```bash
systemctl status dnsmasq
```

Найти адрес шлюза, который предоставляет Docker для внутренней подсети
```
ip addr | grep docker0
```

*Пример результата, фактические результаты могут отличаться, нужно использовать фактический адрес*:
  **docker0**: <BROADCAST,MULTICAST,UP,LOWER_UP>
  ...
    inet **`172.17.0.1`**/16

Добавить адрес в конфигурационный файл

```bash
sudo nano /etc/docker/daemon.json
```

Пример

```javascript
{
    "dns": ["172.17.0.1"]
}
```

Перезапустить службу Docker

```bash
sudo service docker restart
```

Создать конфигурационный файл для bridge сети

```
sudo mkdir -p /etc/NetworkManager/dnsmasq.d
sudo nano /etc/NetworkManager/dnsmasq.d/docker-bridge.conf
```

Добавить в него те же самые адреса:

```
listen-address=172.17.0.1
```

Перезапустить службу:

```
sudo service dnsmasq restart    
```

#### Развертывание контейнеров типовой конфигурации

1. Войти в консоль сервера
2. Клонировать этот репозиторий
3. Перейти в созданную в результате клонирования папку
4. **Важно!** В случае обновления с предыдущей версии необходимо выполнить очистку системы:

        ./clean.sh
        docker system prune
        rm -rf crypto-config
        ./clean.sh

Для очистки и развертывания Peer можно использовать готовый скрипт: factor-network/clean-install.sh

5. Задать переменную окружения с названием Сети:

        EXPORT DOMAIN="factoring"

6. **Только для участников ordering-serice!**  Выполнить команду для развертывания прикладного ПО для упорядочивающего узла (`Orderer`):

        docker-compose -f docker-compose-orderer.yaml -f docker-compose-orderer-ports.yaml up -d

    В результате запуститься и работать следующие контейнеры:

    * orderer.`org`.`domain` - упорядочивающий узел
    * www.`org`.`domain` - nginx для раздачи сертификатов
    * cli.`org`.`domain` - среда исполнения служебных команд

7. Установить переменные окружения для развертывания узла и смарт-контрактов

       EXPORT ORG="" #краткое название организации латинскими буквами, без дефисов
       EXPORT CHAINCODE_VERSION = #версия смарт-контракта, по умолчанию 1.0, желательно уточнять у владельца канала
       WORK_DIR = текущая директория

8. Выполнить команду

        docker-compose -f docker-compose.yaml -f docker-compose-ports.yaml up -d

    В результате запуститься и работать следующие контейнеры
    
    * peer0.`org`.`domain` - Узел участника
    * peer1.`org`.`domain` - Узел участника
    * api.`org`.`domain` - API сервер
    * ca.`org`.`domain` - Удостоверяющий центр
    * cli.`org`.`domain` - Сервер для выполнения служебных команд
    * www.`org`.`domain` - Сервер Nginx
    * backend.`org`.`domain` - Сервисный слой и веб-приложение
    
### Присоединение к консорциуму и создание каналов
**Только для участников ordering-serice!**

1. После настройки и проверки сетевого соединения, необходимо обеспечить включение организации (т.е. ее публичного ключа) в список доверенных участников. Данная команда должна быть выполнена на `Orderer`:

        ./consortium-add-org.sh $ORG

    В ближайшем обновлении платформы будет добавлена возможность приглашать новых участников для все действующих членов консорциума, и команда будет выполняться на `Peer`.

2. Для обеспечения взаимодействия среди всех участников создается канал по умолчанию `common` (это делается один раз при создании всей Сети) путем выполнения команды на `Peer`:

        ./channel-create.sh common

3. Для обеспечения возможности взаимодействия с конкретной подключаемой организацией необходимо создать канал между этими организациями путем выполнения команды на `Peer`:

        ./channel-create.sh org1-org2

В рамках проекта Факторинг для корректного отображения каналов в UI предполагается, что наименование канала формируется как "фактор-покупатель"
4. Для предоставления полномочий подключаемой организации на просмотр и изменение данных в канале необходимо выполнить следующую команду на `Peer` создателя канала:

        `./channel-add-org.sh $CHANNEL_NAME $ORG`

3. Необходимо от лица подключаемой организации выполнить комманду для каждого планируемого к использованию канала: `./channel-join.sh $CHANNEL_NAME `  

 **Примечание**:

* `$ORG` - название новой организации, указанное при развертывании узлов
* `$CHANNEL_NAME` - название канала, которое можно получить от его создателя

### Развертывание веб-приложения:
Для удобства работы со смарт-контрактом Факторинг было разработано небольшое приложение, обеспечивающее формирование отчетности и работу с файлами. Поскольку, оно не относится к типовой конфигурации, то инструкция по развертыванию вынесена в отдельный раздел, а установка выполняется отдельной командой

        docker-compose -f factor-network/docker/backend-compose.yaml up -d
### Развертывание смарт-контрактов

```bash
export CHAINCODE_VERSION=1.0
./chaincode-install.sh factor_scala $CHAINCODE_VERSION /opt/chaincode/java/factoring  java

```

### Проверить работоспособность приложения
Зайти на страницу http://hostname:5500/login