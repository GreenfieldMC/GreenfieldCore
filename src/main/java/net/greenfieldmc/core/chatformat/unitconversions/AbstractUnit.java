package net.greenfieldmc.core.chatformat.unitconversions;

import java.util.regex.Pattern;

public abstract class AbstractUnit implements IUnit {

    private final String niceName;
    private final String[] aliases;
    private final Pattern pattern;
    private final double conversionFactor;

    public AbstractUnit(double conversionFactor, Pattern pattern, String niceName, String... aliases) {
        this.conversionFactor = conversionFactor;
        this.niceName = niceName;
        this.pattern = pattern;
        this.aliases = aliases;
    }

    @Override
    public String getName() {
        return niceName;
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }

    @Override
    public double getConversionFactor() {
        return conversionFactor;
    }
}
