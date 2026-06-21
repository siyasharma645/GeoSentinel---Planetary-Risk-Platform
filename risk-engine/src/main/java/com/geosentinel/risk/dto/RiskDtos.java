package com.geosentinel.risk.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class RiskDtos {

    @Data
    public static class LocationRequest {
        private String name;
        private String country;
        private String countryCode;
        private Double lat;
        private Double lon;
        private boolean forceRefresh = false;
    }

    @Data @Builder
    public static class RiskReport {
        private String locationName;
        private String countryCode;
        private Double lat;
        private Double lon;
        private Integer overallRisk;
        private String riskLevel;
        private String summary;
        private Map<String, CategoryScore> categories;
        private List<ActiveThreat> activeThreats;
        private List<KeyMetric> keyMetrics;
        private Forecast forecast;
        private List<String> recommendations;
        private Instant generatedAt;
        private boolean fromCache;
    }

    @Data @Builder
    public static class CategoryScore {
        private Integer score;
        private String label;
        private String detail;
    }

    @Data @Builder
    public static class ActiveThreat {
        private String type;
        private String severity;
        private String description;
    }

    @Data @Builder
    public static class KeyMetric {
        private String label;
        private String value;
        private String trend;
    }

    @Data @Builder
    public static class Forecast {
        private String thirtyDay;
        private String ninetyDay;
        private Integer probability;
    }

    @Data @Builder
    public static class CountryRiskSummary {
        private String countryCode;
        private String countryName;
        private Integer overallScore;
        private Integer delta;
        private String riskLevel;
        private Instant calculatedAt;
    }

    @Data @Builder
    public static class GlobalRiskOverview {
        private Integer globalRiskIndex;
        private Integer activeDisasters;
        private Integer countriesAtHighRisk;
        private Integer alertsActive;
        private List<CountryRiskSummary> topRiskCountries;
        private Instant timestamp;
    }
}
