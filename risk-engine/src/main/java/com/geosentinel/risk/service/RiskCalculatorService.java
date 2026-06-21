package com.geosentinel.risk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geosentinel.risk.dto.RiskDtos.*;
import com.geosentinel.risk.model.LocationRiskReport;
import com.geosentinel.risk.repository.LocationRiskReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCalculatorService {

    private final LocationRiskReportRepository reportRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${risk.cache.report-ttl-minutes:60}")
    private int reportTtlMinutes;

    private static final String CACHE_PREFIX = "risk:report:";

    @Transactional
    public RiskReport getOrGenerateReport(LocationRequest request) {
        String cacheKey = CACHE_PREFIX + String.format("%.4f:%.4f", request.getLat(), request.getLon());

        // 1. Check Redis cache first (fastest)
        if (!request.isForceRefresh()) {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    RiskReport report = objectMapper.readValue(cached, RiskReport.class);
                    log.debug("Cache hit for {}", request.getName());
                    return report.toBuilder().fromCache(true).build();
                } catch (Exception e) {
                    log.warn("Failed to deserialize cached report", e);
                }
            }

            // 2. Check Postgres for recent report
            Optional<LocationRiskReport> dbReport = reportRepository.findValidByCoords(
                    request.getLat(), request.getLon(), Instant.now());
            if (dbReport.isPresent()) {
                log.debug("DB cache hit for {}", request.getName());
                return deserializeReport(dbReport.get(), true);
            }
        }

        // 3. Generate fresh report via AI scoring
        log.info("Generating fresh risk report for {}", request.getName());
        RiskReport report = generateReport(request);

        // Persist to DB
        persistReport(request, report);

        // Cache in Redis
        try {
            String serialized = objectMapper.writeValueAsString(report);
            redisTemplate.opsForValue().set(cacheKey, serialized, reportTtlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache report in Redis", e);
        }

        return report;
    }

    private RiskReport generateReport(LocationRequest request) {
        // Multi-factor risk scoring model
        // In production these scores would come from real data feeds
        // (GDACS, USGS, NASA FIRMS, WHO, FAO, etc.)
        Map<String, CategoryScore> categories = calculateCategoryScores(request);

        int overallRisk = calculateOverallRisk(categories);
        String riskLevel = getRiskLevel(overallRisk);

        List<ActiveThreat> threats = deriveThreats(request, categories);
        List<KeyMetric> metrics = buildKeyMetrics(request, categories);
        Forecast forecast = buildForecast(overallRisk, categories);
        List<String> recommendations = buildRecommendations(riskLevel, categories);

        return RiskReport.builder()
                .locationName(request.getName())
                .countryCode(request.getCountryCode())
                .lat(request.getLat())
                .lon(request.getLon())
                .overallRisk(overallRisk)
                .riskLevel(riskLevel)
                .summary(buildSummary(request.getName(), overallRisk, riskLevel, categories))
                .categories(categories)
                .activeThreats(threats)
                .keyMetrics(metrics)
                .forecast(forecast)
                .recommendations(recommendations)
                .generatedAt(Instant.now())
                .fromCache(false)
                .build();
    }

    /**
     * Multi-dimensional risk scoring using geographic and contextual signals.
     * Weights derived from INFORM Risk Index methodology.
     */
    private Map<String, CategoryScore> calculateCategoryScores(LocationRequest req) {
        double lat = req.getLat(), lon = req.getLon();
        Map<String, CategoryScore> scores = new LinkedHashMap<>();

        // Climate score — higher near equator/tropics, coastal low-lying areas
        int climateScore = computeClimateScore(lat, lon, req.getCountryCode());
        scores.put("climate", CategoryScore.builder()
                .score(climateScore)
                .label(getRiskLevel(climateScore))
                .detail(climateDetail(climateScore))
                .build());

        // Disaster score — seismic zones, tropical cyclone belts, flood plains
        int disasterScore = computeDisasterScore(lat, lon, req.getCountryCode());
        scores.put("disaster", CategoryScore.builder()
                .score(disasterScore)
                .label(getRiskLevel(disasterScore))
                .detail(disasterDetail(disasterScore))
                .build());

        // Water score — arid regions, over-extracted aquifers
        int waterScore = computeWaterScore(lat, lon, req.getCountryCode());
        scores.put("water", CategoryScore.builder()
                .score(waterScore)
                .label(getRiskLevel(waterScore))
                .detail(waterDetail(waterScore))
                .build());

        // Food score — correlates with water, conflict, and climate
        int foodScore = Math.min(100, (waterScore + climateScore) / 2 + countryFoodOffset(req.getCountryCode()));
        scores.put("food", CategoryScore.builder()
                .score(foodScore)
                .label(getRiskLevel(foodScore))
                .detail(foodDetail(foodScore))
                .build());

        // Health score — disease burden, sanitation, conflict
        int healthScore = computeHealthScore(lat, lon, req.getCountryCode());
        scores.put("health", CategoryScore.builder()
                .score(healthScore)
                .label(getRiskLevel(healthScore))
                .detail(healthDetail(healthScore))
                .build());

        // Conflict score — based on country
        int conflictScore = countryConflictScore(req.getCountryCode());
        scores.put("conflict", CategoryScore.builder()
                .score(conflictScore)
                .label(getRiskLevel(conflictScore))
                .detail(conflictDetail(conflictScore))
                .build());

        return scores;
    }

    private int calculateOverallRisk(Map<String, CategoryScore> categories) {
        // INFORM-style weighted composite
        double climate  = categories.get("climate").getScore()  * 0.20;
        double disaster = categories.get("disaster").getScore() * 0.25;
        double water    = categories.get("water").getScore()    * 0.15;
        double food     = categories.get("food").getScore()     * 0.15;
        double health   = categories.get("health").getScore()   * 0.10;
        double conflict = categories.get("conflict").getScore() * 0.15;
        return (int) Math.min(100, Math.round(climate + disaster + water + food + health + conflict));
    }

    private String getRiskLevel(int score) {
        if (score >= 75) return "CRITICAL";
        if (score >= 55) return "HIGH";
        if (score >= 35) return "MODERATE";
        return "LOW";
    }

    // ── Geographic scoring helpers ───────────────────────────────────────────

    private int computeClimateScore(double lat, double lon, String cc) {
        int base = 40;
        // Tropical zone higher baseline
        if (Math.abs(lat) < 23.5) base += 20;
        // Sahel / Middle East arid zone
        if (lat > 10 && lat < 30 && lon > -20 && lon < 60) base += 15;
        // High-emission large economies
        if (cc != null) base += switch (cc) {
            case "CN", "IN", "US", "RU" -> 10;
            case "BD", "PK", "MM", "PH" -> 18; // Highly exposed
            case "NO", "NZ", "IS" -> -15;
            default -> 0;
        };
        return clamp(base + (int)(Math.random() * 6 - 3));
    }

    private int computeDisasterScore(double lat, double lon, String cc) {
        int base = 30;
        // Pacific Ring of Fire — seismic + volcanic
        if ((lon > 120 && lon < 180 && lat > -50 && lat < 65) ||
            (lon > -130 && lon < -60 && lat > -55 && lat < 65)) base += 25;
        // Tropical cyclone belt
        if (Math.abs(lat) > 5 && Math.abs(lat) < 25) base += 15;
        // Flood-prone low-lying
        if (cc != null) base += switch (cc) {
            case "BD" -> 35; // Bangladesh — extreme flood risk
            case "PH" -> 30; // Philippines — typhoon alley
            case "IN", "MM" -> 20;
            case "JP" -> 28; // Earthquake + tsunami
            case "TR" -> 22; // Seismic
            case "NL" -> 12; // Sea level
            default -> 0;
        };
        return clamp(base + (int)(Math.random() * 6 - 3));
    }

    private int computeWaterScore(double lat, double lon, String cc) {
        int base = 30;
        // Hyper-arid zones
        if (lat > 15 && lat < 35 && lon > -15 && lon < 60) base += 35; // MENA
        if (lat > 20 && lat < 40 && lon > 60 && lon < 85) base += 20; // South Asia arid
        if (cc != null) base += switch (cc) {
            case "SO", "YE", "ER" -> 45;
            case "AF", "PK", "IQ", "SY" -> 35;
            case "IN", "BD" -> 20;
            case "NO", "CA", "NZ" -> -20;
            default -> 0;
        };
        return clamp(base + (int)(Math.random() * 6 - 3));
    }

    private int computeHealthScore(double lat, double lon, String cc) {
        int base = 25;
        // Sub-Saharan Africa — disease burden
        if (lat > -35 && lat < 15 && lon > -20 && lon < 52) base += 30;
        if (cc != null) base += switch (cc) {
            case "SO", "SS", "CF", "CD" -> 40;
            case "ML", "NE", "BF", "GN" -> 30;
            case "IN", "BD", "PK" -> 20;
            case "SE", "NO", "FI", "JP" -> -20;
            default -> 0;
        };
        return clamp(base + (int)(Math.random() * 6 - 3));
    }

    private int countryConflictScore(String cc) {
        if (cc == null) return 20;
        return switch (cc) {
            case "SO", "SS", "YE", "SY", "AF" -> 90;
            case "IQ", "CF", "CD", "ML", "BF" -> 75;
            case "UA", "MM", "ET", "SD" -> 65;
            case "MX", "HT", "LY" -> 50;
            case "NG", "CM", "IN" -> 35;
            case "US", "FR", "GB", "DE" -> 15;
            case "JP", "SE", "NO", "NZ" -> 5;
            default -> 20;
        };
    }

    private int countryFoodOffset(String cc) {
        if (cc == null) return 0;
        return switch (cc) {
            case "SO", "SS", "YE" -> 30;
            case "AF", "CF", "ML" -> 20;
            case "ET", "NE", "ER" -> 15;
            case "US", "AU", "CA", "FR" -> -20;
            default -> 0;
        };
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }

    // ── Detail strings ────────────────────────────────────────────────────────

    private String climateDetail(int s) {
        if (s >= 75) return "Extreme temperature anomalies and precipitation disruption observed.";
        if (s >= 55) return "Significant climate stress with seasonal weather pattern shifts.";
        if (s >= 35) return "Moderate climate variability with manageable risks.";
        return "Stable climate conditions with low anomaly risk.";
    }

    private String disasterDetail(int s) {
        if (s >= 75) return "High exposure to multiple natural hazard types with frequent occurrence.";
        if (s >= 55) return "Regular disaster events with significant population exposure.";
        if (s >= 35) return "Periodic natural hazards requiring preparedness measures.";
        return "Low natural hazard exposure with infrequent events.";
    }

    private String waterDetail(int s) {
        if (s >= 75) return "Acute water scarcity with critically stressed aquifers and reservoirs.";
        if (s >= 55) return "High water stress affecting agriculture and urban populations.";
        if (s >= 35) return "Moderate water stress with seasonal shortages.";
        return "Adequate water resources with sustainable withdrawal rates.";
    }

    private String foodDetail(int s) {
        if (s >= 75) return "Acute food insecurity with famine conditions in vulnerable areas.";
        if (s >= 55) return "Significant food crisis with supply chain disruptions.";
        if (s >= 35) return "Food system under stress with price volatility.";
        return "Food security maintained with stable supply chains.";
    }

    private String healthDetail(int s) {
        if (s >= 75) return "Active disease outbreaks with overwhelmed health infrastructure.";
        if (s >= 55) return "Elevated disease burden with limited healthcare access.";
        if (s >= 35) return "Moderate health risks with functional but strained systems.";
        return "Robust health infrastructure with low disease burden.";
    }

    private String conflictDetail(int s) {
        if (s >= 75) return "Active armed conflict severely disrupting civilian life and aid access.";
        if (s >= 55) return "Significant political instability and security threats.";
        if (s >= 35) return "Elevated tensions with localized security incidents.";
        return "Stable security environment with functional governance.";
    }

    // ── Derived data builders ─────────────────────────────────────────────────

    private List<ActiveThreat> deriveThreats(LocationRequest req, Map<String, CategoryScore> cats) {
        List<ActiveThreat> threats = new ArrayList<>();
        cats.forEach((key, cat) -> {
            if (cat.getScore() >= 60) {
                threats.add(ActiveThreat.builder()
                        .type(key.toUpperCase() + " RISK")
                        .severity(cat.getScore() >= 75 ? "HIGH" : "MEDIUM")
                        .description(cat.getDetail())
                        .build());
            }
        });
        if (threats.isEmpty()) {
            threats.add(ActiveThreat.builder()
                    .type("BASELINE MONITORING")
                    .severity("LOW")
                    .description("No acute threats detected. Routine monitoring active.")
                    .build());
        }
        return threats;
    }

    private List<KeyMetric> buildKeyMetrics(LocationRequest req, Map<String, CategoryScore> cats) {
        int climate = cats.get("climate").getScore();
        int water = cats.get("water").getScore();
        return List.of(
                KeyMetric.builder().label("Climate Risk Score").value(climate + "/100").trend(climate > 60 ? "up" : "stable").build(),
                KeyMetric.builder().label("Water Stress Index").value(String.format("%.2f", water / 100.0)).trend(water > 55 ? "up" : "stable").build(),
                KeyMetric.builder().label("Food Insecurity Rate").value(cats.get("food").getScore() + "%").trend("up").build(),
                KeyMetric.builder().label("Disease Outbreak Risk").value(cats.get("health").getScore() + "/100").trend("stable").build(),
                KeyMetric.builder().label("Conflict Intensity").value(cats.get("conflict").getScore() + "/100").trend("stable").build()
        );
    }

    private Forecast buildForecast(int overall, Map<String, CategoryScore> cats) {
        int prob = Math.min(95, overall + 5);
        return Forecast.builder()
                .thirtyDay(overall >= 70
                        ? "Conditions likely to remain critical or worsen. Immediate response recommended."
                        : "Conditions expected to persist. Monitor for escalation triggers.")
                .ninetyDay(overall >= 55
                        ? "Risk trajectory trending upward. Long-term intervention planning required."
                        : "Stable outlook. Continued surveillance recommended.")
                .probability(prob)
                .build();
    }

    private List<String> buildRecommendations(String level, Map<String, CategoryScore> cats) {
        List<String> recs = new ArrayList<>();
        if ("CRITICAL".equals(level) || "HIGH".equals(level)) {
            recs.add("Activate emergency response protocols and deploy rapid assessment teams.");
        } else {
            recs.add("Maintain situational awareness and pre-position response resources.");
        }
        if (cats.get("water").getScore() >= 55) {
            recs.add("Prioritize water security interventions and infrastructure assessment.");
        }
        if (cats.get("health").getScore() >= 55) {
            recs.add("Strengthen disease surveillance and deploy mobile health units.");
        }
        if (recs.size() < 3) {
            recs.add("Coordinate with local authorities for contingency planning and capacity building.");
        }
        return recs.subList(0, Math.min(3, recs.size()));
    }

    private String buildSummary(String name, int risk, String level, Map<String, CategoryScore> cats) {
        String topRisk = cats.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().getScore()))
                .map(e -> e.getKey())
                .orElse("multiple factors");
        return String.format(
                "%s presents a %s risk profile with a composite index of %d/100. " +
                "The primary driver is %s risk (score: %d). " +
                "Immediate attention recommended for the highest-scoring vulnerability domains.",
                name, level, risk, topRisk.toUpperCase(),
                cats.get(topRisk).getScore());
    }

    @Transactional
    private void persistReport(LocationRequest req, RiskReport report) {
        try {
            Map<String, Object> json = objectMapper.convertValue(report, Map.class);
            LocationRiskReport entity = LocationRiskReport.builder()
                    .locationName(req.getName())
                    .countryCode(req.getCountryCode())
                    .lat(req.getLat())
                    .lon(req.getLon())
                    .overallRisk(report.getOverallRisk())
                    .riskLevel(report.getRiskLevel())
                    .summary(report.getSummary())
                    .reportJson(json)
                    .generatedAt(Instant.now())
                    .expiresAt(Instant.now().plus(reportTtlMinutes, ChronoUnit.MINUTES))
                    .build();
            reportRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to persist risk report", e);
        }
    }

    private RiskReport deserializeReport(LocationRiskReport entity, boolean fromCache) {
        try {
            RiskReport report = objectMapper.convertValue(entity.getReportJson(), RiskReport.class);
            return report.toBuilder().fromCache(fromCache).build();
        } catch (Exception e) {
            log.error("Failed to deserialize stored report", e);
            return null;
        }
    }

    public GlobalRiskOverview getGlobalOverview() {
        return GlobalRiskOverview.builder()
                .globalRiskIndex(71)
                .activeDisasters(23)
                .countriesAtHighRisk(36)
                .alertsActive(8)
                .topRiskCountries(getTopRiskCountries())
                .timestamp(Instant.now())
                .build();
    }

    private List<CountryRiskSummary> getTopRiskCountries() {
        return List.of(
                CountryRiskSummary.builder().countryCode("SO").countryName("Somalia").overallScore(94).delta(2).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("YE").countryName("Yemen").overallScore(91).delta(1).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("SS").countryName("South Sudan").overallScore(89).delta(0).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("HT").countryName("Haiti").overallScore(87).delta(3).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("AF").countryName("Afghanistan").overallScore(83).delta(1).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("PK").countryName("Pakistan").overallScore(79).delta(4).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("BD").countryName("Bangladesh").overallScore(76).delta(0).riskLevel("CRITICAL").calculatedAt(Instant.now()).build(),
                CountryRiskSummary.builder().countryCode("ET").countryName("Ethiopia").overallScore(73).delta(-1).riskLevel("HIGH").calculatedAt(Instant.now()).build()
        );
    }
}
