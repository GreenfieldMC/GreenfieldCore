package net.greenfieldmc.core.chatformat.unitconversions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MassConverter implements IUnitConverter<MassConverter.MassUnit> {

    static {
        UNITS = new ArrayList<>();
    }

    public static final MassUnit KILOGRAM = new MassUnit(1, "Kilograms", "kilograms", "kilogram", "kgs", "kg");
    public static final MassUnit GRAM = new MassUnit(1000, "Grams", "grams", "g");
    public static final MassUnit MILLIGRAM = new MassUnit(1000000, "Milligrams", "milligrams", "milligram", "mg");
    public static final MassUnit POUND = new MassUnit(2.20462, "Pounds", "pounds", "pound", "lbs", "lb");
    public static final MassUnit OUNCE = new MassUnit(35.274, "Ounces", "ounces", "ounce", "oz");
    public static final MassUnit STONE = new MassUnit(0.157473, "Stone", "stones", "stone", "st");
    public static final MassUnit METRIC_TON = new MassUnit(0.001, "Metric Tons", "metric-tons", "metric tons", "metric-ton", "metric ton", "tonnes", "tonne", "t");
    public static final MassUnit IMPERIAL_TON = new MassUnit(0.000984207, "Imperial Tons", "imperial-tons", "imperial tons", "imperial-ton", "imperial ton", "tons", "ton");
    public static final MassUnit YOUR_MOM = new MassUnit(0.00005, "Your Mother", "your-mom", "your mom", "moms", "mom");

    private static final List<MassUnit> UNITS;

    @Override
    public List<MassUnit> getUnits() {
        return UNITS;
    }

    public static class MassUnit extends AbstractUnit {

        public MassUnit(double conversionFactor, String niceName, String... aliases) {
            super(conversionFactor, Pattern.compile("(\\d*\\.?\\d*)\\s?(" + String.join("|", aliases) + ")(?=\\s|$)", Pattern.CASE_INSENSITIVE), niceName, aliases);
            UNITS.add(this);
        }
    }
}

