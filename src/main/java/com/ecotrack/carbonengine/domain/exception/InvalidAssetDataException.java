package com.ecotrack.carbonengine.domain.exception;

/**
 * Raised when an asset does not contain the required data for carbon calculation.
 */
public class InvalidAssetDataException extends AssetCalculationException {

    /**
     * Creates an exception with a message.
     *
     * @param message the validation error message.
     */
    public InvalidAssetDataException(final String message) {
        super(message);
    }
}