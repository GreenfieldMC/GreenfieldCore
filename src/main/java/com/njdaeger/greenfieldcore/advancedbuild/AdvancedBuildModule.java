package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.advancedbuild.handlers.*;
import com.njdaeger.pdk.command.CommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Optional;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.LIGHT_PURPLE;

public class AdvancedBuildModule extends Module implements Listener {

    private AdvBuildConfig config;
    private final List<BlockHandler> blockHandlers = List.of(
            new AirHandler(),
            new BisectedHandler(),
            new CandleHandler(),
            new CommandBlockHandler(),
            new CoralHandler(),
            new DefaultPlantHandler(),
            new JigsawHandler(),
            new ObserverHandler(),
            new DirectionalHandler(),
            new SwitchHandler(),
            new TorchHandler()
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
        switch (e.getBlockPlaced().getType()) {
            case BUDDING_AMETHYST, SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD, AMETHYST_CLUSTER -> {
                e.setBuild(false);
                e.setCancelled(true);
                return;
            }
        }
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
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), loc, loc.getBlock().getType(), loc.getBlock().getBlockData());
                loc.getBlock().setType(newType, false);
                loc.getBlock().setBlockData(newData, false);
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), e.getBlock().getLocation(), newType, newData);
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

            Optional<BlockHandler> handler = blockHandlers.stream().filter(hdlr -> hdlr.canHandleMaterial(handMat)).findFirst();
            if (handler.isPresent() && handler.get().handleBlock(e.getPlayer(), clickedBlock.getLocation(), placeableLocation, face, handMat)) {
                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
            }
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.getPlayer().isSneaking() && e.getClickedBlock() != null && e.getClickedBlock().getBlockData() instanceof Candle c) {
//            if (!(e.getClickedBlock().getBlockData() instanceof Candle c)) return;
            //todo right click on rail to rotate
            //todo if it has a block below it, minecraft adds a candle so stop that somehow
            //todo lighting rod place facing down if placed on top of block and if placed on/beside another lightning rod, do opposite of its direction
            c.setCandles(c.getCandles() == c.getMaximumCandles() ? 1 : c.getCandles() + 1);
            if (!CandleHandler.CANDLE_SESSIONS.containsKey(e.getPlayer().getUniqueId())) CandleHandler.CANDLE_SESSIONS.put(e.getPlayer().getUniqueId(), new CandleHandler.CandleSession());
            CandleHandler.CANDLE_SESSIONS.get(e.getPlayer().getUniqueId()).updateCandle(e.getClickedBlock().getType(), c);
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) return;
            if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
            clickedBlock.setBlockData(c, false);
            if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) return;
            if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
            clickedBlock.setType(Material.AIR, false);
            e.getPlayer().playSound(clickedBlock.getLocation(), clickedBlock.getBlockData().getSoundGroup().getBreakSound(), .1f, 2.f);
            if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
        }
    }

}
