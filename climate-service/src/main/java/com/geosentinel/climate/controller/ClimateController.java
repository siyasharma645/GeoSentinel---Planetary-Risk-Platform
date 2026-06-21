package com.geosentinel.climate.controller;
import com.geosentinel.climate.model.ClimateMetric; import com.geosentinel.climate.service.ClimateService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag; import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*; import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*; import java.util.Map;
@RestController @RequestMapping("/api/v1/climate") @RequiredArgsConstructor
@Tag(name="Climate",description="Climate metrics and anomalies") @SecurityRequirement(name="bearerAuth")
public class ClimateController {
    private final ClimateService svc;
    @GetMapping("/metrics/{type}")  @Operation(summary="Metrics by type")    public ResponseEntity<Page<ClimateMetric>> byType(@PathVariable String type,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size){return ResponseEntity.ok(svc.byType(type,PageRequest.of(page,size)));}
    @GetMapping("/country/{code}")  @Operation(summary="Metrics by country") public ResponseEntity<List<ClimateMetric>> byCountry(@PathVariable String code){return ResponseEntity.ok(svc.byCountry(code));}
    @GetMapping("/anomalies")       @Operation(summary="Significant anomalies") public ResponseEntity<List<ClimateMetric>> anomalies(@RequestParam(defaultValue="1.5")double threshold){return ResponseEntity.ok(svc.anomalies(threshold));}
    @PostMapping("/metrics")        @Operation(summary="Record metric")      public ResponseEntity<ClimateMetric> record(@RequestBody ClimateMetric m){return ResponseEntity.ok(svc.record(m));}
    @GetMapping("/summary")         @Operation(summary="Global summary")     public ResponseEntity<Map<String,Object>> summary(){return ResponseEntity.ok(Map.of("tempAnomaly","+1.4C","co2Ppm",426.3,"seaLevelRiseMmPerYear",3.9,"arcticIceExtentKm2",3200000,"deforestationHaPerYear",4700000));}
    @GetMapping("/health") public ResponseEntity<Map<String,String>> health(){return ResponseEntity.ok(Map.of("status","UP","service","climate-service"));}
}
