package com.ecotrack.carbonengine.domain.model;

import com.ecotrack.carbonengine.domain.exception.CalculationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the result of a carbon footprint calculation for a tenant asset.
 */
public final class CarbonFootprint extends TenantScopedEntity {

    private final UUID assetId;
    private final int usageDurationDays;
    private final BigDecimal energyConsumedKwh;
    private final BigDecimal carbonEmissionKgCo2e;
    private final Instant calculatedAt;

    /**
     * Creates a carbon footprint result.
     *
     * @param id the result identifier.
     * @param tenantId the tenant identifier.
     * @param assetId the related asset identifier.
     * @param usageDurationDays the calculation duration in days.
     * @param energyConsumedKwh the consumed energy over the duration.
     * @param carbonEmissionKgCo2e the calculated emissions.
     * @param calculatedAt the calculation timestamp.
     */
    public CarbonFootprint(
            final UUID id,
            final UUID tenantId,
            final UUID assetId,
            final int usageDurationDays,
            final BigDecimal energyConsumedKwh,
            final BigDecimal carbonEmissionKgCo2e,
            final Instant calculatedAt
    ) {
        super(id, tenantId);
        this.assetId = Objects.requireNonNull(assetId, "Asset identifier is required");
        this.usageDurationDays = validateDuration(usageDurationDays);
        this.energyConsumedKwh = validateNonNegative(energyConsumedKwh, "Energy consumed");
        this.carbonEmissionKgCo2e = validateNonNegative(carbonEmissionKgCo2e, "Carbon emission");
        this.calculatedAt = Objects.requireNonNull(calculatedAt, "Calculation timestamp is required");
    }

    /**
     * Returns the asset identifier associated with the result.
     *
     * @return the asset identifier.
     */
    public UUID getAssetId() {
        return assetId;
    }

    /**
     * Returns the duration used for the calculation in days.
     *
     * @return the usage duration in days.
     */
    public int getUsageDurationDays() {
        return usageDurationDays;
    }

    /**
     * Returns the consumed energy over the calculation period.
     *
     * @return the consumed energy in kWh.
     */
    public BigDecimal getEnergyConsumedKwh() {
        return energyConsumedKwh;
    }

    /**
     * Returns the carbon emissions for the calculation period.
     *
     * @return the carbon emissions in kgCO2e.
     */
    public BigDecimal getCarbonEmissionKgCo2e() {
        return carbonEmissionKgCo2e;
    }

    /**
     * Returns the timestamp of the calculation.
     *
     * @return the calculation timestamp.
     */
    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    private int validateDuration(final int value) {
        if (value <= 0) {
            throw new CalculationException("Usage duration must be strictly positive");
        }
        return value;
    }

    private BigDecimal validateNonNegative(final BigDecimal value, final String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() < 0) {
            throw new CalculationException(fieldName + " cannot be negative");
        }
        return value;
    }
}