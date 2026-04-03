package com.ecotrack.carbonengine.domain.exception;

/**
 * Base exception for all carbon calculation domain errors.
 */
public class AssetCalculationException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message the error message.
     */
    public AssetCalculationException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the error message.
     * @param cause the underlying cause.
     */
    public AssetCalculationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}