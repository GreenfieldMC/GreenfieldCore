package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.event.player.PlayerInteractEvent;

public class RailInteraction extends InteractionHandler {

    public RailInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.RAIL,
                Material.ACTIVATOR_RAIL,
                Material.DETECTOR_RAIL,
                Material.POWERED_RAIL
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allow the unusual placement of rails.").append(Component.text(" NOTE: the breaking of rails that utilize redstone does not work. It is a minecraft bug. It WILL update surrounding things. To work around this, place a block below the rail, remove the desired rail, and then remove the block below.", NamedTextColor.GRAY, TextDecoration.ITALIC));
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to place a rail against the blockface you clicked. Clicking on the top or bottom of a block will make the rail ascending in the direction you were facing when you clicked the block.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var mat = getHandMat(event);

        if (event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
            event.setUseItemInHand(PlayerInteractEvent.Result.DENY);

            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;

            var data = (Rail) mat.createBlockData();
            var waterlog = placementLocation.getBlock().getBlockData().getMaterial() == Material.WATER;
            var shape = getRequiredShape(event.getPlayer().getFacing(), event.getBlockFace());

            data.setShape(shape);
            data.setWaterlogged(waterlog);

            if (data instanceof RedstoneRail rail) placeBlockNatively(event.getPlayer(), placementLocation, rail);
            else placeBlockAt(event.getPlayer(), placementLocation, mat, data);
        } else if (!event.getPlayer().isSneaking()){
            if (event.getClickedBlock() != null && event.getClickedBlock() instanceof RedstoneRail rail) {
                event.setCancelled(true);
                event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
                event.setUseItemInHand(PlayerInteractEvent.Result.DENY);
                rail.setPowered(!rail.isPowered());
                placeBlockNatively(event.getPlayer(), event.getClickedBlock().getLocation(), rail);
            }
        }
    }

    @Override
    public void onLeftClickBlock(PlayerInteractEvent event) {
        event.setCancelled(true);
        event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
        event.setUseItemInHand(PlayerInteractEvent.Result.DENY);
        placeBlockNatively(event.getPlayer(), event.getClickedBlock().getLocation(), Material.AIR);
    }

    private static Rail.Shape getRequiredShape(BlockFace playerDirection, BlockFace clickedFace) {
        if (clickedFace != BlockFace.DOWN && clickedFace != BlockFace.UP) {
            return switch (clickedFace) {
                case NORTH, SOUTH -> Rail.Shape.NORTH_SOUTH;
                case EAST, WEST -> Rail.Shape.EAST_WEST;
                default -> throw new RuntimeException("Unknown block face: " + clickedFace);
            };
        } else {
            return switch (playerDirection) {
                case NORTH -> Rail.Shape.ASCENDING_NORTH;
                case SOUTH -> Rail.Shape.ASCENDING_SOUTH;
                case EAST -> Rail.Shape.ASCENDING_EAST;
                case WEST -> Rail.Shape.ASCENDING_WEST;
                default -> throw new RuntimeException("Unknown player direction: " + playerDirection);
            };
        }
    }
}
