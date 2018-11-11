package com.wshid;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import static com.wshid.Constants.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Project:  kakfa-practice-java
 * Author :  wshid
 * Date :  12/11/2018 12:04 AM
 */
public class consumer_partition {

    public static void main(String[] args){
        Properties props = new Properties();
        props.put(KAFKA_OPTION_BROKER, KAFKA_BOOTSTRAP_SERVER);
        props.put(KAFKA_OPTION_GROUP_ID, KAFKA_GROUP_ID);
        props.put(KAFKA_OPTION_ENABLE_COMMIT, "false");
        props.put(KAFKA_OPTION_AUTO_OFFEST, KAFKA_AUTO_OFFEST_COMMIT);
        props.put(KAFKA_OPTION_KEY_DESERIALIZER, KAFKA_KEY_DESERIALIZER);
        props.put(KAFKA_OPTION_VALUE_DESERIALIZER, KAFKA_VALUE_DESERIALIZER);

        KafkaConsumer<String, String>consumer = new KafkaConsumer<String, String>(props);

        /**
         * 토픽의 파티션 정의 후
         * 배열로 partition0, partition1 할당
         */
        TopicPartition partition0 = new TopicPartition(KAFKA_TOPIC_NAME, 0);
        TopicPartition partition1 = new TopicPartition(KAFKA_TOPIC_NAME, 1);
        consumer.assign(Arrays.asList(partition0, partition1));


        /**
         * seek(partition, offset)
         *  컨슈머가 다음 poll()하는 위치를 지정
         *  partition0, partition1의 2번 오프셋부터 메세지를 가져온다.
         */
        consumer.seek(partition0, 2);
        consumer.seek(partition1, 2);

        try{
            while(true){
                Duration duration = Duration.ofMillis(100);
                ConsumerRecords<String, String> records = consumer.poll(duration);
                for(ConsumerRecord<String, String> record : records){
                    System.out.printf("Topic: %s, Partition: %s, Offset: %s, Key: %s, Value:%s\n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());
                }
                try{
                    consumer.commitSync();
                }catch(CommitFailedException exception){
                    System.out.printf("Error", exception);
                }
            }
        }finally {
            consumer.close();
        }

    }
}
