package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TurtleEggInteraction extends InteractionHandler {

    private final Map<UUID, EggSession> sessions;

    public TurtleEggInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof TurtleEgg;
                },
                Material.TURTLE_EGG);
        this.sessions = new HashMap<>();
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Cycles and saves egg quantity on right click. Removes block updates on shift place and cycling quantity").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click to toggle the egg quantity.").color(NamedTextColor.GRAY);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (getHandMat(event) == Material.AIR || (getHandMat(event) == Material.TURTLE_EGG && !event.getPlayer().isSneaking())) {
            var session = getSession(event.getPlayer().getUniqueId());

            if (!(event.getClickedBlock().getBlockData() instanceof TurtleEgg eggs)) return;
            session.setEggAmount(eggs.getMaximumEggs() == eggs.getEggs() ? 1 : eggs.getEggs() + 1);
            eggs.setEggs(session.getEggAmount());
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), getHandMat(event), eggs);
        } else if (event.getPlayer().isSneaking()) {

            var placeableLocation = getPlaceableLocation(event);
            if (placeableLocation != null) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                var eggs = (TurtleEgg) getHandMat(event).createBlockData();
                eggs.setEggs(getSession(event.getPlayer().getUniqueId()).getEggAmount());
                placeBlockAt(event.getPlayer(), placeableLocation, getHandMat(event), eggs);
            }
        }
    }

    private EggSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) sessions.put(uuid, new EggSession(1));
        return sessions.get(uuid);
    }

    public class EggSession {

        private int eggAmount;

        public EggSession(int eggAmount) { this.eggAmount = eggAmount; }

        public int getEggAmount() { return eggAmount; }

        public void setEggAmount(int eggAmount) { this.eggAmount = eggAmount; }

    }

}