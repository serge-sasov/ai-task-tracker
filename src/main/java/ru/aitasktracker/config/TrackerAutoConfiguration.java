package ru.aitasktracker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TrackerProperties.class)
public class TrackerAutoConfiguration {
}
