package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class BisectedInteraction extends InteractionHandler {

    public BisectedInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand != Material.AIR &&
                            event.getClickedBlock() != null &&
                            mainHand.isBlock() &&
                            !(mainHand.createBlockData() instanceof TrapDoor) &&
                            !(mainHand.createBlockData() instanceof Stairs) &&
                            mainHand.createBlockData() instanceof Bisected;
                }
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of blocks that are bisected (normally take 2 blocks of space) vertically.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a bisected block against the blockface you clicked.");
    }

    @Override
    public TextComponent getMaterialListText() {
        return Component.text("Any block that has a \"half\" property. (Except doors (handled separately) and trapdoors)");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMat = getHandMat(event);
        if (event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Bisected block) {
            var clickedFace = event.getBlockFace();
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            var placementOther = clickedFace == BlockFace.DOWN ? placementLocation.clone().subtract(0, 1, 0) : placementLocation.clone().add(0, 1, 0);
            if (!canPlaceAt(placementOther)) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            var upperData = (Bisected) block.clone();
            upperData.setHalf(Bisected.Half.TOP);
            var lowerData = (Bisected) block.clone();
            lowerData.setHalf(Bisected.Half.BOTTOM);

            if (clickedFace == BlockFace.DOWN) {
                placeBlockAt(event.getPlayer(), placementOther, handMat, lowerData);
                placeBlockAt(event.getPlayer(), placementLocation, handMat, upperData);
            } else {
                placeBlockAt(event.getPlayer(), placementLocation, handMat, lowerData);
                placeBlockAt(event.getPlayer(), placementOther, handMat, upperData);
            }
        }
    }
}
