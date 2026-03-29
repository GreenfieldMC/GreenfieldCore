package net.greenfieldmc.core.signmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Function;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class SignManagerMessages {

    public static final TextComponent MODULE = moduleMessage("SignManager");

    public static final String ERROR_NOT_HOLDING_SIGN = "You must be holding a sign item in your main hand.";
    public static final String ERROR_SIGN_NOT_FOUND = "No saved sign found with that name.";
    public static final String ERROR_GROUP_NOT_FOUND = "No saved sign group found with that name.";
    public static final String ERROR_SIGN_ALREADY_EXISTS = "A sign or group with that name already exists.";
    public static final String ERROR_NO_SIGNS_IN_HOTBAR = "No sign items found in your hotbar.";
    public static final String ERROR_ENTRY_NOT_FOUND = "No saved sign or group found with that name.";

    public static final Function<String, TextComponent> SIGN_SAVED = (name) -> MODULE.append(Component.text("Successfully saved sign \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> SIGN_DELETED = (name) -> MODULE.append(Component.text("Successfully deleted sign \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> GROUP_SAVED = (name) -> MODULE.append(Component.text("Successfully saved sign group \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<String, TextComponent> GROUP_DELETED = (name) -> MODULE.append(Component.text("Successfully deleted sign group \"" + name + "\".", NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> GROUP_SAVED_COUNT = (count) -> MODULE.append(Component.text("Saved " + count + " sign(s) from your hotbar.", NamedTextColor.GRAY));
}
