package com.geosentinel.alert.kafka;
import com.geosentinel.alert.model.*; import com.geosentinel.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener; import org.springframework.stereotype.Component;
import java.time.Instant; import java.util.Map;
@Slf4j @Component @RequiredArgsConstructor
public class AlertKafkaConsumer {
    private final AlertRepository repo;
    @KafkaListener(topics={"geosentinel.disaster.created","geosentinel.disaster.escalated","geosentinel.disaster.updated"},groupId="alert-service-group")
    public void consume(Map<String,Object> event){
        try{
            String evtType=(String)event.get("eventType");
            String type=(String)event.getOrDefault("type","UNKNOWN");
            String loc=(String)event.getOrDefault("locationName","Unknown");
            String cc=(String)event.getOrDefault("countryCode","");
            double sev=((Number)event.getOrDefault("severity",0)).doubleValue();
            String status=(String)event.getOrDefault("status","ACTIVE");
            AlertLevel level=evtType!=null&&evtType.contains("escalated")?AlertLevel.CRITICAL:sev>=7.5?AlertLevel.CRITICAL:sev>=6.0?AlertLevel.HIGH:sev>=4.5?AlertLevel.MEDIUM:AlertLevel.LOW;
            repo.save(Alert.builder().alertType(AlertType.DISASTER).level(level)
                .title(level.name()+" - "+type+" event: "+loc)
                .message(String.format("%s event %s at %s. Severity: %.1f. Status: %s.",type,evtType!=null&&evtType.contains("created")?"detected":"updated",loc,sev,status))
                .countryCode(cc.isEmpty()?null:cc).region(loc).status("ACTIVE")
                .createdAt(Instant.now()).expiresAt(Instant.now().plusSeconds(86400))
                .build());
            log.info("Alert created: {} for {} in {}",level,type,loc);
        }catch(Exception e){log.error("Alert consumer error: {}",e.getMessage(),e);}
    }
}
