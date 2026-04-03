package com.ecotrack.carbonengine.domain.exception;

/**
 * Raised when no energy configuration can be resolved for a country.
 */
public class EnergyConfigurationNotFoundException extends AssetCalculationException {

    /**
     * Creates an exception for the provided country code.
     *
     * @param countryCode the unresolved ISO country code.
     */
    public EnergyConfigurationNotFoundException(final String countryCode) {
        super("No energy configuration found for country code: " + countryCode);
    }
}