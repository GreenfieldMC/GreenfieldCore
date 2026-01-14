package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Orientation;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class JigsawInteraction extends InteractionHandler {

    public JigsawInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, Material.JIGSAW);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the placement of jigsaw blocks.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a jigsaw block against the blockface you clicked.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {

        var placeMaterial = getHandMat(event);
        var clickedBlock = event.getClickedBlock();
        var placementLocation = getPlaceableLocation(clickedBlock.getLocation(), event.getBlockFace());
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

    public static Orientation getJigsawOrientation(BlockFace clickedFace, Player player) {
        return switch (clickedFace) {
            case EAST -> Orientation.EAST_UP;
            case SOUTH -> Orientation.SOUTH_UP;
            case WEST -> Orientation.WEST_UP;
            case DOWN -> switch (player.getFacing()) {
                case EAST -> Orientation.DOWN_EAST;
                case SOUTH -> Orientation.DOWN_SOUTH;
                case WEST -> Orientation.DOWN_WEST;
                default -> Orientation.NORTH_UP;
            };
            case UP -> switch (player.getFacing()) {
                case EAST -> Orientation.UP_EAST;
                case SOUTH -> Orientation.UP_SOUTH;
                case WEST -> Orientation.UP_WEST;
                default -> Orientation.UP_NORTH;
            };
            default -> Orientation.NORTH_UP;
        };
    }

}
