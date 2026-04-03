package com.ecotrack.carbonengine.application.service;

import com.ecotrack.carbonengine.domain.exception.InvalidLogFormatException;
import com.ecotrack.carbonengine.domain.model.ServerLogEntry;
import com.ecotrack.carbonengine.infrastructure.parser.ServerLogParser;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing server log entries.
 *
 * <p>Provides operations to store, retrieve, and parse server logs
 * with multi-tenant data isolation.</p>
 */
@Service
public class ServerLogService {

    private final Map<Long, TenantScopedLog> logStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Retrieves a server log entry by its unique identifier.
     *
     * @param id       the unique identifier of the log entry
     * @param tenantId the tenant identifier for data isolation
     * @return an Optional containing the log entry if found and belongs to the tenant,
     *         or empty if not found or belongs to another tenant
     */
    public Optional<ServerLogEntry> findById(Long id, Long tenantId) {
        TenantScopedLog scopedLog = logStorage.get(id);
        if (scopedLog == null || !scopedLog.tenantId().equals(tenantId)) {
            return Optional.empty();
        }
        return Optional.of(scopedLog.entry());
    }

    /**
     * Parses a raw log string and stores it with tenant association.
     *
     * @param logString the raw log string to parse
     * @param tenantId  the tenant identifier for data isolation
     * @return the generated unique identifier for the stored log entry
     * @throws InvalidLogFormatException if the log string format is invalid
     */
    public Long parseAndStore(String logString, Long tenantId) {
        ServerLogEntry entry = ServerLogParser.parseServerLog(logString);
        Long id = idGenerator.getAndIncrement();
        logStorage.put(id, new TenantScopedLog(tenantId, entry));
        return id;
    }

    /**
     * Internal record for storing log entries with tenant association.
     */
    private record TenantScopedLog(Long tenantId, ServerLogEntry entry) {}
}
