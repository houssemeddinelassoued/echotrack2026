package com.ecotrack.carbonengine.application.strategy;

import com.ecotrack.carbonengine.domain.enums.AssetType;
import com.ecotrack.carbonengine.domain.exception.CalculationException;
import com.ecotrack.carbonengine.domain.model.Asset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Energy computation strategy for laptop assets.
 */
public final class LaptopCarbonCalculationStrategy implements CarbonCalculationStrategy {

    private static final BigDecimal ACTIVE_HOURS_PER_DAY = BigDecimal.valueOf(8);
    private static final BigDecimal STANDBY_HOURS_PER_DAY = BigDecimal.valueOf(16);
    private static final BigDecimal STANDBY_CONSUMPTION_RATIO = BigDecimal.valueOf(0.15);
    private static final BigDecimal WATTS_PER_KILOWATT = BigDecimal.valueOf(1000);
    private static final int ENERGY_SCALE = 6;

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetType supportedAssetType() {
        return AssetType.LAPTOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal computeEnergyConsumptionKwh(final Asset asset, final int usageDurationDays) {
        validateInputs(asset, usageDurationDays);

        final BigDecimal powerWatts = asset.getPowerConsumptionWatts();
        final BigDecimal activeKwhPerDay = powerWatts
                .multiply(ACTIVE_HOURS_PER_DAY)
                .divide(WATTS_PER_KILOWATT, ENERGY_SCALE, RoundingMode.HALF_UP);

        final BigDecimal standbyKwhPerDay = powerWatts
                .multiply(STANDBY_HOURS_PER_DAY)
                .multiply(STANDBY_CONSUMPTION_RATIO)
                .divide(WATTS_PER_KILOWATT, ENERGY_SCALE, RoundingMode.HALF_UP);

        return activeKwhPerDay
                .add(standbyKwhPerDay)
                .multiply(BigDecimal.valueOf(usageDurationDays))
                .setScale(ENERGY_SCALE, RoundingMode.HALF_UP);
    }

    private void validateInputs(final Asset asset, final int usageDurationDays) {
        Objects.requireNonNull(asset, "Asset is required");
        if (asset.getAssetType() != AssetType.LAPTOP) {
            throw new CalculationException("Laptop strategy can only process LAPTOP assets");
        }
        if (usageDurationDays <= 0) {
            throw new CalculationException("Usage duration must be strictly positive");
        }
    }
}
