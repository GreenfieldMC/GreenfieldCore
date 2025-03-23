package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.greenfieldcore.services.ICoreProtectService;
import com.njdaeger.greenfieldcore.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class DirectionalInteraction extends InteractionHandler {

    public DirectionalInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.LADDER,
                Material.BIG_DRIPLEAF,
                Material.TRIPWIRE_HOOK,
                Material.REPEATER,
                Material.COMPARATOR
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of directional blocks. (Ladders, tripwire hooks, etc.)");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a directional block against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        if (event.getPlayer().isSneaking() && mat.createBlockData() instanceof Directional block) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            block.setFacing(event.getPlayer().getFacing().getOppositeFace());
            placeBlockAt(event.getPlayer(), placementLocation, mat, block);
        }
    }
}
