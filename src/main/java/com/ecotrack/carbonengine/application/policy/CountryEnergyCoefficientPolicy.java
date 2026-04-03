package com.ecotrack.carbonengine.application.policy;

import com.ecotrack.carbonengine.domain.enums.EnergySource;

import java.math.BigDecimal;

/**
 * Resolves the multiplier applied to emissions based on the country energy source.
 */
public interface CountryEnergyCoefficientPolicy {

    /**
     * Resolves a coefficient for the given energy source.
     *
     * @param energySource the dominant energy source.
     * @return the coefficient to apply to the base emission value.
     */
    BigDecimal resolveCoefficient(EnergySource energySource);
}
