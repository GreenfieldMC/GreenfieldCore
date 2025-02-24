package com.njdaeger.greenfieldcore.chatformat.unitconversions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IUnitConverter<T extends IUnit> {

    List<T> getUnits();

    default T resolveUnit(String input) {
        for (T unit : getUnits()) {
            if (unit.getPattern().matcher(input).find()) {
                return unit;
            }
        }
        return null;
    }

    default double convert(double value, T from, T to) {
        return (value / from.getConversionFactor()) * to.getConversionFactor();
    }

    default Map<IUnit, Double> getConversions(double value, IUnit from) {
        return getUnits().stream().filter(unit -> unit != from).collect(Collectors.toMap(unit -> unit, unit -> convert(value, (T) from, unit)));
    }

}
