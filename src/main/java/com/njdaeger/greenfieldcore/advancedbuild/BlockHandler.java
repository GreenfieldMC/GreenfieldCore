package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class BlockHandler {

    private final List<Material> handledMaterials;

    public BlockHandler(Material... materials) {
        this.handledMaterials = List.of(materials);
    }

    public boolean canHandleMaterial(Material material) {
        return handledMaterials.contains(material);
    }

    public abstract boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial);

    public final void playSoundFor(boolean placement, Player player, Material material) {
        player.playSound(player.getLocation(), placement ? material.createBlockData().getSoundGroup().getPlaceSound() : material.createBlockData().getSoundGroup().getBreakSound(), .1f, 2.f);
    }

    public final BlockFace get3DDirection(Player player) {
        if (player.getLocation().getPitch() > 45) return BlockFace.DOWN;
        else if (player.getLocation().getPitch() < -45) return BlockFace.UP;
        else return player.getFacing();
    }

    public final void log(boolean placement, Player player, Block changedBlock) {
        if (placement) GreenfieldCore.getPlugin(GreenfieldCore.class).getCoreApi().logPlacement(player.getName(), changedBlock.getLocation(), changedBlock.getType(), changedBlock.getBlockData());
        else GreenfieldCore.getPlugin(GreenfieldCore.class).getCoreApi().logRemoval(player.getName(), changedBlock.getLocation(), changedBlock.getType(), changedBlock.getBlockData());
    }

    public final Jigsaw.Orientation getJigsawOrientation(BlockFace clickedFace, Player player) {
        return switch (clickedFace) {
            case EAST -> Jigsaw.Orientation.EAST_UP;
            case SOUTH -> Jigsaw.Orientation.SOUTH_UP;
            case WEST -> Jigsaw.Orientation.WEST_UP;
            case DOWN -> switch (player.getFacing()) {
                case EAST -> Jigsaw.Orientation.DOWN_EAST;
                case SOUTH -> Jigsaw.Orientation.DOWN_SOUTH;
                case WEST -> Jigsaw.Orientation.DOWN_WEST;
                default -> Jigsaw.Orientation.NORTH_UP;
            };
            case UP -> switch (player.getFacing()) {
                case EAST -> Jigsaw.Orientation.UP_EAST;
                case SOUTH -> Jigsaw.Orientation.UP_SOUTH;
                case WEST -> Jigsaw.Orientation.UP_WEST;
                default -> Jigsaw.Orientation.UP_NORTH;
            };
            default -> Jigsaw.Orientation.NORTH_UP;
        };
    }

    public final boolean canPlaceAt(Location location, Material material) {
        BlockData data = material.createBlockData();
        if (!location.getBlock().getType().isAir() || location.getBlock().getType().isSolid()) return false;
        if (data instanceof Bisected) {
            return location.getBlockY() != 255 && (location.clone().add(0, 1, 0).getBlock().getType().isAir() || !location.clone().add(0, 1, 0).getBlock().getType().isSolid());
        }
        return true;
    }

}
