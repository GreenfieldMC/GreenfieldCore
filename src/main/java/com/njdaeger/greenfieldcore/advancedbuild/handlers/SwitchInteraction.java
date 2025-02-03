package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class SwitchInteraction extends InteractionHandler {

    public SwitchInteraction() {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand != Material.AIR &&
                            event.getClickedBlock() != null &&
                            mainHand.isBlock() && mainHand.createBlockData() instanceof Switch;
                }
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allow the unnatural placement of blocks that are considered switches. (buttons, levers, etc)");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a switch type block against the blockface you clicked.");
    }

    @Override
    public TextComponent getMaterialListText() {
        return Component.text("Any block that has a \"face\" property and a \"facing\" property.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var handMat = getHandMat(event);
        if (event.getPlayer().isSneaking() && handMat.createBlockData() instanceof Switch block) {
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            if (event.getBlockFace() == BlockFace.DOWN) block.setAttachedFace(FaceAttachable.AttachedFace.CEILING);
            else if (event.getBlockFace() == BlockFace.UP) block.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
            else block.setAttachedFace(FaceAttachable.AttachedFace.WALL);

            if (!block.getFaces().contains(event.getBlockFace())) block.setFacing(event.getPlayer().getFacing().getOppositeFace());
            else block.setFacing(event.getBlockFace());

            placeBlockAt(event.getPlayer(), placementLocation, handMat, block);
        }
    }
}
