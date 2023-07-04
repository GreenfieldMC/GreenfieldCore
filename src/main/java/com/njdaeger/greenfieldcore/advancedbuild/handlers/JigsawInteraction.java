package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class JigsawInteraction extends InteractionHandler {

    public JigsawInteraction() {
        super(Material.JIGSAW);
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the placement of jigsaw blocks.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("Shift and right click to place a jigsaw block against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {

        var placeMaterial = getHandMat(event);
        var placementLocation = getPlaceableLocation(event.getClickedBlock().getLocation(), event.getBlockFace());
        var clickedFace = event.getBlockFace();
        var player = event.getPlayer();
        if (!player.isSneaking()) return;

        Jigsaw j = (Jigsaw) placeMaterial.createBlockData();
        if (!canPlaceAt(placementLocation)) return;
        j.setOrientation(getJigsawOrientation(clickedFace, player));

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        placeBlockAt(player, placementLocation, placeMaterial, j);
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
