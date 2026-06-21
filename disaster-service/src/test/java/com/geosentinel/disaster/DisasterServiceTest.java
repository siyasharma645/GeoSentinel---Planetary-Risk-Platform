package com.geosentinel.disaster;
import com.geosentinel.disaster.kafka.DisasterEventProducer;
import com.geosentinel.disaster.model.*; import com.geosentinel.disaster.repository.DisasterRepository;
import com.geosentinel.disaster.service.DisasterService;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal; import java.util.*; import java.util.UUID;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class DisasterServiceTest {
    @Mock DisasterRepository repo; @Mock DisasterEventProducer producer; @InjectMocks DisasterService svc;
    @Test @DisplayName("Create sets ACTIVE and publishes") void createOk(){
        Disaster d=Disaster.builder().type("FLOOD").locationName("City").build();
        when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        assertThat(svc.create(d).getStatus()).isEqualTo(DisasterStatus.ACTIVE);
        verify(producer).created(any());
    }
    @Test @DisplayName("Severity +1.5 triggers escalation") void escalation(){
        UUID id=UUID.randomUUID();
        Disaster d=Disaster.builder().id(id).type("TYPHOON").severity(new BigDecimal("6.0")).status(DisasterStatus.ACTIVE).build();
        when(repo.findById(id)).thenReturn(Optional.of(d)); when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        assertThat(svc.updateSeverity(id,new BigDecimal("7.6")).getStatus()).isEqualTo(DisasterStatus.ESCALATING);
        verify(producer).escalated(any());
    }
}
