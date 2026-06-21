package com.geosentinel.disaster.service;
import com.geosentinel.disaster.kafka.DisasterEventProducer;
import com.geosentinel.disaster.model.*; import com.geosentinel.disaster.repository.DisasterRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.Instant; import java.util.List; import java.util.UUID;
@Slf4j @Service @RequiredArgsConstructor
public class DisasterService {
    private final DisasterRepository repo; private final DisasterEventProducer producer;
    public List<Disaster> getActive(){return repo.findAllActive();}
    public Disaster getById(UUID id){return repo.findById(id).orElseThrow(()->new IllegalArgumentException("Not found: "+id));}
    public Page<Disaster> byCountry(String cc,Pageable p){return repo.findByCountryCodeIgnoreCaseOrderByStartedAtDesc(cc,p);}
    public Page<Disaster> byType(String t,Pageable p){return repo.findByTypeIgnoreCaseOrderBySeverityDesc(t,p);}
    public List<Disaster> near(double lat,double lon,double km){double d=km/111.0;return repo.findInBox(lat-d,lat+d,lon-d,lon+d);}
    @Transactional public Disaster create(Disaster d){
        d.setStatus(DisasterStatus.ACTIVE); if(d.getStartedAt()==null)d.setStartedAt(Instant.now());
        Disaster s=repo.save(d); producer.created(s); log.info("Created: {} at {}",d.getType(),d.getLocationName()); return s;
    }
    @Transactional public Disaster updateStatus(UUID id,DisasterStatus status){
        Disaster d=getById(id); d.setStatus(status);
        if(status==DisasterStatus.RESOLVED)d.setEndedAt(Instant.now());
        Disaster u=repo.save(d);
        if(status==DisasterStatus.ESCALATING||status==DisasterStatus.CRITICAL)producer.escalated(u);
        else producer.updated(u); return u;
    }
    @Transactional public Disaster updateSeverity(UUID id,BigDecimal sev){
        Disaster d=getById(id); BigDecimal old=d.getSeverity(); d.setSeverity(sev);
        if(old!=null&&sev.subtract(old).doubleValue()>=1.5){d.setStatus(DisasterStatus.ESCALATING);Disaster u=repo.save(d);producer.escalated(u);return u;}
        return repo.save(d);
    }
}
