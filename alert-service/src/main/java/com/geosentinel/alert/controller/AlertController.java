package com.geosentinel.alert.controller;
import com.geosentinel.alert.model.Alert; import com.geosentinel.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag; import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*; import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1/alerts") @RequiredArgsConstructor
@Tag(name="Alerts",description="Real-time alert system") @SecurityRequirement(name="bearerAuth")
public class AlertController {
    private final AlertService svc;
    @GetMapping              @Operation(summary="Active alerts (paginated)") public ResponseEntity<Page<Alert>> active(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ResponseEntity.ok(svc.getActive(PageRequest.of(page,size,Sort.by("createdAt").descending())));}
    @GetMapping("/critical") @Operation(summary="Critical and high alerts")  public ResponseEntity<List<Alert>> critical(){return ResponseEntity.ok(svc.getCriticalAndHigh());}
    @GetMapping("/country/{code}") @Operation(summary="Alerts by country")  public ResponseEntity<List<Alert>> byCountry(@PathVariable String code){return ResponseEntity.ok(svc.byCountry(code.toUpperCase()));}
    @GetMapping("/count")    @Operation(summary="Active alert count")        public ResponseEntity<Map<String,Long>> count(){return ResponseEntity.ok(Map.of("active",svc.countActive()));}
    @PostMapping             @Operation(summary="Create manual alert")       public ResponseEntity<Alert> create(@RequestBody Alert a){return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(a));}
    @PatchMapping("/{id}/resolve") @Operation(summary="Resolve alert")      public ResponseEntity<Alert> resolve(@PathVariable UUID id){return ResponseEntity.ok(svc.resolve(id));}
    @GetMapping("/health") public ResponseEntity<Map<String,String>> health(){return ResponseEntity.ok(Map.of("status","UP","service","alert-service"));}
}
