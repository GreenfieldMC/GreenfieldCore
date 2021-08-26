package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class DefaultPlantHandler extends BlockHandler {

    public DefaultPlantHandler() {
        super(
                Material.OAK_SAPLING,
                Material.SPRUCE_SAPLING,
                Material.BIRCH_SAPLING,
                Material.JUNGLE_SAPLING,
                Material.ACACIA_SAPLING,
                Material.DARK_OAK_SAPLING,
                Material.GRASS,
                Material.FERN,
                Material.DEAD_BUSH,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.OXEYE_DAISY,
                Material.CORNFLOWER,
                Material.LILY_OF_THE_VALLEY,
                Material.WITHER_ROSE,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,
                Material.CRIMSON_FUNGUS,
                Material.WARPED_FUNGUS,
                Material.CRIMSON_ROOTS,
                Material.WARPED_ROOTS,
                Material.NETHER_SPROUTS,
                Material.SUGAR_CANE,
                Material.BAMBOO,
                Material.CACTUS,
                Material.LILY_PAD,
                Material.SPORE_BLOSSOM,
                Material.HANGING_ROOTS,
                Material.NETHER_WART,
                Material.TWISTING_VINES
        );
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(placeMaterial.createBlockData(), false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }
}
