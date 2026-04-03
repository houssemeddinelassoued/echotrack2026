package com.ecotrack.carbonengine.application.service;

import com.ecotrack.carbonengine.application.factory.CarbonCalculationStrategyFactory;
import com.ecotrack.carbonengine.application.policy.CountryEnergyCoefficientPolicy;
import com.ecotrack.carbonengine.application.port.EnergyConfigurationProvider;
import com.ecotrack.carbonengine.application.strategy.CarbonCalculationStrategy;
import com.ecotrack.carbonengine.domain.enums.AssetType;
import com.ecotrack.carbonengine.domain.enums.EnergySource;
import com.ecotrack.carbonengine.domain.exception.AssetCalculationException;
import com.ecotrack.carbonengine.domain.exception.CalculationException;
import com.ecotrack.carbonengine.domain.exception.EnergyConfigurationNotFoundException;
import com.ecotrack.carbonengine.domain.model.Asset;
import com.ecotrack.carbonengine.domain.model.CarbonFootprint;
import com.ecotrack.carbonengine.domain.model.EnergyConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the CarbonCalculationEngine service.
 * Tests orchestration logic, validation, exception handling, and multi-tenant isolation.
 */
@DisplayName("CarbonCalculationEngine Tests")
class CarbonCalculationEngineTest {

    @Test
    @DisplayName("Should calculate carbon footprint with valid inputs")
    void shouldCalculateWithValidInputs() {
        // Arrange
        Asset asset = createAsset(AssetType.LAPTOP);
        BigDecimal consumedKwh = new BigDecimal("12.5");
        BigDecimal carbonIntensity = new BigDecimal("50");
        BigDecimal coefficient = new BigDecimal("1.2");

        EnergyConfigurationProvider provider = countryCode -> 
            Optional.of(new EnergyConfiguration("FR", EnergySource.GRID_MIX, carbonIntensity, BigDecimal.ZERO));
        
        CarbonCalculationStrategy strategy = new StubStrategy(AssetType.LAPTOP, consumedKwh);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        CountryEnergyCoefficientPolicy policy = energySource -> coefficient;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act
        Instant before = Instant.now();
        CarbonFootprint result = engine.calculateCarbonFootprint(asset, 30, "FR");
        Instant after = Instant.now();

        // Assert
        assertNotNull(result.getId());
        assertEquals(asset.getTenantId(), result.getTenantId());
        assertEquals(asset.getId(), result.getAssetId());
        assertEquals(30, result.getUsageDurationDays());
        assertEquals(consumedKwh, result.getEnergyConsumedKwh());
        assertTrue(result.getCalculatedAt().isAfter(before) || result.getCalculatedAt().equals(before));
        assertTrue(result.getCalculatedAt().isBefore(after) || result.getCalculatedAt().equals(after));
    }

    @Test
    @DisplayName("Should normalize country code to uppercase")
    void shouldNormalizeCountryCode() {
        // Arrange
        Asset asset = createAsset(AssetType.SERVER);
        BigDecimal consumedKwh = BigDecimal.ONE;
        
        java.util.concurrent.atomic.AtomicReference<String> capturedCode = new java.util.concurrent.atomic.AtomicReference<>();
        EnergyConfigurationProvider provider = code -> {
            capturedCode.set(code);
            return Optional.of(new EnergyConfiguration("DE", EnergySource.RENEWABLE, BigDecimal.TEN, BigDecimal.ZERO));
        };
        
        CarbonCalculationStrategy strategy = new StubStrategy(AssetType.SERVER, consumedKwh);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act
        engine.calculateCarbonFootprint(asset, 1, "  de  ");

        // Assert
        assertEquals("DE", capturedCode.get());
    }

    @Test
    @DisplayName("Should apply country coefficient to base emission")
    void shouldApplyCountryCoefficient() {
        // Arrange
        Asset asset = createAsset(AssetType.SCREEN);
        BigDecimal consumedKwh = new BigDecimal("100.0");
        BigDecimal carbonIntensity = new BigDecimal("200.0"); // 200g per kWh
        BigDecimal coefficient = new BigDecimal("1.5");

        EnergyConfigurationProvider provider = countryCode ->
            Optional.of(new EnergyConfiguration("GB", EnergySource.NUCLEAR, carbonIntensity, BigDecimal.ZERO));
        
        CarbonCalculationStrategy strategy = new StubStrategy(AssetType.SCREEN, consumedKwh);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        CountryEnergyCoefficientPolicy policy = energySource -> coefficient;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act
        CarbonFootprint result = engine.calculateCarbonFootprint(asset, 10, "GB");

        // Assert
        // Base emission = 100 kWh * 200 g/kWh / 1000 = 20 kgCO2e
        // Adjusted = 20 * 1.5 = 30 kgCO2e
        assertEquals(new BigDecimal("30.000000"), result.getCarbonEmissionKgCo2e());
    }

    @Test
    @DisplayName("Should reject null asset argument")
    void shouldRejectNullAsset() {
        CarbonCalculationEngine engine = createEngineWithDefaults();

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> engine.calculateCarbonFootprint(null, 5, "FR")
        );
        assertEquals("Asset is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject zero duration")
    void shouldRejectZeroDuration() {
        CarbonCalculationEngine engine = createEngineWithDefaults();
        Asset asset = createAsset(AssetType.SERVER);

        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> engine.calculateCarbonFootprint(asset, 0, "FR")
        );
        assertEquals("Usage duration must be strictly positive", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject negative duration")
    void shouldRejectNegativeDuration() {
        CarbonCalculationEngine engine = createEngineWithDefaults();
        Asset asset = createAsset(AssetType.SERVER);

        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> engine.calculateCarbonFootprint(asset, -1, "FR")
        );
        assertEquals("Usage duration must be strictly positive", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null country code")
    void shouldRejectNullCountryCode() {
        CarbonCalculationEngine engine = createEngineWithDefaults();
        Asset asset = createAsset(AssetType.SERVER);

        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> engine.calculateCarbonFootprint(asset, 1, null)
        );
        assertEquals("Country code is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject blank country code")
    void shouldRejectBlankCountryCode() {
        CarbonCalculationEngine engine = createEngineWithDefaults();
        Asset asset = createAsset(AssetType.SERVER);

        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> engine.calculateCarbonFootprint(asset, 1, "   ")
        );
        assertEquals("Country code is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw when energy configuration not found")
    void shouldThrowWhenConfigurationNotFound() {
        // Arrange
        Asset asset = createAsset(AssetType.SERVER);
        
        EnergyConfigurationProvider provider = countryCode -> Optional.empty();
        CarbonCalculationStrategy strategy = new StubStrategy(AssetType.SERVER, BigDecimal.ONE);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act & Assert
        assertThrows(
                EnergyConfigurationNotFoundException.class,
                () -> engine.calculateCarbonFootprint(asset, 5, "XX")
        );
    }

    @Test
    @DisplayName("Should propagate AssetCalculationException from strategy")
    void shouldPropagateAssetCalculationException() {
        // Arrange
        Asset asset = createAsset(AssetType.SERVER);
        AssetCalculationException expectedEx = new CalculationException("Strategy failed");
        
        CarbonCalculationStrategy strategy = new ThrowingStrategy(AssetType.SERVER, expectedEx);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        
        EnergyConfigurationProvider provider = countryCode ->
            Optional.of(new EnergyConfiguration("FR", EnergySource.GRID_MIX, BigDecimal.ONE, BigDecimal.ZERO));
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act & Assert
        AssetCalculationException actual = assertThrows(
                AssetCalculationException.class,
                () -> engine.calculateCarbonFootprint(asset, 5, "FR")
        );
        assertSame(expectedEx, actual);
    }

    @Test
    @DisplayName("Should wrap unexpected runtime exceptions")
    void shouldWrapUnexpectedExceptions() {
        // Arrange
        Asset asset = createAsset(AssetType.SERVER);
        
        EnergyConfigurationProvider provider = countryCode -> {
            throw new IllegalStateException("Database unavailable");
        };
        
        CarbonCalculationStrategy strategy = new StubStrategy(AssetType.SERVER, BigDecimal.ONE);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act & Assert
        CalculationException ex = assertThrows(
                CalculationException.class,
                () -> engine.calculateCarbonFootprint(asset, 5, "FR")
        );

        assertEquals("Unexpected error while calculating carbon footprint", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertEquals("Database unavailable", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("Should reject null strategy factory in constructor")
    void shouldRejectNullStrategyFactory() {
        EnergyConfigurationProvider provider = countryCode -> Optional.empty();
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonCalculationEngine(null, provider, policy)
        );
        assertEquals("Strategy factory is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null energy configuration provider in constructor")
    void shouldRejectNullProvider() {
        CarbonCalculationStrategyFactory factory = createFactoryWithDefaults();
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonCalculationEngine(factory, null, policy)
        );
        assertEquals("Energy configuration provider is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject null coefficient policy in constructor")
    void shouldRejectNullCoefficientPolicy() {
        CarbonCalculationStrategyFactory factory = createFactoryWithDefaults();
        EnergyConfigurationProvider provider = countryCode -> Optional.empty();

        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new CarbonCalculationEngine(factory, provider, null)
        );
        assertEquals("Country energy coefficient policy is required", ex.getMessage());
    }

    @Test
    @DisplayName("Should maintain tenant isolation in calculation results")
    void shouldMaintainTenantIsolation() {
        // Arrange
        UUID tenant1 = UUID.randomUUID();
        UUID tenant2 = UUID.randomUUID();

        Asset asset1 = new Asset(UUID.randomUUID(), tenant1, AssetType.SERVER, "Server1",
                LocalDate.of(2024, 1, 1), new BigDecimal("300"));
        Asset asset2 = new Asset(UUID.randomUUID(), tenant2, AssetType.SERVER, "Server2",
                LocalDate.of(2024, 1, 1), new BigDecimal("300"));

        CarbonCalculationEngine engine = createEngineWithDefaults();

        // Act
        CarbonFootprint result1 = engine.calculateCarbonFootprint(asset1, 10, "FR");
        CarbonFootprint result2 = engine.calculateCarbonFootprint(asset2, 10, "FR");

        // Assert
        assertEquals(tenant1, result1.getTenantId());
        assertEquals(tenant2, result2.getTenantId());
    }

    @Test
    @DisplayName("Should round emission scale to 6 decimal places")
    void shouldRoundEmissionScale() {
        // Arrange
        Asset asset = createAsset(AssetType.LAPTOP);
        BigDecimal consumedKwh = new BigDecimal("13.333333");
        BigDecimal carbonIntensity = new BigDecimal("33.333333");
        BigDecimal coefficient = new BigDecimal("3.333333");

        EnergyConfigurationProvider provider = countryCode ->
            Optional.of(new EnergyConfiguration("FR", EnergySource.GRID_MIX, carbonIntensity, BigDecimal.ZERO));
        
        CarbonCalculationStrategy strategy = new StubStrategy(AssetType.LAPTOP, consumedKwh);
        CarbonCalculationStrategyFactory factory = new CarbonCalculationStrategyFactory(List.of(strategy));
        CountryEnergyCoefficientPolicy policy = energySource -> coefficient;

        CarbonCalculationEngine engine = new CarbonCalculationEngine(factory, provider, policy);

        // Act
        CarbonFootprint result = engine.calculateCarbonFootprint(asset, 1, "FR");

        // Assert
        String emissionString = result.getCarbonEmissionKgCo2e().toPlainString();
        int decimalIndex = emissionString.indexOf('.');
        int decimalPlaces = decimalIndex > -1 ? emissionString.length() - decimalIndex - 1 : 0;
        assertTrue(decimalPlaces <= 6, "Emission should be rounded to max 6 decimal places");
    }

    /**
     * Stub implementation of CarbonCalculationStrategy for testing.
     */
    private static final class StubStrategy implements CarbonCalculationStrategy {
        private final AssetType supportedType;
        private final BigDecimal consumedEnergy;

        StubStrategy(AssetType supportedType, BigDecimal consumedEnergy) {
            this.supportedType = supportedType;
            this.consumedEnergy = consumedEnergy;
        }

        @Override
        public AssetType supportedAssetType() {
            return supportedType;
        }

        @Override
        public BigDecimal computeEnergyConsumptionKwh(Asset asset, int usageDurationDays) {
            return consumedEnergy;
        }
    }

    /**
     * Strategy that throws an exception for testing error handling.
     */
    private static final class ThrowingStrategy implements CarbonCalculationStrategy {
        private final AssetType supportedType;
        private final AssetCalculationException exceptionToThrow;

        ThrowingStrategy(AssetType supportedType, AssetCalculationException exceptionToThrow) {
            this.supportedType = supportedType;
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        public AssetType supportedAssetType() {
            return supportedType;
        }

        @Override
        public BigDecimal computeEnergyConsumptionKwh(Asset asset, int usageDurationDays) {
            throw exceptionToThrow;
        }
    }

    // Helper methods

    private Asset createAsset(AssetType type) {
        return new Asset(
                UUID.randomUUID(),
                UUID.randomUUID(),
                type,
                "Test Asset",
                LocalDate.of(2024, 1, 1),
                new BigDecimal("250")
        );
    }

    private CarbonCalculationStrategyFactory createFactoryWithDefaults() {
        return new CarbonCalculationStrategyFactory(
                List.of(new StubStrategy(AssetType.SERVER, BigDecimal.ONE))
        );
    }

    private CarbonCalculationEngine createEngineWithDefaults() {
        CarbonCalculationStrategyFactory factory = createFactoryWithDefaults();
        EnergyConfigurationProvider provider = countryCode ->
                Optional.of(new EnergyConfiguration("FR", EnergySource.GRID_MIX, BigDecimal.TEN, BigDecimal.ZERO));
        CountryEnergyCoefficientPolicy policy = energySource -> BigDecimal.ONE;
        return new CarbonCalculationEngine(factory, provider, policy);
    }
}
