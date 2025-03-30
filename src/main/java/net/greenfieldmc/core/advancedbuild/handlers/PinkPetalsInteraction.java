package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PinkPetalsInteraction extends InteractionHandler {

    private final Map<UUID, PetalSession> sessions;

    public PinkPetalsInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof PinkPetals;
                },
                Material.PINK_PETALS
        );
        this.sessions = new HashMap<>();
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the natural placement and modification of pink petals.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("If hand is empty or is holding pink petals: right clicking pink petals cycles the petal amount.", NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY)
                .append(Component.text("If hand is holding pink petals and shifting: pink petals will be placed with the last petal amount you used.", NamedTextColor.GRAY)));
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (getHandMat(event) == Material.AIR || (getHandMat(event) == Material.PINK_PETALS && !event.getPlayer().isSneaking())) {
            var session = getSession(event.getPlayer().getUniqueId());

            if (!(event.getClickedBlock().getBlockData() instanceof PinkPetals petals)) return;
            session.setFlowerAmount(petals.getMaximumFlowerAmount() == petals.getFlowerAmount() ? 1 : petals.getFlowerAmount() + 1);
            petals.setFlowerAmount(session.getFlowerAmount());
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), getHandMat(event), petals);
        } else if (event.getPlayer().isSneaking()) {

            var placeableLocation = getPlaceableLocation(event);
            if (placeableLocation != null) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                var petals = (PinkPetals) getHandMat(event).createBlockData();
                petals.setFlowerAmount(getSession(event.getPlayer().getUniqueId()).getFlowerAmount());
                petals.setFacing(event.getPlayer().getFacing().getOppositeFace());
                placeBlockAt(event.getPlayer(), placeableLocation, getHandMat(event), petals);
            }
        }
    }

    private PetalSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) sessions.put(uuid, new PetalSession(1));
        return sessions.get(uuid);
    }

    public class PetalSession {

        private int flowerAmount;

        public PetalSession(int flowerAmount) {
            this.flowerAmount = flowerAmount;
        }

        public int getFlowerAmount() {
            return flowerAmount;
        }

        public void setFlowerAmount(int flowerAmount) {
            this.flowerAmount = flowerAmount;
        }

    }

}
