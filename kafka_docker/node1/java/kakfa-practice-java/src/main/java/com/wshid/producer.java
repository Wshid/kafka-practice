import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;


import java.util.Properties;

public class KafkaBookProducer1 {

    private static final String KAFKA_OPITOIN_BROKER="bootstrap.servers";
    private static final String KAFKA_OPTION_SERIALIZER="key.serializer";
    private static final String KAFKA_OPTION_VALUE_SERIALIZER="value.serializer";
    private static final String KAFKA_TOPIC_NAME = "peter-topic";
    private static final String KAFKA_BOOTSTRAP_SERVER = "peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092";
    private static final String KAFKA_KEY_SERIALIZER="org.apache.kafka.common.serialization.StringSerializer";
    private static final String KAFKA_VALUE_SERIALIZER="org.apache.kafka.common.serialization.StringSerializer";


    public static void main(String[] args){



        Properties props = new Properties();
        props.put(KAFKA_OPITOIN_BROKER, KAFKA_BOOTSTRAP_SERVER); // 브로커 리스트 정의
        props.put(KAFKA_OPTION_SERIALIZER, KAFKA_KEY_SERIALIZER);
        props.put(KAFKA_OPTION_VALUE_SERIALIZER, KAFKA_VALUE_SERIALIZER);

        Producer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, "Apache kafka is a distributed streaming platform"));

        producer.close();
    }
}