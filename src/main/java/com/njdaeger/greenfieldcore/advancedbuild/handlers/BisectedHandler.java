package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Player;

public class BisectedHandler extends BlockHandler {

    public BisectedHandler() {
        super(
                Material.SMALL_DRIPLEAF,
                Material.SUNFLOWER,
                Material.LILAC,
                Material.ROSE_BUSH,
                Material.PEONY,
                Material.TALL_GRASS,
                Material.LARGE_FERN
        );
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        Bisected bisected = (Bisected) placeMaterial.createBlockData();
        Location pairedLocation = placementLocation.clone().add(0, 1, 0);
        if (!canPlaceAt(placementLocation, placeMaterial)) return false;
        bisected.setHalf(Bisected.Half.BOTTOM);
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(bisected, false);
        log(true, player, placementLocation.getBlock());
        bisected.setHalf(Bisected.Half.TOP);
        log(false, player, pairedLocation.getBlock());
        pairedLocation.getBlock().setType(placeMaterial, false);
        pairedLocation.getBlock().setBlockData(bisected, false);
        log(true, player, pairedLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }
}
