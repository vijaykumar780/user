package com.user.user.configs;

import com.google.gson.Gson;
import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.time.Duration;

@Slf4j
@Configuration
public class Config {
    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.region:eu-west-2}")
    private String region;

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry() {
        log.info("Initializing CloudWatch Meter Registry...");
        CloudWatchConfig config = new CloudWatchConfig() {
            @Override
            public String get(String key) {
                if ("cloudwatch.namespace".equals(key))
                    return "SpringBootAppMetrics";
                if ("cloudwatch.step".equals(key))
                    return Duration.ofMinutes(1).toString();
                return null;
            }

            @Override
            public String prefix() {
                return "cloudwatch";
            }
        };
        log.info("Configuring CloudWatch Meter Registry with namespace: {}", config.get("cloudwatch.namespace"));
        return new CloudWatchMeterRegistry(
                config,
                Clock.SYSTEM,
                CloudWatchAsyncClient.builder()
                        .region(Region.of(region))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                        .build());
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

}