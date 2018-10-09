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