package com.ecotrack.carbonengine.domain.model;

import com.ecotrack.carbonengine.domain.exception.CalculationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the CarbonFootprint domain model.
 * Tests immutability, validation rules, and tenant isolation.
 */
@DisplayName("CarbonFootprint Tests")
class CarbonFootprintTest {

    @Test
    @DisplayName("Should create CarbonFootprint with valid inputs")
    void shouldCreateWithValidInputs() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Instant calculatedAt = Instant.now();
        BigDecimal energy = new BigDecimal("42.500000");
        BigDecimal emission = new BigDecimal("12.345678");
        int duration = 30;

        // Act
        CarbonFootprint footprint = new CarbonFootprint(
                id, tenantId, assetId, duration, energy, emission, calculatedAt
        );

        // Assert
        assertEquals(id, footprint.getId());
        assertEquals(tenantId, footprint.getTenantId());
        assertEquals(assetId, footprint.getAssetId());
        assertEquals(duration, footprint.getUsageDurationDays());
        assertEquals(energy, footprint.getEnergyConsumedKwh());
        assertEquals(emission, footprint.getCarbonEmissionKgCo2e());
        assertEquals(calculatedAt, footprint.getCalculatedAt());
    }

    @Test
    @DisplayName("Should reject zero usage duration")
    void shouldRejectZeroDuration() {
        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        0, BigDecimal.ONE, BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Usage duration must be strictly positive", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject negative usage duration")
    void shouldRejectNegativeDuration() {
        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        -5, BigDecimal.ONE, BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Usage duration must be strictly positive", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject negative energy consumed")
    void shouldRejectNegativeEnergy() {
        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        1, new BigDecimal("-0.000001"), BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Energy consumed cannot be negative", ex.getMessage());
    }

    @Test
    @DisplayName("Should accept zero energy consumed")
    void shouldAcceptZeroEnergy() {
        CarbonFootprint footprint = new CarbonFootprint(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now()
        );
        assertEquals(BigDecimal.ZERO, footprint.getEnergyConsumedKwh());
    }

    @Test
    @DisplayName("Should reject negative carbon emission")
    void shouldRejectNegativeEmission() {
        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        1, BigDecimal.ONE, new BigDecimal("-0.1"), Instant.now()
                )
        );
        assertEquals("Carbon emission cannot be negative", ex.getMessage());
    }

    @Test
    @DisplayName("Should accept zero carbon emission")
    void shouldAcceptZeroEmission() {
        CarbonFootprint footprint = new CarbonFootprint(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, BigDecimal.ONE, BigDecimal.ZERO, Instant.now()
        );
        assertEquals(BigDecimal.ZERO, footprint.getCarbonEmissionKgCo2e());
    }

    @Test
    @DisplayName("Should reject null asset ID")
    void shouldRejectNullAssetId() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), null,
                        1, BigDecimal.ONE, BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Asset identifier is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null energy consumed")
    void shouldRejectNullEnergy() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        1, null, BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Energy consumed is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null carbon emission")
    void shouldRejectNullEmission() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        1, BigDecimal.ONE, null, Instant.now()
                )
        );
        assertEquals("Carbon emission is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null timestamp")
    void shouldRejectNullTimestamp() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        1, BigDecimal.ONE, BigDecimal.ONE, null
                )
        );
        assertEquals("Calculation timestamp is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null entity ID (from BaseEntity)")
    void shouldRejectNullEntityId() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonFootprint(
                        null, UUID.randomUUID(), UUID.randomUUID(),
                        1, BigDecimal.ONE, BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Entity identifier is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null tenant ID (from TenantScopedEntity)")
    void shouldRejectNullTenantId() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonFootprint(
                        UUID.randomUUID(), null, UUID.randomUUID(),
                        1, BigDecimal.ONE, BigDecimal.ONE, Instant.now()
                )
        );
        assertEquals("Tenant identifier is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should be immutable after construction")
    void shouldBeImmutable() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Instant calculatedAt = Instant.now();

        CarbonFootprint footprint = new CarbonFootprint(
                id, tenantId, assetId, 5,
                new BigDecimal("10.0"), new BigDecimal("2.5"), calculatedAt
        );

        // All fields are private and final, so we can only verify through getters
        assertEquals(id, footprint.getId());
        assertEquals(tenantId, footprint.getTenantId());
        assertNotNull(footprint.getEnergyConsumedKwh());
        assertNotNull(footprint.getCarbonEmissionKgCo2e());
    }

    @Test
    @DisplayName("Should support large numeric values")
    void shouldSupportLargeValues() {
        BigDecimal largeEnergy = new BigDecimal("999999999.999999");
        BigDecimal largeEmission = new BigDecimal("888888888.888888");

        CarbonFootprint footprint = new CarbonFootprint(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                365, largeEnergy, largeEmission, Instant.now()
        );

        assertEquals(largeEnergy, footprint.getEnergyConsumedKwh());
        assertEquals(largeEmission, footprint.getCarbonEmissionKgCo2e());
        assertEquals(365, footprint.getUsageDurationDays());
    }

    @Test
    @DisplayName("Should support maximum int duration")
    void shouldSupportMaxDuration() {
        CarbonFootprint footprint = new CarbonFootprint(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Integer.MAX_VALUE, BigDecimal.ONE, BigDecimal.ONE, Instant.now()
        );

        assertEquals(Integer.MAX_VALUE, footprint.getUsageDurationDays());
    }

    @Test
    @DisplayName("Should support fractional values with high precision")
    void shouldSupportHighPrecision() {
        BigDecimal precision = new BigDecimal("0.000001");

        CarbonFootprint footprint = new CarbonFootprint(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                1, precision, precision, Instant.now()
        );

        assertEquals(precision, footprint.getEnergyConsumedKwh());
        assertEquals(precision, footprint.getCarbonEmissionKgCo2e());
    }
}
