package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class CoralInteraction extends InteractionHandler {

    public CoralInteraction() {
        super(
                Material.BRAIN_CORAL_FAN,
                Material.BUBBLE_CORAL_FAN,
                Material.FIRE_CORAL_FAN,
                Material.HORN_CORAL_FAN,
                Material.TUBE_CORAL_FAN,
                Material.DEAD_BRAIN_CORAL_FAN,
                Material.DEAD_BUBBLE_CORAL_FAN,
                Material.DEAD_FIRE_CORAL_FAN,
                Material.DEAD_HORN_CORAL_FAN,
                Material.DEAD_TUBE_CORAL_FAN,
                Material.BRAIN_CORAL,
                Material.BUBBLE_CORAL,
                Material.FIRE_CORAL,
                Material.HORN_CORAL,
                Material.TUBE_CORAL,
                Material.DEAD_BRAIN_CORAL,
                Material.DEAD_BUBBLE_CORAL,
                Material.DEAD_FIRE_CORAL,
                Material.DEAD_HORN_CORAL,
                Material.DEAD_TUBE_CORAL
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement of coral blocks and coral fans.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a coral block or fan against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);
        var placementLocation = getPlaceableLocation(event);
        if (placementLocation == null) return;
        if (event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            Waterlogged data = (Waterlogged) mat.createBlockData();
            var waterlog = placementLocation.getBlock().getType() == Material.WATER;
            data.setWaterlogged(waterlog);
            if (event.getBlockFace() != BlockFace.DOWN && event.getBlockFace() != BlockFace.UP && mat.name().endsWith("_FAN")) {
                mat = switch (mat) {
                    case BRAIN_CORAL_FAN -> Material.BRAIN_CORAL_WALL_FAN;
                    case BUBBLE_CORAL_FAN -> Material.BUBBLE_CORAL_WALL_FAN;
                    case FIRE_CORAL_FAN -> Material.FIRE_CORAL_WALL_FAN;
                    case HORN_CORAL_FAN -> Material.HORN_CORAL_WALL_FAN;
                    case TUBE_CORAL_FAN -> Material.TUBE_CORAL_WALL_FAN;
                    case DEAD_BRAIN_CORAL_FAN -> Material.DEAD_BRAIN_CORAL_WALL_FAN;
                    case DEAD_BUBBLE_CORAL_FAN -> Material.DEAD_BUBBLE_CORAL_WALL_FAN;
                    case DEAD_FIRE_CORAL_FAN -> Material.DEAD_FIRE_CORAL_WALL_FAN;
                    case DEAD_HORN_CORAL_FAN -> Material.DEAD_HORN_CORAL_WALL_FAN;
                    case DEAD_TUBE_CORAL_FAN -> Material.DEAD_TUBE_CORAL_WALL_FAN;
                    default -> throw new RuntimeException("Unknown coral fan type: " + mat);
                };
                data = (CoralWallFan) mat.createBlockData();
                ((CoralWallFan)data).setFacing(event.getBlockFace());
                data.setWaterlogged(waterlog);
            }
            placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        }
    }
}
