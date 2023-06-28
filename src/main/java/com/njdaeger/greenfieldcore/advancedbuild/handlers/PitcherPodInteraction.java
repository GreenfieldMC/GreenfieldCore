package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.PitcherCrop;
import org.bukkit.event.player.PlayerInteractEvent;

public class PitcherPodInteraction extends InteractionHandler {

    public PitcherPodInteraction() {
        super(Material.PITCHER_POD);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) {
            if (event.getClickedBlock().getBlockData() instanceof PitcherCrop crop) {
                crop.setAge(crop.getMaximumAge() == crop.getAge() ? 0 : crop.getAge() + 1);
                var secondHalf = crop.getHalf() == PitcherCrop.Half.BOTTOM ? PitcherCrop.Half.TOP : PitcherCrop.Half.BOTTOM;
                var secondHalfLoc = secondHalf == Bisected.Half.BOTTOM
                        ? event.getClickedBlock().getLocation().clone().subtract(0, 1, 0)
                        : event.getClickedBlock().getLocation().clone().add(0, 1, 0);
                if (crop.getAge() >= 3 && canPlaceAt(secondHalfLoc)) {
                    var secondHalfData = (PitcherCrop) crop.clone();
                    secondHalfData.setHalf(secondHalf);
                    placeBlockAt(event.getPlayer(), secondHalfLoc, Material.PITCHER_CROP, secondHalfData);
                    placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), Material.PITCHER_CROP, crop);
                } else {
                    if (secondHalf == Bisected.Half.BOTTOM) crop.setHalf(PitcherCrop.Half.BOTTOM);
                    placeBlockAt(event.getPlayer(), secondHalf == Bisected.Half.BOTTOM ? secondHalfLoc : event.getClickedBlock().getLocation(), Material.PITCHER_CROP, crop);
                    if (crop.getAge() == 0 && secondHalfLoc.getBlock().getType() == Material.PITCHER_CROP) {
                        placeBlockAt(event.getPlayer(), secondHalf == Bisected.Half.BOTTOM ? event.getClickedBlock().getLocation() : secondHalfLoc, Material.AIR);
                    }
                }

            }
        } else {
            placeBlockAt(event.getPlayer(), getPlaceableLocation(event), Material.PITCHER_CROP);
        }
    }
}
