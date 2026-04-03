package com.ecotrack.carbonengine.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for entities that must remain scoped to a single tenant.
 */
public abstract class TenantScopedEntity extends BaseEntity {

    private final UUID tenantId;

    /**
     * Creates a tenant-scoped entity.
     *
     * @param id the entity identifier.
     * @param tenantId the tenant identifier.
     */
    protected TenantScopedEntity(final UUID id, final UUID tenantId) {
        super(id);
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant identifier is required");
    }

    /**
     * Returns the tenant identifier.
     *
     * @return the tenant identifier.
     */
    public UUID getTenantId() {
        return tenantId;
    }
}