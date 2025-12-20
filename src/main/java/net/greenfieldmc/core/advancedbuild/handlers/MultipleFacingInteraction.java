package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class MultipleFacingInteraction extends InteractionHandler {

    public MultipleFacingInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) ->
                ((event.getClickedBlock() != null && event.getClickedBlock().getBlockData() instanceof MultipleFacing && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR))
                        || (event.getPlayer().getInventory().getItemInMainHand().getType().isBlock() && event.getPlayer().getInventory().getItemInMainHand().getType().createBlockData() instanceof MultipleFacing));
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the modifying and setting of states for blocks that can face multiple directions at the same time.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right clicking a block that is Multiple Facing will toggle the face of the block clicked on.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) return;

        var blockDataInHand = event.getPlayer().getInventory().getItemInMainHand().getType().isBlock()
                ? event.getPlayer().getInventory().getItemInMainHand().getType().createBlockData()
                : null;

        if (blockDataInHand instanceof MultipleFacing mf) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            var faceToSet = mf.getAllowedFaces().contains(event.getBlockFace().getOppositeFace())
                    ? event.getBlockFace().getOppositeFace()
                    : event.getBlockFace();
            if (mf.getAllowedFaces().contains(faceToSet)) mf.setFace(faceToSet, true);

            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event), mf);

        } else if (event.getClickedBlock().getBlockData() instanceof MultipleFacing mf) {
            var clickedFace = event.getBlockFace();
            if (!mf.getAllowedFaces().contains(clickedFace)) return;
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            mf.setFace(clickedFace, !mf.hasFace(clickedFace));
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), mf.getMaterial(), mf);
        }
    }
}
