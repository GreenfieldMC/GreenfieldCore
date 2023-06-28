package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class JigsawInteraction extends InteractionHandler {

    public JigsawInteraction() {
        super(Material.JIGSAW);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {

        var placeMaterial = getHandMat(event);
        var placementLocation = getPlaceableLocation(event.getClickedBlock().getLocation(), event.getBlockFace());
        var clickedFace = event.getBlockFace();
        var player = event.getPlayer();

        Jigsaw j = (Jigsaw) placeMaterial.createBlockData();
        if (!canPlaceAt(placementLocation)) return;
        j.setOrientation(getJigsawOrientation(clickedFace, player));
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(j, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
    }

    public static Jigsaw.Orientation getJigsawOrientation(BlockFace clickedFace, Player player) {
        return switch (clickedFace) {
            case EAST -> Jigsaw.Orientation.EAST_UP;
            case SOUTH -> Jigsaw.Orientation.SOUTH_UP;
            case WEST -> Jigsaw.Orientation.WEST_UP;
            case DOWN -> switch (player.getFacing()) {
                case EAST -> Jigsaw.Orientation.DOWN_EAST;
                case SOUTH -> Jigsaw.Orientation.DOWN_SOUTH;
                case WEST -> Jigsaw.Orientation.DOWN_WEST;
                default -> Jigsaw.Orientation.NORTH_UP;
            };
            case UP -> switch (player.getFacing()) {
                case EAST -> Jigsaw.Orientation.UP_EAST;
                case SOUTH -> Jigsaw.Orientation.UP_SOUTH;
                case WEST -> Jigsaw.Orientation.UP_WEST;
                default -> Jigsaw.Orientation.UP_NORTH;
            };
            default -> Jigsaw.Orientation.NORTH_UP;
        };
    }

}
