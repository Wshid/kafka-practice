## 3장, 카프카 디자인
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