package com.geosentinel.risk.dto;
import lombok.Data;
@Data public class LocationRequest {
    private String name, country, countryCode;
    private Double lat, lon;
    private boolean forceRefresh = false;
}
