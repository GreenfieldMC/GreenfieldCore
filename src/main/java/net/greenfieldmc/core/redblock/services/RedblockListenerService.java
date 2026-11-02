package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.serviceprovider.Nullable;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.Util;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfRedblockSearchRequest;
import net.greenfieldmc.core.redblock.RedblockMessages;
import net.greenfieldmc.core.shared.services.IUserService;
import net.greenfieldmc.core.shared.services.IVaultService;
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

import java.util.List;
import java.util.logging.Logger;

public class RedblockListenerService implements Listener {

    private final IGreenfieldCorePlugin plugin;
    private final IRedblockServiceV2 redblockService;
    private final IRedblockConfigService configService;
    private final IUserService userService;
    private final Logger logger;

    public RedblockListenerService(IGreenfieldCorePlugin plugin, IRedblockServiceV2 redblockService, IRedblockConfigService configService, IUserService userService, Logger logger) {
        this.plugin = plugin;
        this.redblockService = redblockService;
        this.configService = configService;
        this.userService = userService;
        this.logger = logger;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND) {
            //if the player right or left clicks a block, get a list of redblocks that are near the player and check if the block is one of them

            var block = e.getClickedBlock();
            if (block == null) return;

            var clickLocation = block.getLocation();
            var projectKey = configService.getProjectKey(clickLocation.getWorld());

            if (block.getType() == Material.RED_WOOL || block.getType() == Material.LIME_WOOL || block.getType() == Material.ORANGE_WOOL || block.getType() == Material.OAK_SIGN) {
                var possible = redblockService.getRedblocks(rb ->
                        rb.getProject().getProjectKey().equalsIgnoreCase(projectKey)
                        && rb.getVector().distanceSquared(clickLocation.toVector()) <= 12
                );


                var found = possible.stream().filter(rb -> rb.isPartOfRedblock(clickLocation.toVector())).findFirst();
                if (found.isEmpty()) return;

                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
                if (block.getType() == Material.OAK_SIGN) {
                    if (found.get().isIncomplete())
                        e.getPlayer().performCommand("rbcomplete flags: -id " + found.get().getKey());
                    else if (found.get().isPending()) {
                        if (e.getAction() == Action.LEFT_CLICK_BLOCK)
                            e.getPlayer().performCommand("rbapprove flags: -id " + found.get().getKey());
                        else if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                            e.getPlayer().performCommand("rbdeny flags: -id " + found.get().getKey());
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


        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            var gfUser = userService.getUserByUuid(player.getUniqueId());

            var search = new GfRedblockSearchRequest();
            search.statusFilter = new GfRedblockSearchRequest.StatusFilter(List.of("Incomplete"), GfRedblockSearchRequest.StatusFilter.MatchType.OR);
            search.resultsPerPage = 1;
            search.userAssignmentFilter = new GfRedblockSearchRequest.UserAssignmentFilter(List.of(gfUser.getUserId()), GfRedblockSearchRequest.UserAssignmentFilter.MatchType.OR);

            var result = redblockService.searchRedblocks(player.getWorld(), search).join();
            if (result.isFailure()) {
                logger.warning("Failed to search for redblocks for user " + player.getName() + " on join. Error: " + result.getErrorMessage());
                return;
            }

            var onlinePlayer = Bukkit.getPlayer(e.getPlayer().getName());
            if (onlinePlayer != null) onlinePlayer.sendMessage(RedblockMessages.REDBLOCK_JOIN_NOTIFICATION.apply((int) result.getData().getTotalResults()));
        }, 100);
    }

}
