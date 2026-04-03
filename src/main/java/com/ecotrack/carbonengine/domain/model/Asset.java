package com.ecotrack.carbonengine.domain.model;

import com.ecotrack.carbonengine.domain.enums.AssetType;
import com.ecotrack.carbonengine.domain.exception.InvalidAssetDataException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a tenant-owned IT asset whose carbon footprint can be calculated.
 */
public final class Asset extends TenantScopedEntity {

    private static final BigDecimal WATTS_PER_KILOWATT = BigDecimal.valueOf(1000);
    private static final int KWH_SCALE = 6;

    private final AssetType assetType;
    private final String name;
    private final LocalDate commissioningDate;
    private final BigDecimal powerConsumptionWatts;

    /**
     * Creates an asset.
     *
     * @param id the asset identifier.
     * @param tenantId the tenant identifier.
     * @param assetType the asset category.
     * @param name the asset display name.
     * @param commissioningDate the date when the asset started being used.
     * @param powerConsumptionWatts the nominal power consumption in watts.
     * @throws InvalidAssetDataException when required values are missing or invalid.
     */
    public Asset(
            final UUID id,
            final UUID tenantId,
            final AssetType assetType,
            final String name,
            final LocalDate commissioningDate,
            final BigDecimal powerConsumptionWatts
    ) {
        super(id, tenantId);
        this.assetType = Objects.requireNonNull(assetType, "Asset type is required");
        this.name = validateName(name);
        this.commissioningDate = Objects.requireNonNull(
                commissioningDate,
                "Commissioning date is required"
        );
        this.powerConsumptionWatts = validatePositive(powerConsumptionWatts, "Power consumption");
    }

    /**
     * Returns the asset category.
     *
     * @return the asset type.
     */
    public AssetType getAssetType() {
        return assetType;
    }

    /**
     * Returns the asset name.
     *
     * @return the display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the date when the asset started being used.
     *
     * @return the commissioning date.
     */
    public LocalDate getCommissioningDate() {
        return commissioningDate;
    }

    /**
     * Returns the nominal power consumption of the asset in watts.
     *
     * @return the power consumption in watts.
     */
    public BigDecimal getPowerConsumptionWatts() {
        return powerConsumptionWatts;
    }

    /**
     * Calculates the daily energy consumption in kilowatt-hour based on nominal power.
     *
     * @return the daily energy consumption in kWh.
     */
    public BigDecimal calculateDailyEnergyConsumptionKwh() {
        return powerConsumptionWatts
                .multiply(BigDecimal.valueOf(24))
                .divide(WATTS_PER_KILOWATT, KWH_SCALE, RoundingMode.HALF_UP);
    }

    private String validateName(final String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new InvalidAssetDataException("Asset name is required");
        }
        return candidate;
    }

    private BigDecimal validatePositive(final BigDecimal value, final String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() <= 0) {
            throw new InvalidAssetDataException(fieldName + " must be strictly positive");
        }
        return value;
    }
}