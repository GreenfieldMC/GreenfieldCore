package com.njdaeger.greenfieldcore.utilities;

public enum LengthType {

    CENTIMETER("Centimeter", "Centimeters", 0, 100, false, LengthTypeArrays.metricUnits),
    METER("Meter", "Meters", 1, 1000, false, LengthTypeArrays.metricUnits),
    KILOMETER("Kilometer", "Kilometers", 2, 1000, false, LengthTypeArrays.metricUnits),
    MILE("Mile", "Miles", 3, 1760, true, LengthTypeArrays.imperialUnits),
    YARD("Yard", "Yards", 2, 1760, true, LengthTypeArrays.imperialUnits),
    FOOT("Foot", "Feet", 1, 3, true, LengthTypeArrays.imperialUnits),
    INCH("Inch", "Inches", 0, 12, true, LengthTypeArrays.imperialUnits);

    private static final double MI_TO_KM = 1.609344;
    private final double toNextUnit;
    final String pluralName;
    private final boolean imperial;
    final String singularName;
    private final int size;

    /**
     * Creates a length type.
     * @param nicename The nice, singular, name of this unit
     * @param pluralName The nice, pluralName, name of this unit
     * @param toNextUnit The conversion from one measuring system base unt to the other. Eg. Meters -> Feet and Feet -> Meters
     * @param size Determines whether to multiply or divide in the operation.
     */
    LengthType(String nicename, String pluralName, int size, double toNextUnit, boolean imperial, LengthType[] array) {
        this.singularName = nicename;
        this.pluralName = pluralName;
        this.toNextUnit = toNextUnit;
        this.imperial = imperial;
        this.size = size;
        array[size] = this;
    }

    public double convertTo(LengthType to, double number) {
        return convertTo(this, to, number);
    }

    private static double convertTo(LengthType from, LengthType to, double number) {
        if ((from.imperial && to.imperial) || (!from.imperial && !to.imperial)) {
            int currentSize = from.size;
            double currentNumber = number;
            if (currentSize == to.size) return currentNumber;//The units match
            while (currentSize != to.size) {
                if (currentSize < to.size) {//We want to make the smaller unit into a larger unit
                    currentNumber /= (from.imperial ? LengthTypeArrays.imperialUnits : LengthTypeArrays.metricUnits)[currentSize].toNextUnit;
                    currentSize++;
                    //This current length is the from and the type variable is the to.
                }
                else {
                    currentSize--;
                    currentNumber *= (from.imperial ? LengthTypeArrays.imperialUnits : LengthTypeArrays.metricUnits)[currentSize].toNextUnit;
                }
            }
            return currentNumber;
        }
        else if (from.imperial) {//We're going to metric
            return convertTo(KILOMETER, to, toKilometers(from.toMaxImperial(number)));
        } else return convertTo(MILE, to, toMiles(from.toMaxMetric(number)));
    }

    private double toMaxImperial(double number) {
        if (imperial) {
            System.out.println(number);
            switch (this) {
                case INCH:
                    number /= INCH.toNextUnit;

                case FOOT:
                    number /= FOOT.toNextUnit;

                case YARD:
                    number /= YARD.toNextUnit;

            }
        }
        return number;
    }

    private double toMaxMetric(double number) {
        if (!imperial) {
            switch (this) {
                case CENTIMETER:
                    number /= CENTIMETER.toNextUnit;
                case METER:
                    number /= METER.toNextUnit;
            }
        }
        return number;
    }

    private static double toKilometers(double miles) {
        return miles * MI_TO_KM;
    }

    private static double toMiles(double kilometers) {
        return kilometers / MI_TO_KM;
    }

}
