## 6장, 카프카 운영 가이드
---
- 6.1 필수 카프카 명령어
    - 카프카 기본 제공 명령어 : ```$KAFKA_HOME/bin```에 위치
    - 6.1.1 토픽 생성
        - ```kafka-topics.sh```
            ```
            #Options
                --zookeeper
                --replication-factor
                --partitions
                --topic
            ```
            ```
            $KAFKA_HOME/bin/kafka-topics.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --replication-factor 1 --partitions 1 --topic peter-topic --create
            ```
    - 6.1.2 토픽 리스트 확인
        - ```kafka-topics.sh```
            ```
            $KAFKA_HOME/bin/kafka-topics.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --list
            ```
    - 6.1.3 토픽 상세보기
        - ```kafka-topics.sh```
        - 토픽의 파티션, 리더 확인
            ```
            $KAFKA_HOME/bin/kafka-topics.sh \ 
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --topic peter-topic --describe
            ```
    - 6.1.4 토픽 설정 변경
        - ```kafka-configs.sh```
        - 토픽의 메세지 보관주기 변경 등
            - default : 7 days
        - 디스크 공간 확보시,
            - 가장 많이 차지하는 토픽의 디스크 보관주기를 줄이면 됨
            ```
            # peter-topic의 디스크 보관주기를 1시간으로 설정
            $KAFKA_HOME/bin/kafka-configs.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --alter --entity-type topics --entity-name peter-topic --add-config retention.ms=3600000

            # config 삭제 : --delete-config 사용
            $KAFKA_HOME/bin/kafka-configs.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --alter --entity-type topics --entity-name peter-topic --delete-config retention.ms
            ```
        - **describe**를 통해 변경 내용을 확인할 수 있다.
    - 6.1.5 토픽의 파티션 수 변경
        - 처리량이 높아짐에 따라 파티션 수를 늘려야 하는경우
            - **단, 파티션 수 증가는 가능하지만, 감소는 불가능하다**
        - 적은 파티션으로 시작하여 점차 늘려가는 방법이 좋음
        - 파티션 수만큼 컨슈머 수도 증가시켜 주어야 함
        - ```kafka-topics.sh```를 사용
            ```
            # 파티션 수를 증가시키는 구문
            $KAFKA_HOME/bin/kafka-topics.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --alter --topic peter-topic --partitions 2
            ```
        - 파티션 수를 증가시킬 경우, 메세지 순서에 영향을 줄 수 있음
            - 특히, key를 통하여 파티션 분리시, 내용을 꼭 확인해야함
        - describe로 변경 내용 확인이 가능하다.
    - 6.1.6 토픽의 리플리케이션 팩터 변경
        - 특정 설정 파일을 만들어 적용시키도록 한다.
            ```
            #rt.json
            {"version" : 1,
            "partitions":[
                {"topic":"peter-topic", "partition":0,"replicas":[1,2]},
                {"topic":"peter-topic","partition":1,"replicas":[2,3]}
            ]}
            ```
            - replica의 앞 숫자가 **leader**를 의미한다.
            - 현재 상태의 토픽 파티션 정보를 확인한 뒤, **파티션의 리더 정보와 일치하도록 설정하여야 함**
            - replica의 수를 더 늘리고 싶으면, 해당 replicas 값의 배열을 더 증가시켜 주면 된다.
                - [1,2,3] 등
        - ```kafka-reassign-partitions.sh```
            ```
            $KAFKA_HOME/bin/kafka-reassign-partitions.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --reassignment-json-file /foo/bar/rt.json --execute
            ```
    - 6.1.7 컨슈머 그룹 리스트 확인
        - 컨슈머의 상태를 확인하는 명령어
        - old/new consumer에 따라 확인 방법이 다름
            - old consumer
                - ```--zookeeper``` 리스트 입력
            - new consumer
                - ```--bootstrap-server```와 브로커 리스트를 입력
        - ```kafka-consumer-groups.sh``` 명령어를 사용한다.
            ```
            $KAFKA_HOME/bin/kafka-consumer-groups.sh \
            --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --list
            ```
        - **현재 파이썬 예제는 kafka 2.0.0에서 돌아가지 않는다.**
            - 라이브러리 만료로 보인다.
    - 6.1.8 컨슈머 상태와 오프셋 확인
        - ```kafka-consumer-groups.sh```
            - 옵션
                - ```--bootstrap-server```
                - ```--group```
            ```
            kafka-consumer-groups.sh \
            --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --group peter-consumer-group --describe

            Consumer group 'peter-consumer-group' has no active members.

            TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
            peter-topic     0          17              17              0               -               -               -
            ```
            - peter-consumer-group이 종료된 상태이기 때문에 활성화된 멤버가 없다는 의미
            - 파티션 0에 대하여 현재 오프셋은 17이며, 마지막 오프셋도 17이다.
            - LAG는 0으로 설정되어 있다.
                - LAG
                    - 현재 토픽에 저장된 메세지와, 컨슈머가 가져간 메세지의 차이를 의미
                    - 토픽 저장 메세지 10, 컨슈머 저장 메세지가 5일때
                        - LAG = 5
                    - LAG 숫자가 높다는 것은 **해당 토픽 또는 파티션에 컨슈머가 읽어가지 못한 메세지가 많이 있다는 의미**
- 6.2 주키퍼 스케일 아웃
    - 시작부터 큰 앙상블로 운영하는 것이 아닌,
        - 최초로 3대만 앙상블을 하여 운영하다가
            - 요청 수가 늘어 증설하는 것을 추천
    - 내부 설정 후 myid를 설정한 후
        - 주키퍼 설정 진행
            ```
            echo "4" > /data/myid
            ```
    - zoo.cfg 설정
        - 기존 유지되고 있던 브로커와 동일하게 설정하며
        - server의 설정을 증가시킨다.
            ```
            server.1=peter-zk001:2888:3888
            server.2=peter-zk002:2888:3888
            server.3=peter-zk003:2888:3888
            server.4=peter-zk004:2888:3888
            server.5=peter-zk005:2888:3888
            ```
    - 설정값을 모든 브로커에서 변경한 뒤, 재시작 작업을 시작해야 함
        - 재시작 순서가 있는 것은 아니지만, **앙상블 리더의 경우 마지막에 작업하는 것을 권장**
            - 리더가 변경되면서 문제가 발생할 수 있기 때문
        - 현재의 리더를 찾고 마지막으로 로드할 수 있도록 함
        - 주키퍼 노드가 follower인지 leader인지 확인하는 방법
            ```
                $ZOOKEEPER_HOME/bin/zkServer.sh status
            ```
    - 주키퍼에서 앙상블 상태 확인
        - ```mntr```과 ```nc```명령어를 활용한다
            - ```mntr``` : 주키퍼에서 현재 상태의 앙상블 모니터링을 위해 제공
            - ```nc``` : tcp/udp를 연결할 수 있는 명령어
        - 명령어
            ```
            echo mntr | nc localhost 2181 | grep zk_synced_followers

            zk_synched_followers    2
            ```
            - 단, 해당 결과는 리더에서만 확인 가능하다.
                - 팔로워 확인이기 때문
- 6.3 카프카 스케일 아웃
    - 주키퍼 설정보다 매우 간단
    - 카프카 설정 파일에 ```broker.id``` 부분만 타 서버와 겹치지 않게 부여하면 됨
        ```
        # server.properties 파일 내부

        18 ############################# Server Basics #############################
        19 
        20 # The id of the broker. This must be set to a unique integer for each broker.
        21 #broker.id=0
        22 broker.id=4
        ```
    - 토픽 새로 추가하기
        ```
        $KAFKA_HOME/bin/kafka-topics.sh \
        --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
        --replication-factor 2 --partitions 5 --topic peter5 --create

        Topic:peter5    PartitionCount:5        ReplicationFactor:2     Configs:
                Topic: peter5   Partition: 0    Leader: 2       Replicas: 2,3   Isr: 2,3
                Topic: peter5   Partition: 1    Leader: 3       Replicas: 3,1   Isr: 3,1
                Topic: peter5   Partition: 2    Leader: 1       Replicas: 1,2   Isr: 1,2
                Topic: peter5   Partition: 3    Leader: 2       Replicas: 2,1   Isr: 2,1
                Topic: peter5   Partition: 4    Leader: 3       Replicas: 3,2   Isr: 3,2
        [root@node1 config]# 
        ```
    - 이후 설정한 peter-kafka004, peter-kafka005에서 카프카를 시작해주면 된다.
    - 추가된 서버들이 카프카 클러스터에 잘 조인되었는지 확인하는 방법
        - 브로커 정보를 확인하면 된다.
            ```
            $ZOOKEEPER_HOME/bin/zkCli.sh
            ls /
            ```
            - 해당 명령어를 입력하여 ls 명령어로 정보가 확인되는지 본다.
        - 브로커 아이디 확인
            ```
            ls /peter-kafka/brokers/ids

            [1,2,3,4,5]
            ```
    - 이후 파티션 재분배를 해주어야 한다.
        - 브로커가 추가 되었으나, 클러스터에 파티션 재배치를 자동으로 해주지 않기 때문
        - peter5의 토픽 상세 정보 확인
            ```
            $KAFKA_HOME/bin/kafka-topics.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --topic peter5 --describe 
            ```
            - 확인하게 되면, 파티션 분배가 전혀 안 일어난 것을 확인할 수 있다.
        - 파티션 분산을 위해 **partition.json**파일을 작성한다.
            ```
            # partition.json
            {"version":1,
            "partitions":[
                {"topic":"peter5", "partition":0,"replicas":[2,1]},
                {"topic":"peter5", "partition":1,"replicas":[3,2]},
                {"topic":"peter5", "partition":2,"replicas":[4,3]},
                {"topic":"peter5", "partition":3,"replicas":[5,4]},
                {"topic":"peter5", "partition":4,"replicas":[1,5]}
            ]}
            ```
        - ```kafka-reassign-partitions.sh```를 활용하여 재배치 해준다.
            ```
            $KAFKA_HOME/bin/kafka-reassign-partitions.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --reassignment-json-file /foo/bar/partition.json --execute
            ```
    - 단, 안전한 작업을 위하여 유의해야할 점
        - 파티션의 크기가 매우 클 것이므로, 재배치 작업은 상당한 네트워크 부하를 유발할 수 있다.
            - 브로커에게도 부담
        - 토픽 사용량이 가장 적은 시간에 수행
        - 토픽의 보관 주기를 줄여, 임시로 사이즈를 축소시킨 후 작업할 것
- 6.4 카프카 모니터링
    - 메모 제외
- 6.5 카프카 매니저 활용
    - [kafka-manager](https://github.com/yahoo/kafka-manager)
    - 토픽 추가, 삭제, 설정, 변경 등의 작업을 웹 GUI로 사용
- 6.6 카프카 운영에 대한 Q&A
    - 카프카 운영시 옵션을 변경하려면
        - 카프카의 옵션 설정 시점은 **카프카가 실행 될 때**
        - 운영중인 상태에서 옵션을 변경하려면
            - 원하는 옵션 설정을 변경한 후
            - 클러스터내 **브로커를 1대씩 재시작** 해야 변경된 옵션이 적용
    - 토픽이 삭제되지 않을 때
        - 토픽을 삭제하려면
            - ```delete.topic.enable=true``` 설정이 되어야 함
            - v1.0 이하일 때, 기본값이 false, 토픽이 삭제되지 않았음
    - 디스크 사용량이 높을 때, 디스크 full 일 때
        - 카프카 데이터 디렉토리에서 사용량이 많은 토픽을 찾음
        - 토픽의 보관주기를 변경,
            - 오래된 데이터가 삭제되어 디스크 공간 확보 가능
        - 카프카 기본 설정이 7일 이므로,
            - ```log.retention.hours``` 옵션을 48시간 또는 72시간으로 변경
    - 디스크 추가
        - log.dirs 옵션에 추가된 디스크 경로를 추가한 후 broker를 재시작하면 됨
    - OS 점검시
        - 클러스터에서 broker 1대를 제외하고 OS 점검을 실시
        - 클러스터 내 브로커 하나를 제외하더라도 전체가 다운되는 문제는 발생하지 않으나,
            - 리더 변경으로 인한 connection 에러는 발생할 수 있음
    - consumer의 LAG 상태 관리
        - CLI로 관리
        - [Burrow](https://github.com/linkedin/Burrow)
            - kafka consumer의 LAG를 체크하기 위해 Go Language로 만들어진 오픈소스
            - http 요청을 통해 컨슈머의 상태, 컨슈머의 LAG정보 등을 확인할 수 있음
    - 토픽에 전송한 메세지 확인 방법
        - consumer consumer를 사용하여 해당 토픽의 메세지를 가져오기
    - zookeeper 설치시 apache version vs kafka에 포함된 버전 선택
        - 주키퍼는 카프카의 노드 관리와 메타 정보를 관리하는 용도
        - 사용자 편의에 따라 선택
    - 주키퍼 앙상블 정보 확인
        - ```mntr```을 사용하여 앙상블 상태 확인 가능
    - 앙상블 전체 상태정보를 편하게 사용하는 툴
        - [zktop](https://github.com/phunt/zktop)
    - 카프카 버전 업그레이드 방법
        - 두가지 방법
            - 한 대씩 내렸다가 올리는 롤링 업그레이드
            - 모든 브로커를 종료 한 후, 새로운 버전으로 실행하는 방법
                - 이 방법이 더 간단
    - 롤링 업그레이드 방법
        - 브로커 한 대씩 새 버전을 설치
        - ```server.properties```에 내용 추가
            ```
            inter.broker.protocol.version=<current kafka version>
            log.message.format.version=<current kafka version>
            ```
        - 이후 재시작
    - 카프카 버전 업그레이드시 주의점
        - 버전이 업그레이드 되면서, 환경설정 값이 변경되는 경우 존재
        - Release Note를 확인하여 관련 부분 미리 체크
        - 일반적으로 하위 Client와의 지원은 잘 되지만,
            - 카프카 버전 업그레이드 이후, 전송과 수신 또는 연결에서 문제 발생 가능
            - 클라이언트 버전 호환성도 체크하기
    - 카프카 버전 업그레이드 시, 주키퍼 업데이트가 필요한가
        - 별도의 어플리케이션
        - 일반적으로 카프카 버전 업그레이드만 수행
    - Java기반의 Heap 사이즈는 물리 메모리의 절반 정도 설정, 카프카의 설정 방법
        - kafka의 힙 사이즈는 **5~6GB** 정도로 설정
        - 남아 있는 메모리는 페이지 캐시로 사용하는 것을 권장




    
        

