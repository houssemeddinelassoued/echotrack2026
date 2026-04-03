package com.ecotrack.carbonengine.domain.model;

import com.ecotrack.carbonengine.domain.enums.EnergySource;
import com.ecotrack.carbonengine.domain.exception.CalculationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/**
 * Describes the energy context used to convert consumed energy into carbon emissions.
 */
public final class EnergyConfiguration {

    private static final BigDecimal GRAMS_PER_KILOGRAM = BigDecimal.valueOf(1000);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int EMISSION_SCALE = 6;

    private final String countryCode;
    private final EnergySource energySource;
    private final BigDecimal carbonIntensityGramsPerKwh;
    private final BigDecimal renewableSharePercentage;

    /**
     * Creates an energy configuration.
     *
     * @param countryCode the ISO country code.
     * @param energySource the dominant energy source.
     * @param carbonIntensityGramsPerKwh the carbon intensity in grams of CO2e per kWh.
     * @param renewableSharePercentage the share of renewable energy in percent.
     */
    public EnergyConfiguration(
            final String countryCode,
            final EnergySource energySource,
            final BigDecimal carbonIntensityGramsPerKwh,
            final BigDecimal renewableSharePercentage
    ) {
        this.countryCode = validateCountryCode(countryCode);
        this.energySource = Objects.requireNonNull(energySource, "Energy source is required");
        this.carbonIntensityGramsPerKwh = validateNonNegative(
                carbonIntensityGramsPerKwh,
                "Carbon intensity"
        );
        this.renewableSharePercentage = validatePercentage(renewableSharePercentage);
    }

    /**
     * Returns the ISO country code.
     *
     * @return the country code.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Returns the dominant energy source.
     *
     * @return the energy source.
     */
    public EnergySource getEnergySource() {
        return energySource;
    }

    /**
     * Returns the carbon intensity in grams of CO2e per kWh.
     *
     * @return the carbon intensity.
     */
    public BigDecimal getCarbonIntensityGramsPerKwh() {
        return carbonIntensityGramsPerKwh;
    }

    /**
     * Returns the renewable share percentage of the energy mix.
     *
     * @return the renewable share percentage.
     */
    public BigDecimal getRenewableSharePercentage() {
        return renewableSharePercentage;
    }

    /**
     * Converts an energy consumption into carbon emissions expressed in kilograms of CO2e.
     *
     * @param consumedEnergyKwh the consumed energy in kWh.
     * @return the resulting emissions in kgCO2e.
     */
    public BigDecimal convertToKgCo2e(final BigDecimal consumedEnergyKwh) {
        final BigDecimal normalizedConsumedEnergy = validateNonNegative(consumedEnergyKwh, "Consumed energy");

        return normalizedConsumedEnergy
                .multiply(carbonIntensityGramsPerKwh)
                .divide(GRAMS_PER_KILOGRAM, EMISSION_SCALE, RoundingMode.HALF_UP);
    }

    private String validateCountryCode(final String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new CalculationException("Country code is required");
        }
        return candidate.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal validateNonNegative(final BigDecimal value, final String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() < 0) {
            throw new CalculationException(fieldName + " cannot be negative");
        }
        return value;
    }

    private BigDecimal validatePercentage(final BigDecimal value) {
        final BigDecimal normalizedValue = validateNonNegative(value, "Renewable share percentage");
        if (normalizedValue.compareTo(ONE_HUNDRED) > 0) {
            throw new CalculationException("Renewable share percentage cannot exceed 100");
        }
        return normalizedValue;
    }
}