package com.ecotrack.carbonengine.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable record representing a parsed server log entry.
 * <p>
 * Contains the essential fields extracted from raw server logs for carbon footprint
 * calculations: timestamp, server name, and CPU load percentage.
 * </p>
 *
 * @param timestamp  the date and time when the log was recorded (ISO-8601 format)
 * @param serverName the unique identifier of the server (e.g., "SRV-01")
 * @param cpuLoad    the CPU load percentage as a numeric value (0.0 to 100.0)
 *
 * @author EcoTrack Team
 * @since 1.0.0
 */
public record ServerLogEntry(
        LocalDateTime timestamp,
        String serverName,
        double cpuLoad
) {
    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException     if timestamp or serverName is null
     * @throws IllegalArgumentException if serverName is blank or cpuLoad is out of range
     */
    public ServerLogEntry {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(serverName, "serverName must not be null");

        if (serverName.isBlank()) {
            throw new IllegalArgumentException("serverName must not be blank");
        }

        if (cpuLoad < 0.0 || cpuLoad > 100.0) {
            throw new IllegalArgumentException(
                    "cpuLoad must be between 0 and 100, got: " + cpuLoad
            );
        }
    }

    /**
     * Returns a human-readable string representation of this log entry.
     *
     * @return formatted string with timestamp, server name, and CPU load
     */
    @Override
    public String toString() {
        return String.format("ServerLogEntry[%s | %s | CPU: %.1f%%]",
                timestamp, serverName, cpuLoad);
    }
}
