package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.pdk.utils.Text;
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

import java.util.ArrayList;
import java.util.List;

public class RedblockListener implements Listener {

    private final RedblockStorage storage;

    public RedblockListener(RedblockStorage storage) {
        this.storage = storage;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getHand() == EquipmentSlot.HAND) {
            //if the player right or left clicks a block, get a list of redblocks that are near the player and check if the block is one of them

            var block = e.getClickedBlock();
            if (block == null) return;

            var location = block.getLocation();

            if (block.getType() == Material.RED_WOOL || block.getType() == Material.LIME_WOOL || block.getType() == Material.OAK_SIGN) {
                List<Redblock> possible = new ArrayList<>();
                for (Redblock rb : storage.getRedblocksFiltered(rb -> !rb.isDeleted() && !rb.isApproved())) {
                    if (rb.getLocation().getWorld().getUID().equals(location.getWorld().getUID()) && rb.getLocation().distanceSquared(location) <= 12) possible.add(rb);
                }
                var found = possible.stream().filter(rb -> rb.isPartOfRedblock(location)).findFirst();
                if (found.isPresent()) {
                    e.setCancelled(true);
                    e.setUseInteractedBlock(Event.Result.DENY);
                    e.setUseItemInHand(Event.Result.DENY);
                    if (block.getType() == Material.OAK_SIGN) {
                        if (found.get().isIncomplete()) {
                            e.getPlayer().performCommand("rbcomplete -id " + found.get().getId());
                        } else if (found.get().isPending()) {
                            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                                e.getPlayer().performCommand("rbapprove -id " + found.get().getId());
                            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                e.getPlayer().performCommand("rbdeny -id " + found.get().getId());
                            }
                        }
                        return;
                    }
                    e.getPlayer().sendMessage(ChatColor.RED + "You cannot destroy a redblock!");
                }
            }

        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        RedblockUtils.userNameMap.put(player.getUniqueId(), player.getName());
        List<Redblock> redblocks = storage.getRedblocksFiltered(rb -> rb.getAssignedTo() != null && rb.getAssignedTo().equals(player.getUniqueId()) && rb.isIncomplete());
        if (redblocks.size() > 0) {
            var text = Text.of("[Redblocks] ").setColor(ChatColor.LIGHT_PURPLE)
                    .append("You have ").setColor(ChatColor.GRAY)
                    .append(redblocks.size() + "").setColor(ChatColor.LIGHT_PURPLE)
                    .append(" redblocks assigned to you. ").setColor(ChatColor.GRAY)
                    .append("\n[Click this to view them]").setColor(ChatColor.LIGHT_PURPLE).setUnderlined(true)
                    .clickEvent(Text.ClickAction.RUN_COMMAND, "/rbl -mine -incomplete");
            Bukkit.getScheduler().runTaskLater(storage.getPlugin(), () -> {
                Text.sendTo(text, player);
            }, 100);
        }
    }

}
