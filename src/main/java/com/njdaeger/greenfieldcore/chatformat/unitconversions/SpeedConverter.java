package com.njdaeger.greenfieldcore.chatformat.unitconversions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SpeedConverter implements IUnitConverter<SpeedConverter.SpeedUnit> {

    static {
        UNITS = new ArrayList<>();
    }

    public static final SpeedUnit METRES_PER_SECOND = new SpeedUnit(1, "m/s", "mps", "m/s");
    public static final SpeedUnit KILOMETRES_PER_HOUR = new SpeedUnit(3.6, "km/h", "kph", "km/h");
    public static final SpeedUnit MILES_PER_HOUR = new SpeedUnit(2.23694, "mi/h", "mph", "mi/h");
    public static final SpeedUnit FOOT_PER_SECOND = new SpeedUnit(3.28084, "ft/s", "fps", "ft/s");
    public static final SpeedUnit KNOTS = new SpeedUnit(1.94384, "Knots", "knots", "knot", "kts", "kt", "kn");

    private static final List<SpeedUnit> UNITS;

    @Override
    public List<SpeedUnit> getUnits() {
        return UNITS;
    }

    public static class SpeedUnit extends AbstractUnit {

        public SpeedUnit(double conversionFactor, String niceName, String... aliases) {
            super(conversionFactor, Pattern.compile("(\\d*\\.?\\d*)\\s?(" + String.join("|", Stream.of(aliases).map(alias -> alias.contains("/") ? alias.replace("/", "\\/") : alias).toArray(String[]::new)) + ")", Pattern.CASE_INSENSITIVE), niceName, aliases);
            UNITS.add(this);
        }
    }
}
