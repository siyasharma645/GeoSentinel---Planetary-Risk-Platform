package com.geosentinel.risk.controller;
import com.geosentinel.risk.dto.*; import com.geosentinel.risk.service.RiskScoringService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag; import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/risk") @RequiredArgsConstructor
@Tag(name="Risk Engine",description="Location risk intelligence") @SecurityRequirement(name="bearerAuth")
public class RiskController {
    private final RiskScoringService svc;
    @PostMapping("/report")         @Operation(summary="Get risk report for location") public ResponseEntity<RiskReport> report(@RequestBody LocationRequest r) { return ResponseEntity.ok(svc.getReport(r)); }
    @PostMapping("/report/refresh") @Operation(summary="Force-refresh risk report")   public ResponseEntity<RiskReport> refresh(@RequestBody LocationRequest r) { r.setForceRefresh(true); return ResponseEntity.ok(svc.getReport(r)); }
    @GetMapping("/overview")        @Operation(summary="Global risk overview")         public ResponseEntity<Map<String,Object>> overview() { return ResponseEntity.ok(svc.globalOverview()); }
    @GetMapping("/health") public ResponseEntity<Map<String,String>> health() { return ResponseEntity.ok(Map.of("status","UP","service","risk-engine")); }
}
