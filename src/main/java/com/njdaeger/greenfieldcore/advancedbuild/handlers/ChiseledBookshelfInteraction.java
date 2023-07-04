package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.AdvBuildConfig;
import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule.LIGHT_BLUE;
import static java.lang.Math.*;

public class ChiseledBookshelfInteraction extends InteractionHandler implements Listener {

    private final AdvBuildConfig config;
    private final Map<UUID, BookshelfSession> sessions;

    public ChiseledBookshelfInteraction(Plugin plugin, AdvBuildConfig config) {
        super((event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof ChiseledBookshelf;
                },
                Material.CHISELED_BOOKSHELF);

        this.sessions = new HashMap<>();
        this.config = config;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public Text.Section getInteractionDescription() {
        return Text.of("Allows the placement of chiseled bookshelves on any block face without the need to use the debug stick to set the block data.");
    }

    @Override
    public Text.Section getInteractionUsage() {
        return Text.of("If hand is empty: right click a bookshelf toggles your ").setColor(LIGHT_BLUE).append("selecting shelf").setUnderlined(true).appendParent(" status.")
                .appendRoot(" ----- ").setColor(ChatColor.DARK_GRAY)
                .appendRoot("If a bookshelf is selected (aka 'selecting shelf'): The scroll wheel will allow the traversal of all bookshelf options in that color.").setColor(LIGHT_BLUE)
                .appendRoot(" ----- ").setColor(ChatColor.DARK_GRAY)
                .appendRoot("If hand is holding a bookshelf and NOT shifting: clicking on the front or back of the bookshelf will toggle between the available colors").setColor(LIGHT_BLUE)
                .appendRoot(" ----- ").setColor(ChatColor.DARK_GRAY)
                .appendRoot("If hand is holding a bookshelf and shifting: right clicking will place a bookshelf in the desired location with the last placed book amount.").setColor(LIGHT_BLUE);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var session = getSession(event.getPlayer().getUniqueId());
        if (getHandMat(event) == Material.AIR) {

            if (session.isSelectingShelf()) {
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Bookshelf locked.");
                session.setIsSelectingShelf(false, null, event.getPlayer());
            } else {
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Bookshelf selected.");
                session.setIsSelectingShelf(true, event.getClickedBlock().getLocation(), event.getPlayer());
            }
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        } else {

            if (!event.getPlayer().isSneaking()) {
                if (!(event.getClickedBlock().getBlockData() instanceof ChiseledBookshelf clicked)) return;
                if (event.getBlockFace() == clicked.getFacing() || event.getBlockFace().getOppositeFace() == clicked.getFacing()) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                    event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                    clicked.setSlotOccupied(0, !clicked.isSlotOccupied(0));
                    session.setLast(clicked);
                    log(false, event.getPlayer(), event.getClickedBlock());
                    event.getClickedBlock().setBlockData(clicked, false);
                    log(true, event.getPlayer(), event.getClickedBlock());
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (config.isEnabledFor(e.getPlayer())) {
            var session = getSession(e.getPlayer().getUniqueId());
            if (session.isSelectingShelf()) {
                e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Bookshelf locked.");
                session.setIsSelectingShelf(false, null, e.getPlayer());
            }

            if (e.getBlock().getBlockData() instanceof ChiseledBookshelf) {
                var last = session.getLast();
                if (last != null) {
                    last.setFacing(e.getPlayer().getFacing());
                    log(false, e.getPlayer(), e.getBlock());
                    e.getBlock().setBlockData(last, false);
                    log(true, e.getPlayer(), e.getBlock());
                }
            }
        }
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent event) {
        if (config.isEnabledFor(event.getPlayer())) {
            var session = getSession(event.getPlayer().getUniqueId());
            if (session.isSelectingShelf()) {
                var current = (ChiseledBookshelf) session.getSelected().getBlock().getBlockData();
                int diff = event.getNewSlot() - event.getPreviousSlot();
                int shift = diff == 8 ? -1 : diff == -8 ? 1 : diff;
                session.getSelected().getBlock().setBlockData(getNextState(current, shift > 0), false);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (config.isEnabledFor(event.getPlayer())) {
            var session = getSession(event.getPlayer().getUniqueId());
            if (session.isSelectingShelf()) {
                var loc = session.getSelected();
                if (loc != null && hasMovedFar(event.getPlayer().getLocation(), loc)) {
                    event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Bookshelf locked.");
                    session.setIsSelectingShelf(false, null, event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        var session = getSession(e.getPlayer().getUniqueId());
        if (session.isSelectingShelf()) session.setIsSelectingShelf(false, null, e.getPlayer());
    }

    private static boolean hasMovedFar(Location loc1, Location loc2) {
        double dist = sqrt(pow(loc2.getX()-loc1.getX(), 2) + pow(loc2.getY()-loc1.getY(), 2) + pow(loc2.getZ()-loc1.getZ(), 2));
        return abs(dist) > 8;
    }

    private BookshelfSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) {
            sessions.put(uuid, new BookshelfSession(null));
        }
        return sessions.get(uuid);
    }

    private ChiseledBookshelf getNextState(ChiseledBookshelf current, boolean forward) {
        var slot1 = current.isSlotOccupied(1) ? 1 : 0;
        var slot2 = current.isSlotOccupied(2) ? 1 : 0;
        var slot3 = current.isSlotOccupied(3) ? 1 : 0;
        var slot4 = current.isSlotOccupied(4) ? 1 : 0;
        var slot5 = current.isSlotOccupied(5) ? 1 : 0;

        int currentNumber = Integer.parseInt(String.format("%d%d%d%d%d", slot1, slot2, slot3, slot4, slot5), 2);
        if (currentNumber == 0 && !forward) currentNumber = 32;
        else if (currentNumber == 31 && forward) currentNumber = -1;

        int nextNumber = currentNumber + (forward ? 1 : -1);
        var binary = String.format("%s%5s", current.isSlotOccupied(0) ? 1 : 0,Integer.toBinaryString(nextNumber)).replace(' ', '0');

        current.setSlotOccupied(0, current.isSlotOccupied(0));
        current.setSlotOccupied(1, binary.charAt(1) == '1');
        current.setSlotOccupied(2, binary.charAt(2) == '1');
        current.setSlotOccupied(3, binary.charAt(3) == '1');
        current.setSlotOccupied(4, binary.charAt(4) == '1');
        current.setSlotOccupied(5, binary.charAt(5) == '1');

        return current;
    }

    public class BookshelfSession {

        private Location selected;
        private ChiseledBookshelf last;
        private boolean isSelectingShelf;

        public BookshelfSession(ChiseledBookshelf last) {
            this.last = last;
        }

        public ChiseledBookshelf getLast() {
            return last;
        }

        public void setLast(ChiseledBookshelf last) {
            this.last = last;
        }

        public Location getSelected() {
            return selected;
        }

        public boolean isSelectingShelf() {
            return isSelectingShelf;
        }

        public void setIsSelectingShelf(boolean hasShelfSelected, Location selected, Player player) {
            this.isSelectingShelf = hasShelfSelected;
            if (!hasShelfSelected && this.selected != null) {
                this.last = (ChiseledBookshelf) this.selected.getBlock().getBlockData();
                log(true, player, this.selected.getBlock());
            } else if (hasShelfSelected) log(false, player, selected.getBlock());
            this.selected = selected;
        }

    }

}
