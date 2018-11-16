from kafka import KafkaProducer
producer = KafkaProducer(bootstrap_servers=['peter-kafka001:9092','peter-kafka002:9092','peter-kafka003:9092'],api_version=(2, 0, 0))
for i in range(1, 11):
    producer.send('peter-topic', b'Apache Kafka is a distributed streaming platform - %d' % i)
    print('%d message is sended' % i)


