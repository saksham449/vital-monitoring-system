package com.saksham.vitalmonitoring.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saksham.vitalmonitoring.config.MqttConfig;
import com.saksham.vitalmonitoring.model.VitalData;
import com.saksham.vitalmonitoring.service.VitalProcessingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class MqttListener {
    private static final Logger log = LoggerFactory.getLogger(MqttListener.class);

    private final MqttConfig config;
    private final ObjectMapper objectMapper;
    private final VitalProcessingService processingService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile MqttClient client;

    public MqttListener(MqttConfig config, ObjectMapper objectMapper, VitalProcessingService processingService) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.processingService = processingService;
    }

    @PostConstruct
    public void start() {
        scheduler.execute(this::connectWithRetry);
    }

    private void connectWithRetry() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                connect();
                return;
            } catch (Exception e) {
                log.warn("MQTT connection failed: {}. Retrying in 5 seconds.", e.getMessage());
                sleep(5000);
            }
        }
    }

    private synchronized void connect() throws MqttException {
        if (client != null && client.isConnected()) return;

        client = new MqttClient(config.getBrokerUrl(), "vital-monitor-backend-" + UUID.randomUUID());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        if (config.getUsername() != null && !config.getUsername().isBlank()) {
            options.setUserName(config.getUsername());
            options.setPassword(config.getPassword().toCharArray());
        }

        client.setCallback(new MqttCallback() {
            @Override public void connectionLost(Throwable cause) {
                log.warn("MQTT connection lost: {}", cause == null ? "unknown" : cause.getMessage());
            }

            @Override public void messageArrived(String topic, MqttMessage message) {
                handleMessage(topic, message);
            }

            @Override public void deliveryComplete(IMqttDeliveryToken token) { }
        });

        client.connect(options);
        client.subscribe(config.getTopic(), 1);
        log.info("Connected to MQTT broker {} and subscribed to {}", config.getBrokerUrl(), config.getTopic());
    }

    private void handleMessage(String topic, MqttMessage message) {
        try {
            VitalData data = objectMapper.readValue(message.getPayload(), VitalData.class);
            processingService.process(data);
        } catch (Exception e) {
            log.error("Invalid MQTT message on topic {}: {}", topic, e.getMessage());
        }
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
        try {
            if (client != null && client.isConnected()) client.disconnect();
            if (client != null) client.close();
        } catch (MqttException e) {
            log.warn("Error while closing MQTT client: {}", e.getMessage());
        }
    }
}
