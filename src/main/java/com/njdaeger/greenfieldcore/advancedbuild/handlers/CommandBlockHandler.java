package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.entity.Player;

public class CommandBlockHandler extends BlockHandler {

    public CommandBlockHandler() {
        super(
                Material.COMMAND_BLOCK,
                Material.CHAIN_COMMAND_BLOCK,
                Material.REPEATING_COMMAND_BLOCK
        );
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        CommandBlock data = (CommandBlock) placeMaterial.createBlockData();
        data.setFacing(getFacingDirection(player));
        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(data, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }

    public BlockFace getFacingDirection(Player player) {
        if (player.getLocation().getPitch() > 45) return BlockFace.UP;
        else if (player.getLocation().getPitch() < -45) return BlockFace.DOWN;
        else return player.getFacing();
    }

}
