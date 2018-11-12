## 5장, Kafka Consumer
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
- 5.2 콘솔 컨슈머로 메세지 가져오기
    - ```kafka-console-consumer.sh```
        - ```--bootstrap-server``` : 브로커 서버 목록
        - ```--topic``` : 토픽 이름
        ```
        kafka-console-consumer.sh \
        --bootstrap-server peter-kafka001:9292,peter-kafka002:9092,peter-kafka003:9092 \
        --topic peter-topic --from-beginning
        ```
    - 컨슈머를 실행할 때는 **Consumer Group**이 필요
        - 추가 옵션으로 컨슈머 그룹을 지정해야 함
        - 지정하지 않으면 ```console-consumer-XXXXX```와 같은 임의의 형태로 만들어짐
        - 그룹 이름 확인시, **kafka-consumer-groups.sh** 확인
        ```
        # 컨슈머 그룹 확인
        [root@node1 logs]# kafka-consumer-groups.sh \
        >  --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
        >  --list
        # 결과
        console-consumer-13806
        console-consumer-200
        console-consumer-58953
        ```
    - 컨슈머 그룹 지정
        ```
        kafka-console-consumer.sh \
        --bootstrap-server peter-kafka001:9292,peter-kafka002:9092,peter-kafka003:9092 \
        --topic peter-topic --group peter-consumer-group --from-beginning
        ```
- 5.3 자바와 파이썬을 사용한 컨슈머
    - 코드 참조
- 5.4 파티션과 메세지 순서
    - **replication factor**는 다르게 설정해도 되지만, **partition 수**는 동일하게 topic을 만들어 주어야 한다.
    ```
    # peter-01 토픽 생성
    ## replication-factor = 1
    ## partitions = 3
    kafka-topics.sh \
    --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
    --topic peter-01 --partitions 3 --replication-factor 1 --create
    ```
    - 5.4.1 파티션 3개로 구성한 peter-01 토픽과 메세지 순서
        - **kafka-console.producer.sh**를 활용하여 메세지를 전송한다.
            ```
            # producer 입력 프롬프트 띄우기
            kafka-console-producer.sh \
            --broker-list peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --topic peter-01
            ```
        - 입력된 내용 확인하기
            ```
            kafka-console-consumer.sh \
            --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --topic peter-01 --from-beginning

            # 결과
            b
            e
            c
            a
            d
            ```
            - 입력된 순서가 보낸 순서가 아닌 것을 확인할 수 있다.
        - 숫자 전송하기(1,2,3,4,5) 입력
        - 추후 확인
            ```
            b
            e
            1
            4
            c
            2
            5
            a
            d
            3
            ```
            - 내용이 꼬여 있는 것을 확인할 수 있다?
            - 하지만 이는 정상이다
        - peter-01 토픽에 메세지 저장이 어떻게 되었는지 확인
            - **kafka-console-consumer.sh** 파일을 활용한다.
                - ```--partition``` : 특정 파티션에 해당하는 메세지만을 가져올 수 있음
        - 특정 파티션의 메세지 가져오기
            ```
            kafka-console-consumer.sh \
            --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --topic peter-01 --partition 0 --from-beginning

            # 결과
            c
            2
            5
            ```
        - 분석
            - producer가 peter-01 topic으로 메세지를 a,b,c,d,e를 보냈지만
                - 해당 메세지는 하나의 파티션이 아닌, 각개 파티션에 나누어 저장된다.
            - consumer의 경우 어떤 순서로 producer가 전송하였는지 알 수 없음
            - consumer는 오직 partition의 offset 순서대로만 메세지를 가져옴
        - 카프카 컨슈머의 메세지 순서는
            - **동일한 파티션 내에서는 producer가 생성한 순서와 동일하게 처리하지만,**
            - **파티션과 파티션 사이에는 순서를 보장하지 않음**
    - 5.4.2 파티션 1개로 구성한 peter-02 토픽과 메세지 순서
        - 토픽 생성하기
            ```
            kafka-topics.sh \
            --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
            --topic peter-02 --partitions 1 --replication-factor 1 --create
            ```
        - 메세지 보내기
            ```
            kafka-console-producer.sh \
            --broker-list peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --topic peter-02
            >a
            >b
            >c
            >d
            >e
            ```
        - 메세지 확인하기
            ```
            [root@node1 logs]# kafka-console-consumer.sh \
            --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
            --topic peter-02 --from-beginning
            a
            b
            c
            d
            e
            ```
            - 순서대로 잘 나오는 것을 확인할 수 있다.
        - 추가 입력하기
            ```
            [root@node1 logs]# kafka-console-consumer.sh
            --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092
            --topic peter-02 --from-beginning
            a
            b
            b
            c
            d
            e
            1
            2
            3
            4
            5
            ```
        - 순서를 보장하기 위해서, **partition=1**을 해도 되지만, 분산 처리가 불가능 하다.
            - 메세지 순서를 보장하려 하면, 파티션 수를 하나로 만든 토픽을 사용해야 하며,
            - 처리량이 떨어지는 부분은 감안 해야함
- 5.5 컨슈머 그룹
    - Consumer = 카프카 토픽에서 메세지를 읽어오는 역할
    - Consumer Group
        - 하나의 토픽에 여러 컨슈머 그룹이 동시에 접속하여 메세지를 가져올 수 있음
        - 다른 메세징큐 시스템 과의 차이점
            - 컨슈머가 메세지를 가져가면 다른 컨슈머가 메세지를 가져가지 못함
    - 컨슈머 확장이 가능
        - 컨슈머가 메세지를 가져가는 속도 > 프로듀서가 토픽에 메세지 보내는 속도
            - 점점 카프카에 머무르는 시간차가 나게 됨
        - 단순히 컨슈머 확장만 하게 되면, 
            - offset 정보가 꼬여 메세지가 꼬일 수 있음
    - **동일한 토픽에 대하여 여러 컨슈머가 메세지를 가져갈 수 있도록 하는 것**
    - 확장이 용이하며, 컨슈머 장애 처리에도 빠른 대처 가능
    - 컨슈머 그룹 내 컨슈머는, 메세지를 가져오고 있는 **topic의 partition**에 대하여 소유권을 공유
    - 그룹 내 컨슈머 수가 부족해, 프로듀서가 전송하는 메세지 처리하지 못하는 경우
        - 컨슈머 추가 필요
        - 컨슈머가 추가되면, 기존 그룹 내 컨슈머의 partition을 따라감
    - 예시
        - 기존 peter-01 토픽, partition 3개(0,1,2)
        - consumer-group 01, consumer 01
        - 이 환경에서 기존에는 peter-01 토픽의 모든 파티션 -> consumer 01로 몰리게 된다.
            - 이때 consumer 02, 03을 추가하게 되면
                - partition 0 - consumer 01
                - partition 1 - consumer 02
                - partition 2 - consumer 03
            - 으로 소유권이 이동된다.
    - 소유권이 이동하는 과정을 **rebalance(리밸런스)**라고 한다.
    - rebalance
        - 컨슈머를 쉽고 안전하게 추가/제거가 가능함
        - 높은 가용성과 확장성 확보 가능
        - rebalance가 되는 동안, 일시적으로 consumer는 메세지를 가져올 수 없음
            - 즉, **rebalance**발생 시, 컨슈머 그룹 전체를 일시적으로 사용할 수 없음
        - 그룹 내 리밸런스 발생시, **토픽의 각 파티션마다 하나의 컨슈머**가 연결 됨
            - 리밸런스가 끝나게 되면 각자 담당하고 있는 파티션으로부터 메세지를 가져옴
    - 컨슈머 추가보다 받는 메세지 수가 더 많을 경우
        - 메세지가 계속 쌓이는 상황
        - 단순히 컨슈머 수만 늘리면 되지 않음
            - 1:1 매핑 이후에는 더 이상 리벨런스가 일어나지 않는다?
            - **토픽의 파티션에는 하나의 컨슈머만 연결 가능하다**
            - **따라서 토픽의 파티션 수를 같이 늘려주어야 함**
    - 컨슈머 그룹 내에서 컨슈머가 하나 다운되는 경우
        - 컨슈머가 컨슈머 그룹 내에게서 멤버 유지 및 파티션 소유권 유지 방법
            - heartbeat 보내기
        - 일정 주기의 heartbeat = 해당 파티션의 메세지를 잘 처리한다는 의미
        - heartbeat는
            - consumer가 poll할 때,
            - 가져간 메세지의 offset을 commit 할 때 보내게 됨
        - 일정 시간동안 heartbeat를 보내지 않는경우
            - session timeout
            - 해당 consumer down 판단, rebalance 발생
                - rebalance가 동작하면, 하나의 컨슈머가 여러 파티션을 맡게 될 수 있음
                - 불균형한 상황 발생
            - **모니터링을 통해 컨슈머의 장애 상황을 인지하여, 새로운 컨슈머 추가가 필요**
        - 카프카는 하나의 토픽에 대해 여러 용도로 사용할 수 있음
            - 다른 메세징 큐 솔류션과의 차이점
            - 컨슈머가 메세지를 한번 가져가도, 다른 컨슈머에서 메세지를 가져올 수 있음
        - 여러 컨슈머 그룹이 하나의 토픽에 대해 메세지를 가져오는 상황
            - 각 컨슈머 그룹별 offset을 따로 관리
            - 두 개이상의 컨슈머 그룹이 연결되어도 무방
            - 단, 컨슈머 그룹 아이디가 중복되지 않도록 해야함
                **ConsumerGroupID는 Offset과 연관**
- 5.6 커밋과 오프셋
    - poll() 호출시마다 컨슈머 그룹은
        - 카프카에 저장되어 있는 아직 읽지 않은 메세지를 가져옴
    - 컨슈머 그룹은 각각 파티션 오프셋을 가지고 있음
    - Commit
        - 각 파티션에 대해 현재 위치를 업데이트 하는 동작
    - 카프카는 각 컨슈머 그룹의 파티션별로 오프셋 저장을 하기위한 저장소 필요
        - Old Kafka Consumer(:0.9)
            - 카프카 토픽에 해당 정보 저장
        - New Kafka Consumer(0.9:)
            - 카프카 내에 별도에 사용하는 토픽 사용
                - **__consumer_offsets**
                    - 토픽의 오프셋 정보 저장
    - rebalance
        - 컨슈머가 갑자기 다운되거나, 새로운 컨슈머 join시 발생
        - 이전에 처리했던 토픽의 파티션이 아닌, 새로운 파티션에 할당
        - 새로운 파티션에 대해 가장 최근 커밋 오프셋을 읽고 메세지를 가져옴
        - commit offset < 실제 처리 오프셋
            - 메세지 중복 처리
        - commit offset > 실제 처리 오프셋
            - 오프셋 차이 메세지 누락
    - 5.6.1 자동 커밋
        - 카프카 어플리케이션들이 가장 많이 사용하는 방법
        - ```enable.auto.commit=true``` 설정
            - 5초마다 poll()을 호출하여, 마지막 offset 커밋
                - default
            - ```auto.commit.interval.ms``` 옵션을 통해 조정 가능
        - 컨슈머가 poll() 요청을 할 때마다
            - 커밋할 시간인지 아닌지 체크
            - poll()의 요청으로 가져온 마지막 오프셋 커밋
        - rebalance가 발생
            - 새로운 consumer가 추가되여 rebalance가 발생했을 때,
            - 기존 컨슈머의 offset을 복사하여 가져오게 되므로
                - 그 이후 내용을 가져오게 된다.
                - 이미 이전 컨슈머가 가져온 메세지여도 중복해서 가져오게 됨
            - 중복을 줄이기 위해 자동 커밋의 시간을 줄일 수 있으나  
                - **중복을 완전히 제거할 수 없음**
        - **방법은 매우 편리하나, 중복이 발생할 수 있음**
    - 5.6.2 수동 커밋
        - 메세지 처리가 완료될 때까지 메세지를 가져온 것으로 간주되면 안될 때 사용
        - 예시
            - 데이터 베이스에 메세지 저장
            - 자동 커밋이라면, 5초마다 최신 값으로 commit
            - 일부 메세지가 DB에 저장 못한 상태로 consumer 장애 발생시 손실
            - consumer가 message를 가져오자마자가 아닌, DB에 메세지 저장 후 commit을 하도록 한다.
        - 메세지를 가져온 것으로 간주되는 시점을 자유롭게 조정할 수 있음
        - 수동 커밋에도 중복이 발생할 수 있음
            - 메세지 DB저장시 에러가 발생하면,
                - 마지막 커밋된 오프셋부터 메세지를 가져오기 때문
        - **적어도 한 번 중복은 존재하나, 손실은 없음**
    - 5.6.3 특정 파티션 할당
        - Consumer는 **Topic을 Subscribe**하며,
            - 카프카가 컨슈머 그룹의 컨슈머들에게 직접 파티션을 공평하게 분배
        - 파티션에 대해 세밀하게 제어하는 방법
        - 두가지 방법
            - 키-값 형태로 파티션 저장, 특정 파티션에 대한 메세지들만 가져와야 하는 경우
            - 컨슈퍼 프로세스가 가용성이 높은 구성인 경우(YARN, Mesos 등)
                - 카프카가 컨슈머의 실패를 감지하고 재조정 필요 x
                - 자동으로 컨슈머 프로세스가 다른 시스템에서 재시작되는 경우
        - 수동으로 파티션을 할당해 가져오는 경우,
            - **컨슈머 인스턴스마다 컨슈머 그룹 아이디를 서로 다르게 설정해야 함**
                - 동일하게 설정시, 오프셋 정보 공유
                    - 종료된 컨슈머의 파티션을 타 컨슈머가 할당받아 메세지를 가져감
                    - 오프셋을 커밋
        - [consumer_partition.java](kafka_docker/node1/kafka-java/kafka-practice-java/src/main/java/com/wshid/consumer_partition.java)
    - 5.6.4 특정 오프셋으로부터 메세지 가져오기
        - 카프카의 컨슈머 API를 사용하게 되면,
            - 메세지 중복 처리 등의 이유로, 오프셋 관리를 수동으로 하는 경우도 있음
            - 수동으로 어디서부터 메시지를 읽어올지 결정
                - ```seek()``` 메소드 사용
        - [consumer_partition.java](kafka_docker/node1/kafka-java/kafka-practice-java/src/main/java/com/wshid/consumer_partition.java)

### 6장, 카프카 운영 가이드
---
- 6.1 필수 카프카 명령어
    - 카프카 기본 제공 명령어 : ```$KAFKA_HOME/bin```에 위치


    
        

