## 7장, 카프카를 활용한 데이터 파이프라인 구축
---
- 7.1 카프카를 활용한 데이터 흐름도
    - 프로듀서
        - 메세지 생산 후 카프카로 전송
    - 카프카
        - 생산한 메세지를 저장하는 중간 큐 역할
    - 컨슈머
        - 카프카에 저장된 메세지를 가져옴
        - 데이터 처리, 로그 분석을 위해
            - 다른 어플리케이션으로 메세지를 전송하는 역할
    - 흐름도 및 기술 스택
        - 프로듀서 : Filebeat
        - 컨슈머 : Nifi
        - 데이터 처리 : Nifi
    - Nifi
        - Apache Nifi
        - 데이터 흐름을 정의하고, 정의된 흐름대로 자동으로 실행해주는 어플리케이션
        - 데이터 흐름 기반으로 사용하기 유용
        - 웹 기반 인터페이스 제공
- 7.2 파일비트를 이용한 메세지 전송
    - 카프카에서 생성되는 로그를 카프카의 토픽으로 전송
    - 토픽생성 : **peter-log**
        ```
        $KAFKA_HOME/bin/kafka-topics.sh \
        --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka \
        --topic peter-log --partitions 3 --replication-factor 2 --create
        ```
    - 토픽 생성 확인
        ```
        [root@node1 filebeat]# kafka-topics.sh --zookeeper peter-zk001:2181,peter-zk002:2181,peter-zk003:2181/peter-kafka --topic peter-log --describe
        Topic:peter-log PartitionCount:3        ReplicationFactor:2     Configs:
                Topic: peter-log        Partition: 0    Leader: 2       Replicas: 2,1   Isr: 2,1
                Topic: peter-log        Partition: 1    Leader: 3       Replicas: 3,2   Isr: 3,2
                Topic: peter-log        Partition: 2    Leader: 1       Replicas: 1,3   Isr: 1,3
        [root@node1 filebeat]# 
        ```
    - 7.2.1 파일비트 설치
        - yum을 사용하여 설치하기
        - [filebeat page](https://www.elastic.co/guide/en/beats/filebeat/current/setup-repositories.html)
            ```
            sudo rpm --import https://packages.elastic.co/GPG-KEY-elasticsearch
            
            # vi /etc/yum.repos.d/elastic.repo
            [elastic-6.x]
            name=Elastic repository for 6.x packages
            baseurl=https://artifacts.elastic.co/packages/6.x/yum
            gpgcheck=1
            gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
            enabled=1
            autorefresh=1
            type=rpm-md

            yum install -y filebeat
            ```
    - 7.2.2 파일비트 설정
        - filebeat.yml
            ```
            vi /etc/filebeat/filebeat.yml
            ```
            - 자세한 내용은 [filebeat.yml](kafka_docker/node1/filebeat/filebeat.yml) 참고
        - 설정 후 실행
            ```
            filebeat -c filebeat.yml -e &
            ```
            - propspector가 7.0부터 deprecated 된다는 내용 존재
            - 추후 input으로 변경할 것
        - bg에 있는 작업을 fg로 끌어올 때 사용하는 명령어
            ```
            jobs # 작업 번호 확인
            fg %<작업번호> # fg %1과 같이 작업 번호를 명시한다.

            ```
    - 7.2.3 카프카 토픽의 메세지 유입 확인하기
        ```
        $KAFKA_HOME/bin/kafka-console-consumer.sh \
        --bootstrap-server peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092 \
        --topic peter-log --max-messages 10 --from-beginning

        {"@timestamp":"2018-11-18T04:15:05.741Z","@metadata":{"beat":"filebeat","type":"doc","version":"6.5.0","topic":"peter-log"},"source":"/usr/local/packages/kafka/logs/server.log.2018-11-08-11","offset":889,"message":"[2018-11-08 11:19:31,529] INFO Client environment:java.vendor=Oracle Corporation (org.apache.zookeeper.ZooKeeper)","fields":{"pipeline":"kafka-logs"},"beat":{"name":"node1","hostname":"node1","version":"6.5.0"},"host":{"name":"node1"}}
        {"@timestamp":"2018-11-18T04:15:05.742Z","@metadata":{"beat":"filebeat","type":"doc","version":"6.5.0","topic":"peter-log"},"host":{"name":"node1"},"source":"/usr/local/packages/kafka/logs/server.log.2018-11-15-07","offset":889,"message":"[2018-11-15 07:30:35,870] INFO Client environment:java.vendor=Oracle Corporation (org.apache.zookeeper.ZooKeeper)","fields":{"pipeline":"kafka-logs"},"beat":{"name":"node1","hostname":"node1","version":"6.5.0"}}
        {"@timestamp":"2018-11-18T04:15:05.742Z","@metadata":{"beat":"filebeat","type":"doc","version":"6.5.0","topic":"peter-log"},"offset":5877,"message":"[2018-11-15 07:30:35,872] INFO Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib (org.apache.zookeeper.ZooKeeper)","fields":{"pipeline":"kafka-logs"},"beat":{"name":"node1","hostname":"node1","version":"6.5.0"},"host":{"name":"node1"},"source":"/usr/local/packages/kafka/logs/server.log.2018-11-15-07"}
        {"@timestamp":"2018-11-18T04:15:05.742Z","@metadata":{"beat":"filebeat","type":"doc","version":"6.5.0","topic":"peter-log"},"message":"[2018-11-15 07:30:35,873] INFO Client environment:java.compiler=\u003cNA\u003e (org.apache.zookeeper.ZooKeeper)","fields":{"pipeline":"kafka-logs"},"beat":{"name":"node1","hostname":"node1","version":"6.5.0"},"host":{"name":"node1"},"source":"/usr/local/packages/kafka/logs/server.log.2018-11-15-07","offset":6142}
        Processed a total of 10 messages
        ```
        - 내용을 잘 로드하는 것을 확인할 수 있다.
        - 하단에 Processed .. 문장을 보면, 10개의 메세지를 가져온다는 것을 확인할 수 있다.
- 7.3 나이파이를 이용해 메세지 가져오기
    - 카프카 peter-log 토픽으로 부터 메세지를 가져오기
    - Consumer로 Nifi를 사용한다.
    - 7.3.1 나이파이 설치
        - [Apache Nifi Downloads](https://nifi.apache.org/download.html)
        - 파일을 다운 받고 환경 변수로 설정한다.
    - 7.3.2 나이파이 설정
        - ```$NIFI_HOME/conf/nifi.properties```
        ```
        nifi.web.http.host=peter-kafka001 # 각 노드별 로컬 호스트 설정
        nifi.cluster.is.node=true
        nifi.cluster.node.address=peter-kafka001 # 각 노드별 로컬 호스트 설정
        nifi.cluster.node.protocol.port=8082
        nifi.zookeeper.connect.string=peter-zk001:2181,peter-zk002:2181,peter-zk003:2181
        ```
        - zookeeper host 정보 설정
        - 클러스터 구성을 하기 위해, 동일하게 설정하여 준다.
        - 포트번호를 열어도 제대로 설정이 되지 않는다.

