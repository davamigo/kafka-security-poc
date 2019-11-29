package com.privalia.poc.kafka.security.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer to receive messages from a Kafka topic
 */
@Component
public class KafkaConsumer {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    /**
     * Kafka listener
     *
     * @param value The value of the message
     * @param ack   The acknowledgment object
     * @param key   The key of the message
     * @param topic The name of the topic
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.sim-test1}",
            groupId = "${spring.kafka.group-ids.sim-test1}",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "false"
    )
    public void listen(
            String value,
            Acknowledgment ack,
            @Nullable @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        LOGGER.info(">>> Consuming from Kafka: Topic={}, Key={}, Value={}", topic, key, value);
        ack.acknowledge();
    }
}
