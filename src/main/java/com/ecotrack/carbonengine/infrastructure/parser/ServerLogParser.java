package com.ecotrack.carbonengine.infrastructure.parser;

import com.ecotrack.carbonengine.domain.exception.InvalidLogFormatException;
import com.ecotrack.carbonengine.domain.model.ServerLogEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for raw server log lines received from client infrastructure.
 * <p>
 * Extracts timestamp, server name, and CPU load from log lines formatted as:
 * {@code [2024-10-12T14:00:00] SRV-01 | CPU_Load: 45% | MEM: 12GB | User: admin}
 * </p>
 * <p>
 * This parser is thread-safe as the compiled Pattern is immutable and can be
 * shared across threads.
 * </p>
 *
 * @author EcoTrack Team
 * @since 1.0.0
 */
public final class ServerLogParser {

    /**
     * Optimized regex pattern for server log extraction.
     * <p>
     * Captures three groups:
     * <ol>
     *   <li>Timestamp in ISO-8601 format (YYYY-MM-DDTHH:MM:SS)</li>
     *   <li>Server name (alphanumeric with hyphens, underscores, dots)</li>
     *   <li>CPU load numeric value (integer or decimal)</li>
     * </ol>
     * </p>
     * <p>
     * Pattern breakdown:
     * <ul>
     *   <li>{@code ^\[} - Line starts with opening bracket</li>
     *   <li>{@code (\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})} - ISO timestamp (group 1)</li>
     *   <li>{@code \]\s+} - Closing bracket followed by whitespace</li>
     *   <li>{@code ([A-Za-z0-9._-]+)} - Server name (group 2)</li>
     *   <li>{@code \s*\|\s*CPU_Load:\s*} - Pipe separator and CPU_Load label</li>
     *   <li>{@code (\d+(?:\.\d+)?)} - Numeric value, optional decimal (group 3)</li>
     *   <li>{@code \s*%} - Percent sign with optional leading space</li>
     * </ul>
     * </p>
     */
    private static final Pattern SERVER_LOG_PATTERN = Pattern.compile(
            "^\\[" +                                    // Opening bracket
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})" + // Group 1: ISO timestamp
            "]\\s+" +                                   // Closing bracket + whitespace
            "([A-Za-z0-9._-]+)" +                       // Group 2: Server name
            "\\s*\\|\\s*CPU_Load:\\s*" +                // Pipe + CPU_Load label
            "(\\d+(?:\\.\\d+)?)" +                      // Group 3: CPU load value
            "\\s*%"                                     // Percent sign
    );

    private static final int GROUP_TIMESTAMP = 1;
    private static final int GROUP_SERVER_NAME = 2;
    private static final int GROUP_CPU_LOAD = 3;

    /**
     * Private constructor to prevent instantiation.
     * All methods are static utilities.
     */
    private ServerLogParser() {
        // Utility class
    }

    /**
     * Parses a raw server log line and extracts structured data.
     * <p>
     * Expected input format:
     * {@code [YYYY-MM-DDTHH:MM:SS] SERVER-NAME | CPU_Load: XX% | ...}
     * </p>
     *
     * @param logString the raw log line to parse; must not be null
     * @return a {@link ServerLogEntry} containing the extracted timestamp, server name, and CPU load
     * @throws NullPointerException      if logString is null
     * @throws InvalidLogFormatException if the log line does not match the expected format
     *                                   or contains invalid data (e.g., malformed timestamp,
     *                                   CPU load out of range)
     *
     * @example
     * <pre>{@code
     * String log = "[2024-10-12T14:00:00] SRV-01 | CPU_Load: 45% | MEM: 12GB | User: admin";
     * ServerLogEntry entry = ServerLogParser.parseServerLog(log);
     * // entry.timestamp() -> LocalDateTime of 2024-10-12T14:00:00
     * // entry.serverName() -> "SRV-01"
     * // entry.cpuLoad() -> 45.0
     * }</pre>
     */
    public static ServerLogEntry parseServerLog(String logString) {
        Objects.requireNonNull(logString, "logString must not be null");

        String trimmedLog = logString.trim();
        if (trimmedLog.isEmpty()) {
            throw new InvalidLogFormatException("Log line is empty", logString);
        }

        Matcher matcher = SERVER_LOG_PATTERN.matcher(trimmedLog);

        if (!matcher.find()) {
            throw new InvalidLogFormatException(
                    "Log line does not match expected format: " +
                    "[YYYY-MM-DDTHH:MM:SS] SERVER-NAME | CPU_Load: XX% | ...",
                    logString
            );
        }

        // Extract captured groups
        String timestampStr = matcher.group(GROUP_TIMESTAMP);
        String serverName = matcher.group(GROUP_SERVER_NAME);
        String cpuLoadStr = matcher.group(GROUP_CPU_LOAD);

        // Parse timestamp
        LocalDateTime timestamp = parseTimestamp(timestampStr, logString);

        // Parse CPU load
        double cpuLoad = parseCpuLoad(cpuLoadStr, logString);

        return new ServerLogEntry(timestamp, serverName, cpuLoad);
    }

    /**
     * Parses the timestamp string into a LocalDateTime.
     *
     * @param timestampStr the timestamp string in ISO-8601 format
     * @param rawLog       the original log line for error reporting
     * @return the parsed LocalDateTime
     * @throws InvalidLogFormatException if the timestamp cannot be parsed
     */
    private static LocalDateTime parseTimestamp(String timestampStr, String rawLog) {
        try {
            return LocalDateTime.parse(timestampStr);
        } catch (DateTimeParseException e) {
            throw new InvalidLogFormatException(
                    "Invalid timestamp format: " + timestampStr,
                    rawLog,
                    e
            );
        }
    }

    /**
     * Parses the CPU load string into a double value.
     *
     * @param cpuLoadStr the CPU load string (numeric value)
     * @param rawLog     the original log line for error reporting
     * @return the parsed CPU load as a double
     * @throws InvalidLogFormatException if the CPU load cannot be parsed or is out of range
     */
    private static double parseCpuLoad(String cpuLoadStr, String rawLog) {
        try {
            double cpuLoad = Double.parseDouble(cpuLoadStr);

            if (cpuLoad < 0.0 || cpuLoad > 100.0) {
                throw new InvalidLogFormatException(
                        "CPU load must be between 0 and 100, got: " + cpuLoad,
                        rawLog
                );
            }

            return cpuLoad;
        } catch (NumberFormatException e) {
            throw new InvalidLogFormatException(
                    "Invalid CPU load value: " + cpuLoadStr,
                    rawLog,
                    e
            );
        }
    }

    /**
     * Returns the compiled regex pattern used for parsing.
     * <p>
     * Useful for debugging or extending the parser behavior.
     * </p>
     *
     * @return the compiled Pattern instance
     */
    public static Pattern getPattern() {
        return SERVER_LOG_PATTERN;
    }
}
