package com.ecotrack.carbonengine.domain.exception;

import com.ecotrack.carbonengine.domain.enums.AssetType;

/**
 * Raised when the engine receives an asset type that is not supported.
 */
public class UnsupportedAssetTypeException extends AssetCalculationException {

    /**
     * Creates an exception for the provided asset type.
     *
     * @param assetType the unsupported asset type.
     */
    public UnsupportedAssetTypeException(final AssetType assetType) {
        super("No carbon calculation strategy is registered for asset type: " + assetType);
    }
}