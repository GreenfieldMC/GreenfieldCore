package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class DefaultInteraction extends InteractionHandler {

    public DefaultInteraction() {
        super();
    }

    @Override
    public boolean handles(PlayerInteractEvent event) {
        return false;
    }
}
