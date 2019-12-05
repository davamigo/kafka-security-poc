package com.privalia.poc.kafka.security.runner;

import com.privalia.poc.kafka.security.monitor.KafkaStreamProcessesStatusMonitor;
import com.privalia.poc.kafka.security.producer.KafkaProducer;
import com.privalia.poc.kafka.security.service.RandomTextGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * Command line runner to process the command line parameter
 *
 * @author david.amigo
 */
@Component
public class KafkaSecurityCommandLineRunner implements CommandLineRunner {

    /** Logger object */
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSecurityCommandLineRunner.class);

    /** Command line argument */
    private static final String ARG_RUN_PRODUCER = "--produce";
    private static final String ARG_RUN_CONSUMER = "--consume";
    private static final String ARG_RUN_STREAM = "--stream";

    /** Application context. Used to close the application */
    private final ConfigurableApplicationContext context;

    /** Service to publish to Kafka */
    private final KafkaProducer kafkaProducer;

    /** Random text generator */
    private final RandomTextGenerator textGenerator;

    /** Manager for the lifecycle of the listener containers */
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    /** The status monitor for the Kafka Stream processes */
    private final KafkaStreamProcessesStatusMonitor kafkaStreamProcessesStatusMonitor;

    /**
     * Constructor
     *
     * @param context       the application context
     * @param kafkaProducer the service to publish to Kafka
     * @param textGenerator the text generator
     * @param kafkaListenerEndpointRegistry the manager for the lifecycle of the listener containers
     */
    @Autowired
    public KafkaSecurityCommandLineRunner(
            ConfigurableApplicationContext context,
            KafkaProducer kafkaProducer,
            RandomTextGenerator textGenerator,
            KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
            KafkaStreamProcessesStatusMonitor kafkaStreamProcessesStatusMonitor
    ) {
        this.context = context;
        this.kafkaProducer = kafkaProducer;
        this.textGenerator = textGenerator;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.kafkaStreamProcessesStatusMonitor = kafkaStreamProcessesStatusMonitor;
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {

        boolean runProducer = false;
        boolean runConsumer = false;
        boolean runStreamer = false;
        String lastArg = "";
        int messagesToProduce = 1;

        LOGGER.info(">>> Procesing command line arguments: {}", Arrays.toString(args));

        for (String arg : args) {
            switch (arg) {
                case ARG_RUN_PRODUCER:
                    runProducer = true;
                    break;

                case ARG_RUN_CONSUMER:
                    runConsumer = true;
                    break;

                case ARG_RUN_STREAM:
                    runStreamer = true;
                    break;

                default:
                    if (ARG_RUN_PRODUCER.equals(lastArg)) {
                        try {
                            messagesToProduce = Integer.parseInt(arg);
                        } catch (NumberFormatException exc) {
                            // Ignore exception
                        }
                    }
            }
            lastArg = arg;
        }

        if (runProducer) {
            for (int num  = 0; num < messagesToProduce; num++) {
                kafkaProducer.publish(textGenerator.getRandomText());
            }
        }

        if (runConsumer) {
            Set<String> listenerContainers = kafkaListenerEndpointRegistry.getListenerContainerIds();
            listenerContainers.forEach(id -> {
                LOGGER.info(">>> Starting Kafka listener: {}", id);
                kafkaListenerEndpointRegistry.getListenerContainer(id).start();
            });
        }

        if (runStreamer) {
            kafkaStreamProcessesStatusMonitor.getBeanQualifiers().forEach(qualifier -> {
                LOGGER.info(">>> Starting Kafka Streams: {}", qualifier);
                try {
                    kafkaStreamProcessesStatusMonitor.start(qualifier);
                } catch (KafkaStreamProcessesStatusMonitor.BeanNotFoundException exc) {
                    exc.printStackTrace();
                }
            });
        }

        if (!runProducer && !runConsumer && !runStreamer) {
            LOGGER.error(">>> Program argument required: [--produce [num]] [--consume] [--stream]");
        }
    }

    /**
     * Terminates the currently running Java Virtual Machine. It is separated to allow unit testing.
     */
    protected void closeApp() {
        System.exit(SpringApplication.exit(context));
    }
}
