package com.njdaeger.greenfieldcore.chatformat.unitconversions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TemperatureConverter implements IUnitConverter<TemperatureConverter.TemperatureUnit> {

    static {
        UNITS = new ArrayList<>();
    }

    public static final TemperatureUnit CELSIUS = new TemperatureUnit(0, "°C", "celsius", "°c", "c");
    public static final TemperatureUnit FAHRENHEIT = new TemperatureUnit(0, "°F", "fahrenheit", "°f", "f");
    public static final TemperatureUnit KELVIN = new TemperatureUnit(0, "°K", "kelvin", "°k", "k");

    private static final List<TemperatureUnit> UNITS;

    @Override
    public List<TemperatureUnit> getUnits() {
        return UNITS;
    }

    @Override
    public double convert(double value, TemperatureUnit from, TemperatureUnit to) {
        if (from == to) return value;
        if (from == CELSIUS) {
            if (to == FAHRENHEIT) return value * 9 / 5 + 32;
            else return value + 273.15;
        } else if (from == FAHRENHEIT) {
            if (to == CELSIUS) return (value - 32) * 5 / 9;
            else return (value - 32) * 5 / 9 + 273.15;
        } else {
            if (to == CELSIUS) return value - 273.15;
            else return (value - 273.15) * 9 / 5 + 32;
        }
    }

    public static class TemperatureUnit extends AbstractUnit {

        public TemperatureUnit(double conversionFactor, String niceName, String... aliases) {
            super(conversionFactor, Pattern.compile("(\\d*\\.?\\d*)\\s?(" + String.join("|", aliases) + ")", Pattern.CASE_INSENSITIVE), niceName, aliases);
            UNITS.add(this);
        }
    }
}

