package com.ecotrack.carbonengine.web.controller;

import com.ecotrack.carbonengine.application.service.ServerLogService;
import com.ecotrack.carbonengine.web.dto.ServerLogResponse;
import com.ecotrack.carbonengine.web.mapper.ServerLogMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing server log entries.
 *
 * <p>Provides endpoints to retrieve and manage server logs
 * with multi-tenant data isolation.</p>
 *
 * @see ServerLogService
 */
@RestController
@RequestMapping("/api/v1/server-logs")
public class ServerLogController {

    private final ServerLogService serverLogService;
    private final ServerLogMapper serverLogMapper;

    /**
     * Constructs the controller with required dependencies.
     *
     * @param serverLogService the service for server log operations
     * @param serverLogMapper  the mapper for DTO conversions
     */
    public ServerLogController(ServerLogService serverLogService,
                               ServerLogMapper serverLogMapper) {
        this.serverLogService = serverLogService;
        this.serverLogMapper = serverLogMapper;
    }

    /**
     * Retrieves a server log entry by its unique identifier.
     *
     * @param id       the unique identifier of the log entry
     * @param tenantId the tenant identifier from the security context
     * @return the server log response with HTTP 200, or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServerLogResponse> getById(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId) {

        return serverLogService.findById(id, tenantId)
                .map(entry -> serverLogMapper.toResponse(id, entry))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
