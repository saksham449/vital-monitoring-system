package com.saksham.vitalmonitoring.controller;

import com.saksham.vitalmonitoring.dto.AlertResponse;
import com.saksham.vitalmonitoring.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> getAll() { return alertService.all(); }

    @GetMapping("/active")
    public List<AlertResponse> getActive() { return alertService.active(); }

    @GetMapping("/bed/{bedId}")
    public List<AlertResponse> getByBed(@PathVariable String bedId) { return alertService.byBed(bedId); }
}
