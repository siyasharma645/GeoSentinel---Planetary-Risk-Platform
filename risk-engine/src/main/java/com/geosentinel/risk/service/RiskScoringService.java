package com.geosentinel.risk.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geosentinel.risk.dto.*;
import com.geosentinel.risk.model.LocationRiskReport;
import com.geosentinel.risk.repository.RiskReportRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.util.*; import java.util.concurrent.TimeUnit;

@Slf4j @Service @RequiredArgsConstructor
public class RiskScoringService {
    private final RiskReportRepository repo;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    @Value("${risk.cache-minutes:60}") private int cacheMins;
    private static final String KEY = "risk:rpt:";

    @Transactional
    public RiskReport getReport(LocationRequest req) {
        if (!req.isForceRefresh()) {
            String cached = redis.opsForValue().get(KEY + cacheKey(req));
            if (cached != null) try { return mapper.readValue(cached, RiskReport.class).toBuilder().fromCache(true).build(); } catch (Exception ignored) {}
            var db = repo.findValidByCoords(req.getLat(), req.getLon(), Instant.now());
            if (db.isPresent()) return fromEntity(db.get());
        }
        RiskReport r = compute(req);
        persist(req, r); cacheRedis(req, r);
        return r;
    }

    private RiskReport compute(LocationRequest req) {
        double lat = req.getLat(), lon = req.getLon();
        String cc = req.getCountryCode() != null ? req.getCountryCode().toUpperCase() : "";
        int climate=clamp(climateScore(lat,lon,cc)), disaster=clamp(disasterScore(lat,lon,cc)),
            water=clamp(waterScore(lat,lon,cc)),     food=clamp(foodScore(water,climate,cc)),
            health=clamp(healthScore(lat,lon,cc)),   conflict=clamp(conflictScore(cc));
        var cats = new LinkedHashMap<String,RiskReport.CategoryScore>();
        cats.put("climate",  cs(climate,  lbl(climate),  "Climate: "+climateD(climate)));
        cats.put("disaster", cs(disaster, lbl(disaster), "Disaster: "+disasterD(disaster)));
        cats.put("water",    cs(water,    lbl(water),    "Water: "+waterD(water)));
        cats.put("food",     cs(food,     lbl(food),     "Food: "+foodD(food)));
        cats.put("health",   cs(health,   lbl(health),   "Health: "+healthD(health)));
        cats.put("conflict", cs(conflict, lbl(conflict), "Conflict: "+conflictD(conflict)));
        int overall = clamp((int)Math.round(climate*.20+disaster*.25+water*.15+food*.15+health*.10+conflict*.15));
        return RiskReport.builder().locationName(req.getName()).country(req.getCountry())
            .countryCode(req.getCountryCode()).lat(lat).lon(lon).overallRisk(overall).riskLevel(lbl(overall))
            .summary(summary(req.getName(),overall,cats)).categories(cats).activeThreats(threats(cats))
            .keyMetrics(metrics(cats)).forecast(forecast(overall)).recommendations(recs(overall,cats))
            .generatedAt(Instant.now()).fromCache(false).build();
    }

    private int climateScore(double lat,double lon,String cc) {
        int s=35; if(Math.abs(lat)<23.5)s+=18; if(lat>10&&lat<30&&lon>-18&&lon<60)s+=14;
        s+=switch(cc){case"BD","MM","PH","VN"->20;case"PK","IN","ET"->14;case"SO","YE","ER"->18;case"US","CN"->8;case"NO","NZ","FI","SE"->-18;default->0;}; return s;
    }
    private int disasterScore(double lat,double lon,String cc) {
        int s=28; if((lon>120&&lon<180&&lat>-50&&lat<65)||(lon>-130&&lon<-60))s+=22;
        if(Math.abs(lat)>5&&Math.abs(lat)<25)s+=14;
        s+=switch(cc){case"BD"->35;case"PH"->30;case"JP"->28;case"IN","MM"->20;case"TR"->22;case"SO","HT"->18;default->0;}; return s;
    }
    private int waterScore(double lat,double lon,String cc) {
        int s=28; if(lat>15&&lat<35&&lon>-15&&lon<62)s+=32; if(lat>20&&lat<40&&lon>60&&lon<85)s+=18;
        s+=switch(cc){case"SO","YE","ER"->44;case"AF","IQ","SY"->34;case"PK","IN","BD"->20;case"NO","CA","NZ","FI"->-22;default->0;}; return s;
    }
    private int foodScore(int w,int c,String cc) {
        int s=(w+c)/2; s+=switch(cc){case"SO","SS","YE"->28;case"AF","CF","ML","NE"->20;case"ET","SD"->14;case"US","AU","CA","FR","DE"->-20;default->0;}; return s;
    }
    private int healthScore(double lat,double lon,String cc) {
        int s=24; if(lat>-35&&lat<15&&lon>-18&&lon<52)s+=28;
        s+=switch(cc){case"SO","SS","CF","CD"->38;case"ML","NE","BF"->28;case"IN","BD","PK"->18;case"JP","SE","NO","FI","NZ"->-20;default->0;}; return s;
    }
    private int conflictScore(String cc) {
        return switch(cc){case"SO","SS","YE","SY","AF"->90;case"IQ","CF","ML","BF","SD"->74;case"UA","MM","ET"->64;case"MX","HT"->50;case"NG","CM"->34;case"US","FR","GB","DE","AU"->12;case"JP","SE","NO","NZ","FI"->5;default->20;};
    }
    private int clamp(int v){return Math.max(0,Math.min(100,v));}
    private String lbl(int s){return s>=75?"CRITICAL":s>=55?"HIGH":s>=35?"MODERATE":"LOW";}
    private RiskReport.CategoryScore cs(int v,String l,String d){return RiskReport.CategoryScore.builder().score(v).label(l).detail(d).build();}
    private String climateD(int s) {return s>=75?"Extreme temperature anomalies and severe precipitation disruption.":s>=55?"Significant climate stress with shifting seasonal patterns.":s>=35?"Moderate climate variability.":"Stable climate conditions.";}
    private String disasterD(int s){return s>=75?"High multi-hazard exposure - seismic, hydrological, meteorological.":s>=55?"Regular disaster occurrence with significant exposure.":s>=35?"Periodic natural hazards requiring preparedness.":"Low natural hazard exposure.";}
    private String waterD(int s)   {return s>=75?"Acute water scarcity with critically depleted aquifers.":s>=55?"High water stress impacting agriculture and urban areas.":s>=35?"Moderate water stress with seasonal shortages.":"Adequate water resources.";}
    private String foodD(int s)    {return s>=75?"Acute food insecurity; IPC Phase 4+ conditions present.":s>=55?"Significant food crisis with supply disruptions.":s>=35?"Food system under stress with price volatility.":"Food security maintained.";}
    private String healthD(int s)  {return s>=75?"Active disease outbreaks with overwhelmed health infrastructure.":s>=55?"Elevated disease burden with limited healthcare access.":s>=35?"Moderate health risks with strained systems.":"Robust health infrastructure.";}
    private String conflictD(int s){return s>=75?"Active armed conflict disrupting civilian life and aid.":s>=55?"Significant political instability and security threats.":s>=35?"Elevated tensions with localized incidents.":"Stable security environment.";}
    private String summary(String n,int r,Map<String,RiskReport.CategoryScore> c) {
        String top=c.entrySet().stream().max(Comparator.comparingInt(e->e.getValue().getScore())).map(Map.Entry::getKey).orElse("multiple");
        return String.format("%s presents a %s risk profile (index: %d/100). Primary driver: %s risk (%d/100).",n,lbl(r),r,top.toUpperCase(),c.get(top).getScore());
    }
    private List<RiskReport.ActiveThreat> threats(Map<String,RiskReport.CategoryScore> c) {
        var l=new ArrayList<RiskReport.ActiveThreat>();
        c.forEach((k,v)->{if(v.getScore()>=55)l.add(RiskReport.ActiveThreat.builder().type(k.toUpperCase()+" RISK").severity(v.getScore()>=75?"HIGH":"MEDIUM").description(v.getDetail()).build());});
        if(l.isEmpty())l.add(RiskReport.ActiveThreat.builder().type("BASELINE").severity("LOW").description("No acute threats. Routine monitoring.").build());
        return l;
    }
    private List<RiskReport.KeyMetric> metrics(Map<String,RiskReport.CategoryScore> c) {
        return List.of(
            RiskReport.KeyMetric.builder().label("Climate Risk").value(c.get("climate").getScore()+"/100").trend(c.get("climate").getScore()>55?"up":"stable").build(),
            RiskReport.KeyMetric.builder().label("Water Stress").value(String.format("%.2f",c.get("water").getScore()/100.0)).trend(c.get("water").getScore()>50?"up":"stable").build(),
            RiskReport.KeyMetric.builder().label("Food Insecurity").value(c.get("food").getScore()+"%").trend("up").build(),
            RiskReport.KeyMetric.builder().label("Health Threat").value(c.get("health").getScore()+"/100").trend("stable").build(),
            RiskReport.KeyMetric.builder().label("Conflict Index").value(c.get("conflict").getScore()+"/100").trend("stable").build()
        );
    }
    private RiskReport.Forecast forecast(int o) {
        return RiskReport.Forecast.builder()
            .thirtyDay(o>=70?"Conditions critical or worsening. Immediate response required.":"Stable. Monitor escalation triggers.")
            .ninetyDay(o>=55?"Risk trending upward. Long-term planning required.":"Stable outlook. Surveillance recommended.")
            .probability(Math.min(95,o+5)).build();
    }
    private List<String> recs(int o,Map<String,RiskReport.CategoryScore> c) {
        var r=new ArrayList<String>();
        r.add(o>=70?"Activate emergency response protocols and deploy rapid assessment teams.":"Pre-position response resources and maintain situational awareness.");
        if(c.get("water").getScore()>=55)r.add("Prioritize water security interventions and aquifer monitoring.");
        if(c.get("health").getScore()>=55)r.add("Strengthen disease surveillance and deploy mobile health units.");
        if(r.size()<3)r.add("Coordinate with local authorities for contingency planning.");
        return r.subList(0,Math.min(3,r.size()));
    }
    private void persist(LocationRequest req,RiskReport r) {
        try {
            Map<String,Object> json=mapper.convertValue(r,new TypeReference<>(){});
            repo.save(LocationRiskReport.builder().locationName(req.getName()).countryCode(req.getCountryCode())
                .lat(req.getLat()).lon(req.getLon()).overallRisk(r.getOverallRisk()).riskLevel(r.getRiskLevel())
                .summary(r.getSummary()).reportJson(json).generatedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(cacheMins*60L)).build());
        } catch(Exception e){log.warn("Persist failed: {}",e.getMessage());}
    }
    private void cacheRedis(LocationRequest req,RiskReport r) {
        try{redis.opsForValue().set(KEY+cacheKey(req),mapper.writeValueAsString(r),cacheMins,TimeUnit.MINUTES);}
        catch(Exception e){log.warn("Redis cache failed: {}",e.getMessage());}
    }
    private RiskReport fromEntity(LocationRiskReport e) {
        try{return mapper.convertValue(e.getReportJson(),RiskReport.class).toBuilder().fromCache(true).build();}
        catch(Exception ex){return null;}
    }
    private String cacheKey(LocationRequest r){return String.format("%.4f:%.4f",r.getLat(),r.getLon());}

    public Map<String,Object> globalOverview() {
        return Map.of("globalRiskIndex",71,"activeDisasters",23,"countriesAtHighRisk",36,"alertsActive",8,
            "topRiskCountries",List.of(
                Map.of("code","SO","name","Somalia","score",94,"delta",2),
                Map.of("code","YE","name","Yemen","score",91,"delta",1),
                Map.of("code","SS","name","South Sudan","score",89,"delta",0),
                Map.of("code","HT","name","Haiti","score",87,"delta",3),
                Map.of("code","AF","name","Afghanistan","score",83,"delta",1),
                Map.of("code","PK","name","Pakistan","score",79,"delta",4),
                Map.of("code","BD","name","Bangladesh","score",76,"delta",0),
                Map.of("code","ET","name","Ethiopia","score",73,"delta",-1)
            ),"timestamp",Instant.now().toString());
    }
}
