package net.greenfieldmc.core.chatformat.unitconversions;

import java.util.regex.Pattern;

public interface IUnit {

    String getName();

    Pattern getPattern();

    String[] getAliases();

    double getConversionFactor();

}
