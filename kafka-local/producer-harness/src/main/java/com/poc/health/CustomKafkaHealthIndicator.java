package com.poc.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class CustomKafkaHealthIndicator extends AbstractHealthIndicator {

    private final String bootstrapServers = "localhost:9092";
    
    public CustomKafkaHealthIndicator() {
        System.out.println(">>> CustomKafkaHealthIndicator CREATED <<<");
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(props)) {

            DescribeClusterResult clusterResult = adminClient.describeCluster();

            String clusterId = clusterResult.clusterId().get(3, TimeUnit.SECONDS);
            Collection<Node> nodes = clusterResult.nodes().get(3, TimeUnit.SECONDS);

            builder.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodes.size());

        } catch (Exception ex) {

            builder.down()
                    .withDetail("error", ex.getMessage());
        }
    }
}