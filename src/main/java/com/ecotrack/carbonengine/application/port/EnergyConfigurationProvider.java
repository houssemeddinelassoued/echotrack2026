package com.ecotrack.carbonengine.application.port;

import com.ecotrack.carbonengine.domain.model.EnergyConfiguration;

import java.util.Optional;

/**
 * Port used by the engine to resolve country energy configuration.
 */
public interface EnergyConfigurationProvider {

    /**
     * Finds an energy configuration by ISO country code.
     *
     * @param countryCode the ISO country code.
     * @return an optional containing the configuration when found.
     */
    Optional<EnergyConfiguration> findByCountryCode(String countryCode);
}
