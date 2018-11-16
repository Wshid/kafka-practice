### 2장, 카프카 설치
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