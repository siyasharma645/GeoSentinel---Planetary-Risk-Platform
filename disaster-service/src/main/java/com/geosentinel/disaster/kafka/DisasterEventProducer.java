package com.geosentinel.disaster.kafka;
import com.geosentinel.disaster.model.Disaster;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate; import org.springframework.stereotype.Component;
import java.time.Instant; import java.util.Map;
@Slf4j @Component @RequiredArgsConstructor
public class DisasterEventProducer {
    private final KafkaTemplate<String,Object> kafka;
    public static final String CREATED="geosentinel.disaster.created",UPDATED="geosentinel.disaster.updated",ESCALATED="geosentinel.disaster.escalated";
    public void created(Disaster d){send(CREATED,d);}
    public void updated(Disaster d){send(UPDATED,d);}
    public void escalated(Disaster d){log.warn("ESCALATION: {} at {}",d.getType(),d.getLocationName());send(ESCALATED,d);}
    private void send(String topic,Disaster d){
        kafka.send(topic,d.getId().toString(),Map.of(
            "eventType",topic,"disasterId",d.getId().toString(),"type",d.getType(),
            "locationName",d.getLocationName()!=null?d.getLocationName():"",
            "countryCode",d.getCountryCode()!=null?d.getCountryCode():"",
            "severity",d.getSeverity()!=null?d.getSeverity().doubleValue():0.0,
            "status",d.getStatus().name(),
            "populationAffected",d.getPopulationAffected()!=null?d.getPopulationAffected():0L,
            "timestamp",Instant.now().toString()
        )).whenComplete((r,ex)->{if(ex!=null)log.error("Kafka failed: {}",ex.getMessage());});
    }
}
