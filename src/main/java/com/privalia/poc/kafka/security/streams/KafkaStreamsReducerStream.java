package com.privalia.poc.kafka.security.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Kafka streams reducer example with SSL enabled
 *
 * @author david.amigo
 */
@Component
public class KafkaStreamsReducerStream {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsReducerStream.class);

    /** The name of the input topic (KStream) */
    private final String inputTopic;

    /** The name of the output topic (KTable) */
    private final String outputTopic;

    /**
     * Autowired constructor
     *
     * @param inputTopic  the name of the input topic (KStream)
     * @param outputTopic the name of the output topic (KTable)
     */
    @Autowired
    public KafkaStreamsReducerStream(
            @Value("${spring.kafka.topics.sim-test1}") String inputTopic,
            @Value("${spring.kafka.topics.sim-test2}") String outputTopic
    ) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
    }

    /**
     * Builds the topology of the Kafka Streams.
     *
     * @param builder the streams builder
     * @return the builder configured with the topology
     */
    @Bean("sim-kafka-steams-count-words-topology")
    public StreamsBuilder startProcessing(
            @Qualifier("sim-kafka-steams-count-words-bean") StreamsBuilder builder
    ) {
        KStream<String, String> inputStream = builder
                .stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> wordsStream = inputStream
                .flatMap((key, value) -> {
                    LOGGER.info(">>> Read input stream - Key={} - Value={}", key, value);
                    List<KeyValue<String, String>> result = new LinkedList<>();
                    Arrays.stream(value.split(" ")).forEach(word -> result.add(KeyValue.pair(word, word)));
                    return result;
                });

        KTable<String, Long> outputTable = wordsStream
                .groupByKey()
                .count();

        KStream<String, Long> outputStream = outputTable
                .toStream()
                .peek((key, value) -> {
                    LOGGER.info(">>> Written output stream - Key={} - Value={}", key, value);
                });

        outputStream.to(
                outputTopic,
                Produced.with(Serdes.String(), Serdes.Long())
        );

        return builder;
    }
}
