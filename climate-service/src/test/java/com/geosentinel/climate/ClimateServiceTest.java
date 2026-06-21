package com.geosentinel.climate;
import com.geosentinel.climate.model.ClimateMetric; import com.geosentinel.climate.repository.ClimateMetricRepository;
import com.geosentinel.climate.service.ClimateService;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class ClimateServiceTest {
    @Mock ClimateMetricRepository repo; @InjectMocks ClimateService svc;
    @Test @DisplayName("Record metric saves and returns") void record(){
        ClimateMetric m=ClimateMetric.builder().metricType("TEMPERATURE").value(new BigDecimal("28.5")).unit("C").build();
        when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        ClimateMetric result=svc.record(m);
        assertThat(result.getRecordedAt()).isNotNull();
        verify(repo).save(any());
    }
}
