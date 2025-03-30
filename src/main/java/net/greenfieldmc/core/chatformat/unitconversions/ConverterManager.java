package net.greenfieldmc.core.chatformat.unitconversions;

import java.util.List;

public class ConverterManager {

    private static final List<IUnitConverter<?>> converters = List.of(
            new LengthConverter(),
            new SpeedConverter(),
            new TemperatureConverter(),
            new MassConverter()
    );

    public static List<IUnitConverter<?>> getConverters() {
        return converters;
    }

}
