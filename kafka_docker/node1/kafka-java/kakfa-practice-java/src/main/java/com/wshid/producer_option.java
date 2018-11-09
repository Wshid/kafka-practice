package com.wshid;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import static com.wshid.Constants.*;

/**
 * Project:  kafka-practice-java
 * Author :  wshid
 * Date :  08/11/2018 8:13 PM
 */
public class producer_option {
    public static void main(String args[]){
        Properties props = new Properties();
        props.put(KAFKA_OPITOIN_BROKER, KAFKA_BOOTSTRAP_SERVER);
        props.put("acks", "1");
        props.put("compression.type", "gzip");
        props.put(KAFKA_OPTION_SERIALIZER, KAFKA_KEY_SERIALIZER);
        props.put(KAFKA_OPTION_VALUE_SERIALIZER, KAFKA_OPTION_SERIALIZER);

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        producer.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, KAFKA_MESSAGE));
        producer.close();
    }
}
