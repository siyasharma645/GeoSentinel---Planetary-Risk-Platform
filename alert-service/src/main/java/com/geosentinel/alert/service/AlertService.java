package com.geosentinel.alert.service;
import com.geosentinel.alert.model.*; import com.geosentinel.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor; import org.springframework.data.domain.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.*; import java.util.UUID;
@Service @RequiredArgsConstructor
public class AlertService {
    private final AlertRepository repo;
    public Page<Alert> getActive(Pageable p){return repo.findByStatusOrderByCreatedAtDesc("ACTIVE",p);}
    public List<Alert> getCriticalAndHigh(){return repo.findByLevelInAndStatusOrderByCreatedAtDesc(List.of(AlertLevel.CRITICAL,AlertLevel.HIGH),"ACTIVE");}
    public List<Alert> byCountry(String cc){return repo.findByCountryCodeAndStatusOrderByCreatedAtDesc(cc,"ACTIVE");}
    public long countActive(){return repo.countByStatus("ACTIVE");}
    @Transactional public Alert resolve(UUID id){
        Alert a=repo.findById(id).orElseThrow(()->new IllegalArgumentException("Alert not found: "+id));
        a.setStatus("RESOLVED"); return repo.save(a);
    }
    @Transactional public Alert create(Alert a){return repo.save(a);}
}
