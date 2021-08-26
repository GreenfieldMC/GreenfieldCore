package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;

public class JigsawHandler extends BlockHandler {

    public JigsawHandler() {
        super(Material.JIGSAW);
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        Jigsaw j = (Jigsaw) placeMaterial.createBlockData();
        if (placementLocation.getBlock().getType() == placeMaterial) return false;
        j.setOrientation(getJigsawOrientation(clickedFace, player));
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(j, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return false;
    }
}
