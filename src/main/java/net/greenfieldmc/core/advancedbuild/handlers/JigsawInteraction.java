package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
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
