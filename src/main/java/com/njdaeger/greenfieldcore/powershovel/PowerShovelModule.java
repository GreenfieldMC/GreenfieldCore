package com.njdaeger.greenfieldcore.powershovel;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Wall;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class PowerShovelModule extends Module implements Listener {


    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("e4ac149f-2815-4e06-8ce1-770a628a2cfc"), "Powerline Insulator");
    private static final PowerShovel SHOVEL = new PowerShovel();
    private Field profileField;
    private static final Map<UUID, Long> TIMEOUTS = new HashMap<>();

    static {
        PROFILE.getProperties().put("textures", new Property("textures", new String(Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/633c0bb37ebe1193ee4618103460a7f129277a8c7fd081b6aedb34a92bd5").getBytes()))));
    }

    public PowerShovelModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        CommandBuilder.of("powershovel", "ps", "powerconnector")
                .executor(this::powerShovelCommand)
                .permissions("greenfieldcore.powershovel")
                .usage("/powershovel")
                .description("Gives you a power shovel for easy power connectors.")
                .register(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable() {

    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {

        //Check if the action is a right click on a block, and if it is with the power shovel.
        if (SHOVEL.equals(e.getPlayer().getInventory().getItemInMainHand())) {
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

            if (clickedBlock.getBlockData() instanceof Fence) { //if they right click a fence, assume they are placing a transformer
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
                Fence fence = (Fence) clickedBlock.getBlockData();
                fence.setFace(clickedFace, true);
                clickedBlock.setBlockData(fence, false);
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());

                Location transformerLocation = new Location(clickedBlock.getWorld(), clickedBlock.getX() + clickedFace.getDirection().getBlockX(), clickedBlock.getY(), clickedBlock.getZ()  + clickedFace.getDirection().getBlockZ());
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
                transformerLocation.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL, false);
                Wall wall = (Wall) transformerLocation.getBlock().getBlockData();
                wall.setUp(true);
                wall.setHeight(clickedFace.getOppositeFace(), Wall.Height.LOW);
                transformerLocation.getBlock().setBlockData(wall, false);
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());

                transformerLocation = new Location(transformerLocation.getWorld(), transformerLocation.getX(), transformerLocation.getY() + 1, transformerLocation.getZ());
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
                transformerLocation.getBlock().setType(Material.PLAYER_HEAD);
                Skull skull = (Skull) transformerLocation.getBlock().getState();

                try {
                    if (profileField == null) {
                        profileField = skull.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                    }
                    profileField.set(skull, PROFILE);
                } catch (NoSuchFieldException | IllegalAccessException noSuchFieldException) {
                    noSuchFieldException.printStackTrace();
                }
                skull.update(false, false);

                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
            }

            else {
                Location meterLocation = new Location(clickedBlock.getWorld(), clickedBlock.getX() + clickedFace.getDirection().getBlockX(), clickedBlock.getY(), clickedBlock.getZ()  + clickedFace.getDirection().getBlockZ());
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), meterLocation, meterLocation.getBlock().getType(), meterLocation.getBlock().getBlockData());
                meterLocation.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL, false);
                Wall wall = (Wall) meterLocation.getBlock().getBlockData();
                wall.setUp(true);
                if (clickedFace != BlockFace.NORTH) wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
                if (clickedFace != BlockFace.SOUTH) wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
                if (clickedFace != BlockFace.EAST) wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
                if (clickedFace != BlockFace.WEST) wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
                meterLocation.getBlock().setBlockData(wall, false);
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), meterLocation, meterLocation.getBlock().getType(), meterLocation.getBlock().getBlockData());
            }
        }
    }

    private void powerShovelCommand(CommandContext context) {
        Player player = context.asPlayer();
        context.send(ChatColor.LIGHT_PURPLE + "[PowerShovel] " + ChatColor.GRAY + "You now have the power shovel.");
        player.getInventory().addItem(SHOVEL);
    }

}
