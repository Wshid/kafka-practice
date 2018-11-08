from kafka import KafkaProducer

# 브로커 리스트 지정
producer = KafkaProducer(bootstrap_servers='peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092')

# 토픽명과 보낼 메세지의 내용 지정, 전송
producer.send('peter-topic', 'Apache Kafka is a distributed streaming platform')

