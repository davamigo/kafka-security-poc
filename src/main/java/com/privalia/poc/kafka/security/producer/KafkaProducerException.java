package com.privalia.poc.kafka.security.producer;

import org.springframework.kafka.KafkaException;

/**
 * Exception to be thrown when an error occurred publishing to a Kafka topic
 *
 * @author david.amigo
 */
public class KafkaProducerException extends KafkaException {

    /** The name of the topic */
    private String topic;

    /** The value of the message */
    private String value;

    /** The key of the message */
    private String key;

    /**
     * Constructor
     *
     * @param cause the nested exception
     * @param topic the topic name
     * @param value the value of the message
     * @param key   the key of the message
     */
    public KafkaProducerException(Throwable cause, String topic, String value, String key) {
        super(KafkaProducerException.buildErrorMessage(topic, value, key), cause);
        this.topic = topic;
        this.value = value;
        this.key = key;
    }

    /**
     * @return the name of the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the value of the message
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the key of the message
     */
    public String getKey() {
        return key;
    }

    /**
     * @param topic the topic name
     * @param value the value of the message
     * @param key   the key of the message
     * @return the error message
     */
    private static String buildErrorMessage(String topic, String value, String key) {
        return "An error occurred publishing to Kafka - topic=" + topic + " key=" + key + " Value=" + value;
    }
}
