package com.ecotrack.carbonengine.domain.exception;

/**
 * Raised when a calculation fails after business validation passed.
 */
public class CalculationException extends AssetCalculationException {

    /**
     * Creates an exception with a message.
     *
     * @param message the calculation error message.
     */
    public CalculationException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message the calculation error message.
     * @param cause the underlying cause.
     */
    public CalculationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}