package com.wshid;

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
 * Date :  10/11/2018 9:23 PM
 */
public class consumer {
    public static void main(String args[]){
        Properties props = new Properties();
        props.put(KAFKA_OPTION_BROKER, KAFKA_BOOTSTRAP_SERVER);
        props.put(KAFKA_OPTION_GROUP_ID, KAFKA_GROUP_ID); // Consumer GroupID 지정
        props.put(KAFKA_OPTION_ENABLE_COMMIT, KAFKA_ENABLE_AUTO_COMMIT);

        /**
         * OFFSET RESET 값을 지정
         *  earlist : 토픽의 처음부터 메세지를 가져옴
         *  latest : 토픽의 마지막 메세지를 가져옴
         *      default : latest
         *      필요한 경우에는 earlist로 지정
         */
        props.put(KAFKA_OPTION_AUTO_OFFEST, KAFKA_AUTO_OFFEST_COMMIT);
        props.put(KAFKA_OPTION_KEY_DESERIALIZER, KAFKA_KEY_DESERIALIZER);
        props.put(KAFKA_OPTION_VALUE_DESERIALIZER, KAFKA_VALUE_DESERIALIZER);

        KafkaConsumer<String, String>consumer = new KafkaConsumer<String, String>(props);

        /**
         * subscribe
         *  메세지를 가져올 토픽을 구독
         *  리스트로 여러개의 토픽 입력 가능
         */
        consumer.subscribe(Arrays.asList(KAFKA_TOPIC_NAME));
        try{
            while(true){  // 지속적으로 가져오기 위해 poll
                Duration duration = Duration.ofMillis(100);

                //ConsumerRecords<String, String> records = consumer.poll(100); // deprecated
                /**
                 * Consumer는 Kafka에 Polling 하는 것을 유지해야 함
                 *  그렇지 않으면 종료된 것으로 간주되어,
                 *      컨슈머에 할당된 파티션은 다른 컨슈머에게 전달되고
                 *          새로운 컨슈머에 의해 소비(Consume)된다.
                 *  poll()은 본래 Timeout주기 이며,
                 *      데이터가 컨슈머 버퍼에 없다면 poll()은 얼마나 block할지를 조정
                 *      0으로 설정하면 즉시 리턴
                 *      값을 입력하면 정해진 시간동안 대기
                 */
                ConsumerRecords<String, String> records = consumer.poll(duration);


                /**
                 * poll()은 레코드 전체를 리턴한다.
                 *  레코드 = 토픽, 파티션, 파티션 오프셋, 키, 값 등을 포함
                 *  한번에 한개의 메세지만을 가져오는 것이 아니기 때문에, 반복문이 필요하다
                 *  실제 환경에서는 단순 출력 코드 뿐만 아니라
                 *      하둡에 저장하거나
                 *      데이터베이스에 저장하거나
                 *      수신한 메세지를 이용해 특정 레코드를 분석하는 로직 추가 가능
                 */
                for(ConsumerRecord<String, String> record : records){
                    System.out.printf("Topic: %s, Partition: %s, Offset: %d, Key:%d, Value: %s\n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());
                }
            }
        } finally{
            /**
             * Consumer 종료 전에 close()사용
             * 네트워크 연결 및 소켓을 종료
             * consumer가 heartbeat를 보내지 않아, 그룹 코디네이터가 해당 consumer를 종료하는 것보다
             *  빠르게 진행되며, 즉시 rebalance가 일어남
             */
            consumer.close();
        }
    }
}
