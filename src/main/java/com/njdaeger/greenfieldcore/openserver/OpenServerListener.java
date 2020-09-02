package com.njdaeger.greenfieldcore.openserver;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;

public class OpenServerListener implements Listener {

    private final OpenServerModule openserver;

    OpenServerListener(GreenfieldCore plugin, OpenServerModule module) {
        this.openserver = module;

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }



    private boolean cancel(Cancellable event, Player player) {
        if (openserver.isEnabled() && !player.hasPermission("greenfieldcore.openserver.bypass")) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void blockDamage(BlockDamageEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void blockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            if (cancel(event, (Player) event.getTargetEntity())) Util.notAllowed(event.getTargetEntity());
        }
    }

    @EventHandler
    public void blockFertilize(BlockFertilizeEvent event) {
        if (event.getPlayer() != null) {
            if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
        }
    }

    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() != null) {
            if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
        }
    }

    @EventHandler
    public void cauldronLevelChange(CauldronLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (cancel(event, (Player) event.getEntity())) Util.notAllowed(event.getEntity());
        }
    }

    @EventHandler
    public void enchantItem(EnchantItemEvent event) {
        cancel(event, event.getEnchanter());
    }

    @EventHandler
    public void entityBlockForm(EntityBlockFormEvent event) {
        if (event.getEntity() instanceof Player) cancel(event, (Player) event.getEntity());
    }

    @EventHandler
    public void entityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) cancel(event, (Player) event.getBreeder());
    }

    @EventHandler
    public void entityCombustByEntity(EntityCombustByEntityEvent event) {
        if (event.getCombuster() instanceof Player) cancel(event, (Player) event.getEntity());
    }

    @EventHandler
    public void playerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void playerEnterBed(PlayerBedEnterEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void playerBucketFill(PlayerBucketFillEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!openserver.isEnabled()) return;
        String[] message = event.getMessage().split(" ");
        if (message[0] != null) {
            if (!openserver.isCommandAllowed(message[0].substring(1))) {
                if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent event) {
        if (openserver.isEnabled() && !event.getPlayer().hasPermission("greenfieldcore.openserver.bypass"))
            event.getItemDrop().remove();
    }

    @EventHandler
    public void playerEditBook(PlayerEditBookEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerFishEvent(PlayerFishEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {

        Material type;
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
                break;
            case RIGHT_CLICK_BLOCK:
                type = event.getClickedBlock().getType();
                switch (type) {
                    case DARK_OAK_DOOR:
                    case DARK_OAK_TRAPDOOR:
                    case DARK_OAK_BUTTON:
                    case DARK_OAK_FENCE_GATE:
                    case OAK_DOOR:
                    case OAK_TRAPDOOR:
                    case OAK_BUTTON:
                    case OAK_FENCE_GATE:
                    case ACACIA_DOOR:
                    case ACACIA_TRAPDOOR:
                    case ACACIA_BUTTON:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_DOOR:
                    case BIRCH_TRAPDOOR:
                    case BIRCH_BUTTON:
                    case BIRCH_FENCE_GATE:
                    case JUNGLE_DOOR:
                    case JUNGLE_TRAPDOOR:
                    case JUNGLE_BUTTON:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_DOOR:
                    case SPRUCE_TRAPDOOR:
                    case SPRUCE_BUTTON:
                    case SPRUCE_FENCE_GATE:
                    case STONE_BUTTON:
                    case LEVER:
                        break;
                    default:
                        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
                        break;
                }
                break;
            case PHYSICAL:
                type = event.getClickedBlock().getType();
                switch (type) {
                    case STONE_PRESSURE_PLATE:
                    case DARK_OAK_PRESSURE_PLATE:
                    case OAK_PRESSURE_PLATE:
                    case ACACIA_PRESSURE_PLATE:
                    case BIRCH_PRESSURE_PLATE:
                    case JUNGLE_PRESSURE_PLATE:
                    case SPRUCE_PRESSURE_PLATE:
                    case HEAVY_WEIGHTED_PRESSURE_PLATE:
                    case LIGHT_WEIGHTED_PRESSURE_PLATE:
                        break;
                    default:
                        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
                        break;
                }
                break;
            default:
                if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
        }
    }

    @EventHandler
    public void hangingBreak(HangingBreakByEntityEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY && event.getRemover() instanceof Player) {
            if (cancel(event, (Player) event.getRemover())) Util.notAllowed(event.getRemover());

        }
    }

    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (cancel(event, (Player) event.getDamager())) Util.notAllowed(event.getDamager());
        }
    }

    @EventHandler
    public void interactEntity(PlayerInteractEntityEvent event) {
        if (cancel(event, event.getPlayer())) Util.notAllowed(event.getPlayer());
    }

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerItemDamage(PlayerItemDamageEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerLeashEntity(PlayerLeashEntityEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void entityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) cancel(event, (Player) event.getEntity());
    }

    @EventHandler
    public void playerPortal(PlayerPortalEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerDiscoverRecipe(PlayerRecipeDiscoverEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerShearEntity(PlayerShearEntityEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void playerUnleashEntity(PlayerUnleashEntityEvent event) {
        cancel(event, event.getPlayer());
    }

}
