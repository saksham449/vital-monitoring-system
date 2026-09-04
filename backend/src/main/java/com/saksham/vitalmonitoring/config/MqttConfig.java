package com.saksham.vitalmonitoring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    @Value("${mqtt.broker-url}")
    private String brokerUrl;
    @Value("${mqtt.username:}")
    private String username;
    @Value("${mqtt.password:}")
    private String password;
    @Value("${mqtt.topic}")
    private String topic;

    public String getBrokerUrl() { return brokerUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getTopic() { return topic; }
}
