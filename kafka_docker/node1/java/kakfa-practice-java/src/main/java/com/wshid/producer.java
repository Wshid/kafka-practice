package com.wshid;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import static com.wshid.Constants.*;

import java.util.Properties;


public class producer {




    public static void main(String[] args) {


        Properties props = new Properties();
        props.put(KAFKA_OPITOIN_BROKER, KAFKA_BOOTSTRAP_SERVER); // 브로커 리스트 정의
        props.put(KAFKA_OPTION_SERIALIZER, KAFKA_KEY_SERIALIZER);
        props.put(KAFKA_OPTION_VALUE_SERIALIZER, KAFKA_VALUE_SERIALIZER);

        Producer<String, String> producer = new KafkaProducer<>(props);


        /**
         * 메세지를 보내고 확인하지 않음
         * 메세지는 버퍼에 저장되고, 별도의 스레드를 통해 브로커로 전송
         * send()는 자바 Future Object, 리턴값을 받지만 무시, 성공 전송인지 여부 판단이 되지 않음
         * 메세지 손실 가능성이 있으므로 일반적으로 사용하지 않음
         */
        try {
            producer.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, KAFKA_MESSAGE));
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            // producer.close();
        }

        /**
         * 동기 전송
         * get method를 사용하여 future를 기다린 후, send()가 성공했는지 실패했는지 리턴
         * 브로커가 전송한 메세지가 성공했는지, 실패했는지 확인함
         * 메세지가 성공적으로 전달하지 않는다면 get 부분에서 에러 발생
         */
        try {
            RecordMetadata metadata = producer.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, "Apache Kafka is a distributed")).get();
            System.out.printf("Partition : %d, Offset %d", metadata.partition(), metadata.offset());
        } catch (Exception exception) { // 재시도 가능한 예외는 재전송 하여 해결(커넥션 문제), 재시도 불가능 예외 - 메세지가 큰 경우
            exception.printStackTrace();
        } finally {
            //producer.close();
        }

        /**
         * 비동기 전송
         * 응답을 기다리지 않음
         * 빠른 전송이 가능하며, 에러 처리 후 향후 분석에 사용할 수 있음
         */
        try {
            producer.send(new ProducerRecord<String, String>(KAFKA_TOPIC_NAME, KAFKA_MESSAGE), new PeterCallback());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}