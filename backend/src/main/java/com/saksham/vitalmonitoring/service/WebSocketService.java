package com.saksham.vitalmonitoring.service;

import com.saksham.vitalmonitoring.dto.AlertResponse;
import com.saksham.vitalmonitoring.dto.BedStatusResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishBedStatus(BedStatusResponse response) {
        messagingTemplate.convertAndSend("/topic/vitals", response);
    }

    public void publishAlert(AlertResponse response) {
        messagingTemplate.convertAndSend("/topic/alerts", response);
    }
}
