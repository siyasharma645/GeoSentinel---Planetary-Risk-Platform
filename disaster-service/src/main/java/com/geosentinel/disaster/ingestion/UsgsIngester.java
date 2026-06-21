package com.geosentinel.disaster.ingestion;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper;
import com.geosentinel.disaster.kafka.DisasterEventProducer;
import com.geosentinel.disaster.model.*; import com.geosentinel.disaster.repository.DisasterRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal; import java.time.Instant;
@Slf4j @Component @RequiredArgsConstructor
public class UsgsIngester {
    private final DisasterRepository repo; private final DisasterEventProducer producer;
    private final RestTemplate restTemplate; private final ObjectMapper mapper;
    @Value("${ingestion.usgs.url}") private String url;
    @Value("${ingestion.usgs.enabled:true}") private boolean enabled;
    @Scheduled(fixedDelay=300000)
    public void ingest(){
        if(!enabled)return;
        try{
            String json=restTemplate.getForObject(url+"?format=geojson&minmagnitude=4.5&limit=50&orderby=time",String.class);
            JsonNode features=mapper.readTree(json).path("features");
            int n=0;
            for(JsonNode f:features){
                String extId="usgs-"+f.path("id").asText();
                if(repo.existsByExternalId(extId))continue;
                JsonNode p=f.path("properties"),c=f.path("geometry").path("coordinates");
                double mag=p.path("mag").asDouble(0);
                Disaster d=Disaster.builder().externalId(extId).type("EARTHQUAKE")
                    .title("M"+mag+" - "+p.path("place").asText("Unknown"))
                    .locationName(p.path("place").asText("Unknown"))
                    .severity(BigDecimal.valueOf(mag))
                    .lon(BigDecimal.valueOf(c.get(0).asDouble()))
                    .lat(BigDecimal.valueOf(c.get(1).asDouble()))
                    .status(mag>=6.5?DisasterStatus.CRITICAL:mag>=5.5?DisasterStatus.ACTIVE:DisasterStatus.MONITORING)
                    .startedAt(Instant.ofEpochMilli(p.path("time").asLong(0)))
                    .source("USGS").sourceUrl("https://earthquake.usgs.gov/earthquakes/eventpage/"+f.path("id").asText())
                    .build();
                repo.save(d); if(mag>=5.5)producer.created(d); n++;
            }
            if(n>0)log.info("USGS: {} new earthquakes ingested",n);
        }catch(Exception e){log.error("USGS ingestion error: {}",e.getMessage());}
    }
}
