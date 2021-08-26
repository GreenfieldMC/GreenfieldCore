package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;

public class SwitchHandler extends BlockHandler {

    public SwitchHandler() {
        super(
                Material.LEVER,
                Material.ACACIA_BUTTON,
                Material.BIRCH_BUTTON,
                Material.CRIMSON_BUTTON,
                Material.DARK_OAK_BUTTON,
                Material.JUNGLE_BUTTON,
                Material.OAK_BUTTON,
                Material.POLISHED_BLACKSTONE_BUTTON,
                Material.SPRUCE_BUTTON,
                Material.STONE_BUTTON,
                Material.WARPED_BUTTON
        );

    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        Switch swtch = (Switch) placeMaterial.createBlockData();

        swtch.setFacing(clickedFace);
        if (clickedFace == BlockFace.DOWN) swtch.setAttachedFace(FaceAttachable.AttachedFace.CEILING);
        else if (clickedFace == BlockFace.UP) swtch.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
        else swtch.setAttachedFace(FaceAttachable.AttachedFace.WALL);

        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(swtch, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);

        return true;
    }
}
