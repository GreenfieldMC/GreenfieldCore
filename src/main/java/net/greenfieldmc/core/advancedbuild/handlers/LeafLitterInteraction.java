package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.advancedbuild.services.IAdvBuildService;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.LeafLitter;
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

public class LeafLitterInteraction extends InteractionHandler implements Listener {

    private final IAdvBuildService advService;
    private final Map<UUID, LeafLitterSession> sessions;

    public LeafLitterInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService, Plugin plugin, IAdvBuildService advService) {
        super(worldEditService, coreProtectService, (InteractPredicate) event -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof LeafLitter;
                },
                Material.LEAF_LITTER);

        this.sessions = new HashMap<>();
        this.advService = advService;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows leaf litter segment and facing selection with scroll support.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("If hand is empty or holding leaf litter: right click leaf litter toggles your ", NamedTextColor.GRAY)
                .append(Component.text("selecting leaf litter", NamedTextColor.GRAY))
                .append(Component.text(" status.", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If leaf litter is selected: use the scroll wheel to cycle segment amount and facing.", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If hand is holding leaf litter and shifting: right clicking places leaf litter with your last selected state.", NamedTextColor.GRAY));
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var hand = getHandMat(event);
        var clicked = event.getClickedBlock();
        var session = getSession(event.getPlayer().getUniqueId());

        if (clicked != null && clicked.getBlockData() instanceof LeafLitter leafLitter && (hand == Material.AIR || hand == Material.LEAF_LITTER) && !event.getPlayer().isSneaking()) {
            if (session.isSelectingLeafLitter()) {
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Leaf litter locked.");
                session.setIsSelectingLeafLitter(false, null, event.getPlayer());
            } else {
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Leaf litter selected.");
                session.setIsSelectingLeafLitter(true, clicked.getLocation(), event.getPlayer());
                session.setLast(copy(leafLitter));
            }
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            return;
        }

        if (event.getPlayer().isSneaking() && hand == Material.LEAF_LITTER) {
            var placeableLocation = getPlaceableLocation(event);
            if (placeableLocation == null) return;

            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);

            var last = session.getLast();
            var toPlace = last == null ? (LeafLitter) Material.LEAF_LITTER.createBlockData() : copy(last);
            placeBlockAt(event.getPlayer(), placeableLocation, Material.LEAF_LITTER, toPlace);
            session.setLast(copy(toPlace));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!advService.isEnabledFor(event.getPlayer().getUniqueId())) return;

        var session = getSession(event.getPlayer().getUniqueId());
        if (session.isSelectingLeafLitter()) {
            event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Leaf litter locked.");
            session.setIsSelectingLeafLitter(false, null, event.getPlayer());
        }

        if (event.getBlock().getBlockData() instanceof LeafLitter l) {
            var toPlace = session.getLast() == null ? l : copy(session.getLast());
            log(false, event.getPlayer(), event.getBlock());
            event.getBlock().setBlockData(toPlace, false);
            log(true, event.getPlayer(), event.getBlock());
            session.setLast(copy((LeafLitter) event.getBlock().getBlockData()));
        }
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent event) {
        if (!advService.isEnabledFor(event.getPlayer().getUniqueId())) return;

        var session = getSession(event.getPlayer().getUniqueId());
        if (!session.isSelectingLeafLitter() || session.getSelected() == null) return;

        if (!(session.getSelected().getBlock().getBlockData() instanceof LeafLitter current)) {
            session.setIsSelectingLeafLitter(false, null, event.getPlayer());
            return;
        }

        int diff = event.getNewSlot() - event.getPreviousSlot();
        int shift = diff == 8 ? -1 : diff == -8 ? 1 : diff;
        var next = getNextState(copy(current), shift > 0);
        session.getSelected().getBlock().setBlockData(next, false);
        session.setLast(copy(next));
        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!advService.isEnabledFor(event.getPlayer().getUniqueId())) return;

        var session = getSession(event.getPlayer().getUniqueId());
        if (!session.isSelectingLeafLitter()) return;

        var selected = session.getSelected();
        if (selected != null && hasMovedFar(event.getPlayer().getLocation(), selected)) {
            event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[AdvBuild] " + ChatColor.GRAY + "Leaf litter locked.");
            session.setIsSelectingLeafLitter(false, null, event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        var session = getSession(event.getPlayer().getUniqueId());
        if (session.isSelectingLeafLitter()) {
            session.setIsSelectingLeafLitter(false, null, event.getPlayer());
        }
    }

    private boolean hasMovedFar(Location a, Location b) {
        return a.getWorld() != b.getWorld() || a.distanceSquared(b) > 64;
    }

    private LeafLitterSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) {
            sessions.put(uuid, new LeafLitterSession(null));
        }
        return sessions.get(uuid);
    }

    private LeafLitter copy(LeafLitter source) {
        return (LeafLitter) Bukkit.createBlockData(source.getAsString());
    }

    private LeafLitter getNextState(LeafLitter current, boolean forward) {
        int min = 1;
        int max = current.getMaximumSegmentAmount();
        int nextSegments = current.getSegmentAmount() + (forward ? 1 : -1);

        if (nextSegments > max) {
            current.setSegmentAmount(min);
            current.setFacing(nextFacing(current.getFacing(), true));
            return current;
        }

        if (nextSegments < min) {
            current.setSegmentAmount(max);
            current.setFacing(nextFacing(current.getFacing(), false));
            return current;
        }

        current.setSegmentAmount(nextSegments);
        return current;
    }

    private BlockFace nextFacing(BlockFace current, boolean forward) {
        BlockFace[] order = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        int index = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == current) {
                index = i;
                break;
            }
        }

        int next = (index + (forward ? 1 : -1) + order.length) % order.length;
        return order[next];
    }

    public class LeafLitterSession {

        private Location selected;
        private LeafLitter last;
        private boolean selectingLeafLitter;

        public LeafLitterSession(LeafLitter last) {
            this.last = last;
        }

        public Location getSelected() {
            return selected;
        }

        public LeafLitter getLast() {
            return last;
        }

        public void setLast(LeafLitter last) {
            this.last = last;
        }

        public boolean isSelectingLeafLitter() {
            return selectingLeafLitter;
        }

        public void setIsSelectingLeafLitter(boolean selectingLeafLitter, Location selected, Player player) {
            this.selectingLeafLitter = selectingLeafLitter;
            if (!selectingLeafLitter && this.selected != null && this.selected.getBlock().getBlockData() instanceof LeafLitter data) {
                this.last = copy(data);
                log(true, player, this.selected.getBlock());
            } else if (selectingLeafLitter && selected != null) {
                log(false, player, selected.getBlock());
            }
            this.selected = selected;
        }
    }
}

