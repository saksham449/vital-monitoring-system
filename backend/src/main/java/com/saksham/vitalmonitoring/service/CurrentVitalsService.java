package com.saksham.vitalmonitoring.service;

import com.saksham.vitalmonitoring.model.VitalData;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrentVitalsService {
    private final ConcurrentHashMap<String, VitalData> latest = new ConcurrentHashMap<>();

    public void update(VitalData data) {
        latest.put(data.bedId(), data);
    }

    public VitalData get(String bedId) {
        return latest.get(bedId);
    }

    public List<VitalData> getAll() {
        return new ArrayList<>(latest.values());
    }
}
