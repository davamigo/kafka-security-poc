package com.privalia.poc.kafka.security.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Kafka producer to publish messages to a Kafka topic
 *
 * @author david.amigo
 */
@Component
public class KafkaProducer {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

    /**
     * Kafka template for sending messages to Kafka topic
     */
    private final KafkaTemplate<String, String> kafkaProducerTemplate;

    /**
     * The name of the Kafka topic to publish the messages
     */
    private final String topicName;

    /**
     * Autowired constructor
     *
     * @param kafkaProducerTemplate the Kafka template for producing messages
     */
    @Autowired
    public KafkaProducer(
            KafkaTemplate<String, String> kafkaProducerTemplate,
            @Value("${spring.kafka.topics.sim-test1}") String topicName
    ) {
        this.kafkaProducerTemplate = kafkaProducerTemplate;
        this.topicName = topicName;
    }

    /**
     * Publish a new message to Kafka
     *
     * @param value the value of the message
     * @param key   the key of the message
     * @throws KafkaProducerException when can't publish
     */
    public void publish(String value, String key) throws KafkaProducerException {
        publish(topicName, value, key);
    }

    /**
     * Publish a new message to Kafka without key
     *
     * @param value the value of the message
     * @throws KafkaProducerException when can't publish
     */
    public void publish(String value) throws KafkaProducerException {
        publish(topicName, value, null);
    }

    /**
     * Publish a message to a Kafka topic
     *
     * @param topic  the name of the topic
     * @param value the value of the message
     * @param key   the key of the message
     * @throws KafkaProducerException when can't publish
     */
    private void publish(String topic, String value, String key) throws KafkaProducerException {
        LOGGER.info(">>> Publishing a message to Kafka - Topic={} - Key={} - Value={}", topic, key, value);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        try {
            SendResult<String, String> result = kafkaProducerTemplate.send(record).get();
            LOGGER.info(">>> Message published to Kafka - Topic={} - Key={} - Value={}", topic, key, value);
        } catch (InterruptedException | ExecutionException | KafkaException exc) {
            LOGGER.error(">>> An error occurred publishing to Kafka - Topic={} - Key={} - Value={}", topic, key, value);
            throw new KafkaProducerException(exc, topic, value, key);
        }
    }
}
