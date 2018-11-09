from kafka import KafkaProducer

# ë¸ë¡ì»¤ ë¦¬ì¤í¸ ì§ì 
producer = KafkaProducer(bootstrap_servers='peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092')

# í í½ëªê³¼ ë³´ë¼ ë©ì¸ì§ì ë´ì© ì§ì , ì ì¡
producer.send('peter-topic', b'Apache Kafka is a distributed streaming platform')

