package com.geosentinel.climate.ingestion;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper;
import com.geosentinel.climate.model.ClimateMetric; import com.geosentinel.climate.repository.ClimateMetricRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal; import java.time.Instant;
// Regions: {name, lat, lon}
@Slf4j @Component @RequiredArgsConstructor
public class OpenMeteoIngester {
    private final ClimateMetricRepository repo; private final RestTemplate rest; private final ObjectMapper mapper;
    @Value("${ingestion.open-meteo.url}") private String baseUrl;
    @Value("${ingestion.enabled:true}") private boolean enabled;
    private static final double[][] REGIONS = {{23.68,90.35},{5.15,46.20},{30.38,69.35},{15.55,48.52},{-3.47,-62.22},{28.39,84.12}};
    @Scheduled(fixedDelay=900000) // 15 min
    public void ingest(){
        if(!enabled)return;
        for(double[] r:REGIONS){
            try{
                String url=baseUrl+"/forecast?latitude="+r[0]+"&longitude="+r[1]+"&current=temperature_2m,relative_humidity_2m,precipitation&timezone=UTC";
                String json=rest.getForObject(url,String.class);
                JsonNode root=mapper.readTree(json);
                JsonNode cur=root.path("current");
                double temp=cur.path("temperature_2m").asDouble();
                double humid=cur.path("relative_humidity_2m").asDouble();
                double precip=cur.path("precipitation").asDouble();
                double baseline=20.0; // rough global baseline
                repo.save(ClimateMetric.builder().metricType("TEMPERATURE").lat(BigDecimal.valueOf(r[0])).lon(BigDecimal.valueOf(r[1])).value(BigDecimal.valueOf(temp)).unit("C").anomaly(BigDecimal.valueOf(temp-baseline)).recordedAt(Instant.now()).source("open-meteo").build());
                repo.save(ClimateMetric.builder().metricType("HUMIDITY").lat(BigDecimal.valueOf(r[0])).lon(BigDecimal.valueOf(r[1])).value(BigDecimal.valueOf(humid)).unit("%").recordedAt(Instant.now()).source("open-meteo").build());
                repo.save(ClimateMetric.builder().metricType("PRECIPITATION").lat(BigDecimal.valueOf(r[0])).lon(BigDecimal.valueOf(r[1])).value(BigDecimal.valueOf(precip)).unit("mm").recordedAt(Instant.now()).source("open-meteo").build());
            }catch(Exception e){log.error("OpenMeteo ingestion error for [{},{}]: {}",r[0],r[1],e.getMessage());}
        }
        log.info("OpenMeteo ingestion complete for {} regions",REGIONS.length);
    }
}
