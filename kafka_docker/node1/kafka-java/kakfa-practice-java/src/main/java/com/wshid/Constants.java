package com.wshid;

/**
 * Project:  kakfa-practice-java
 * Author :  wshid
 * Date :  08/11/2018 8:14 PM
 */
public class Constants {

    public static final String KAFKA_OPTION_BROKER = "bootstrap.servers";
    public static final String KAFKA_OPTION_SERIALIZER = "key.serializer";
    public static final String KAFKA_OPTION_VALUE_SERIALIZER = "value.serializer";
    public static final String KAFKA_TOPIC_NAME = "peter-topic";
    public static final String KAFKA_BOOTSTRAP_SERVER = "peter-kafka001:9092,peter-kafka002:9092,peter-kafka003:9092";
    public static final String KAFKA_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String KAFKA_VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    public static final String KAFKA_MESSAGE = "Apache kafka is a distributed streaming platform";

    public static final String KAFKA_OPTION_GROUP_ID = "group.id";
    public static final String KAFKA_GROUP_ID = "peter-consumer";

    public static final String KAFKA_OPTION_ENABLE_COMMIT = "enable.auto.commit";
    public static final String KAFKA_ENABLE_AUTO_COMMIT = "true";

    public static final String KAFKA_OPTION_AUTO_OFFEST = "auto.offset.reset";
    public static final String KAFKA_AUTO_OFFEST_COMMIT = "latest";

    public static final String KAFKA_OPTION_KEY_DESERIALIZER = "key.deserializer";
    public static final String KAFKA_KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";

    public static final String KAFKA_OPTION_VALUE_DESERIALIZER = "value.desrializer";
    public static final String KAFKA_VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.StringDesrializer";

}
