package com.geosentinel.disaster.controller;
import com.geosentinel.disaster.model.*; import com.geosentinel.disaster.service.DisasterService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag; import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*; import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1/disasters") @RequiredArgsConstructor
@Tag(name="Disasters",description="Disaster monitoring") @SecurityRequirement(name="bearerAuth")
public class DisasterController {
    private final DisasterService svc;
    @GetMapping("/active")          @Operation(summary="Active disasters") public ResponseEntity<List<Disaster>> active(){return ResponseEntity.ok(svc.getActive());}
    @GetMapping("/{id}")            @Operation(summary="By ID")            public ResponseEntity<Disaster> byId(@PathVariable UUID id){return ResponseEntity.ok(svc.getById(id));}
    @GetMapping("/country/{code}")  @Operation(summary="By country")       public ResponseEntity<Page<Disaster>> byCountry(@PathVariable String code,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ResponseEntity.ok(svc.byCountry(code,PageRequest.of(page,size)));}
    @GetMapping("/type/{type}")     @Operation(summary="By type")          public ResponseEntity<Page<Disaster>> byType(@PathVariable String type,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ResponseEntity.ok(svc.byType(type,PageRequest.of(page,size)));}
    @GetMapping("/near")            @Operation(summary="Near location")     public ResponseEntity<List<Disaster>> near(@RequestParam double lat,@RequestParam double lon,@RequestParam(defaultValue="500")double km){return ResponseEntity.ok(svc.near(lat,lon,km));}
    @PostMapping                    @Operation(summary="Create disaster")   public ResponseEntity<Disaster> create(@RequestBody Disaster d){return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(d));}
    @PatchMapping("/{id}/status")   @Operation(summary="Update status")    public ResponseEntity<Disaster> status(@PathVariable UUID id,@RequestBody Map<String,String> b){return ResponseEntity.ok(svc.updateStatus(id,DisasterStatus.valueOf(b.get("status").toUpperCase())));}
    @PatchMapping("/{id}/severity") @Operation(summary="Update severity")  public ResponseEntity<Disaster> severity(@PathVariable UUID id,@RequestBody Map<String,Number> b){return ResponseEntity.ok(svc.updateSeverity(id,new BigDecimal(b.get("severity").toString())));}
    @GetMapping("/health") public ResponseEntity<Map<String,String>> health(){return ResponseEntity.ok(Map.of("status","UP","service","disaster-service"));}
}
