package com.wshid;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Project:  kafka-practice-java
 * Author :  wshid
 * Date :  04/11/2018 11:30 AM
 */
public class PeterCallback implements Callback { // callback을 사용하기 위해 override

    /**
     * 오류 리턴시 onCompletion이라는 에러를 가진다
     * 실제 서비스에서는 추가적인 예외처리를 해야함
     *
     * @param recordMetadata
     * @param e
     */
    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (recordMetadata != null) {
            System.out.printf("Partition : %d, Offset : %d\n", recordMetadata.partition(), recordMetadata.offset());
        } else {
            e.printStackTrace();
        }
    }
}
