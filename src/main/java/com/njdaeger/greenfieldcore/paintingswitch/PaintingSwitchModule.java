package com.njdaeger.greenfieldcore.paintingswitch;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;
import static java.lang.Math.*;

public class PaintingSwitchModule extends Module implements Listener {

    private static final String PERM = "greenfieldcore.paintingswitch.use";
    private final Map<UUID, PaintingSession> users = new HashMap<>();

    public PaintingSwitchModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void tryDisable() {

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        PaintingSession session;
        if ((session = users.get(event.getPlayer().getUniqueId())) != null) {
            Location loc = session.getSelectedLocation();
            if (loc != null && hasMovedFar(event.getPlayer().getLocation(), loc)) {
                session.setLastArt(session.getSelected().getArt());
                session.setSwitching(false, null);
                event.getPlayer().sendMessage(moduleMessage("PaintingSwitch").append(Component.text("Painting locked.", NamedTextColor.GRAY)));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().hasPermission(PERM) && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            PaintingSession session = users.getOrDefault(player.getUniqueId(), new PaintingSession());
            users.putIfAbsent(player.getUniqueId(), session);

            if (session.isSwitching()) {

                if (session.justStarted()) {
                    session.setJustStarted(false);
                } else {
                    session.setLastArt(session.getSelected().getArt());
                    session.setSwitching(false, null);
                    player.sendMessage(moduleMessage("PaintingSwitch").append(Component.text("Painting locked.", NamedTextColor.GRAY)));
                }
            }
        }
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (event.getPlayer().hasPermission(PERM) && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            PaintingSession session = users.getOrDefault(player.getUniqueId(), new PaintingSession());
            users.putIfAbsent(player.getUniqueId(), session);

            if (event.getRightClicked() instanceof Painting && !session.isSwitching()) {
                session.setSwitching(true, (Painting) event.getRightClicked());
                session.setJustStarted(true);
                player.sendMessage(moduleMessage("PaintingSwitch").append(Component.text("Scroll to select painting.", NamedTextColor.GRAY)));
            }
        }
    }

    @EventHandler
    public void placePainting(HangingPlaceEvent event) {
        if (event.getPlayer() != null && event.getEntity().getType() == EntityType.PAINTING && event.getPlayer().hasPermission(PERM)) {
            PaintingSession session = users.getOrDefault(event.getPlayer().getUniqueId(), new PaintingSession());
            Painting painting = ((Painting)event.getEntity());
            if (session.hasLastArt()) {
                if (!painting.setArt(session.getLastArt())) {
                    painting.setArt(Art.values()[0]);
                }
            }
            else session.setLastArt(painting.getArt());
        }
    }

    @EventHandler
    public void onBreak(HangingBreakByEntityEvent e) {
        PaintingSession session;
        if (e.getRemover() instanceof Player && e.getEntity() instanceof Painting && (session = users.get(e.getRemover().getUniqueId())) != null) {
            if (session.isSwitching()) {
                session.setLastArt(session.getSelected().getArt());
                session.setSwitching(false, null);
                e.getRemover().sendMessage(moduleMessage("PaintingSwitch").append(Component.text("Painting removed.", NamedTextColor.GRAY)));
            }
        }
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent event) {
        if (event.getPlayer().hasPermission(PERM)) {
            PaintingSession session = users.getOrDefault(event.getPlayer().getUniqueId(), new PaintingSession());
            if (session.isSwitching()) {
                int diff = event.getNewSlot() - event.getPreviousSlot();
                int shift = diff == 8 ? -1 : diff == -8 ? 1 : diff;
                applyNextArt(session, shift);
                event.setCancelled(true);
            }
        }
    }

    private static void applyNextArt(PaintingSession session, int amount) {
        Art art = Art.values()[getNextIndex(session.getSelected().getArt(), amount)];
        int checkAmount = 0;
        while (session.getSelected() != null && checkAmount < Art.values().length) {
            if (session.getSelected().setArt(art)) break;
            art = Art.values()[getNextIndex(art, amount < 0 ? -1 : 1)];
            checkAmount++;
        }
    }

    private static int getNextIndex(Art current, int shift) {
        int length = Art.values().length;

        //Checks if above max
        int upperCheck = (current.ordinal() + shift) >= length ? (current.ordinal() + shift) - length : current.ordinal() + shift;
        //Checks if below min
        return upperCheck < 0 ? length + upperCheck : upperCheck;
    }

    private static boolean hasMovedFar(Location loc1, Location loc2) {
        double dist = sqrt(pow(loc2.getX()-loc1.getX(), 2) + pow(loc2.getY()-loc1.getY(), 2) + pow(loc2.getZ()-loc1.getZ(), 2));
        return abs(dist) > 8;
    }

}
