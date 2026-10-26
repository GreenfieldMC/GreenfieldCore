package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.MossyCarpet;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class MossyCarpetInteraction extends InteractionHandler {

    public MossyCarpetInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, Material.PALE_MOSS_CARPET);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unnatural placement and editing of pale moss carpet.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click the top of a block to place the carpet as a bottom carpet.", NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Shift right click the side of a block to place the carpet against it with a tall wall.", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Right click the side of a pale moss carpet to cycle the wall height on the opposite side.", NamedTextColor.GRAY));
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var face = event.getBlockFace();
        var clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        if (event.getPlayer().isSneaking()) {
            if (face != BlockFace.UP && face != BlockFace.DOWN && !isHorizontal(face)) return;

            var location = getPlaceableLocation(clickedBlock.getLocation(), face);
            // An existing mossy carpet is added to rather than replaced
            var carpet = location.getBlock().getBlockData() instanceof MossyCarpet existing ? existing : null;
            if (carpet == null) {
                if (!canPlaceAt(location)) return;
                carpet = (MossyCarpet) getHandMat(event).createBlockData();
                carpet.setBottom(false);
            }

            if (face == BlockFace.UP || face == BlockFace.DOWN) carpet.setBottom(true);
            else carpet.setHeight(face.getOppositeFace(), Wall.Height.TALL);

            cancel(event);
            placeBlockAt(event.getPlayer(), location, carpet.getMaterial(), carpet);
        } else if (isHorizontal(face)) {
            // Cycle the carpet that was clicked, or the carpet sitting against the clicked face
            var carpetBlock = clickedBlock.getBlockData() instanceof MossyCarpet mc ? clickedBlock : getPlaceableLocation(clickedBlock.getLocation(), face).getBlock();
            if (!(carpetBlock.getBlockData() instanceof MossyCarpet carpet)) return;

            cancel(event);
            var side = face.getOppositeFace();
            var height = carpet.getHeight(side);
            if (height == Wall.Height.NONE) carpet.setHeight(side, Wall.Height.LOW);
            else if (height == Wall.Height.LOW) carpet.setHeight(side, Wall.Height.TALL);
            else carpet.setHeight(side, Wall.Height.NONE);
            placeBlockAt(event.getPlayer(), carpetBlock.getLocation(), carpet.getMaterial(), carpet);
        }
    }

    private void cancel(PlayerInteractEvent event) {
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
    }

    private boolean isHorizontal(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST;
    }
}
