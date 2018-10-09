### Memo
---
* 주키퍼, znode에서 노드간의 정보 관리
* 분산 코디네이터 역할
* 과반수가 생존해야 함
* Ensemble = Cluster

설정 방법
* Path/data/myid
    * 주키퍼 노드를 구분하기 위한 ID
        * 내용은 숫자 1
* 주키퍼 환경 설정 파일
    * /usr/local/zookeeper/conf/zoo.cfg
        * TickTime
        * initLimit
        * syncLimit
        * dataDir
        * clientPort
        * server.x
            * server.<my_id>형식으로 기입
                * Ex) server.1=192.168.0.1
* /usr/local/zookeeper/bin/zkServer.sh start
    * 주키퍼 시작
* Systemd
    * 리눅스 운영체제에서 서비스를 시작, 중지, 관리를 조정하기 위함
    * 다양한 상황에 따라 프로세스들을 관리하는 방법
        * vi /etc/systemd/system/zookeeper-server.service
            * [Unit] // 일반적인 옵션, 실행순서 조정을 많이 사용(After, Before)
            * Description=zookeeper-server // systemctl status 명령어에 나오는 내용
            * After=network.target // 지정된 유닛이 실행 된 이후 시작
                * Cf. Before은 그 반대
            * .
            * [Service] // 서비스 실행 관련 옵션
            * Type=forking...
* Systemctl daemon-reload
* systemctl start zookeeper-server.service
* systemctl enable zookeeper-server.service
    * 시스템 부팅시 자동시작 옵션
* Systemctl status zookeeper-server.service

카프카 설치
    * 과반수 방식으로 구성되는 주키퍼 => 따라서 홀수로 구성해야함
    * Kafka의 경우 홀수 구성에 영향을 받지 않음(3대, 4대 등 자유로움)
        * 하지만 카프카와 주키퍼는 물려서 구동되기 때문에, 주키퍼 에러가 나면 카프카도 장애가 남
        * 동일한 서버에 설치하는 것을 권장하지 않음
    카프카 환경 설정
    * peter-kafka001 - broker.id=1
    * peter-kafka002 - broker.id=2
    * peter-kafka003 - broker.id=3
    카프카는 consumer가 데이터를 가져가도 저장된 데이터를 임시 보관해야함
    * 따라서 data 디렉토리가 필요함
    * mkdir -p /data1과 같이 사용
    설정시 전체 zookeeper리스트를 설정해주는것이 좋음
    * 하나의 주키퍼노드만 설정시, 해당 노드가 죽으면 다같이 죽음
        * zookeeper.connect-peter-zk001:2181,peter-zk002:2181, peter-zk003:2181
            * 이렇게 설정하면 znode의 최상위 경로를 가져가게 됨
            * 하지만 이렇게 되면, 하나의 어플리케이션만 실행 가능
            * Znode를 구분하면 어플리케이션 다중 실행이 가능함
            * 호스트와 포트정보 이외에 znode 정보를 뒤에 입력하면 됨
        * zookeeper.connect-peter-zk001:2181,peter-zk002:2181, peter-zk003:2181/kafka01
        * 보통은 카프카 클러스터 이름으로 znode를 구분해서 사용한다.
    Vi /usr/local/kafka/config/server.properties
    * broker.id=1
    * log.dirs=/data1,/data2
        * 물리적 디스크 여러개 사용시, 위와 같이 다중으로
    * Zookeeper.connect=localhost:2181
        * zookeeper의 최상위가 아닌 별도의 znode설정해 사용하는 경우
            * 브로커가 시작할때 해당 znode가 zookeeper에 없다면 자동 생성
        * Zookeeper.connect=peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka
    카프카 실행
    * /usr/local/kafka/bin/kafka-server-start.sh /usr/local/kafka/config/server.properties
        * 쉘이 종료될 경우 같이 종료되므로, bg에서 실행시켜야 함
        * Kafka-server-stop.sh로 종료도 가능
    * /etc/systemd/system/kafka-server.servie
        * systemd 설정
    * 설정 이후, systemctl daemon-reload

    제대로 실행되었는지 확인
    * Netstat -ntlp | grep 2181
    * netstat -ntpl | grep 9092

    Zookeeper에서 Znode를 통한 카프가 정보 확인
    * Zookeeper중 하나의 서버에 접속하기
    * /usr/local/zookeeper/bin/zkCli.sh
        * ls /
        * ls /peter-kafka/brokers/ids
            * broker들의 id 확인
    카프카 로그 확인
    * Cat /usr/local/kafka/logs/server.log

    카프카 토픽 생성
    * Kafka-topics.sh --zookeeper .../peter-kafka --topic 
    메세지 퍼블리싱
    * Kafka-console.producer.sh

#### 2장, 카프카 설치
- systemctl을 사용하여,
    ```
    vi /etc/systemd/system/zookeeper-server.service
    
    [root@21a62dcd95c9 bin]# systemctl daemon-reload 
    Failed to get D-Bus connection: Operation not permitted 
    ```
    - Docker 관련 문제로 보임
    - 이후 문항 경우, 무시하고 진행

    - /etc/hosts 설정 해주기
    - zookeeper 구동시 상태확인
        ```
        zkServer.sh status # 현재 상태 확인

        zkServer.sh start-foreground # 로그를 출력하여, fg로 실행
        ```
    - docker 내부에서, /etc/hosts로 설정하더라도 제대로 먹지 않는다.
        - docker-compose를 수정하여 network 설정을 외부에서 잡아준다.
        - 단 recreating 과정을 거치기 때문에 컨테이너가 초기화 된다.
    - kafka 실행
        ```
        kafka-server-start.sh $KAFKA_HOME/config/server.properties # fg 실행
        kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties  # bg 실행방법 1
        kafka-server-start.sh $KAFKA_HOME/config/server.properties & # bg 실행방법 2
        ```
        - 쉘이 강제 중지 되면, 같이 종료되기때문에 fg보다 bg로 실행한다.
        - daemon 옵션은 **sh 파일 뒤**에 선언한다.
        ```
        yum -y install net-tools #netstat 설치

        netstat -ntlp | grep 2181 # zookeeper
        netstat -ntlp | grep 9092 # kafka
        # n : number
        # t : tcp
        # l : listening
        # p : pid
        ```
    - 카프카 정보 확인 및 접속
        - 카프카 접속 시, **zkCli.sh**를 이용한다.
        - Znode 확인
            ```
            [zk: localhost:2181(CONNECTED) 0] ls /
            [zookeeper, peter-kafka]
            ```
            - zookeeper.connect에 peter-kafka Znode 설정을 하였기 때문에, peter-kafka Znode 확인이 가능
        - Znode 정보 확인
            ```
            [zk: localhost:2181(CONNECTED) 1] ls /peter-kafka/brokers/ids
            [1, 2, 3]   
            ```
            - **broker.id**에 해당하는 정보를 확인할 수 있다.
    - 로그 확인
        - ``` cat $KAFKA_HOME/logs/server.log```로 확인할 수 있다.
    - 토픽(topic) 생성 및 메세지 보내기
        - **kafka-topics.sh** 파일을 이용한다.
        - options
            - **\-\-zookeeper** : zookeeper 정보 추가
            - **\-\-replication-factor** : replica 설정
            - **\-\-partitions** : 파티션 설정
            - **\-\-topic** : 토픽 이름 설정
            - **\-\-create** : 토픽 생성
            - **\-\-delete** : 토픽 제거
        - 실행 구문
            ```
            #CREATE
            [2018-10-09 06:35:33,296] INFO [KafkaServer id=1] started (kafka.server.KafkaServer)      │a8361b734732 (org.apache.kafka.common.utils.AppInfoP
            [root@node1 config]# kafka-topics.sh - --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka  --replication-factor 1 --partitions 1 --topic peter-topic --create
            Created topic "peter-topic". 

            #DELETE
            # replication-factor, partitions 옵션 X
            [root@node1 config]# kafka-topics.sh - --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka --topic peter-topic --delete
            Topic peter-topic is marked for deletion.
            Note: This will have no impact if delete.topic.enable is not set to true. 
            ```
        - 메세지 생성(producer)
            - **kafka-console-producer.sh**를 이용한다.
            - options
                - **\-\-broker-list** : 카프카를 설치한 서버 입력
                - **\-\-topic** : 토픽 이름 입력
            - 실행 구문
                ```
                [root@node1 config]# kafka-console-producer.sh \
                > --broker-list peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
                > --topic peter-topic
                >This is a message
                >This is another message
                >^C
                [root@node1 config]#   
                ```
        - 메세지 읽기(consumer)
            - **kafka-console-consumer.sh**를 이용한다.
            - options
                - **\-\-bootstrap-server** : 카프카 호스트 정보 입력
                - **\-\-topic** : 토픽 이름 입력
                - **\-\-from-beginning** : 시작부터 메세지 가져오기
            - 실행구문
                ```
                [root@node1 config]# kafka-console-consumer.sh --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 --topic peter-topic --from-beginning
                This is a message
                This is another message
                ```
        


    