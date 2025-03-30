package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class CommandBlockInteraction extends InteractionHandler {

    public CommandBlockInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.COMMAND_BLOCK,
                Material.CHAIN_COMMAND_BLOCK,
                Material.REPEATING_COMMAND_BLOCK
        );
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the placement of command blocks.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift right click to place a command block against the blockface you clicked, if not shifting, right clicking a command block will toggle the \"conditional\" property.");
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking() && getHandMat(event).createBlockData() instanceof CommandBlock block) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            block.setFacing(getFacingDirection(event.getPlayer()));
            placeBlockAt(event.getPlayer(), placementLocation, getHandMat(event), block);
        } else if (!event.getPlayer().isSneaking() && event.getClickedBlock().getBlockData() instanceof CommandBlock block) {
            block.setConditional(!block.isConditional());
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), event.getClickedBlock().getType(), block);
        }
    }

    public BlockFace getFacingDirection(Player player) {
        if (player.getLocation().getPitch() > 45) return BlockFace.UP;
        else if (player.getLocation().getPitch() < -45) return BlockFace.DOWN;
        else return player.getFacing();
    }
}
