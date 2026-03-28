package net.greenfieldmc.core.signmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Function;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class SignManagerMessages {

    public static final TextComponent MODULE = moduleMessage("SignManager");

    public static final TextComponent ERROR_NO_RESULTS = Component.text("There are no results to display.", NamedTextColor.RED);
    public static final String ERROR_NOT_HOLDING_SIGN = "You must be holding a sign item in your main hand.";
    public static final String ERROR_SIGN_NOT_FOUND = "No saved sign found with that name.";
    public static final String ERROR_GROUP_NOT_FOUND = "No saved sign group found with that name.";
    public static final String ERROR_SIGN_ALREADY_EXISTS = "A sign with that name already exists.";
    public static final String ERROR_NO_SIGNS_IN_HOTBAR = "No sign items found in your hotbar.";
    public static final String ERROR_MUST_BE_PLAYER = "This command can only be used by players.";

    public static final Function<String, TextComponent> SIGN_SAVED = (name) -> MODULE.append(Component.text("Successfully saved sign \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> SIGN_DELETED = (name) -> MODULE.append(Component.text("Successfully deleted sign \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> GROUP_SAVED = (name) -> MODULE.append(Component.text("Successfully saved sign group \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> GROUP_DELETED = (name) -> MODULE.append(Component.text("Successfully deleted sign group \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> GROUP_SAVED_COUNT = (count) -> MODULE.append(Component.text("Saved " + count + " sign(s) from your hotbar.", NamedTextColor.GRAY));

    public static final Component GUI_TITLE = Component.text("Sign Manager", NamedTextColor.DARK_PURPLE);
    public static final Function<String, Component> GUI_TITLE_SEARCH = (query) -> Component.text("Sign Manager - Search: ", NamedTextColor.DARK_PURPLE).append(Component.text(query, NamedTextColor.GOLD));
    public static final Function<String, Component> GUI_TITLE_FLAG = (flag) -> Component.text("Sign Manager - Flag: ", NamedTextColor.DARK_PURPLE).append(Component.text(flag, NamedTextColor.AQUA));
}

