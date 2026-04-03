package com.ecotrack.carbonengine.web.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for server log entry API operations.
 *
 * @param id         the unique identifier of the log entry
 * @param timestamp  the date and time when the log was recorded
 * @param serverName the name of the server that generated the log
 * @param cpuLoad    the CPU load percentage at the time of logging (0-100)
 */
public record ServerLogResponse(
        Long id,
        LocalDateTime timestamp,
        String serverName,
        double cpuLoad
) {}
