package com.njdaeger.greenfieldcore.advancedbuild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class AdvBuildMessages {

    public static final TextComponent MODULE = moduleMessage("AdvBuild");

    public static final String ERROR_PLAYERS_ONLY = "Only players can toggle advanced build mode.";
    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);

    public static final TextComponent ENABLED_AVB = MODULE.append(Component.text("Enabled advanced build mode.", NamedTextColor.GRAY));
    public static final TextComponent DISABLED_AVB = MODULE.append(Component.text("Disabled advanced build mode.", NamedTextColor.GRAY));

}
