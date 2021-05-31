package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.LIGHT_PURPLE;

public class AdvancedBuildModule extends Module implements Listener {

    private AdvBuildConfig config;

    public AdvancedBuildModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.config = new AdvBuildConfig(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        CommandBuilder.of("advbuild", "avb")
                .executor(this::advBuildCmd)
                .permissions("greenfieldcore.advbuild")
                .max(0)
                .usage("/advbuild")
                .description("Enables advanced building mode. (Placing blocks that aren't normally placeable without specific surfaces etc)")
                .build().register(plugin);
    }

    @Override
    public void onDisable() {
        config.save();
    }

    private void advBuildCmd(CommandContext context) {
        config.setEnabled(context.asPlayer(), !config.isEnabledFor(context.asPlayer()));
        context.send(LIGHT_PURPLE + "[AdvBuild] " + GRAY + (config.isEnabledFor(context.asPlayer()) ? "Enabled Advanced Building." : "Disabled Advanced Building."));
    }

    @EventHandler(ignoreCancelled = true)
    public void interact(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND || !config.isEnabledFor(e.getPlayer())) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking()) {

            BlockFace face = e.getBlockFace();
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) return;
            Location placeableLocation = new Location(e.getPlayer().getWorld(), clickedBlock.getX() + face.getDirection().getBlockX(), clickedBlock.getY() + face.getDirection().getBlockY(), clickedBlock.getZ() + face.getDirection().getBlockZ());
            Material handMat = e.getPlayer().getInventory().getItemInMainHand().getType();

            if (Tag.BEDS.isTagged(handMat)) return;
            if (placeBlock(placeableLocation, handMat, e.getPlayer(), face)) e.getPlayer().playSound(placeableLocation, Sound.ENTITY_ITEM_PICKUP, 1.1f, 2.f);

        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) return;
            plugin.getCoreApi().logRemoval(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
            clickedBlock.setType(Material.AIR, false);
            e.getPlayer().playSound(clickedBlock.getLocation(), Sound.ENTITY_ITEM_PICKUP, .1f, 2.f);
            plugin.getCoreApi().logPlacement(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
        }
    }

    private boolean placeBlock(Location location, Material material, Player player, BlockFace clickedFace) {
        if (!canPlaceAt(location, material, player)) return false;
        Material oldType = location.getBlock().getType();
        BlockData oldData = location.getBlock().getBlockData().clone();
        Material pairedOldType = null;
        BlockData pairedOldData = null;

        BlockFace facing = player.getFacing();
        BlockData data = material.createBlockData();
        BlockData pairedData = null;
        Location pairedLocation = null;

        boolean blockPlaced = false;
        if (data instanceof Bisected) {
            pairedData = material.createBlockData();
            pairedLocation = location.clone().add(0, 1, 0);

            pairedOldType = pairedLocation.getBlock().getType();
            pairedOldData = pairedLocation.getBlock().getBlockData().clone();

            ((Bisected)pairedData).setHalf(((Bisected) data).getHalf() == Bisected.Half.BOTTOM ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
            location.getBlock().setType(material, false);
            pairedLocation.getBlock().setType(material, false);
            pairedLocation.getBlock().setBlockData(pairedData, false);
            blockPlaced = true;
        }

        switch (material) {
            case DEAD_BRAIN_CORAL_FAN:
            case DEAD_BUBBLE_CORAL_FAN:
            case DEAD_FIRE_CORAL_FAN:
            case DEAD_HORN_CORAL_FAN:
            case DEAD_TUBE_CORAL_FAN:
                if (clickedFace != BlockFace.DOWN && clickedFace != BlockFace.UP) {
                    Material wallMat = Material.getMaterial(material.name().split("_FAN")[0] + "_WALL_FAN");
                    if (wallMat != null) {
                        material = wallMat;
                        data = material.createBlockData();
                        ((Directional) data).setFacing(clickedFace);
                    }
                }
                location.getBlock().setType(material, false);
                location.getBlock().setBlockData(data, false);
                blockPlaced = true;
                break;
            case OBSERVER:
                ((Directional) data).setFacing(get3DDirection(player).getOppositeFace());
                location.getBlock().setType(material, false);
                location.getBlock().setBlockData(data, false);
                return true;
            case OAK_SAPLING:
            case SPRUCE_SAPLING:
            case BIRCH_SAPLING:
            case JUNGLE_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case GRASS:
            case FERN:
            case DEAD_BUSH:
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case WITHER_ROSE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case CRIMSON_FUNGUS:
            case WARPED_FUNGUS:
            case CRIMSON_ROOTS:
            case WARPED_ROOTS:
            case NETHER_SPROUTS:
            case SUGAR_CANE:
            case BAMBOO:
            case CACTUS:
            case LILY_PAD:
                location.getBlock().setType(material, false);
                location.getBlock().setBlockData(data, false);
                blockPlaced = true;
        }

        if (data instanceof Jigsaw) {
            if (location.getBlock().getType() == material) return false;
            ((Jigsaw) data).setOrientation(getJigsawOrientation(clickedFace, player));
            location.getBlock().setType(material, false);
            location.getBlock().setBlockData(data, false);
            return true;
        }
        if (data instanceof CommandBlock) {
            ((CommandBlock) data).setFacing(get3DDirection(player));
            location.getBlock().setType(material, false);
            location.getBlock().setBlockData(data, false);
            return true;
        }

        if (data instanceof Directional && !material.name().endsWith("_WALL_FAN") && !material.name().contains("SIGN")) {
            if (pairedData != null) {
                ((Directional)pairedData).setFacing(facing);
                pairedLocation.getBlock().setBlockData(pairedData, false);
            }
            ((Directional) data).setFacing(facing);
            location.getBlock().setType(material, false);
            location.getBlock().setBlockData(data, false);
            blockPlaced = true;
        }
        if (data instanceof Waterlogged && !material.name().contains("SIGN")) {
            if (pairedData != null) {
                ((Waterlogged) pairedData).setWaterlogged(false);
                pairedLocation.getBlock().setBlockData(pairedData, false);
            }
            location.getBlock().setType(material, false);
            ((Waterlogged) data).setWaterlogged(false);
            location.getBlock().setBlockData(data, false);
            blockPlaced = true;
        }
        if (blockPlaced) {
            plugin.getCoreApi().logRemoval(plugin.getName(), location, oldType, oldData);
            plugin.getCoreApi().logPlacement(player.getName(), location, location.getBlock().getType(), data);
            if (pairedData != null) {
                plugin.getCoreApi().logRemoval(plugin.getName(), pairedLocation, pairedOldType, pairedOldData);
                plugin.getCoreApi().logPlacement(player.getName(), pairedLocation, pairedLocation.getBlock().getType(), pairedData);
            }

        }
        return blockPlaced;
    }

    private BlockFace get3DDirection(Player player) {
        if (player.getLocation().getPitch() > 45) return BlockFace.DOWN;
        else if (player.getLocation().getPitch() < -45) return BlockFace.UP;
        else return player.getFacing();
    }

    private Jigsaw.Orientation getJigsawOrientation(BlockFace clickedFace, Player player) {
        switch (clickedFace) {
            case NORTH:
                return Jigsaw.Orientation.NORTH_UP;
            case EAST:
                return Jigsaw.Orientation.EAST_UP;
            case SOUTH:
                return Jigsaw.Orientation.SOUTH_UP;
            case WEST:
                return Jigsaw.Orientation.WEST_UP;
            case DOWN:
                switch (player.getFacing()) {
                    case NORTH:
                        return Jigsaw.Orientation.DOWN_NORTH;
                    case EAST:
                        return Jigsaw.Orientation.DOWN_EAST;
                    case SOUTH:
                        return Jigsaw.Orientation.DOWN_SOUTH;
                    case WEST:
                        return Jigsaw.Orientation.DOWN_WEST;
                }
            case UP:
                switch (player.getFacing()) {
                    case NORTH:
                        return Jigsaw.Orientation.UP_NORTH;
                    case EAST:
                        return Jigsaw.Orientation.UP_EAST;
                    case SOUTH:
                        return Jigsaw.Orientation.UP_SOUTH;
                    case WEST:
                        return Jigsaw.Orientation.UP_WEST;
                }
            default:
                return Jigsaw.Orientation.NORTH_UP;
        }
    }

    private boolean canPlaceAt(Location location, Material material, Player player) {
        BlockData data = material.createBlockData();
        if (!location.getBlock().getType().isAir() || location.getBlock().getType().isSolid()) return false;
        if (data instanceof Bisected) {
            return location.getBlockY() != 255 && (location.clone().add(0, 1, 0).getBlock().getType().isAir() || !location.clone().add(0, 1, 0).getBlock().getType().isSolid());
        }
        return true;
    }

}
