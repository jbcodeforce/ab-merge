package io.confluent.developer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.SaslConfigs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.ab.developer.avro.pricing;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class MergeStreams {

    public Topology buildTopology(Properties allProps) {
        final StreamsBuilder builder = new StreamsBuilder();

        final String pricingTopic = allProps.getProperty("input.pricing.topic.name");
        final String pricingDeleteTopic = allProps.getProperty("input.pricingDelete.topic.name");
        final String allPricingTopic = allProps.getProperty("output.topic.name");

        KStream<String, pricing> pricingMessage = builder.stream(pricingTopic);
        KStream<String, pricing> pricingDeleteMessage = builder.stream(pricingDeleteTopic);
        KStream<String, pricing> allPricingMessage = pricingMessage.merge(pricingDeleteMessage);

        allPricingMessage.to(allPricingTopic);
        return builder.build();
    }

    public void createTopics(Properties allProps) {
        AdminClient client = AdminClient.create(allProps);

        List<NewTopic> topics = new ArrayList<>();

        topics.add(new NewTopic(
                allProps.getProperty("input.pricing.topic.name"),
                Integer.parseInt(allProps.getProperty("input.pricing.topic.partitions")),
                Short.parseShort(allProps.getProperty("input.pricing.topic.replication.factor"))));

        topics.add(new NewTopic(
                allProps.getProperty("input.pricingDelete.topic.name"),
                Integer.parseInt(allProps.getProperty("input.pricingDelete.topic.partitions")),
                Short.parseShort(allProps.getProperty("input.pricingDelete.topic.replication.factor"))));

        topics.add(new NewTopic(
                allProps.getProperty("output.topic.name"),
                Integer.parseInt(allProps.getProperty("output.topic.partitions")),
                Short.parseShort(allProps.getProperty("output.topic.replication.factor"))));

        client.createTopics(topics);
        client.close();
    }

    public Properties loadEnvProperties(String fileName) throws IOException {
        Properties allProps = new Properties();
        FileInputStream input = new FileInputStream(fileName);
        allProps.load(input);
        input.close();

        return allProps;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        MergeStreams ms = new MergeStreams();
        Properties allProps = ms.loadEnvProperties(args[0]);
        allProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        allProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        allProps.put(SCHEMA_REGISTRY_URL_CONFIG, allProps.getProperty("schema.registry.url"));
        allProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, allProps.getProperty("security.protocol.config"));
        allProps.put(SaslConfigs.DEFAULT_SASL_MECHANISM, allProps.getProperty("sasl.mechanism.config"));
        allProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,allProps.getProperty("ssl.truststore.location.config"));
        allProps.put(SaslConfigs.SASL_JAAS_CONFIG,allProps.getProperty("sasl.jaas.config"));
        allProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, allProps.getProperty("ssl.truststore.password.config"));
        
        Topology topology = ms.buildTopology(allProps);

        //ms.createTopics(allProps);

        final KafkaStreams streams = new KafkaStreams(topology, allProps);
        final CountDownLatch latch = new CountDownLatch(1);

        // Attach shutdown handler to catch Control-C.
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close(Duration.ofSeconds(5));
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
