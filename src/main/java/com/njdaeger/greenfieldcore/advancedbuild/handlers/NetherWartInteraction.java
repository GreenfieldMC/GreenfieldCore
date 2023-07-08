package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule.LIGHT_BLUE;

public class NetherWartInteraction extends InteractionHandler {

    public NetherWartInteraction() {
        super(Material.NETHER_WART);
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the unusual placement of nether warts.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("If not shifting, and a block clicked is a nether wart: Cycle the \"age\" property").setColor(LIGHT_BLUE)
                .appendRoot(" ----- ").setColor(ChatColor.DARK_GRAY)
                .appendRoot("If shifting: place the nether wart").setColor(LIGHT_BLUE);
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
