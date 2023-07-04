package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule.LIGHT_BLUE;

public class PinkPetalsInteraction extends InteractionHandler {

    private final Map<UUID, PetalSession> sessions;

    public PinkPetalsInteraction() {
        super((event) -> {
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
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the natural placement and modification of pink petals.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("If hand is empty or is holding pink petals: right clicking pink petals cycles the petal amount.").setColor(LIGHT_BLUE)
                .appendRoot(" ----- ").setColor(ChatColor.DARK_GRAY)
                .appendRoot("If hand is holding pink petals and shifting: pink petals will be placed with the last petal amount you used.").setColor(LIGHT_BLUE);
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
