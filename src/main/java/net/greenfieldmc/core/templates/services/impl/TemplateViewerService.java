package net.greenfieldmc.core.templates.services.impl;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.models.PlacementSession;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.services.DisplayLifecycleManager;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateViewerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TemplateViewerService extends ModuleService<ITemplateViewerService> implements ITemplateViewerService, Listener {

    private Team originTeam;
    private final Map<UUID, PlacementSession> placementSessions = new HashMap<>();

    //todo: something about this
    private static final int RAY_TRACE_DISTANCE = 64;
    private static final int TICK_INTERVAL = 2; // update every 2 ticks

    private ITemplateService templateService;
    private NamespacedKey templateItemKey;
    private final NamespacedKey ignoreAirKey;
    private final NamespacedKey randomRotationKey;

    public TemplateViewerService(Plugin plugin, Module module, ITemplateService templateService) {
        super(plugin, module);
        this.templateItemKey = new NamespacedKey(plugin, "template_name");
        this.ignoreAirKey = new NamespacedKey(plugin, "ignore_air");
        this.randomRotationKey = new NamespacedKey(plugin, "include_entities");
        this.templateService = templateService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.templateItemKey = new NamespacedKey(plugin, "template_name");

        var scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        var existingTeam = scoreboard.getTeam("template_origin");
        if (existingTeam != null) existingTeam.unregister();
        this.originTeam = scoreboard.registerNewTeam("template_origin");
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        for (var entry : placementSessions.entrySet()) {
            var player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                DisplayLifecycleManager.destroyModel(player, getPlugin(), entry.getValue(), originTeam);
            } else {
                var session = entry.getValue();
                session.getDisplayEntries().forEach(de -> de.display().remove());
                session.getOriginDisplay().remove();
                if (session.getTickTask() != null) session.getTickTask().cancel();
            }
        }
        placementSessions.clear();
        originTeam.unregister();
    }

    // =========================================================
    //  ITemplateViewerService
    // =========================================================

    @Override
    public void startPlacementMode(Player player, Template loadedTemplate, BiConsumer<Location, Integer> onConfirm) {
        if (isInPlacementMode(player)) cancelPlacement(player);
        spawnAndTrack(player, loadedTemplate, computeTargetLocation(player), 0, onConfirm);
    }

    @Override
    public Location confirmPlacement(Player player) {
        var session = placementSessions.get(player.getUniqueId());
        if (session == null) return null;

        var anchor = session.getAnchorLocation().clone();
        var rotation = session.getRotationDegrees();
        DisplayLifecycleManager.destroyModel(player, getPlugin(), session, originTeam);
        placementSessions.remove(player.getUniqueId());

        if (session.getOnConfirm() != null) session.getOnConfirm().accept(anchor, rotation);
        return anchor;
    }

    @Override
    public void cancelPlacement(Player player) {
        var session = placementSessions.get(player.getUniqueId());
        if (session == null) return;
        DisplayLifecycleManager.destroyModel(player, getPlugin(), session, originTeam);
        placementSessions.remove(player.getUniqueId());
    }

    @Override
    public boolean isInPlacementMode(Player player) {
        return placementSessions.containsKey(player.getUniqueId());
    }

    // =========================================================
    //  Internal — spawn + tick loop
    // =========================================================

    /**
     * Despawn any existing model, then spawn a fresh one at the given anchor with the
     * given rotation and start the position-tracking tick loop.
     * The existing {@code onConfirm} callback is preserved across rotations.
     */
    private void spawnAndTrack(Player player, Template template, Location anchor,
                                int rotationDegrees, BiConsumer<Location, Integer> onConfirm) {
        PlacementSession session;
        try {
            session = DisplayLifecycleManager.spawnModel(
                    player, getPlugin(), template, anchor, originTeam, onConfirm, rotationDegrees);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text("Error: " + e.getMessage(), NamedTextColor.RED)));
            return;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            if (!player.isOnline() || !placementSessions.containsKey(player.getUniqueId())) return;

            var newAnchor = computeTargetLocation(player);
            var cur = session.getAnchorLocation();
            if (cur.getBlockX() != newAnchor.getBlockX()
                    || cur.getBlockY() != newAnchor.getBlockY()
                    || cur.getBlockZ() != newAnchor.getBlockZ()) {
                DisplayLifecycleManager.teleportModel(session, newAnchor);
            }

            // Action-bar: placement hints + current rotation
            player.sendActionBar(
                    Component.text("Right-click", NamedTextColor.GREEN)
                            .append(Component.text(" place  ", NamedTextColor.GRAY))
                            .append(Component.text("Left-click", NamedTextColor.RED))
                            .append(Component.text(" cancel  ", NamedTextColor.GRAY))
                            .append(Component.text("Scroll", NamedTextColor.YELLOW))
                            .append(Component.text(" rotate  ", NamedTextColor.GRAY))
                            .append(Component.text(session.getRotationDegrees() + "°", NamedTextColor.AQUA))
            );
        }, 0L, TICK_INTERVAL);

        session.setTickTask(task);
        placementSessions.put(player.getUniqueId(), session);
    }

    // =========================================================
    //  Event Listeners
    // =========================================================

    /** Scroll wheel — rotate the template model by ±90° per scroll step. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerScroll(PlayerItemHeldEvent event) {
        var player = event.getPlayer();
        var session = placementSessions.get(player.getUniqueId());
        if (session == null) return;

        // Cancel actual hotbar slot change
        event.setCancelled(true);

        int prev = event.getPreviousSlot();
        int next = event.getNewSlot();

        // Scroll down: slot increases (or wraps 8 → 0) → clockwise (+90 in WE CCW = -90 visual)
        boolean scrollDown = (next > prev && !(prev == 0 && next == 8))
                || (prev == 8 && next == 0);

        // Scroll down = visual clockwise = WorldEdit rotateY(-90) → subtract 90
        int newRotation = scrollDown
                ? (session.getRotationDegrees() - 90 + 360) % 360
                : (session.getRotationDegrees() + 90) % 360;

        var anchor    = session.getAnchorLocation().clone();
        var template  = session.getTemplate();
        var onConfirm = session.getOnConfirm();

        // Destroy old displays (tick task cancelled inside destroyModel)
        DisplayLifecycleManager.destroyModel(player, getPlugin(), session, originTeam);
        placementSessions.remove(player.getUniqueId());

        // Spawn fresh model with new rotation
        spawnAndTrack(player, template, anchor, newRotation, onConfirm);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();

        // First, check if player is right-clicking a template item to enter placement mode
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && !isInPlacementMode(player)) {
            var item = event.getItem();
            if (item != null && item.hasItemMeta()) {
                var meta = item.getItemMeta();
                var pdc = meta.getPersistentDataContainer();
                if (pdc.has(templateItemKey, PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    var templateName = pdc.get(templateItemKey, PersistentDataType.STRING);
                    var template = templateService.getTemplate(templateName);
                    if (template == null) {
                        player.sendMessage(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                                .append(Component.text("Template '" + templateName + "' not found.", NamedTextColor.RED)));
                        return;
                    }

                    // Remove the template item from player's hand
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }

                    // Start placement mode
                    player.performCommand("tview " + templateName);
                    return;
                }
            }
        }

        // Handle placement mode interactions (confirm/cancel)
        if (!isInPlacementMode(player)) return;

        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var anchor = confirmPlacement(player);
            if (anchor != null) {
                player.sendMessage(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("Template placed at ", NamedTextColor.GRAY))
                        .append(Component.text(anchor.getBlockX() + ", " + anchor.getBlockY() + ", " + anchor.getBlockZ(), NamedTextColor.WHITE))
                        .append(Component.text(".", NamedTextColor.GRAY)));
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            cancelPlacement(player);
            player.sendMessage(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text("Placement cancelled.", NamedTextColor.GRAY)));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (isInPlacementMode(event.getPlayer())) cancelPlacement(event.getPlayer());
    }

    // =========================================================
    //  Template Item Creation
    // =========================================================

    public ItemStack createTemplateItem(Template template, boolean ignoreAir, boolean randomRotation) {
        var item = template.getDisplayItem();
        var meta = item.getItemMeta();

        // Store all placement data in PDC
        meta.getPersistentDataContainer().set(templateItemKey, PersistentDataType.STRING, template.getTemplateName());
        meta.getPersistentDataContainer().set(ignoreAirKey, PersistentDataType.BOOLEAN, ignoreAir);
        meta.getPersistentDataContainer().set(randomRotationKey, PersistentDataType.BOOLEAN, randomRotation);

        // Update lore to show placement options
        var lore = new ArrayList<Component>();
        lore.add(Component.text("Right-click to enter placement mode", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Dimensions: " + template.getDimensions(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Paste Mode: " + (ignoreAir ? "Ignore Air" : "Include Air"),
                        ignoreAir ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Rotation: " + (randomRotation ? "Random" : "Fixed"),
                        randomRotation ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        // ... rest ofore

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // =========================================================
    //  Helpers
    // =========================================================

    private Location computeTargetLocation(Player player) {
        var trace = player.rayTraceBlocks(RAY_TRACE_DISTANCE, FluidCollisionMode.NEVER);
        if (trace != null && trace.getHitBlock() != null && trace.getHitBlockFace() != null) {
            BlockFace face = trace.getHitBlockFace();
            var hitBlock = trace.getHitBlock().getLocation();
            return new Location(hitBlock.getWorld(),
                    hitBlock.getBlockX() + face.getModX(),
                    hitBlock.getBlockY() + face.getModY(),
                    hitBlock.getBlockZ() + face.getModZ());
        }
        var dir = player.getLocation().getDirection();
        var eye = player.getEyeLocation();
        return new Location(eye.getWorld(),
                (int) Math.floor(eye.getX() + dir.getX() * RAY_TRACE_DISTANCE),
                (int) Math.floor(eye.getY() + dir.getY() * RAY_TRACE_DISTANCE),
                (int) Math.floor(eye.getZ() + dir.getZ() * RAY_TRACE_DISTANCE));
    }
}