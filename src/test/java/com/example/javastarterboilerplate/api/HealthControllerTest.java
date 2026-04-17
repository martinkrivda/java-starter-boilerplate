package com.example.javastarterboilerplate.api;

import com.example.javastarterboilerplate.api.controller.HealthController;
import com.example.javastarterboilerplate.api.dto.ApplicationComponentStatusResponse;
import com.example.javastarterboilerplate.api.dto.ApplicationInfoResponse;
import com.example.javastarterboilerplate.api.dto.HealthFullResponse;
import com.example.javastarterboilerplate.api.dto.HealthReadinessResponse;
import com.example.javastarterboilerplate.application.ApplicationInfoService;
import com.example.javastarterboilerplate.application.ApplicationShutdownState;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-17T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void reportsReadyBeforeShutdownStarts() {
        HealthController controller = new HealthController(applicationInfoService(), new ApplicationShutdownState(),
                clock);

        HealthReadinessResponse response = controller.ready();

        assertThat(response.status()).isEqualTo("ready");
        assertThat(response.checks()).extracting("name").doesNotContain("shutdown");
    }

    @Test
    void keepsReadinessReadyWhenOptionalIntegrationIsDisabled() {
        ApplicationInfoService service = Mockito.mock(ApplicationInfoService.class);
        Mockito.when(service.getInfo()).thenReturn(new ApplicationInfoResponse("starter", "1.0.0", "desc", "none",
                List.of("test"), List.of(new ApplicationComponentStatusResponse("storage", false, "disabled"),
                        new ApplicationComponentStatusResponse("pdfbox", true, "ready"),
                        new ApplicationComponentStatusResponse("dss", true, "ready"))));
        HealthController controller = new HealthController(service, new ApplicationShutdownState(), clock);

        HealthReadinessResponse response = controller.ready();

        assertThat(response.status()).isEqualTo("ready");
        assertThat(response.checks()).anySatisfy(check -> {
            assertThat(check.name()).isEqualTo("storage");
            assertThat(check.status()).isEqualTo("DOWN");
            assertThat(check.details()).containsEntry("enabled", false);
        });
    }

    @Test
    void reportsNotReadyAndAcceptedDrainDuringShutdown() {
        ApplicationShutdownState shutdownState = new ApplicationShutdownState();
        HealthController controller = new HealthController(applicationInfoService(), shutdownState, clock);

        var response = controller.drain();

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.ACCEPTED.getCode());
        assertThat(shutdownState.isShuttingDown()).isTrue();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().status()).isEqualTo("not_ready");
        assertThat(response.body().checks()).extracting("name").contains("shutdown");
    }

    @Test
    void marksFullHealthDownWhenApplicationIsDraining() {
        ApplicationShutdownState shutdownState = new ApplicationShutdownState();
        HealthController controller = new HealthController(applicationInfoService(), shutdownState, clock);

        shutdownState.beginShutdown();
        HealthFullResponse response = controller.health();

        assertThat(response.status()).isEqualTo("DOWN");
        assertThat(response.checks()).extracting("name").contains("shutdown");
    }

    private ApplicationInfoService applicationInfoService() {
        ApplicationInfoService service = Mockito.mock(ApplicationInfoService.class);
        Mockito.when(service.getInfo()).thenReturn(new ApplicationInfoResponse("starter", "1.0.0", "desc", "none",
                List.of("test"), List.of(new ApplicationComponentStatusResponse("storage", true, "ready"),
                        new ApplicationComponentStatusResponse("pdfbox", true, "ready"),
                        new ApplicationComponentStatusResponse("dss", true, "ready"))));
        return service;
    }
}
