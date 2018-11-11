package com.wshid;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import static com.wshid.Constants.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Project:  kakfa-practice-java
 * Author :  wshid
 * Date :  11/11/2018 10:51 AM
 */
public class consumer_manual {
    public static void main(String[] args){
        Properties props = new Properties();
        props.put(KAFKA_OPTION_BROKER, KAFKA_BOOTSTRAP_SERVER);
        props.put(KAFKA_OPTION_GROUP_ID, KAFKA_GROUP_ID);
        props.put(KAFKA_OPTION_ENABLE_COMMIT, "false"); // 커밋 수동 설정
        props.put(KAFKA_OPTION_AUTO_OFFEST, KAFKA_AUTO_OFFEST_COMMIT);
        props.put(KAFKA_OPTION_KEY_DESERIALIZER, KAFKA_KEY_DESERIALIZER);
        props.put(KAFKA_OPTION_VALUE_DESERIALIZER, KAFKA_VALUE_DESERIALIZER);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(KAFKA_TOPIC_NAME));

        try{
            while(true){
                Duration duration = Duration.ofMillis(100);
                ConsumerRecords<String, String> records =  consumer.poll(duration); // poll로 메세지 가져오기
                for(ConsumerRecord<String, String> record: records){
                    System.out.printf("Topic : %s, Partition : %s, Offset : %d, Key : %s, Value :%s,\n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());

                }
                try{
                    /**
                     * 메세지를 모두 가져온 후, commitSync를 통해 커밋
                     */
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
