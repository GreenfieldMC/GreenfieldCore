package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class TorchInteraction extends InteractionHandler {

    public TorchInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.SOUL_TORCH,
                Material.REDSTONE_TORCH,
                Material.TORCH
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of torches.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a torch against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        var placementLocation = getPlaceableLocation(event);
        if (placementLocation == null || !event.getPlayer().isSneaking()) return;

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        if (event.getBlockFace() != BlockFace.UP) {
            mat = switch (mat) {
                case SOUL_TORCH -> Material.SOUL_WALL_TORCH;
                case REDSTONE_TORCH -> Material.REDSTONE_WALL_TORCH;
                case TORCH -> Material.WALL_TORCH;
                default -> throw new RuntimeException("Unknown torch type: " + mat);
            };
            var data = (Directional)mat.createBlockData();
            data.setFacing(event.getPlayer().getFacing().getOppositeFace());
            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        } else placeBlockAt(event.getPlayer(), placementLocation, mat);
    }
}
