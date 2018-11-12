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
        



    
        

