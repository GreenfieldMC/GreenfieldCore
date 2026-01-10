package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.advancedbuild.services.IAdvBuildService;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChiseledBookshelfInteraction extends InteractionHandler implements Listener {


    private final IAdvBuildService advService;
    private final Map<UUID, BookshelfSession> sessions;

    // Map to track which player is selecting a texture for which bookshelf
    private final Map<UUID, Location> textureSelectionSessions = new HashMap<>();
    // Track the GUI's current light/dark state per player (true==light, false==dark)
    private final Map<UUID, Boolean> guiIsLight = new HashMap<>();

    public ChiseledBookshelfInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService, Plugin plugin, IAdvBuildService advService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
                    var player = event.getPlayer();
                    var mainHand = player.getInventory().getItemInMainHand().getType();
                    return mainHand == Material.AIR &&
                            event.getClickedBlock() != null &&
                            event.getClickedBlock().getBlockData() instanceof ChiseledBookshelf;
                },
                Material.CHISELED_BOOKSHELF);

        this.advService = advService;
        this.sessions = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allows the placement of chiseled bookshelves on any block face without the need to use the debug stick to set the block data.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("If hand is empty: right click a bookshelf toggles your ").color(NamedTextColor.GRAY).append(Component.text("selecting shelf", NamedTextColor.GRAY, TextDecoration.UNDERLINED)).append(Component.text(" status.", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If a bookshelf is selected (aka 'selecting shelf'): A menu will open in which any variant can be selected.", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If hand is holding a bookshelf and NOT shifting: clicking on the front or back of the bookshelf will toggle between the available colors", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If hand is holding a bookshelf and shifting: right clicking will place a bookshelf in the desired location with the last placed book amount.", NamedTextColor.GRAY));
    }

    @EventHandler
    public void onRightClickWithBook(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getBlockData() instanceof ChiseledBookshelf) {
            var mainhand = e.getPlayer().getInventory().getItemInMainHand();
            var offhand = e.getPlayer().getInventory().getItemInOffHand();
            if (mainhand.getType() == Material.BOOK || mainhand.getType() == Material.WRITTEN_BOOK || offhand.getType() == Material.BOOK || offhand.getType() == Material.WRITTEN_BOOK) {
                e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            }
        }
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        // When hand is empty, open the visual texture-select GUI for the clicked bookshelf immediately.
        if (getHandMat(event) == Material.AIR) {
            if (event.getClickedBlock() == null) return;
            if (!(event.getClickedBlock().getBlockData() instanceof ChiseledBookshelf clicked)) return;

            // slot 0: true == dark, false == light, so isLight is the inverse of slot 0
            openTextureSelectionGUI(event.getPlayer(), event.getClickedBlock().getLocation(), !clicked.isSlotOccupied(0));
            event.setCancelled(true);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        } else {
            // When holding a bookshelf, preserve existing toggle-on-front/back behavior using session.last
            if (!event.getPlayer().isSneaking()) {
                if (event.getClickedBlock() == null) return;
                if (!(event.getClickedBlock().getBlockData() instanceof ChiseledBookshelf clicked)) return;
                if (event.getBlockFace() == clicked.getFacing() || event.getBlockFace().getOppositeFace() == clicked.getFacing()) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                    event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                    clicked.setSlotOccupied(0, !clicked.isSlotOccupied(0));
                    var session = getSession(event.getPlayer().getUniqueId());
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
        if (advService.isEnabledFor(e.getPlayer().getUniqueId())) {
            // previous selection-locking removed; keep last-apply behavior
            if (e.getBlock().getBlockData() instanceof ChiseledBookshelf) {
                var last = getSession(e.getPlayer().getUniqueId()).getLast();
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
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!textureSelectionSessions.containsKey(player.getUniqueId())) return;
        // Use Component-based title check
        if (!event.getView().title().equals(Component.text("Select Bookshelf Texture"))) return;
        // Only process clicks in the GUI, not the player's own inventory
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;
        event.setCancelled(true); // Prevent item movement
        int slot = event.getRawSlot();
        if (slot < 0) return;

        // Allow toggling the light/dark toggle in the bottom-right (slot 53)
        if (slot == 53) {
            // Instead of closing/reopening the GUI, update the existing inventory in-place
            UUID uuid = player.getUniqueId();
            Location bookshelfLoc = textureSelectionSessions.get(uuid);
            if (bookshelfLoc == null) return;

            // Determine the new GUI state by flipping the tracked value; fall back to block state if not present
            // Safely derive the block's current color bit (slot 0) if available
            org.bukkit.block.Block b = bookshelfLoc.getBlock();
            boolean defaultGuiLight = true; // fallback to light if we can't read the block
            if (b.getBlockData() instanceof ChiseledBookshelf cb) {
                defaultGuiLight = !cb.isSlotOccupied(0);
            }
            boolean currentGuiLight = guiIsLight.getOrDefault(uuid, defaultGuiLight);
            boolean newGuiLight = !currentGuiLight;
            guiIsLight.put(uuid, newGuiLight);

            Inventory gui = event.getInventory();
            // Refill the GUI items (slots 0-31 and the toggle item) using the new light/dark state
            refillTextureSelectionInventory(gui, newGuiLight);
            return;
        }

        // Normal texture selection slots (0-31)
        if (slot >= 32) return;

        Location bookshelfLoc = textureSelectionSessions.get(player.getUniqueId());
        org.bukkit.block.Block block = bookshelfLoc.getBlock();
        if (!(block.getBlockData() instanceof ChiseledBookshelf bookshelf)) {
            player.closeInventory();
            textureSelectionSessions.remove(player.getUniqueId());
            guiIsLight.remove(player.getUniqueId());
            return;
        }
        // Use the GUI's tracked light/dark state when applying the selection; if not present, fall back to block state
        boolean isLight = guiIsLight.getOrDefault(player.getUniqueId(), !bookshelf.isSlotOccupied(0));
        // Set slots 1-5 based on selected texture (slot is 0-31, treated as a 5-bit binary value)
        boolean bit1 = (slot & 0b00001) != 0; // bit 0 (LSB)
        boolean bit2 = (slot & 0b00010) != 0; // bit 1
        boolean bit3 = (slot & 0b00100) != 0; // bit 2
        boolean bit4 = (slot & 0b01000) != 0; // bit 3
        boolean bit5 = (slot & 0b10000) != 0; // bit 4 (MSB)

        bookshelf.setSlotOccupied(1, bit1);
        bookshelf.setSlotOccupied(2, bit2);
        bookshelf.setSlotOccupied(3, bit3);
        bookshelf.setSlotOccupied(4, bit4);
        bookshelf.setSlotOccupied(5, bit5);
        // Preserve color bit (slot 0): slot0=true == dark, false == light; isLight was set as the inverse
        bookshelf.setSlotOccupied(0, !isLight);

        // Save this selection as the player's last-used bookshelf state so future placements use it
        var session = getSession(player.getUniqueId());
        try {
            // clone to avoid holding a reference to a Block's live data
            session.setLast((ChiseledBookshelf) bookshelf.clone());
        } catch (ClassCastException ex) {
            // fallback: set direct reference if cloning/casting fails
            session.setLast(bookshelf);
        }

        block.setBlockData(bookshelf, false);
        player.sendMessage(Component.text("[AdvBuild] ").color(NamedTextColor.LIGHT_PURPLE).append(Component.text("Bookshelf texture set to #" + (slot + 1)).color(NamedTextColor.GRAY)));
        player.closeInventory();
        textureSelectionSessions.remove(player.getUniqueId());
        guiIsLight.remove(player.getUniqueId());
    }

    // Close handler: if the player closes the GUI without selecting, remove their session mapping
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!Component.text("Select Bookshelf Texture").equals(event.getView().title())) return;
        if (textureSelectionSessions.containsKey(player.getUniqueId())) {
            textureSelectionSessions.remove(player.getUniqueId());
            guiIsLight.remove(player.getUniqueId());
        }
    }

    // Player quit cleanup: ensure the texture selection session is removed if they disconnect while selecting
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        textureSelectionSessions.remove(uuid);
        guiIsLight.remove(uuid);
    }

    private BookshelfSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) {
            sessions.put(uuid, new BookshelfSession(null));
        }
        return sessions.get(uuid);
    }

    /**
     * Opens a double chest GUI for the player to select a bookshelf texture.
     * @param player The player to open the GUI for.
     * @param bookshelfLocation The location of the bookshelf being edited.
     * @param isLight Whether the first slot (color) is light (true) or dark (false).
     */
    public void openTextureSelectionGUI(Player player, Location bookshelfLocation, boolean isLight) {
        // 6 rows, 9 columns = 54 slots (double chest), but we only need 32
        org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 54, Component.text("Select Bookshelf Texture"));

        // Track the session
        textureSelectionSessions.put(player.getUniqueId(), bookshelfLocation);
        // Track the GUI's current displayed color selection separately from the actual block - toggling should not close GUI
        guiIsLight.put(player.getUniqueId(), isLight);

        // Fill the inventory contents
        refillTextureSelectionInventory(gui, isLight);

        player.openInventory(gui);
    }

    // Helper to (re)fill an existing inventory GUI with items for the given light/dark state
    private void refillTextureSelectionInventory(Inventory gui, boolean isLight) {
        final int type = isLight ? 1 : 2;
        final int TYPE_MULTIPLIER = 100;

        for (int i = 0; i < 32; i++) {
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHISELED_BOOKSHELF);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            int cmd = (type * TYPE_MULTIPLIER) + i; // numeric CustomModelData for this (type,variant)
            if (meta != null) {
                String modelIdentifier = "chiseled_bookshelf_type" + type + "_" + i;

                // Ensure the integer CustomModelData is set for resourcepack matching
                meta.setCustomModelData(cmd);

                // Safely update the CustomModelDataComponent string list (if component exists)
                CustomModelDataComponent cmdc = meta.getCustomModelDataComponent();
                java.util.List<String> existing = cmdc.getStrings();
                java.util.List<String> newStrings = new java.util.ArrayList<>(existing);
                newStrings.add(modelIdentifier);
                cmdc.setStrings(newStrings);
                meta.setCustomModelDataComponent(cmdc);

                meta.displayName(Component.text("Texture #" + (i + 1)));
                java.util.List<Component> loreComp = new java.util.ArrayList<>();
                loreComp.add(Component.text((isLight ? "Light" : "Dark") + " variant"));
                loreComp.add(Component.text("Model ID: " + cmd));
                loreComp.add(Component.text(modelIdentifier));
                meta.lore(loreComp);
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }

        // Update toggle slot at bottom-right (53)
        ItemStack toggle = new ItemStack(isLight ? Material.WHITE_WOOL : Material.GRAY_WOOL);
        ItemMeta toggleMeta = toggle.getItemMeta();
        if (toggleMeta != null) {
            toggleMeta.displayName(Component.text((isLight ? "Light" : "Dark") + " bookshelf (click to toggle)"));
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("Toggle between light/dark base color for the textures."));
            lore.add(Component.text("Current: " + (isLight ? "Light" : "Dark")));
            toggleMeta.lore(lore);
            toggle.setItemMeta(toggleMeta);
        }
        gui.setItem(53, toggle);
    }

    public static class BookshelfSession {

        private ChiseledBookshelf last;

        public BookshelfSession(ChiseledBookshelf last) {
            this.last = last;
        }

        public ChiseledBookshelf getLast() {
            return last;
        }

        public void setLast(ChiseledBookshelf last) {
            this.last = last;
        }
    }

}