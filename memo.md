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
### 3장, 카프카 디자인
---
- 3.1. 카프카 디자인의 특징
    - 분산 시스템의 장점
        - 단일 시스템 보다 높은 성능
        - 분산 시스템 중 하나의 노드 장애시 다른 노드가 처리
        - 시스템 확장 용이
    - 페이지 캐시(page cache)
        - kafka와 disk사이에 캐시를 두어 더 빠른 처리가 가능하도록 함
        - kafka를 SATA디스크를 사용해도 무방
    - 배치 전송 처리
        - 여러 IO를 하나의 배치 형태로 처리
        - 4개의 메세지 전송을 4번의 IO를 거치는 것이 아닌
            - 하나로 묶어, 1번의 IO로 처리
- 3.2. 카프카 데이터 모델
    - topic
        - 특정 topic을 정의하여 데이터 저장소를 운영할 수 있음
        - 해당 topic에 접근하여 메세지를 보내거나 받을 수 있음
        - 249자 미만으로 영문, 문자, ., \_, \-로 구성 가능
        - 토픽 이름을 구분해주는 것이 좋음
            - 접두사(prefix) 활용
                - sbs-news, sbs-video
                - kbs-news, kbs-video 등
    - parition
        - topic을 분할한 것
        - 하나의 토픽을 여러 파티션으로 나누어, 병렬로 데이터를 받을 수 있음
        - 메세지의 순서가 보장되어야 함
        - **partition ∝ producer ∝ consumer**
        - 일반적으로 partition의 수가 증가하면 메세지 전송 속도가 증가
        - 고려할 사항
            - 파일 핸들러의 낭비
                - partition은 broker의 dir과 매핑
                - 저장되는 데이터마다 2개의 파일(index, 실제 데이터)
                - kafka의 모든 디렉토리의 파일들에 대해, File handler를 열게 됨
                    - **그에 따른 리소스 낭비 발생**
            - 장애 복구 시간 증가
                - 높은 가용성을 위해 replicaiton 지원
                    - broker에는 topic이 있음
                        - topic에는 여러개의 partition
                            - **broker에는 여러 partition이 존재
                        - 각 partition마다 replication이 동작
                            - partition leader / partition follower로 구분
                - broker가 다운되면
                    - 해당 broker가 PL인 경우 일시적으로 사용 불가
                        - follower중 하나를 leader 선출
                            - broker가 장애처리 시행
                - 장애 복구 하는데에(새로운 리더 선출) 그만큼의 시간이 발생함
        - 적절한 파티션의 수
            - 원하는 목표 처리량의 기준 잡기
                - 예시
                    - 4 * producer, 10msg/sec
                        - topic에서 40msg/sec가 되어야 함
                        - 해당 토픽에서 partition=1, 10msg/sec 이라면.
                            - partition을 4개로 하여, 10초만에 처리 가능하도록 함
                    - 하지만 consumer의 입장도 고려해야함
                        - 8 * consumer, 5msg/sec
                            - 해당 topic의 consumer는 partition과 동일하게 8개로 맞춰주어야 함
        - **파티션의 수는 증가는 수시로 가능하지만, 줄이는 방법은 제공하지 않음**
            - 따라서 줄여야할 상황이 오는 경우, **topic recreate**만이 방법
        - 적절한 파티션 수를 위해, 적은 partition으로 시작
            - 이후, p와 c에서 병목 현상 발생시,
                - 조금씩 partition의 수와 producer 또는 consumer를 늘려가면서 찾아야 함
        - broker당 2,000개의 최대 파티션 수 권장
            - 과도한 파티션 수 보다는 목표 처리량에 맞게 적절한 파티션 수로 유지 및 운영
    - 오프셋과 메세지 순서
        - offset
            - 각 파티션 마다 메세지가 저장되는 위치
            - 하나의 파티션 내에서 유일하고, 순차적으로 증가하는 숫자(64비트 정수)
            - offset 순서가 바뀐 상태에서 consumer가 데이터를 가져갈 수 없음
- 3.3. 카프카의 고가용성과 리플리케이션
    - topic 자체를 replication이 아닌,
        - 각 partition을 replication하는 것
    - replication factor, leader, follower
        - replication factor
            - default : 1
            ```
            [root@node1 packages]# vi $KAFKA_HOME/config/server.properties
            ```
            - 각 토픽별로 다르게 replication factor값 설정이 가능
            ```
            default.replication.factor = 2
            ```
            - 클러스터 내 모든 브로커에 동일하게 설정해야 함
            - 변경한 정보는 ```$KAFKA_HOME/logs/server.log```에서 확인 가능
    - 가용성을 확보하기 위해, replication내에서 **Topic Leader/Follower**로 구분
        - RabiitMQ의 경우, **Master Queue / Mirrored Queue**로 부른다.
        - zookeeper의 경우도 리더와 팔로워의 개념을 가진다.
    - 모든 읽기와 쓰기는 **Leader**에서만 일어난다.
    - 리더와 팔로워는 저장된 데이터의 순서 일치 및 동일한 오프셋과 메세지를 가짐
    ```
        # peter 토픽을 replication factor=2로 생성
        kafka-topics.sh \
        --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
        --topic peter --partitions 1 --replication-factor 2 --create

        kafka-topics.sh \
        --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
        --topic peter --describe


        Topic:peter     PartitionCount:1        ReplicationFactor:2     Configs:        │
        Topic: peter    Partition: 0    Leader: 3       Replicas: 3,1   Isr: 3,1
    ```
    - Partition : 0 // Leader : 3 // Follwer : 1
    - replication은 토픽의 사이즈 만큼 다른 노드로 복제된다.
        - 저장소 크기가 n배 이상 필요하게 된다.
    - 브로커의 리소스 사용량 증가
        - 비활성화된 토픽의 상태를 체크하는 작업 필요
    - **모든 토픽에 ```replication factor = 3```이 아니라, 해당 토픽에 저장되는 데이터의 중요도에 따라**
        - **replication factor를 2또는 3으로 운영하는 것이 좋음**
    - 3.3.2 리더와 팔로워의 원리
        - 팔로워는 주기적으로 리더를 보면서 자신에게 없는 데이터를 주기적으로 가져온다.
        - 리더가 다운되는 경우 새로운 팔로워가 새로운 리더로 승격
            - 팔로워 비정상 작동시, 데이터 차이로 인하여 새로운 리더 승격이 이루어지지 않을 수 있음
        - ISR(In Sync Replica)
            - 현재 리플리케이션 되고 있는 그룹
            - ISR에 속한 구성원만이 리더의 자격을 가질 수 있음
            - 원리
                - 리더는 프로듀서로부터 받은 메세지를 저장
                - 팔로워는 짧은 주기로 새로운 데이터 업데이트
                - 리더는 팔로워들이 주기적으로 데이터를 확인하는지 검토
                    - 일정 주기(**replica.lag.time.max.ms**)를 만족시키지 못하면, ISR에서 팔로워 추방
                - ISR의 그룹 축소
    - 3.4 All Broker Down
        - 하나씩 브로커가 다운되면서, 카프카 클러스터가 다운되는 상황
        - 해결방법
            - 마지막 리더가 살아나기를 기다림
                - 메세지 손실 없이 작업이 가능
                - 마지막 리더가 정상화될 때까지 카프카 클러스터의 장애 지속
            - ISR에서 추방되었지만 먼저 살아나면 자동으로 리더
                - 메세지 손실이 발생할 수 있음
                - old leader의 경우, 새로운 리더가 운영되게 되면, 복구되게 되더라도 새로운 리더와 데이터 동기화
                - 빠르게 정상화 할 수 있음
        - 코드 설정
            ```
            vi server.properties

            unclean.leader.election.enable=true
                # true : 손실 감수, 빠른 서비스 제공
                # false : 마지막 리더를 기다리기
            ```
    - 3.5 Zookeeper Znode 역할
        - zookeeper Cli 접근
            - ```$ZOOKEEPER_HOME/bin/zkCli.sh```

- 4.1. 콘솔 프로듀서로 메세지 보내기
    - producer
        - 메세지를 생산(produce)하여 카프카의 토픽으로 메세지를 보내는 역할을 하는 애플리케이션, 서버 등을 의미
        - 각각의 메세지를 토픽 파티션에 매핑하고
            - 파티션의 리더에 요청을 보내는 역할
        - 키 값을 정하여, 해당 키를 가진 모든 메세지를 동일한 파티션으로 접송 가능
        - 키 값을 입력하지 않으면 파티션은 RR방식으로 파티션에 균등 분배
        - ```auto.create.topics.enable = true```
            - 메세지를 보낼 때, 해당 토픽이 없으면 자동 생성
    - 토픽 생성 및 확인
        ```
        [root@node1 bin]# $KAFKA_HOME/bin/kafka-topics.sh --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka --topic peter-topic --delete

        $KAFKA_HOME/bin/kafka-topics.sh \
        --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
        --topic peter-topic --partitions 1 --replication-factor 3 --create

        [root@node1 bin]# $KAFKA_HOME/bin/kafka-topics.sh --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka --topic peter-topic --describe
        Topic:peter-topic       PartitionCount:1        ReplicationFactor:3     Configs:
                Topic: peter-topic      Partition: 0    Leader: 1       Replicas: 1,2,3 Isr: 1,2,3
        ```
        - 1이 리더이고, 2,3이 팔로워 인것을 확인할 수 있다.
    - 토픽 메세지를 보낼 때 ```kafka-console-producer.sh```를 사용하면 된다.
        - ```--broker-list``` : 브러커의 호스트명:포트번호 형식으로 입력한다.
        ```
        --broker-list peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092
        ```
    - 메세지 보내기
        ```
        [root@node1 bin]# kafka-console-producer.sh --broker-list peter-kakfa001:9092,peter-kafka002:9092,peter-kafka003:9092 --topic peter-topic
        > Hello World!
        ```
    - 메세지 받기 : ```kafka-console-consumer.sh```
        ```
        [root@node1 bin]# kafka-console-consumer.sh --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 --topic peter-topic --from-beginning
        ```
    - producer로 값을 보낼때, acks나 압축 옵션 등도 설정할 수 있다.
- 4.2 자바와 파이썬을 이용한 프로듀서
    - 코드 참조
    - github에 [confluent-kafka-python](https://github.com/confluentinc/confluent-kafka-python), [kafka-python](https://github.com/dpkp/kafka-python) 두가지 라이브러리 존재
        - 인기도는 **kafka-python**이 더 높다
        - 성능상으로는 **confluent-kafka-python**이 4배 더 좋음
            - [popit](http://www.popit.kr/kafka-python-client-성능-테스트)
            - **librdkafka**(아파치 카프카 프로토콜의 C라이브러리)를 반드시 설치해야함
- 4.3 코드 작성 및 환경 구성
    - 파이썬 환경 구성
        ```
        # refer : http://depository.tistory.com/2
        yum install -y https://centos7.iuscommunity.org/ius-release.rpm
        yum install -y python36u python36u-libs python36u-devel python36u-p
        cd /usr/bin
        ln -s python3.6 python3
        ln -s pip3.6 pip
        pip install --upgrade pip
        cd /root/workspace
        virtualenv --python=python3 kafka-python-virtual
        source kafka-python-virtual/bin/activate
        pip install kafka-python
        ```
    - 자바 환경구성
        - Maven으로 작업하도록 한다.
        - executable jar를 만들기 위해, pom.xml을 수정한다.
            ```
            <!-- maven architecture를 따라야 한다. 즉. src/main/java의 구조를 따라야 함 -->
            <build>
                <plugins>
                    <plugin>
                        <!-- Build an executable JAR -->
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-jar-plugin</artifactId>
                        <version>3.1.0</version>
                        <configuration>
                            <archive>
                                <manifest>
                                    <addClasspath>true</addClasspath>
                                    <classpathPrefix>lib/</classpathPrefix>
                                    <mainClass>com.wshid.producer_option</mainClass>
                                </manifest>
                            </archive>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
            ```
- 4.4 프로듀서 주요 옵션
    - bootstrap.servers
        - 호스트:포트 정보의 목록
        - 단일 호스트는 안정성 문제로 추천하지 않음
    - acks
        - producer가 topic의 leader에게 메세지를 보낸 후, 완료하기전 ack의 수를 의미
        - 옵션의 수가 적을 수록 성능은 좋아지나, 손실 가능성도 증가함
        - Value
            - acks=0
                - producer가 어떠한 응답도 기다리지 않음
                - 보내면 끝
                - 높은 처리량, 상대적 높은 손실율
            - acks=1
                - leader는 데이터를 기록
                - follwer는 확인하지 않음
            - acks=all | acks=-1
                - 리더는 ISR의 follower로부터 데이터에 대한 ack를 기다림
                - 하나의 팔로워가 있는 한 데이터는 손실되지 않음
                - 무손실에 대하여 보장
    - buffer.memory
        - producer가 kafak 서버로 데이터를 보내기 위해 잠시 대기할 수 있는 메모리 바이트
            - 잠시 대기 = 배치 전송, 딜레이 등
    - compression.type
        - producer가 데이터를 압축해서 보냄
        - 타입 지정이 가능
            none, gzip, snappy, lz4 등
    - retries
        - 일시적인 오류, 실패시, 데이터를 다시 보내는 횟수
    - batch.size
        - P는 데이터 전송시, 같은 파티션에 해당하는 데이터를 배치로 보내려고 함
        - Client와 Server 양쪽에 성능적 측면 향상 유도
        - Batch Size Byte 단위를 조정할 수 있음
            - 정해진 크기보다 크면 배치를 시도하지 않음
        - 배치 보내기 전 C에 에러 발생시, 배치 내에 있던 메세지 전달되지 않음
        - **고가용성이 필요한 경우, 배치사이즈를 설정하지 않는 것이 방법일 수 있음**
    - linger.ms
        - 배치 형태의 메세지를 보내기 전에, 추가적인 메세지를 위해 기다리는 시간 조정
        - P는 지정된 배치 사이즈에 도달하면 이 옵션과 상관없이 메세지 전송
        - 배치 사이즈에 도달하지 못한 상황에서 linger.ms에 걸리면 전송
        - deafult : 0(지연없음)
            - 0보다 크면, 지연 시간은 조금 발생하나, 처리량은 좋아짐
    - max.request.size
        - P가 보낼 수 있는 최대 메세지 바이트 사이즈
        - default : 1MB
- 4.5 메세지 전송방법
    - 4.5.1 메세지 손실 가능성이 높지만 빠른 전송이 필요할 때
        - **acks=0**
    - 4.5.2 메세지 손실 가능성이 적고, 적당한 속도의 전송 필요한 경우
        - **acks=1**
        - P와 Leader
        - 절차
            - Producer -> Message
            - Leader --Ack--> Producer
        - 기존 메세지 전달 절차
            - producer, acks=1, 리더에게 메세지를 보냄
            - 리더는 해당 내용 저장
            - leader, producer에게 acks 전달
            - follower는 leader를 바라봄
            - leader에 새로운 메세지를 확인, follower들도 저장
        - 손실 메세지 전달 절차
            - producer, acks=1, leader에게 메세지 전달
            - leader 메세지 저장
            - producer에게 acks 전달
            - **leader 장애 발생**
            - follower는 동기화하려고 하지만 leader 부재 파악
            - 새로운 메세지가 있지만 메세지를 동기화 할 수 없음
            - ISR의 팔로워중 하나의 노드가 leader로 선출, producer 내용 처리
                - 이전 전달된 메세지 손실
        - producer app으로 **logstash**나 **filebeat**등을 사용하게 되면
            - 기본값은 acks=1이다
    - 4.5.3 전송 속도는 느리지만 메세지 손실이 없어야 하는 경우
        - **acks=all**
        - prodcuer가 메세지를 전송하고, 리더가 받았는지 확인
            - 이후 fpollower까지 메세지를 받았는지 확인
        - producer 뿐만 아니라 broker 설정도 변경해 주어야 함
        - **acks=all, min.insync.replicas=1**
            - producer에서 acks=all 설정
            - 브로커는 server.properties에서 설정 변경 가능
                ```
                # in server.properties
                min.insync.replicas=3

                # 설정 후 옵션 내용 적용 확인
                cat $KAFKA_HOME/logs/server.logs | grep min.insync.replicas
                ```
            - min.insync.replicas
                - 최소 replication factor를 지정하는 옵션
            - acks=all일지라도 min.insync.replicas=1 이면,
                - **acks=1**과 동일하게 동작한다.
                - 리더가 메세지를 받은 후 저장
                - 이후 리더는 min.insync.replicas 옵션에 의해, 최소 리플리카 수 1을 만족 시켰기 때문에
                    - 리더 본인만 확인후 바로 acks를 전송하기 때문
        - **acks=all, min.insync.replicas=2**
            - 리더 : 메세지를 받는다.
            - 리더 : 메세지 저장
            - 팔로워 : 리더에 변경된 메세지를 확인, 확인 후 자신에게도 저장
            - 리더는 acks를 보내기 전 min.insyn.replicas에 의해 2개의 replication이 유지되는지 확인
            - acks 전송
            - 공식문서에서는 **acks=all, min.insync.replicas=2** 설정을 권장(손실없는 전송 시)
                - 서버 1대가 장애 나더라도 복구가 가능함
            - **acks=all, min.insync.replicas=2, replication factor =3**
        - **acks=all, min.insync.replicas=3**
            - 이 같이 설정하면 문제가 발생
            - 리더 : 메세지를 받고 저장
            - 팔로워 : 변경 메세지 주기적 확인, 본인에게 저장
            - 리더는 min.insync.replicas에 의해 3개의 복제가 유지되는지 확인
            - 3개가 확인되면 acks 전송
            - 이 상황에서 broker가 하나만 다운되더라도 원활한 전송이 이루어지지 않음
                - 리더가 아니더라도 문제 발생
            - 예제
                ```
                $KAFKA_HOME/bin/kafka-console-producer.sh \
                --broker-list peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
                --topic peter-topic --request-required-acks all
                ```
                - 이후 브로커에 해당하는 노드를 강제 종료 시키면 에러가 발생한다
                    - replication factor가 부족하다는 에러 발생

### 5장, Kafka Consumer
---
- 토픽의 메세지를 가져와서 소비하는 역할을 하는 어플리케이션
- 특정 파티션을 관리하고 있는 파티션 리더에게 메세지를 가져오는 요청
- 각 요청은 로그의 오프셋 명시, 그 위치로부터 메세지 수신
- 이미 가져온 메세지를 다시 로드 가능하다
    - RabbitMQ에서는 불가능한 기능
- 5.1 Consumer 주요 옵션
    - Old Consumer, New Consumer
        - 주키퍼의 사용 유무에 따라 다르다.
        - Old Consumer
            - Consumer의 Offset을 Znode에 저장
        - New Consumer
            - Consumer의 Offset을 Kafka Topic에 저장
        - 현재는 kafka topic 및 znode 방법 둘 다 지원하지만, znode 저장 방식은 사라질 예정
    - New Consumer's Option
        - bootstrap.server
            - kafka cluster에 연결하려면 호스트와 포트정보 목록
            - 호스트명:포트
            - 호스트 하나만 설정도 가능하지만, 장애 발생시 접속이 불가하게 됨
                - 비추천
        - fetch.min.bytes
            - 한번에 가져올 수 있는 최소 바이트 설정
            - 지정한 사이즈보다 작을 경우, 요청에 응답하지 않고 누적될 때까지 기다림
        - group.id
            - consuer group 식별자
        - enable.auto.commit
            - bg로 주기적으로 offset commit
        - auto.offset.reset
            - 카프카 초기 오프셋이 없거나, 오프셋 미존재시(데이터 삭제 등) 리셋
            - 리셋 옵션
                - earliest : 가장 초기 오프셋값으로 설정
                - latest : 가장 마지막 오프셋 설정
                - none : 이전 오프셋값 찾지 못하면 에러
        - fetch.max.bytes
            - 한번에 가져올 수 있는 최대 데이터 사이즈
        - request.timeout.ms
            - 요청에 대해 응답을 기다리는 최대 시간
        - session.timeout.ms
            - Consumer와 Broker 사이 세션 타임 아웃 시간
            - 브로커가 consumer가 살아있는 것으로 판단하는 시간
                - deafult : 10s
            - Consumer가 그룹 코디네이터에게 heartbeat를 보내지 않고
                - session.timeout.ms가 지나면
                    - 해당 consumer가 종료되거나 장애가 발생한 것으로 판단
                        - rebalance를 시도
            - heartbeat없이 얼마나 오랫동안 consumer가 있을 수 있는지 제어
            - **heartbeat.interval.ms**와 밀접한 관련이 있음
                - 일반적인 경우 heartbeat와 session.timout을 같이 수정함
            - 기본값보다 낮게 설정
                - 실패를 빨리 감지할 수 있음
                - GC나 pool loop을 완료하는 시간이 길어짐
                    - 원하지 않는 rebalace 발생 가능성 증가
            - 기본값보다 높게 설정시,
                - 원하지 않는 rebalance 발생 감소
                - 실제 오류 감지시 오랜 시간 발생
        - heartbeat.interval.ms
            - 그룹 코디네이터에게 얼마나 자주 KafkaConsumer poll() 메소드로 heartbeat를 보낼것인지 조정
            - **session.timeout.ms**과 연관이 있음
            - 기본적으로 **session.timeout.ms**보다 수치가 낮아야 함
            - 일반적으로 1/3 정도로 설정(default : 3s)
        - max.poll.records
            - 단일 호출 poll()에 대한 최대 레코드 수 조정
            - application이 pool loop에서 데이터 양 조정 가능
        - max.pool.interval.ms
            - Consumer가 heartbeat만 보내고, 실제 메세지를 안 가져가는 경우가 있음
            - consumer가 특정 partition을 점유할 수 없도록 주기적으로 poll을 호출하지 않으면
                - 장애라고 판단하여 Consumer Group에서 제외한 후
                    - 다른 컨슈머가 해당 파티션에서 메세지를 가져갈 수 있도록 함
        - auto.commit.interval.ms
            - 주기적으로 오프셋을 커밋하는 시간
        - fetch.max.wait.ms
            - fetch.min.bytes에 의해 설정된 데이터보다 적을 경우
                - 응답을 기다리는 최대 시간
        

