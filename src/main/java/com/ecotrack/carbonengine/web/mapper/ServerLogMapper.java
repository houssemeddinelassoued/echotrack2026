package com.ecotrack.carbonengine.web.mapper;

import com.ecotrack.carbonengine.domain.model.ServerLogEntry;
import com.ecotrack.carbonengine.web.dto.ServerLogResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between ServerLogEntry domain model and API DTOs.
 *
 * <p>This component handles all conversions between the internal domain
 * representation and the external API contracts.</p>
 */
@Component
public class ServerLogMapper {

    /**
     * Converts a domain entity to its API response representation.
     *
     * @param id    the unique identifier of the log entry
     * @param entry the domain entity to convert
     * @return the API response DTO
     * @throws IllegalArgumentException if entry is null
     */
    public ServerLogResponse toResponse(Long id, ServerLogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        return new ServerLogResponse(
                id,
                entry.timestamp(),
                entry.serverName(),
                entry.cpuLoad()
        );
    }
}
