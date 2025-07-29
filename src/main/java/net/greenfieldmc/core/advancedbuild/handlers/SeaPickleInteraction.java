package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeaPickleInteraction extends InteractionHandler {

    private final Map<UUID, PickleSession> sessions;

    public SeaPickleInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof SeaPickle;
                },
                Material.SEA_PICKLE);
        this.sessions = new HashMap<>();
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Cycles and saves quantity on right click." +
                "Removes block updates on shift place and cycling quantity").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to toggle the quantity.").color(NamedTextColor.GRAY);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (getHandMat(event) == Material.AIR || (getHandMat(event) == Material.SEA_PICKLE && !event.getPlayer().isSneaking())) {
            var session = getSession(event.getPlayer().getUniqueId());

            if (!(event.getClickedBlock().getBlockData() instanceof SeaPickle pickles)) return;
            session.setPickleAmount(pickles.getMaximumPickles() == pickles.getPickles() ? 1 : pickles.getPickles() + 1);
            pickles.setPickles(session.getPickleAmount());
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), getHandMat(event), pickles);
        } else if (event.getPlayer().isSneaking()) {

            var placeableLocation = getPlaceableLocation(event);
            if (placeableLocation != null) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                var pickles = (SeaPickle) getHandMat(event).createBlockData();
                pickles.setPickles(getSession(event.getPlayer().getUniqueId()).getPickleAmount());
                pickles.setWaterlogged(false);
                placeBlockAt(event.getPlayer(), placeableLocation, getHandMat(event), pickles);
            }
        }
    }

    private PickleSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) sessions.put(uuid, new PickleSession(1));
        return sessions.get(uuid);
    }

    public class PickleSession {

        private int pickleAmount;

        public PickleSession(int pickleAmount) { this.pickleAmount = pickleAmount; }

        public int getPickleAmount() { return pickleAmount; }

        public void setPickleAmount(int pickleAmount) { this.pickleAmount = pickleAmount; }

    }

}