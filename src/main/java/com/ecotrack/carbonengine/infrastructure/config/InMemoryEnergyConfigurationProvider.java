package com.ecotrack.carbonengine.infrastructure.config;

import com.ecotrack.carbonengine.application.port.EnergyConfigurationProvider;
import com.ecotrack.carbonengine.domain.model.EnergyConfiguration;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory implementation used to resolve country energy configuration.
 */
public final class InMemoryEnergyConfigurationProvider implements EnergyConfigurationProvider {

    private final Map<String, EnergyConfiguration> configurationByCountryCode;

    /**
     * Creates an in-memory provider.
     *
     * @param configurationByCountryCode map indexed by ISO country code.
     */
    public InMemoryEnergyConfigurationProvider(final Map<String, EnergyConfiguration> configurationByCountryCode) {
        this.configurationByCountryCode = Objects.requireNonNull(
                configurationByCountryCode,
                "Country configuration map is required"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EnergyConfiguration> findByCountryCode(final String countryCode) {
        Objects.requireNonNull(countryCode, "Country code is required");
        final String normalizedCode = countryCode.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(configurationByCountryCode.get(normalizedCode));
    }
}
