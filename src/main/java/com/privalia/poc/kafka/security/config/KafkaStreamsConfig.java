package com.privalia.poc.kafka.security.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Map;

/**
 * Configuration class for Kafka Streams
 *
 * @author david.amigo
 */
@Configuration
public class KafkaStreamsConfig {

    /* The environment object where to get the config options */
    private Environment environment;

    /** The configuration class for Apache Kafka */
    private KafkaConfig kafkaConfig;

    /**
     * Autowired Constructor
     *
     * @param environment Tte environment object where to get the config options
     * @param kafkaConfig the configuration class for Apache Kafka
     */
    @Autowired
    public KafkaStreamsConfig(
            Environment environment,
            KafkaConfig kafkaConfig
    ) {
        this.environment = environment;
        this.kafkaConfig = kafkaConfig;
    }

    /**
     * Global configuration values for all Kafka streams classes
     *
     * @return the default configurations for all the Kafka producers
     */
    private Map<String, Object> streamsConfigs() {
        Map<String, Object> props = kafkaConfig.commonConfigs();
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, environment.getProperty("spring.kafka.streams.threads", "1"));
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, environment.getProperty("spring.kafka.streams.replication-factor", "1"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "default");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    /**
     * Create custom streams builder factory
     *
     * @param applicationId an identifier for the stream processing application.
     * @return A factory to build the stream process
     */
    private StreamsBuilderFactoryBean newCustomStreamsBuilderFactoryBean(String applicationId) {

        Map<String, Object> props = streamsConfigs();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);

        StreamsBuilderFactoryBean bean = new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(props));
        bean.setAutoStartup(Boolean.parseBoolean(environment.getProperty("spring.kafka.streams.auto-startup", "true")));

        return bean;
    }

    /**
     * Creates a bean for a Kafka Streams process
     *
     * @return A factory to build the stream process
     */
    @Bean("sim-kafka-steams-count-words-bean")
    public StreamsBuilderFactoryBean kafkaStreamsCountWordsStreamBuilderFactoryBean() {
        return newCustomStreamsBuilderFactoryBean("sim-kafka-steams-count-words");
    }
}
