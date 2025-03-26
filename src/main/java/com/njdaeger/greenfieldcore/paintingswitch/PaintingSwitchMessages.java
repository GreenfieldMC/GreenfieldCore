package com.njdaeger.greenfieldcore.paintingswitch;

import net.kyori.adventure.text.TextComponent;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class PaintingSwitchMessages {

    public static final String MODULE = "PaintingSwitch";

    public static final TextComponent ENABLED = moduleMessage(MODULE, "Enabled PaintingSwitch.");
    public static final TextComponent DISABLED = moduleMessage(MODULE, "Disabled PaintingSwitch.");

    public static final TextComponent PAINTING_LOCKED = moduleMessage(MODULE, "Painting locked.");
    public static final TextComponent PAINTING_SCROLL = moduleMessage(MODULE, "Scroll to select painting.");
    public static final TextComponent PAINTING_REMOVED = moduleMessage(MODULE, "Painting removed.");

}
