package com.njdaeger.greenfieldcore.hotspots;

import com.njdaeger.pdk.utils.TriFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class HotspotMessages {

    public static final TextComponent MODULE = moduleMessage("Hotspots");

    public static final Function<String, TextComponent> TELEPORT_SUCCESS = (name) -> MODULE.append(Component.text("Successfully teleported to " + name, NamedTextColor.GRAY));
    public static final Function<String, TextComponent> HOTSPOT_CREATE_SUCCESS = (name) -> MODULE.append(Component.text("Successfully created hotspot " + name, NamedTextColor.GRAY));
    public static final Function<String, TextComponent> HOTSPOT_DELETE_SUCCESS = (name) -> MODULE.append(Component.text("Successfully deleted hotspot " + name, NamedTextColor.GRAY));
    public static final TriFunction<String, String, String, TextComponent> HOTSPOT_EDIT_SUCCESS = (name, field, value) -> MODULE.append(Component.text("Successfully edited the " + field + " to " + value + " for hotspot " + name, NamedTextColor.GRAY));

    public static final Function<String, TextComponent> CATEGORY_CREATE_SUCCESS = (name) -> MODULE.append(Component.text("Successfully created category " + name, NamedTextColor.GRAY));
    public static final Function<String, TextComponent> CATEGORY_DELETE_SUCCESS = (name) -> MODULE.append(Component.text("Successfully deleted category " + name, NamedTextColor.GRAY));
    public static final BiFunction<String, String, TextComponent> CATEGORY_DELETE_SUCCESS_REPLACEMENT = (name, replacement) -> MODULE.append(Component.text("Successfully deleted category " + name + " and replaced usages of it with " + replacement, NamedTextColor.GRAY));
    public static final String CATEGORY_DELETE_FAIL_NO_REPLACEMENT = "You must provide a replacement category when deleting a category that has hotspots.";
    public static final TriFunction<String, String, String, TextComponent> CATEGORY_EDIT_SUCCESS = (name, field, value) -> MODULE.append(Component.text("Successfully edited the " + field + " to " + value + " for category " + name, NamedTextColor.GRAY));

}
