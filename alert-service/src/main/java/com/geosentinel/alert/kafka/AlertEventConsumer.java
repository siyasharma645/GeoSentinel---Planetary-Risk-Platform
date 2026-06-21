package com.geosentinel.alert.kafka;

import com.geosentinel.alert.model.Alert;
import com.geosentinel.alert.model.AlertLevel;
import com.geosentinel.alert.model.AlertType;
import com.geosentinel.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventConsumer {

    private final AlertRepository alertRepository;

    @KafkaListener(
        topics = {
            "geosentinel.disaster.created",
            "geosentinel.disaster.escalated",
            "geosentinel.disaster.updated"
        },
        groupId = "alert-service-group"
    )
    public void consumeDisasterEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            String type = (String) event.get("type");
            String location = (String) event.get("locationName");
            String countryCode = (String) event.get("countryCode");
            double severity = ((Number) event.getOrDefault("severity", 0)).doubleValue();
            String status = (String) event.getOrDefault("status", "ACTIVE");

            AlertLevel level = determineLevel(eventType, severity, status);

            Alert alert = Alert.builder()
                    .alertType(AlertType.DISASTER)
                    .level(level)
                    .title(String.format("%s %s — %s", level.name(), type, location))
                    .message(buildDisasterMessage(type, location, severity, status, eventType))
                    .countryCode(countryCode)
                    .region(location)
                    .status("ACTIVE")
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(86400))
                    .build();

            alertRepository.save(alert);
            log.info("Alert created: {} for {} in {}", level, type, location);

        } catch (Exception e) {
            log.error("Failed to process disaster event into alert: {}", e.getMessage(), e);
        }
    }

    private AlertLevel determineLevel(String eventType, double severity, String status) {
        if (eventType.contains("escalated") || "CRITICAL".equals(status)) return AlertLevel.CRITICAL;
        if (severity >= 7.5) return AlertLevel.CRITICAL;
        if (severity >= 6.0 || "ESCALATING".equals(status)) return AlertLevel.HIGH;
        if (severity >= 4.5) return AlertLevel.MEDIUM;
        return AlertLevel.LOW;
    }

    private String buildDisasterMessage(String type, String location, double severity,
                                         String status, String eventType) {
        String action = eventType.contains("escalated") ? "escalating" :
                        eventType.contains("created") ? "detected" : "updated";
        return String.format("%s event %s at %s. Severity: %.1f. Current status: %s.",
                type, action, location, severity, status);
    }
}
