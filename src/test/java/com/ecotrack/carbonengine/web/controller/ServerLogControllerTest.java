package com.ecotrack.carbonengine.web.controller;

import com.ecotrack.carbonengine.application.service.ServerLogService;
import com.ecotrack.carbonengine.domain.model.ServerLogEntry;
import com.ecotrack.carbonengine.web.dto.ServerLogResponse;
import com.ecotrack.carbonengine.web.mapper.ServerLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ServerLogController}.
 *
 * <p>Tests cover nominal cases, tenant isolation, and error scenarios
 * as per the spring-boot-controller skill checklist.</p>
 */
@ExtendWith(MockitoExtension.class)
class ServerLogControllerTest {

    private static final Long LOG_ID = 1L;
    private static final Long TENANT_ID = 100L;
    private static final Long OTHER_TENANT_ID = 999L;

    @Mock
    private ServerLogService serverLogService;

    @Mock
    private ServerLogMapper serverLogMapper;

    @InjectMocks
    private ServerLogController controller;

    private ServerLogEntry testEntry;
    private ServerLogResponse testResponse;

    @BeforeEach
    void setUp() {
        testEntry = new ServerLogEntry(
                LocalDateTime.of(2026, 4, 3, 14, 30, 0),
                "SRV-PROD-01",
                45.5
        );
        testResponse = new ServerLogResponse(
                LOG_ID,
                testEntry.timestamp(),
                testEntry.serverName(),
                testEntry.cpuLoad()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/server-logs/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 with log entry when found for tenant")
        void getById_whenLogExistsForTenant_shouldReturn200WithResponse() {
            // Given
            when(serverLogService.findById(LOG_ID, TENANT_ID))
                    .thenReturn(Optional.of(testEntry));
            when(serverLogMapper.toResponse(LOG_ID, testEntry))
                    .thenReturn(testResponse);

            // When
            ResponseEntity<ServerLogResponse> response = controller.getById(LOG_ID, TENANT_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(LOG_ID);
            assertThat(response.getBody().serverName()).isEqualTo("SRV-PROD-01");
            assertThat(response.getBody().cpuLoad()).isEqualTo(45.5);

            verify(serverLogService).findById(LOG_ID, TENANT_ID);
            verify(serverLogMapper).toResponse(LOG_ID, testEntry);
        }

        @Test
        @DisplayName("should return 404 when log not found")
        void getById_whenLogNotFound_shouldReturn404() {
            // Given
            when(serverLogService.findById(LOG_ID, TENANT_ID))
                    .thenReturn(Optional.empty());

            // When
            ResponseEntity<ServerLogResponse> response = controller.getById(LOG_ID, TENANT_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();

            verify(serverLogService).findById(LOG_ID, TENANT_ID);
            verify(serverLogMapper, never()).toResponse(any(), any());
        }

        @Test
        @DisplayName("should return 404 when log belongs to another tenant (tenant isolation)")
        void getById_whenLogBelongsToOtherTenant_shouldReturn404() {
            // Given - service returns empty when tenant doesn't match
            when(serverLogService.findById(LOG_ID, OTHER_TENANT_ID))
                    .thenReturn(Optional.empty());

            // When
            ResponseEntity<ServerLogResponse> response = controller.getById(LOG_ID, OTHER_TENANT_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();

            verify(serverLogService).findById(LOG_ID, OTHER_TENANT_ID);
        }

        @Test
        @DisplayName("should pass correct parameters to service")
        void getById_shouldPassCorrectParametersToService() {
            // Given
            Long specificId = 42L;
            Long specificTenantId = 123L;
            when(serverLogService.findById(specificId, specificTenantId))
                    .thenReturn(Optional.empty());

            // When
            controller.getById(specificId, specificTenantId);

            // Then
            verify(serverLogService).findById(eq(42L), eq(123L));
        }
    }
}
