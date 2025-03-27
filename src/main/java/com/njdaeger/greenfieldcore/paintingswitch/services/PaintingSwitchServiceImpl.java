package com.njdaeger.greenfieldcore.paintingswitch.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.paintingswitch.PaintingSwitchMessages;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.*;

public class PaintingSwitchServiceImpl extends ModuleService<IPaintingSwitchService> implements IPaintingSwitchService, Listener {

    private static final String PERM = "greenfieldcore.paintingswitch.use";

    private IConfig config;
    private List<UUID> disabledUsers;
    private final Map<UUID, PaintingSession> users = new HashMap<>();
    private static final List<Art> ALL_ART = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT).stream().toList();

    public PaintingSwitchServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "paintingswitch");
            config.addEntry("disabled", new ArrayList<>());
            this.disabledUsers = new ArrayList<>(config.getStringList("disabled").stream().map(UUID::fromString).toList());
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } catch (Exception e) {
            throw new Exception("Failed to load PaintingSwitchService.", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        config.setEntry("disabled", disabledUsers.stream().map(UUID::toString).toList());
        config.save();
    }

    @Override
    public List<UUID> getDisabledUsers() {
        return disabledUsers;
    }

    @Override
    public void setEnabledFor(UUID uuid, boolean enabled) {
        if (enabled) disabledUsers.remove(uuid);
        else {
            disabledUsers.add(uuid);
            users.remove(uuid);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (disabledUsers.contains(event.getPlayer().getUniqueId())) return;
        PaintingSession session;
        if ((session = users.get(event.getPlayer().getUniqueId())) != null) {
            Location loc = session.getSelectedLocation();
            if (loc != null && hasMovedFar(event.getPlayer().getLocation(), loc)) {
                session.setLastArt(session.getSelected().getArt());
                session.setSwitching(false, null);
                event.getPlayer().sendMessage(PaintingSwitchMessages.PAINTING_LOCKED);
            }
        }
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (disabledUsers.contains(event.getPlayer().getUniqueId())) return;
        if (event.getPlayer().hasPermission(PERM) && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            PaintingSession session = users.getOrDefault(player.getUniqueId(), new PaintingSession());
            users.putIfAbsent(player.getUniqueId(), session);

            if (event.getRightClicked() instanceof Painting painting) {
                if (!session.isSwitching()) {
                    session.setSwitching(true, painting);
                    player.sendMessage(PaintingSwitchMessages.PAINTING_SCROLL);
                } else {
                    session.setLastArt(session.getSelected().getArt());
                    session.setSwitching(false, null);
                    player.sendMessage(PaintingSwitchMessages.PAINTING_LOCKED);
                }
            }
        }
    }

    @EventHandler
    public void placePainting(HangingPlaceEvent event) {
        if (event.getPlayer() != null && disabledUsers.contains(event.getPlayer().getUniqueId())) return;
        if (event.getPlayer() != null && event.getEntity().getType() == EntityType.PAINTING && event.getPlayer().hasPermission(PERM)) {
            PaintingSession session = users.getOrDefault(event.getPlayer().getUniqueId(), new PaintingSession());
            Painting painting = ((Painting)event.getEntity());
            if (session.hasLastArt()) {
                if (!painting.setArt(session.getLastArt())) {
                    painting.setArt(ALL_ART.getFirst());
                }
            }
            else session.setLastArt(painting.getArt());
        }
    }

    @EventHandler
    public void onBreak(HangingBreakByEntityEvent e) {
        if (disabledUsers.contains(e.getRemover().getUniqueId())) return;
        PaintingSession session;
        if (e.getRemover() instanceof Player && e.getEntity() instanceof Painting && (session = users.get(e.getRemover().getUniqueId())) != null) {
            if (session.isSwitching()) {
                session.setLastArt(session.getSelected().getArt());
                session.setSwitching(false, null);
                e.getRemover().sendMessage(PaintingSwitchMessages.PAINTING_REMOVED);
            }
        }
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent event) {
        if (disabledUsers.contains(event.getPlayer().getUniqueId())) return;
        if (event.getPlayer().hasPermission(PERM)) {
            PaintingSession session = users.getOrDefault(event.getPlayer().getUniqueId(), new PaintingSession());
            if (session.isSwitching()) {
                var diff = event.getNewSlot() - event.getPreviousSlot();
                var shift = diff == 8 ? -1 : diff == -8 ? 1 : diff;
                applyNextArt(session, shift);
                event.setCancelled(true);
            }
        }
    }

    private static void applyNextArt(PaintingSession session, int amount) {
        var art = ALL_ART.get(getNextIndex(session.getSelected().getArt(), amount));
        var checkAmount = 0;
        while (session.getSelected() != null && checkAmount < ALL_ART.size()) {
            if (session.getSelected().setArt(art)) break;
            art = ALL_ART.get(getNextIndex(art, amount < 0 ? -1 : 1));
            checkAmount++;
        }
    }

    private static int getNextIndex(Art current, int shift) {
        var length = ALL_ART.size();
        var currentIndex = ALL_ART.indexOf(current);

        //Checks if above max
        int upperCheck = (currentIndex + shift) >= length ? (currentIndex + shift) - length : currentIndex + shift;
        //Checks if below min
        return upperCheck < 0 ? length + upperCheck : upperCheck;
    }

    private static boolean hasMovedFar(Location loc1, Location loc2) {
        double dist = sqrt(pow(loc2.getX()-loc1.getX(), 2) + pow(loc2.getY()-loc1.getY(), 2) + pow(loc2.getZ()-loc1.getZ(), 2));
        return abs(dist) > 8;
    }


}
