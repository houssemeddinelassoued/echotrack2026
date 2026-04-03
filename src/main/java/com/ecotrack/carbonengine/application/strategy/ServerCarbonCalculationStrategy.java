package com.ecotrack.carbonengine.application.strategy;

import com.ecotrack.carbonengine.domain.enums.AssetType;
import com.ecotrack.carbonengine.domain.exception.CalculationException;
import com.ecotrack.carbonengine.domain.model.Asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Energy computation strategy for server assets.
 */
public final class ServerCarbonCalculationStrategy implements CarbonCalculationStrategy {

    private static final BigDecimal UTILIZATION_RATE = BigDecimal.valueOf(0.85);
    private static final BigDecimal POWER_USAGE_EFFECTIVENESS = BigDecimal.valueOf(1.40);
    private static final int ENERGY_SCALE = 6;

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetType supportedAssetType() {
        return AssetType.SERVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal computeEnergyConsumptionKwh(final Asset asset, final int usageDurationDays) {
        validateInputs(asset, usageDurationDays);

        return asset.calculateDailyEnergyConsumptionKwh()
                .multiply(BigDecimal.valueOf(usageDurationDays))
                .multiply(UTILIZATION_RATE)
                .multiply(POWER_USAGE_EFFECTIVENESS)
                .setScale(ENERGY_SCALE, RoundingMode.HALF_UP);
    }

    private void validateInputs(final Asset asset, final int usageDurationDays) {
        Objects.requireNonNull(asset, "Asset is required");
        if (asset.getAssetType() != AssetType.SERVER) {
            throw new CalculationException("Server strategy can only process SERVER assets");
        }
        if (usageDurationDays <= 0) {
            throw new CalculationException("Usage duration must be strictly positive");
        }
    }
}
