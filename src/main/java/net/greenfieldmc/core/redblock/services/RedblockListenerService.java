package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.Util;
import net.greenfieldmc.core.redblock.RedblockMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class RedblockListenerService extends ModuleService<RedblockListenerService> implements IModuleService<RedblockListenerService>, Listener {

    private final IRedblockService redblockService;

    public RedblockListenerService(Plugin plugin, Module module, IRedblockService redblockService) {
        super(plugin, module);
        this.redblockService = redblockService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND) {
            //if the player right or left clicks a block, get a list of redblocks that are near the player and check if the block is one of them

            var block = e.getClickedBlock();
            if (block == null) return;

            var location = block.getLocation();

            if (block.getType() == Material.RED_WOOL || block.getType() == Material.LIME_WOOL || block.getType() == Material.OAK_SIGN) {
                var possible = redblockService.getRedblocks(rb -> !rb.isDeleted()
                            && !rb.isApproved()
                            && rb.getLocation().getWorld().getUID().equals(location.getWorld().getUID())
                            && rb.getLocation().distanceSquared(location) <= 12
                );

                var found = possible.stream().filter(rb -> rb.isPartOfRedblock(location)).findFirst();
                if (found.isEmpty()) return;

                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
                if (block.getType() == Material.OAK_SIGN) {
                    if (found.get().isIncomplete())
                        e.getPlayer().performCommand("rbcomplete flags: -id " + found.get().getId());
                    else if (found.get().isPending()) {
                        if (e.getAction() == Action.LEFT_CLICK_BLOCK)
                            e.getPlayer().performCommand("rbapprove flags: -id " + found.get().getId());
                        else if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                            e.getPlayer().performCommand("rbdeny flags: -id " + found.get().getId());
                    }
                    return;
                }
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot destroy a redblock!");
            }

        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Util.userNameMap.put(player.getUniqueId(), player.getName());
        var redblocks = redblockService.getRedblocks(rb -> rb.getAssignedTo() != null
                && rb.getAssignedTo().equals(player.getUniqueId())
                && rb.isIncomplete());
        if (redblocks.isEmpty()) return;

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            var onlinePlayer = Bukkit.getPlayer(e.getPlayer().getName());
            if (onlinePlayer != null) onlinePlayer.sendMessage(RedblockMessages.REDBLOCK_JOIN_NOTIFICATION.apply(redblocks.size()));
        }, 100);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
