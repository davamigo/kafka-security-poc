package com.privalia.poc.kafka.security.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Apache Kafka
 *
 * @author david.amigo
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    /**
     * The environment object where to get the config options
     */
    private Environment environment;

    /**
     * Autowired Constructor
     *
     * @param environment The environment object where to get the config options
     */
    @Autowired
    public KafkaConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Global common configuration values
     *
     * @return the default configurations
     */
    Map<String, Object> commonConfigs() {

        @SuppressWarnings("ConstantConditions")
        Resource sslKeystoreResource = new ClassPathResource(environment.getProperty("spring.kafka.ssl.keystore.location"));

        @SuppressWarnings("ConstantConditions")
        Resource sslTrusSttoreResource = new ClassPathResource(environment.getProperty("spring.kafka.ssl.truststore.location"));

        String sslKeystorePath = "";
        String sslTrusstorePath = "";
        try {
            sslKeystorePath = sslKeystoreResource.getFile().getAbsolutePath();
            sslTrusstorePath = sslTrusSttoreResource.getFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> props = new HashMap<>();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers"));
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystorePath);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, environment.getProperty("spring.kafka.ssl.keystore.password"));
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, environment.getProperty("spring.kafka.ssl.key.password"));
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTrusstorePath);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, environment.getProperty("spring.kafka.ssl.truststore.password"));
        return props;
    }

    /**
     * Global configuration values for all Kafka producers
     *
     * @return the default configurations for all the Kafka producers
     */
    Map<String, Object> producerConfigs() {
        Map<String, Object> props = commonConfigs();
        props.put(ProducerConfig.ACKS_CONFIG, environment.getProperty("spring.kafka.producer.acks"));
        props.put(ProducerConfig.RETRIES_CONFIG, environment.getProperty("spring.kafka.producer.retries"));
        props.put(ProducerConfig.LINGER_MS_CONFIG, environment.getProperty("spring.kafka.producer.linger-ms"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);


        return props;
    }

    /**
     * Global configuration values for all Kafka consumers
     *
     * @return the default configurations for all the Kafka consumers
     */
    Map<String, Object> consumerConfigs() {
        Map<String, Object> props = commonConfigs();
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, environment.getProperty("spring.kafka.consumer.enable-auto-commit"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return props;
    }

    /**
     * Creates a factory for producing messages to Kafka
     *
     * @param props the configuration for the Kafka producer
     * @return the default kafka producer factory
     */
    ProducerFactory<String, String> producerFactory(Map<String, Object> props) {
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Creates a factory for consuming messages from Kafka
     *
     * @return the default kafka consumer factory.
     */
    ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Kafka template bean for producing messages to Kafka
     *
     * @return a new Kafka template for producing messages
     */
    @Bean
    public KafkaTemplate<String, String> kafkaProducerTemplate() {
        return new KafkaTemplate<>(producerFactory(producerConfigs()));
    }

    /**
     * Kafka listener container factory bean for consuming messages from Kafka
     *
     * @return the kafka listener container factory for consumer.
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
