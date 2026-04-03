package com.ecotrack.carbonengine.application.factory;

import com.ecotrack.carbonengine.application.strategy.CarbonCalculationStrategy;
import com.ecotrack.carbonengine.domain.enums.AssetType;
import com.ecotrack.carbonengine.domain.exception.CalculationException;
import com.ecotrack.carbonengine.domain.exception.UnsupportedAssetTypeException;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory responsible for selecting the correct strategy for an asset type.
 */
public final class CarbonCalculationStrategyFactory {

    private final Map<AssetType, CarbonCalculationStrategy> strategyByAssetType;

    /**
     * Creates a factory with all available calculation strategies.
     *
     * @param strategies the registered strategies.
     */
    public CarbonCalculationStrategyFactory(final Collection<CarbonCalculationStrategy> strategies) {
        Objects.requireNonNull(strategies, "Strategies are required");
        this.strategyByAssetType = new EnumMap<>(AssetType.class);

        for (CarbonCalculationStrategy strategy : strategies) {
            final AssetType assetType = strategy.supportedAssetType();
            if (strategyByAssetType.containsKey(assetType)) {
                throw new CalculationException("Duplicate strategy registration for asset type: " + assetType);
            }
            strategyByAssetType.put(assetType, strategy);
        }
    }

    /**
     * Resolves a strategy for the provided asset type.
     *
     * @param assetType the asset type.
     * @return the matching strategy.
     * @throws UnsupportedAssetTypeException when no strategy is registered.
     */
    public CarbonCalculationStrategy createStrategy(final AssetType assetType) {
        Objects.requireNonNull(assetType, "Asset type is required");
        final CarbonCalculationStrategy strategy = strategyByAssetType.get(assetType);
        if (strategy == null) {
            throw new UnsupportedAssetTypeException(assetType);
        }
        return strategy;
    }
}
