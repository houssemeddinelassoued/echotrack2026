package com.ecotrack.carbonengine.domain.exception;

/**
 * Exception thrown when a server log line cannot be parsed due to invalid format.
 * <p>
 * This exception is a specialized form of {@link AssetCalculationException} used
 * specifically for log parsing errors in the EcoTrack ingestion pipeline.
 * </p>
 *
 * @author EcoTrack Team
 * @since 1.0.0
 */
public class InvalidLogFormatException extends AssetCalculationException {

    private final String rawLogLine;

    /**
     * Constructs a new InvalidLogFormatException with the specified detail message.
     *
     * @param message    the detail message explaining why the log format is invalid
     * @param rawLogLine the raw log line that failed to parse
     */
    public InvalidLogFormatException(String message, String rawLogLine) {
        super(message);
        this.rawLogLine = rawLogLine;
    }

    /**
     * Constructs a new InvalidLogFormatException with the specified detail message and cause.
     *
     * @param message    the detail message explaining why the log format is invalid
     * @param rawLogLine the raw log line that failed to parse
     * @param cause      the cause of this exception
     */
    public InvalidLogFormatException(String message, String rawLogLine, Throwable cause) {
        super(message, cause);
        this.rawLogLine = rawLogLine;
    }

    /**
     * Returns the raw log line that failed to parse.
     *
     * @return the raw log line
     */
    public String getRawLogLine() {
        return rawLogLine;
    }
}
