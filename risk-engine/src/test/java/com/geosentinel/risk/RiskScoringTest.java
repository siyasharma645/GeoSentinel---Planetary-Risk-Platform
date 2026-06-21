package com.geosentinel.risk;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geosentinel.risk.dto.*; import com.geosentinel.risk.repository.RiskReportRepository;
import com.geosentinel.risk.service.RiskScoringService;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate; import org.springframework.data.redis.core.ValueOperations;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class RiskScoringTest {
    @Mock RiskReportRepository repo; @Mock StringRedisTemplate redis; @Mock ValueOperations<String,String> vops;
    @Spy ObjectMapper mapper;
    @InjectMocks RiskScoringService svc;
    @BeforeEach void setup(){when(redis.opsForValue()).thenReturn(vops);when(vops.get(any())).thenReturn(null);when(repo.findValidByCoords(any(),any(),any())).thenReturn(Optional.empty());when(repo.save(any())).thenReturn(null);}
    @Test @DisplayName("Somalia is CRITICAL") void somalia(){LocationRequest r=new LocationRequest();r.setName("Somalia");r.setCountryCode("SO");r.setLat(5.15);r.setLon(46.20);RiskReport rpt=svc.getReport(r);assertThat(rpt.getOverallRisk()).isGreaterThanOrEqualTo(75);assertThat(rpt.getRiskLevel()).isEqualTo("CRITICAL");}
    @Test @DisplayName("Norway is LOW") void norway(){LocationRequest r=new LocationRequest();r.setName("Norway");r.setCountryCode("NO");r.setLat(60.47);r.setLon(8.47);assertThat(svc.getReport(r).getOverallRisk()).isLessThan(55);}
    @Test @DisplayName("Report has 6 categories") void cats(){LocationRequest r=new LocationRequest();r.setName("BD");r.setCountryCode("BD");r.setLat(23.68);r.setLon(90.35);RiskReport rpt=svc.getReport(r);assertThat(rpt.getCategories()).containsKeys("climate","disaster","water","food","health","conflict");assertThat(rpt.getKeyMetrics()).hasSize(5);}
}
