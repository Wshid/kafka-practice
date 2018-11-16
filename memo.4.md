## 4장, Kafka Producer
---
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