package net.greenfieldmc.core.advancedbuild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class AdvBuildMessages {

    public static final String MODULE = "AdvBuild";

    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);

    public static final TextComponent ENABLED_AVB = moduleMessage(MODULE, "Enabled advanced build mode.");
    public static final TextComponent DISABLED_AVB = moduleMessage(MODULE, "Disabled advanced build mode.");

}
