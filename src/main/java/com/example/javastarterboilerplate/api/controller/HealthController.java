package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.api.dto.HealthCheckResponse;
import com.example.javastarterboilerplate.api.dto.HealthFullResponse;
import com.example.javastarterboilerplate.api.dto.HealthLivenessResponse;
import com.example.javastarterboilerplate.api.dto.HealthReadinessResponse;
import com.example.javastarterboilerplate.application.ApplicationShutdownState;
import com.example.javastarterboilerplate.application.ApplicationInfoService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Health")
@Controller("/health")
public class HealthController {

    private final ApplicationInfoService applicationInfoService;
    private final ApplicationShutdownState applicationShutdownState;
    private final Clock clock;
    private final long startedAtEpochSeconds;

    public HealthController(ApplicationInfoService applicationInfoService,
            ApplicationShutdownState applicationShutdownState, Clock clock) {
        this.applicationInfoService = applicationInfoService;
        this.applicationShutdownState = applicationShutdownState;
        this.clock = clock;
        this.startedAtEpochSeconds = clock.instant().getEpochSecond();
    }

    @Get
    @Operation(summary = "Full health check", description = "Returns service status, version, uptime and dependency checks.")
    public HealthFullResponse health() {
        List<HealthCheckResponse> checks = checks();
        return new HealthFullResponse(applicationShutdownState.isShuttingDown() ? "DOWN" : "UP",
                applicationInfoService.getInfo().version(), uptimeSeconds(), checks);
    }

    @Get("/live")
    @Operation(summary = "Liveness probe", description = "Returns a basic liveness status used by orchestration systems.")
    public HealthLivenessResponse live() {
        return new HealthLivenessResponse("UP");
    }

    @Get("/ready")
    @Operation(summary = "Readiness probe", description = "Returns readiness status and dependency checks.")
    public HealthReadinessResponse ready() {
        List<HealthCheckResponse> checks = checks();
        boolean allUp = checks.stream().noneMatch(this::isBlockingFailure);
        return new HealthReadinessResponse(allUp ? "ready" : "not_ready", checks);
    }

    @Hidden
    @Post("/drain")
    public HttpResponse<HealthReadinessResponse> drain() {
        applicationShutdownState.beginShutdown();
        return HttpResponse.status(HttpStatus.ACCEPTED).body(ready());
    }

    private long uptimeSeconds() {
        return Math.max(0, clock.instant().getEpochSecond() - startedAtEpochSeconds);
    }

    private List<HealthCheckResponse> checks() {
        List<HealthCheckResponse> checks = new ArrayList<>();
        applicationInfoService.getInfo().integrations()
                .forEach(integration -> checks
                        .add(new HealthCheckResponse(integration.component(), integration.enabled() ? "UP" : "DOWN", 1L,
                                integration.detail(), Map.of("enabled", integration.enabled()))));
        if (applicationShutdownState.isShuttingDown()) {
            checks.add(new HealthCheckResponse("shutdown", "DOWN", 0L,
                    "Application is draining and no longer ready to receive traffic.",
                    Map.of("shuttingDown", true)));
        }
        return checks;
    }

    private boolean isBlockingFailure(HealthCheckResponse check) {
        if (!"DOWN".equals(check.status())) {
            return false;
        }
        Object enabled = check.details().get("enabled");
        return !(enabled instanceof Boolean booleanValue) || booleanValue;
    }
}
