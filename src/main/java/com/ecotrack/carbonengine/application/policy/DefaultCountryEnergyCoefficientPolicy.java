package com.ecotrack.carbonengine.application.policy;

import com.ecotrack.carbonengine.domain.enums.EnergySource;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Default country coefficient policy based on dominant energy source.
 */
public final class DefaultCountryEnergyCoefficientPolicy implements CountryEnergyCoefficientPolicy {

    private static final BigDecimal FOSSIL_COEFFICIENT = BigDecimal.valueOf(1.2);
    private static final BigDecimal RENEWABLE_COEFFICIENT = BigDecimal.valueOf(0.5);
    private static final BigDecimal DEFAULT_COEFFICIENT = BigDecimal.ONE;

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal resolveCoefficient(final EnergySource energySource) {
        Objects.requireNonNull(energySource, "Energy source is required");

        return switch (energySource) {
            case FOSSIL -> FOSSIL_COEFFICIENT;
            case RENEWABLE -> RENEWABLE_COEFFICIENT;
            default -> DEFAULT_COEFFICIENT;
        };
    }
}
