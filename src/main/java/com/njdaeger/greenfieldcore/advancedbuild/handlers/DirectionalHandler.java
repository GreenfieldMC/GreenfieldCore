package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

public class DirectionalHandler extends BlockHandler {

    public DirectionalHandler() {
        super(
                Material.LADDER,
                Material.BIG_DRIPLEAF,
                Material.TRIPWIRE_HOOK,
                Material.REPEATER,
                Material.COMPARATOR
        );
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        Directional directional = (Directional) placeMaterial.createBlockData();
        directional.setFacing(player.getFacing().getOppositeFace());

        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(directional, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }
}
