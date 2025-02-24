package com.njdaeger.greenfieldcore.chatformat.unitconversions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LengthConverter implements IUnitConverter<LengthConverter.LengthUnit> {

    static {
        UNITS = new ArrayList<>();
    }

    public static final LengthUnit METERS = new LengthUnit(1d, "Meters", "blocks", "block", "meters", "meter", "m");
    public static final LengthUnit KILOMETERS = new LengthUnit(.001d, "Kilometers", "kilometers", "kilometer", "km");
    public static final LengthUnit CENTIMETERS = new LengthUnit(100d, "Centimeters", "centimeters", "centimeter", "cm");
    public static final LengthUnit MILLIMETERS = new LengthUnit(1000d, "Millimeters", "millimeters", "millimeter", "mm");
    public static final LengthUnit MILES = new LengthUnit(0.000621371d, "Miles", "miles", "mile", "mi");
    public static final LengthUnit YARDS = new LengthUnit(1.09361d, "Yards", "yards", "yard", "yd");
    public static final LengthUnit FEET = new LengthUnit(3.28084d, "Feet", "feet", "foot", "ft");
    public static final LengthUnit INCHES = new LengthUnit(39.3701d, "Inches", "inches", "inch", "in");

    private static final List<LengthUnit> UNITS;

    @Override
    public List<LengthConverter.LengthUnit> getUnits() {
        return UNITS;
    }

    public static class LengthUnit extends AbstractUnit {

        public LengthUnit(double conversionFactor, String niceName, String... aliases) {
            super(conversionFactor, Pattern.compile("(\\d*\\.?\\d*)\\s?(" + String.join("|", aliases) + ")", Pattern.CASE_INSENSITIVE), niceName, aliases);
            UNITS.add(this);
        }
    }
}
