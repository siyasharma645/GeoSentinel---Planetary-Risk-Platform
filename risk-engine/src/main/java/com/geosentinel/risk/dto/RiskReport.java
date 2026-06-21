package com.geosentinel.risk.dto;
import lombok.*; import java.time.Instant; import java.util.*;
@Data @Builder public class RiskReport {
    private String locationName, country, countryCode;
    private Double lat, lon;
    private Integer overallRisk;
    private String riskLevel, summary;
    private Map<String,CategoryScore> categories;
    private List<ActiveThreat> activeThreats;
    private List<KeyMetric> keyMetrics;
    private Forecast forecast;
    private List<String> recommendations;
    private Instant generatedAt;
    private boolean fromCache;
    @Data @Builder public static class CategoryScore { private Integer score; private String label, detail; }
    @Data @Builder public static class ActiveThreat  { private String type, severity, description; }
    @Data @Builder public static class KeyMetric     { private String label, value, trend; }
    @Data @Builder public static class Forecast      { private String thirtyDay, ninetyDay; private Integer probability; }
}
