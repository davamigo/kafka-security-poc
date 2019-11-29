package com.privalia.poc.kafka.security.runner;

import com.privalia.poc.kafka.security.producer.KafkaProducer;
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

    /** Service to publish to Kafka */
    private KafkaProducer kafkaProducer;

    /** Application context. Used to close the application */
    private ConfigurableApplicationContext context;

    /** Manager for the lifecycle of the listener containers */
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    /**
     * Constructor
     *
     * @param kafkaProducer the service to publish to Kafka
     * @param context       the application context
     * @param kafkaListenerEndpointRegistry the manager for the lifecycle of the listener containers
     */
    @Autowired
    public KafkaSecurityCommandLineRunner(
            KafkaProducer kafkaProducer,
            ConfigurableApplicationContext context,
            KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry
    ) {
        this.kafkaProducer = kafkaProducer;
        this.context = context;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
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
                kafkaProducer.publish("Loren Ipsum " + num);
            }
        }

        if (runConsumer) {
            Set<String> listenerContainers = kafkaListenerEndpointRegistry.getListenerContainerIds();
            listenerContainers.forEach(id -> {
                LOGGER.info(">>> Starting Kafka listener: {}", id);
                kafkaListenerEndpointRegistry.getListenerContainer(id).start();
            });
        }
    }

    /**
     * Terminates the currently running Java Virtual Machine. It is separated to allow unit testing.
     */
    protected void closeApp() {
        System.exit(SpringApplication.exit(context));
    }
}
