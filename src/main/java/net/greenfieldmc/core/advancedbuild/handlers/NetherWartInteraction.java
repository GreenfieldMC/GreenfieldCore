package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class NetherWartInteraction extends InteractionHandler {

    public NetherWartInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, Material.NETHER_WART);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the unusual placement of nether warts.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("If not shifting, and a block clicked is a nether wart: Cycle the \"age\" property", NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If shifting: place the nether wart", NamedTextColor.GRAY));
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        if (!event.getPlayer().isSneaking()) {
            if (event.getClickedBlock().getBlockData() instanceof Ageable crop && event.getClickedBlock().getType() == Material.NETHER_WART) {
                crop.setAge(crop.getMaximumAge() == crop.getAge() ? 0 : crop.getAge() + 1);
                placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), Material.NETHER_WART, crop);
            }
        } else {
            placeBlockAt(event.getPlayer(), getPlaceableLocation(event), Material.NETHER_WART);
        }
    }
}
