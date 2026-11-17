package net.greenfieldmc.core.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class UtilityMessages {

    public static final TextComponent ENABLE_NIGHTVISION = moduleMessage("Utility", "Night vision enabled.");
    public static final TextComponent DISABLE_NIGHTVISION = moduleMessage("Utility", "Night vision disabled.");

    public static final Function<Integer, TextComponent> NUMBER_IS_PRIME = number -> Component.text(number, NamedTextColor.LIGHT_PURPLE)
            .append(Component.text(" is prime!", NamedTextColor.GRAY));
    public static final BiFunction<Integer, List<Integer>, TextComponent> NUMBER_IS_NOT_PRIME = (number, divisors) -> Component.text(number, NamedTextColor.LIGHT_PURPLE)
            .append(Component.text(" is not prime. Divisors: " + divisors + ".", NamedTextColor.GRAY));
}
