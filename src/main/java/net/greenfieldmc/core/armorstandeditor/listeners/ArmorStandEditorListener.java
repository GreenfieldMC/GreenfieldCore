package net.greenfieldmc.core.armorstandeditor.listeners;

import net.greenfieldmc.core.armorstandeditor.handlers.*;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandHotbarService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import net.greenfieldmc.core.armorstandeditor.services.IArmorStandEditorService;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

public class ArmorStandEditorListener implements Listener {

    private final IArmorStandEditorService aseService;
    private final Map<UUID, ArmorStandSessionService.ArmorStandSession> sessions;
    private final ArmorStandSessionService sessionService;
    private final ArmorStandHotbarService hotbarService;
    private final Plugin plugin;
    private final Arms armsHandler;
    private final BasePlate basePlateHandler;
    private final Copy copyHandler;
    private final Lock lockHandler;
    private final Pose poseHandler;
    private final Rotate rotateHandler;
    private final Size sizeHandler;
    private final Slot slotHandler;
    private final Visible visibleHandler;

    public ArmorStandEditorListener(IArmorStandEditorService aseService, Map<UUID, ArmorStandSessionService.ArmorStandSession> sessions, ArmorStandHotbarService armorStandHotbarService, Plugin plugin, ArmorStandSessionService sessionService) {
        this.aseService = aseService;
        this.sessions = sessions;
        this.plugin = plugin;
        this.hotbarService = armorStandHotbarService;
        this.sessionService = sessionService;
        this.armsHandler = new Arms((ArmorStand) null);
        this.basePlateHandler = new BasePlate((ArmorStand) null);
        this.copyHandler = new Copy((ArmorStand) null);
        this.lockHandler = new Lock((ArmorStand) null);
        this.poseHandler = new Pose();
        this.rotateHandler = new Rotate((ArmorStand) null);
        this.sizeHandler = new Size((ArmorStand) null);
        this.slotHandler = new Slot(hotbarService, (ArmorStand) null);
        this.visibleHandler = new Visible((ArmorStand) null);
    }

    @EventHandler
    public void onRightClickAtEntity(PlayerInteractAtEntityEvent e) {
        if (aseService.isEnabledFor(e.getPlayer().getUniqueId())) return;
        ArmorStandSessionService.ArmorStandSession session = sessions.get(e.getPlayer().getUniqueId());
        ArmorStand stand = e.getRightClicked() instanceof ArmorStand ? (ArmorStand) e.getRightClicked() : null;
        ItemStack i = e.getPlayer().getInventory().getItemInMainHand();
        int slotInt = e.getPlayer().getInventory().getHeldItemSlot();

        armsHandler.arms(stand, slotInt, e.getPlayer(), session, hotbarService);
        basePlateHandler.toggleBasePlate(stand, slotInt, e.getPlayer(), session, hotbarService);
        copyHandler.copy(stand, slotInt, e.getPlayer(), session, hotbarService);
        lockHandler.lock(stand, slotInt, e.getPlayer(), session, hotbarService);
        poseHandler.pose(stand, e.getPlayer(), i, sessionService, hotbarService, plugin);
        rotateHandler.rotate(stand, slotInt, e.getPlayer(), session, hotbarService);
        sizeHandler.size(stand, slotInt, e.getPlayer(), session, hotbarService);
        slotHandler.slot(stand, slotInt, e.getPlayer(), plugin);
        visibleHandler.visible(stand, slotInt, e.getPlayer(), session, hotbarService);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        e.getPlayer().sendMessage("PlayerInteractEvent triggered");
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {

    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {

    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {

    }
}