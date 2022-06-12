package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.entity.Player;

public class AirHandler extends BlockHandler {

    public AirHandler() {
        super(Material.AIR);
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        BlockData data = clickedBlockLocation.getBlock().getBlockData();
        if (data instanceof Candle c && !c.isLit()) {
            //todo work on fix for not being able to turn off candle
            //todo vine handler
            Bukkit.getScheduler().runTaskLater(GreenfieldCore.getPlugin(GreenfieldCore.class), () -> {
                log(false, player, clickedBlockLocation.getBlock());
                c.setLit(true);
                clickedBlockLocation.getBlock().setBlockData(c, false);
                log(true, player, clickedBlockLocation.getBlock());
                playSoundFor(true, player, clickedBlockLocation.getBlock().getType());
                CandleHandler.CANDLE_SESSIONS.get(player.getUniqueId()).updateCandle(clickedBlockLocation.getBlock().getType(), c);
            }, 1);
            return true;
        }
        if (data instanceof CommandBlock c) {
            c.setConditional(!c.isConditional());
        }
        log(false, player, clickedBlockLocation.getBlock());
        clickedBlockLocation.getBlock().setBlockData(data, false);
        log(true, player, clickedBlockLocation.getBlock());
        playSoundFor(true, player, clickedBlockLocation.getBlock().getType());
        return true;
    }
}
