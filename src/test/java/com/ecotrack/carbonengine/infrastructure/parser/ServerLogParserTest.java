package com.ecotrack.carbonengine.infrastructure.parser;

import com.ecotrack.carbonengine.domain.exception.InvalidLogFormatException;
import com.ecotrack.carbonengine.domain.model.ServerLogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ServerLogParser}.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Happy path - valid log parsing</li>
 *   <li>Edge cases - boundary values, decimal CPU loads</li>
 *   <li>Error handling - malformed logs, invalid data</li>
 * </ul>
 * </p>
 */
@DisplayName("ServerLogParser")
class ServerLogParserTest {

    @Nested
    @DisplayName("parseServerLog - Valid inputs")
    class ValidInputs {

        @Test
        @DisplayName("should parse standard log line correctly")
        void shouldParseStandardLogLine() {
            // Given
            String log = "[2024-10-12T14:00:00] SRV-01 | CPU_Load: 45% | MEM: 12GB | User: admin";

            // When
            ServerLogEntry entry = ServerLogParser.parseServerLog(log);

            // Then
            assertThat(entry.timestamp()).isEqualTo(LocalDateTime.of(2024, 10, 12, 14, 0, 0));
            assertThat(entry.serverName()).isEqualTo("SRV-01");
            assertThat(entry.cpuLoad()).isEqualTo(45.0);
        }

        @Test
        @DisplayName("should parse decimal CPU load values")
        void shouldParseDecimalCpuLoad() {
            // Given
            String log = "[2024-10-12T14:00:00] SRV-01 | CPU_Load: 45.75% | MEM: 12GB";

            // When
            ServerLogEntry entry = ServerLogParser.parseServerLog(log);

            // Then
            assertThat(entry.cpuLoad()).isEqualTo(45.75);
        }

        @Test
        @DisplayName("should handle server names with dots and underscores")
        void shouldHandleServerNamesWithSpecialChars() {
            // Given
            String log = "[2024-10-12T14:00:00] web_server.prod-01 | CPU_Load: 78% | MEM: 8GB";

            // When
            ServerLogEntry entry = ServerLogParser.parseServerLog(log);

            // Then
            assertThat(entry.serverName()).isEqualTo("web_server.prod-01");
            assertThat(entry.cpuLoad()).isEqualTo(78.0);
        }

        @ParameterizedTest(name = "CPU load {0}% should be valid")
        @CsvSource({
                "0, 0.0",
                "100, 100.0",
                "50, 50.0",
                "99.99, 99.99",
                "0.01, 0.01"
        })
        @DisplayName("should accept CPU load within valid range [0-100]")
        void shouldAcceptValidCpuLoadRange(String input, double expected) {
            // Given
            String log = String.format("[2024-10-12T14:00:00] SRV-01 | CPU_Load: %s%% | MEM: 12GB", input);

            // When
            ServerLogEntry entry = ServerLogParser.parseServerLog(log);

            // Then
            assertThat(entry.cpuLoad()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should handle varying whitespace around separators")
        void shouldHandleVaryingWhitespace() {
            // Given
            String log = "[2024-10-12T14:00:00]   SRV-01  |  CPU_Load:  45 %  | MEM: 12GB";

            // When
            ServerLogEntry entry = ServerLogParser.parseServerLog(log);

            // Then
            assertThat(entry.serverName()).isEqualTo("SRV-01");
            assertThat(entry.cpuLoad()).isEqualTo(45.0);
        }

        @Test
        @DisplayName("should trim leading and trailing whitespace")
        void shouldTrimWhitespace() {
            // Given
            String log = "   [2024-10-12T14:00:00] SRV-01 | CPU_Load: 45% | MEM: 12GB   ";

            // When
            ServerLogEntry entry = ServerLogParser.parseServerLog(log);

            // Then
            assertThat(entry.serverName()).isEqualTo("SRV-01");
        }
    }

    @Nested
    @DisplayName("parseServerLog - Invalid inputs")
    class InvalidInputs {

        @ParameterizedTest
        @NullSource
        @DisplayName("should throw NullPointerException for null input")
        void shouldThrowForNullInput(String log) {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .withMessage("logString must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("should throw InvalidLogFormatException for empty or blank input")
        void shouldThrowForEmptyInput(String log) {
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("should throw for missing timestamp brackets")
        void shouldThrowForMissingBrackets() {
            // Given
            String log = "2024-10-12T14:00:00 SRV-01 | CPU_Load: 45% | MEM: 12GB";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class)
                    .hasMessageContaining("does not match expected format");
        }

        @Test
        @DisplayName("should throw for missing CPU_Load field")
        void shouldThrowForMissingCpuLoad() {
            // Given
            String log = "[2024-10-12T14:00:00] SRV-01 | MEM: 12GB | User: admin";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class);
        }

        @Test
        @DisplayName("should throw for invalid timestamp format")
        void shouldThrowForInvalidTimestamp() {
            // Given - invalid month (13)
            String log = "[2024-13-12T14:00:00] SRV-01 | CPU_Load: 45% | MEM: 12GB";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class)
                    .hasMessageContaining("Invalid timestamp");
        }

        @Test
        @DisplayName("should throw for CPU load above 100%")
        void shouldThrowForCpuLoadAbove100() {
            // Given
            String log = "[2024-10-12T14:00:00] SRV-01 | CPU_Load: 150% | MEM: 12GB";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class)
                    .hasMessageContaining("must be between 0 and 100");
        }

        @Test
        @DisplayName("should throw for negative CPU load")
        void shouldThrowForNegativeCpuLoad() {
            // Given - regex won't match negative, so format error expected
            String log = "[2024-10-12T14:00:00] SRV-01 | CPU_Load: -5% | MEM: 12GB";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class);
        }

        @Test
        @DisplayName("should include raw log line in exception")
        void shouldIncludeRawLogInException() {
            // Given
            String log = "invalid log format";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class)
                    .satisfies(ex -> {
                        InvalidLogFormatException ilfe = (InvalidLogFormatException) ex;
                        assertThat(ilfe.getRawLogLine()).isEqualTo(log);
                    });
        }

        @Test
        @DisplayName("should throw for completely malformed log")
        void shouldThrowForMalformedLog() {
            // Given
            String log = "random garbage data 12345";

            // When/Then
            assertThatThrownBy(() -> ServerLogParser.parseServerLog(log))
                    .isInstanceOf(InvalidLogFormatException.class)
                    .hasMessageContaining("does not match expected format");
        }
    }

    @Nested
    @DisplayName("getPattern")
    class GetPatternTests {

        @Test
        @DisplayName("should return non-null pattern")
        void shouldReturnNonNullPattern() {
            assertThat(ServerLogParser.getPattern()).isNotNull();
        }

        @Test
        @DisplayName("pattern should match valid log format")
        void patternShouldMatchValidLog() {
            // Given
            String log = "[2024-10-12T14:00:00] SRV-01 | CPU_Load: 45% | MEM: 12GB";

            // When/Then
            assertThat(ServerLogParser.getPattern().matcher(log).find()).isTrue();
        }
    }
}
