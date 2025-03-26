package com.njdaeger.greenfieldcore.powershovel;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.services.ICoreProtectService;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class PowerShovelService extends ModuleService<PowerShovelService> implements IModuleService<PowerShovelService>, Listener {

    private PlayerProfile PROFILE;
    private static final PowerShovel SHOVEL = new PowerShovel();
    private static final Map<UUID, Long> TIMEOUTS = new HashMap<>();

    private final ICoreProtectService coreProtectService;

    public PowerShovelService(Plugin plugin, Module module, ICoreProtectService coreProtectService) {
        super(plugin, module);
        this.coreProtectService = coreProtectService;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && PROFILE != null;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        module.getLogger().info("Loading custom head...");
        PROFILE = Bukkit.createProfile(UUID.fromString("e4ac149f-2815-4e06-8ce1-770a628a2cfc"));
        var texture = PROFILE.getTextures();
        texture.setSkin(URI.create("http://textures.minecraft.net/texture/633c0bb37ebe1193ee4618103460a7f129277a8c7fd081b6aedb34a92bd5").toURL());
        PROFILE.setTextures(texture);
        PROFILE.complete();
        module.getLogger().info("Custom head loaded.");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        CommandBuilder.of("powershovel", "ps")
                .permission("greenfieldcore.powershovel")
                .description("Give yourself a power shovel")
                .canExecute(ctx -> {
                    var player = ctx.asPlayer();
                    ctx.send(moduleMessage("PowerShovel", "You now have the power shovel."));
                    player.getInventory().addItem(SHOVEL);
                })
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {
        if (!isEnabled()) return;
        //Check if the action is a right click on a block, and if it is with the power shovel.
        if (e.getPlayer().hasPermission("greenfieldcore.powershovel") && SHOVEL.equals(e.getPlayer().getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            BlockFace clickedFace = e.getBlockFace();
            Block clickedBlock = e.getClickedBlock();

            //If the clicked block is null, or if it is in the off hand, we just return and dont do anything.
            if (clickedBlock == null || e.getHand() == EquipmentSlot.OFF_HAND) return;

            Long tmt = TIMEOUTS.get(e.getPlayer().getUniqueId());
            if (tmt == null) TIMEOUTS.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
            else if (System.currentTimeMillis() - tmt > 500) TIMEOUTS.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
            else return;

            if (clickedFace != BlockFace.EAST && clickedFace != BlockFace.NORTH && clickedFace != BlockFace.SOUTH && clickedFace != BlockFace.WEST) {
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot click on that block face. You must click on any of the following: East, North, South, or West.");
                return;
            }

            if (clickedBlock.getBlockData() instanceof Fence fence) { //if they right click a fence, assume they are placing a transformer
                coreProtectService.logRemoval(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
                fence.setFace(clickedFace, true);
                clickedBlock.setBlockData(fence, false);
                coreProtectService.logPlacement(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());

                Location transformerLocation = new Location(clickedBlock.getWorld(), clickedBlock.getX() + clickedFace.getDirection().getBlockX(), clickedBlock.getY(), clickedBlock.getZ()  + clickedFace.getDirection().getBlockZ());
                coreProtectService.logRemoval(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
                transformerLocation.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL, false);
                Wall wall = (Wall) transformerLocation.getBlock().getBlockData();
                wall.setUp(true);
                wall.setHeight(clickedFace.getOppositeFace(), Wall.Height.LOW);
                transformerLocation.getBlock().setBlockData(wall, false);
                coreProtectService.logPlacement(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());

                transformerLocation = new Location(transformerLocation.getWorld(), transformerLocation.getX(), transformerLocation.getY() + 1, transformerLocation.getZ());
                coreProtectService.logRemoval(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
                transformerLocation.getBlock().setType(Material.PLAYER_HEAD);
                Skull skull = (Skull) transformerLocation.getBlock().getState();

                skull.setPlayerProfile(PROFILE);
                skull.update();
                coreProtectService.logPlacement(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
            }

            else {
                Location meterLocation = new Location(clickedBlock.getWorld(), clickedBlock.getX() + clickedFace.getDirection().getBlockX(), clickedBlock.getY(), clickedBlock.getZ()  + clickedFace.getDirection().getBlockZ());
                coreProtectService.logRemoval(e.getPlayer().getName(), meterLocation, meterLocation.getBlock().getType(), meterLocation.getBlock().getBlockData());
                meterLocation.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL, false);
                Wall wall = (Wall) meterLocation.getBlock().getBlockData();
                wall.setUp(true);
                if (clickedFace != BlockFace.NORTH) wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
                if (clickedFace != BlockFace.SOUTH) wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
                if (clickedFace != BlockFace.EAST) wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
                if (clickedFace != BlockFace.WEST) wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
                meterLocation.getBlock().setBlockData(wall, false);
                coreProtectService.logPlacement(e.getPlayer().getName(), meterLocation, meterLocation.getBlock().getType(), meterLocation.getBlock().getBlockData());
            }
        }
    }

}
