package com.ecotrack.carbonengine.application.service;

import com.ecotrack.carbonengine.application.factory.CarbonCalculationStrategyFactory;
import com.ecotrack.carbonengine.application.policy.CountryEnergyCoefficientPolicy;
import com.ecotrack.carbonengine.application.port.EnergyConfigurationProvider;
import com.ecotrack.carbonengine.application.strategy.CarbonCalculationStrategy;
import com.ecotrack.carbonengine.domain.exception.AssetCalculationException;
import com.ecotrack.carbonengine.domain.exception.CalculationException;
import com.ecotrack.carbonengine.domain.exception.EnergyConfigurationNotFoundException;
import com.ecotrack.carbonengine.domain.model.Asset;
import com.ecotrack.carbonengine.domain.model.CarbonFootprint;
import com.ecotrack.carbonengine.domain.model.EnergyConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Main service orchestrating carbon footprint computation for tenant assets.
 */
public final class CarbonCalculationEngine {

    private static final int EMISSION_SCALE = 6;

    private final CarbonCalculationStrategyFactory strategyFactory;
    private final EnergyConfigurationProvider energyConfigurationProvider;
    private final CountryEnergyCoefficientPolicy countryEnergyCoefficientPolicy;

    /**
     * Creates a calculation engine.
     *
     * @param strategyFactory factory used to resolve per-asset strategies.
     * @param energyConfigurationProvider provider used to resolve country energy configuration.
     * @param countryEnergyCoefficientPolicy policy used to resolve country coefficients.
     */
    public CarbonCalculationEngine(
            final CarbonCalculationStrategyFactory strategyFactory,
            final EnergyConfigurationProvider energyConfigurationProvider,
            final CountryEnergyCoefficientPolicy countryEnergyCoefficientPolicy
    ) {
        this.strategyFactory = Objects.requireNonNull(strategyFactory, "Strategy factory is required");
        this.energyConfigurationProvider = Objects.requireNonNull(
                energyConfigurationProvider,
                "Energy configuration provider is required"
        );
        this.countryEnergyCoefficientPolicy = Objects.requireNonNull(
                countryEnergyCoefficientPolicy,
                "Country energy coefficient policy is required"
        );
    }

    /**
     * Calculates the carbon footprint in kgCO2e for an asset over a usage period.
     *
     * @param asset the tenant asset.
     * @param usageDurationDays the usage duration in days.
     * @param countryCode the company country code.
     * @return the resulting carbon footprint.
     * @throws AssetCalculationException when validation or calculation fails.
     */
    public CarbonFootprint calculateCarbonFootprint(
            final Asset asset,
            final int usageDurationDays,
            final String countryCode
    ) {
        validateInputs(asset, usageDurationDays, countryCode);

        try {
            final EnergyConfiguration configuration = energyConfigurationProvider
                    .findByCountryCode(normalizeCountryCode(countryCode))
                    .orElseThrow(() -> new EnergyConfigurationNotFoundException(countryCode));

            final CarbonCalculationStrategy strategy = strategyFactory.createStrategy(asset.getAssetType());
            final BigDecimal consumedEnergyKwh = strategy.computeEnergyConsumptionKwh(asset, usageDurationDays);
            final BigDecimal baseEmissionKgCo2e = configuration.convertToKgCo2e(consumedEnergyKwh);

            final BigDecimal countryCoefficient = countryEnergyCoefficientPolicy
                    .resolveCoefficient(configuration.getEnergySource());

            final BigDecimal adjustedEmissionKgCo2e = baseEmissionKgCo2e
                    .multiply(countryCoefficient)
                    .setScale(EMISSION_SCALE, RoundingMode.HALF_UP);

            return new CarbonFootprint(
                    UUID.randomUUID(),
                    asset.getTenantId(),
                    asset.getId(),
                    usageDurationDays,
                    consumedEnergyKwh,
                    adjustedEmissionKgCo2e,
                    Instant.now()
            );
        } catch (AssetCalculationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new CalculationException("Unexpected error while calculating carbon footprint", exception);
        }
    }

    private void validateInputs(final Asset asset, final int usageDurationDays, final String countryCode) {
        Objects.requireNonNull(asset, "Asset is required");
        if (usageDurationDays <= 0) {
            throw new CalculationException("Usage duration must be strictly positive");
        }
        if (countryCode == null || countryCode.isBlank()) {
            throw new CalculationException("Country code is required");
        }
    }

    private String normalizeCountryCode(final String countryCode) {
        return countryCode.trim().toUpperCase(Locale.ROOT);
    }
}
