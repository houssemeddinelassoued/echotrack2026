package com.ecotrack.carbonengine.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all domain entities identified by a unique identifier.
 */
public abstract class BaseEntity {

    private final UUID id;

    /**
     * Creates an entity with its unique identifier.
     *
     * @param id the entity identifier.
     */
    protected BaseEntity(final UUID id) {
        this.id = Objects.requireNonNull(id, "Entity identifier is required");
    }

    /**
     * Returns the entity identifier.
     *
     * @return the unique identifier.
     */
    public UUID getId() {
        return id;
    }
}