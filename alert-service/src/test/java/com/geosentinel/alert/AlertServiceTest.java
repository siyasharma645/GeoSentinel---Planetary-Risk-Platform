package com.geosentinel.alert;
import com.geosentinel.alert.model.*; import com.geosentinel.alert.repository.AlertRepository;
import com.geosentinel.alert.service.AlertService;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class AlertServiceTest {
    @Mock AlertRepository repo; @InjectMocks AlertService svc;
    @Test @DisplayName("Create alert persists") void createOk(){
        Alert a=Alert.builder().alertType(AlertType.DISASTER).level(AlertLevel.HIGH).title("Test").message("Msg").build();
        when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        assertThat(svc.create(a)).isNotNull(); verify(repo).save(any());
    }
    @Test @DisplayName("Resolve changes status") void resolveOk(){
        UUID id=UUID.randomUUID();
        Alert a=Alert.builder().id(id).status("ACTIVE").alertType(AlertType.DISASTER).level(AlertLevel.HIGH).title("T").message("M").build();
        when(repo.findById(id)).thenReturn(Optional.of(a)); when(repo.save(any())).thenAnswer(i->i.getArgument(0));
        assertThat(svc.resolve(id).getStatus()).isEqualTo("RESOLVED");
    }
}
