package com.ecotrack.carbonengine.application.strategy;

import com.ecotrack.carbonengine.domain.enums.AssetType;
import com.ecotrack.carbonengine.domain.model.Asset;

import java.math.BigDecimal;

/**
 * Contract for computing energy consumption for a specific asset type.
 */
public interface CarbonCalculationStrategy {

    /**
     * Returns the asset type supported by this strategy.
     *
     * @return the supported asset type.
     */
    AssetType supportedAssetType();

    /**
     * Computes consumed energy in kWh for the provided asset and usage duration.
     *
     * @param asset the asset to evaluate.
     * @param usageDurationDays the usage duration in days.
     * @return the consumed energy in kWh.
     */
    BigDecimal computeEnergyConsumptionKwh(Asset asset, int usageDurationDays);
}
