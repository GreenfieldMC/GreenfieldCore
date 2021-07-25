package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.pdk.command.CommandBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.function.BiPredicate;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.LIGHT_PURPLE;

public class AdvancedBuildModule extends Module implements Listener {

    private AdvBuildConfig config;
    private List<BiPredicate<PlayerInteractEvent, Material>> ignoreList = List.of(
            (playerInteractEvent, handMaterial) -> Tag.BEDS.isTagged(handMaterial) || Tag.TRAPDOORS.isTagged(handMaterial)
    );

    public AdvancedBuildModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.config = new AdvBuildConfig(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        CommandBuilder.of("advbuild", "avb")
                .executor(context -> {
                    config.setEnabled(context.asPlayer(), !config.isEnabledFor(context.asPlayer()));
                    context.send(LIGHT_PURPLE + "[AdvBuild] " + GRAY + (config.isEnabledFor(context.asPlayer()) ? "Enabled Advanced Building." : "Disabled Advanced Building."));
                })
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

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        Material newType = switch (e.getBlockPlaced().getType()) {
            case COPPER_BLOCK -> Material.WAXED_COPPER_BLOCK;
            case EXPOSED_COPPER -> Material.WAXED_EXPOSED_COPPER;
            case WEATHERED_COPPER -> Material.WAXED_WEATHERED_COPPER;
            case OXIDIZED_COPPER -> Material.WAXED_OXIDIZED_COPPER;
            case CUT_COPPER -> Material.WAXED_CUT_COPPER;
            case EXPOSED_CUT_COPPER ->  Material.WAXED_EXPOSED_CUT_COPPER;
            case WEATHERED_CUT_COPPER -> Material.WAXED_WEATHERED_CUT_COPPER;
            case OXIDIZED_CUT_COPPER -> Material.WAXED_OXIDIZED_CUT_COPPER;
            case CUT_COPPER_STAIRS -> Material.WAXED_CUT_COPPER_STAIRS;
            case EXPOSED_CUT_COPPER_STAIRS -> Material.WAXED_EXPOSED_CUT_COPPER_STAIRS;
            case WEATHERED_CUT_COPPER_STAIRS -> Material.WAXED_WEATHERED_CUT_COPPER_STAIRS;
            case OXIDIZED_CUT_COPPER_STAIRS -> Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS;
            case CUT_COPPER_SLAB -> Material.WAXED_CUT_COPPER_SLAB;
            case EXPOSED_CUT_COPPER_SLAB -> Material.WAXED_EXPOSED_CUT_COPPER_SLAB;
            case WEATHERED_CUT_COPPER_SLAB -> Material.WAXED_WEATHERED_CUT_COPPER_SLAB;
            case OXIDIZED_CUT_COPPER_SLAB -> Material.WAXED_OXIDIZED_CUT_COPPER_SLAB;
            default -> null;
        };
        if (newType != null) {
            BlockData newData = Bukkit.createBlockData(e.getBlockPlaced().getBlockData().getAsString().replace("minecraft:", "minecraft:waxed_"));
            Location loc = e.getBlock().getLocation().clone();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (switch (loc.getBlock().getType()) {
                    case COPPER_BLOCK, EXPOSED_COPPER, WEATHERED_COPPER, OXIDIZED_COPPER -> false;
                    case CUT_COPPER, EXPOSED_CUT_COPPER, WEATHERED_CUT_COPPER, OXIDIZED_CUT_COPPER -> false;
                    case CUT_COPPER_STAIRS, EXPOSED_CUT_COPPER_STAIRS, WEATHERED_CUT_COPPER_STAIRS, OXIDIZED_CUT_COPPER_STAIRS -> false;
                    case CUT_COPPER_SLAB, EXPOSED_CUT_COPPER_SLAB, WEATHERED_CUT_COPPER_SLAB, OXIDIZED_CUT_COPPER_SLAB -> false;
                    default -> true;
                }) return;
                plugin.getCoreApi().logRemoval(e.getPlayer().getName(), loc, loc.getBlock().getType(), loc.getBlock().getBlockData());
                loc.getBlock().setType(newType, false);
                loc.getBlock().setBlockData(newData, false);
                plugin.getCoreApi().logPlacement(e.getPlayer().getName(), e.getBlock().getLocation(), newType, newData);
            }, 1);
        }
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

            //So we can add as many negations as we want later, we just make it a list of negation bipredicates
            if (ignoreList.stream().anyMatch(biPred -> biPred.test(e, handMat))) return;
            if (placeBlock(clickedBlock.getLocation(), placeableLocation, handMat, e.getPlayer(), face)) {
                e.getPlayer().playSound(placeableLocation, Sound.ENTITY_ITEM_PICKUP, 1.1f, 2.f);
                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
            }

        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) return;
            plugin.getCoreApi().logRemoval(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
            clickedBlock.setType(Material.AIR, false);
            e.getPlayer().playSound(clickedBlock.getLocation(), Sound.ENTITY_ITEM_PICKUP, .1f, 2.f);
            plugin.getCoreApi().logPlacement(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
        }
    }

    private boolean placeBlock(Location clickedBlockLocation, Location placementLocation, Material material, Player player, BlockFace clickedFace) {
        if (!canPlaceAt(placementLocation, material, player)) return false;
        Material oldType = placementLocation.getBlock().getType();
        BlockData oldData = placementLocation.getBlock().getBlockData().clone();
        Material pairedOldType = null;
        BlockData pairedOldData = null;

        BlockFace facing = player.getFacing();
        BlockData data = material.createBlockData();
        BlockData pairedData = null;
        Location pairedLocation = null;

        boolean blockPlaced = false;
        if (data instanceof Bisected) {
            pairedData = material.createBlockData();
            pairedLocation = placementLocation.clone().add(0, 1, 0);

            pairedOldType = pairedLocation.getBlock().getType();
            pairedOldData = pairedLocation.getBlock().getBlockData().clone();

            ((Bisected)pairedData).setHalf(((Bisected) data).getHalf() == Bisected.Half.BOTTOM ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
            placementLocation.getBlock().setType(material, false);
            pairedLocation.getBlock().setType(material, false);
            pairedLocation.getBlock().setBlockData(pairedData, false);
            blockPlaced = true;
        }

        switch (material) {
            case AIR:
                if (clickedBlockLocation.getBlock().getBlockData() instanceof Candle c) {
                    c.setLit(!c.isLit());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> clickedBlockLocation.getBlock().setBlockData(c, false), 2);
                    return true;
                }
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
                placementLocation.getBlock().setType(material, false);
                placementLocation.getBlock().setBlockData(data, false);
                blockPlaced = true;
                break;
            case OBSERVER:
                ((Directional) data).setFacing(get3DDirection(player).getOppositeFace());
                placementLocation.getBlock().setType(material, false);
                placementLocation.getBlock().setBlockData(data, false);
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
            case SPORE_BLOSSOM:
            case HANGING_ROOTS:
            case NETHER_WART:
                placementLocation.getBlock().setType(material, false);
                placementLocation.getBlock().setBlockData(data, false);
                blockPlaced = true;
                //TODO torches/levers/buttons
        }

        if (data instanceof Candle c) {
            switch (material) {
                case LIME_CANDLE, PINK_CANDLE, GRAY_CANDLE, LIGHT_GRAY_CANDLE, RED_CANDLE, PURPLE_CANDLE:
                    switch (clickedFace) {
                        case EAST -> c.setCandles(1);
                        case NORTH -> c.setCandles(2);
                        case WEST -> c.setCandles(3);
                        case SOUTH -> c.setCandles(4);
                        case UP, DOWN -> {
                            switch (player.getFacing()) {
                                case EAST -> c.setCandles(1);
                                case NORTH -> c.setCandles(2);
                                case WEST -> c.setCandles(3);
                                case SOUTH -> c.setCandles(4);
                            }
                        }
                    }
                    break;
                case CYAN_CANDLE:
                    switch (clickedFace) {
                        case EAST, WEST -> c.setCandles(1);
                        case NORTH, SOUTH -> c.setCandles(2);
                        case UP, DOWN -> {
                            switch (player.getFacing()) {
                                case EAST, WEST -> c.setCandles(1);
                                case NORTH, SOUTH -> c.setCandles(2);
                            }
                        }
                    }
            }
            placementLocation.getBlock().setType(material, false);
            placementLocation.getBlock().setBlockData(data, false);
            blockPlaced = true;


            /*
            directional:

            1 candle = light towards east
            2 candles = light towards north
            3 candles = light towards west
            4 candles = light towards west

            - lime
            - pink
            - gray
            - light gray
            - cyan
            - red
            - purple
             */

        }

        if (data instanceof Jigsaw j) {
            if (placementLocation.getBlock().getType() == material) return false;
            j.setOrientation(getJigsawOrientation(clickedFace, player));
            placementLocation.getBlock().setType(material, false);
            placementLocation.getBlock().setBlockData(data, false);
            return true;
        }
        if (data instanceof CommandBlock c) {
            c.setFacing(get3DDirection(player));
            placementLocation.getBlock().setType(material, false);
            placementLocation.getBlock().setBlockData(data, false);
            return true;
        }

        if (data instanceof Directional d && !material.name().endsWith("_WALL_FAN") && !material.name().contains("SIGN")) {
            if (pairedData != null) {
                d.setFacing(facing);
                pairedLocation.getBlock().setBlockData(pairedData, false);
            }
            d.setFacing(facing);
            placementLocation.getBlock().setType(material, false);
            placementLocation.getBlock().setBlockData(data, false);
            blockPlaced = true;
        }
        if (data instanceof Waterlogged w && !material.name().contains("SIGN")) {
            if (pairedData != null) {
                w.setWaterlogged(false);
                pairedLocation.getBlock().setBlockData(pairedData, false);
            }
            placementLocation.getBlock().setType(material, false);
            w.setWaterlogged(false);
            placementLocation.getBlock().setBlockData(data, false);
            blockPlaced = true;
        }
        if (blockPlaced) {
            plugin.getCoreApi().logRemoval(plugin.getName(), placementLocation, oldType, oldData);
            plugin.getCoreApi().logPlacement(player.getName(), placementLocation, placementLocation.getBlock().getType(), data);
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

    private boolean canPlaceAt(Location location, Material material, Player player) {
        BlockData data = material.createBlockData();
        if (!location.getBlock().getType().isAir() || location.getBlock().getType().isSolid()) return false;
        if (data instanceof Bisected) {
            return location.getBlockY() != 255 && (location.clone().add(0, 1, 0).getBlock().getType().isAir() || !location.clone().add(0, 1, 0).getBlock().getType().isSolid());
        }
        return true;
    }

}
