package com.njdaeger.greenfieldcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ComponentUtils {

    public static TextComponent moduleMessage(String module) {
        return Component.text().append(Component.text("[" + module + "] ", NamedTextColor.LIGHT_PURPLE)).build();
    }



}
