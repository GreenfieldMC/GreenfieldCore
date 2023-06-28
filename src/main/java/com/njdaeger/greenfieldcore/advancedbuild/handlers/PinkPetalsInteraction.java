package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (getHandMat(event) == Material.AIR || (getHandMat(event) == Material.PINK_PETALS && !event.getPlayer().isSneaking())) {
            var session = getSession(event.getPlayer().getUniqueId());

            if (!(event.getClickedBlock().getBlockData() instanceof PinkPetals petals)) return;
            session.setFlowerAmount(petals.getMaximumFlowerAmount() == petals.getFlowerAmount() ? 1 : petals.getFlowerAmount() + 1);
            petals.setFlowerAmount(session.getFlowerAmount());
            log(false, event.getPlayer(), event.getClickedBlock());
            event.getClickedBlock().setBlockData(petals, false);
            log(true, event.getPlayer(), event.getClickedBlock());
        } else {
            if (event.getPlayer().isSneaking()) {
                var placeableLocation = getPlaceableLocation(event.getClickedBlock().getLocation(), event.getBlockFace());
                if (canPlaceAt(placeableLocation)) {
                    var petals = (PinkPetals) getHandMat(event).createBlockData();
                    petals.setFlowerAmount(getSession(event.getPlayer().getUniqueId()).getFlowerAmount());
                    petals.setFacing(event.getPlayer().getFacing().getOppositeFace());
                    log(false, event.getPlayer(), placeableLocation.getBlock());
                    placeableLocation.getBlock().setType(getHandMat(event), false);
                    placeableLocation.getBlock().setBlockData(petals, false);
                    log(true, event.getPlayer(), placeableLocation.getBlock());
                }
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
