package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.DriedGhast;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DriedGhastInteraction extends InteractionHandler {

    private final Map<UUID, GhastSession> sessions;

    public DriedGhastInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof DriedGhast;
                },
                Material.DRIED_GHAST
        );
        this.sessions = new HashMap<>();
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the natural placement and modification of dried ghast.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("If hand is empty or is holding dried ghast: right clicking dried ghast cycles the hydration.", NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY)
                .append(Component.text("If hand is holding dried ghast and shifting: dried ghast will be placed with the last hydration amount you used.", NamedTextColor.GRAY)));
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (getHandMat(event) == Material.AIR || (materials.contains(getHandMat(event)) && !event.getPlayer().isSneaking())) {
            var session = getSession(event.getPlayer().getUniqueId());

            if (!(event.getClickedBlock().getBlockData() instanceof DriedGhast ghast)) return;
            session.setHydration(ghast.getMaximumHydration() == ghast.getHydration() ? 0 : ghast.getHydration() + 1);
            ghast.setHydration(session.getHydration());
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), getHandMat(event), ghast);
            return;
        }

        var placeableLocation = getPlaceableLocation(event);
        if (placeableLocation != null) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            var ghast = (DriedGhast) getHandMat(event).createBlockData();
            ghast.setHydration(getSession(event.getPlayer().getUniqueId()).getHydration());
            ghast.setFacing(event.getPlayer().getFacing().getOppositeFace());
            placeBlockAt(event.getPlayer(), placeableLocation, getHandMat(event), ghast);
        }
    }

    private GhastSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) sessions.put(uuid, new GhastSession(0));
        return sessions.get(uuid);
    }

    public class GhastSession {

        private int hydration;

        public GhastSession(int hydration) {
            this.hydration = hydration;
        }

        public int getHydration() {
            return hydration;
        }

        public void setHydration(int hydration) {
            this.hydration = hydration;
        }

    }

}
