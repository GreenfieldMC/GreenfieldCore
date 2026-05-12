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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DecoratedPot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class DecoratedPotInteraction extends InteractionHandler implements Listener {

    private final IAdvBuildService advService;

    // Tracks which player is selecting a sherd for which pot location
    private final Map<UUID, Location> textureSelectionSessions = new HashMap<>();

    // Tracks the player's last-chosen pottery sherd (used when placing plain decorated pots)
    private final Map<UUID, Material> lastChosenSherds = new HashMap<>();

    // Tracks per-location chosen sherd so a pot replaced via the GUI keeps its design in-memory
    private final Map<Location, Material> decoratedPotDesigns = new HashMap<>();

    // Manual 6x9 layout map (rows x cols). If a cell is null it will remain empty in the GUI.
    // You can reorder or place specific materials in this grid; the code below will map the
    // discovered pottery sherd Materials into the grid in reading order for convenience.
    private static final int ROWS = 6;
    private static final int COLS = 9;

    // Manual grid the developer can edit to specify exactly which materials appear in each GUI slot.
    // Use null for empty slots. Edit this array to organize pottery sherds into the layout you want.
    // Indexing is row-major: index = row * COLS + col (0-based). The array length must be ROWS*COLS.
    private static final Material[] MANUAL_GRID = new Material[] {
            // Row 0 (9 values)
            Material.MOURNER_POTTERY_SHERD, Material.MINER_POTTERY_SHERD, Material.PRIZE_POTTERY_SHERD, null, null, null, null, null, null,
            // Row 1
            Material.DECORATED_POT, Material.SKULL_POTTERY_SHERD, Material.SNORT_POTTERY_SHERD, null, null, null, null, null, null,
            // Row 2
            Material.EXPLORER_POTTERY_SHERD, Material.DANGER_POTTERY_SHERD, Material.FRIEND_POTTERY_SHERD, Material.HOWL_POTTERY_SHERD, null, null, null, null, null,
            // Row 3
            Material.SHEAF_POTTERY_SHERD, Material.PLENTY_POTTERY_SHERD, Material.SHELTER_POTTERY_SHERD, null, null, null, null, null, null,
            // Row 4
            Material.BREWER_POTTERY_SHERD, Material.ARMS_UP_POTTERY_SHERD, Material.BURN_POTTERY_SHERD, Material.HEART_POTTERY_SHERD, null, null, null, null, null,
            // Row 5
            Material.BLADE_POTTERY_SHERD, Material.ANGLER_POTTERY_SHERD, Material.ARCHER_POTTERY_SHERD, Material.HEARTBREAK_POTTERY_SHERD, null, null, null, null, null
    };

    public DecoratedPotInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService, Plugin plugin, IAdvBuildService advService) {
        super(worldEditService, coreProtectService, (InteractPredicate) (event) -> {
            var player = event.getPlayer();
            var mainHand = player.getInventory().getItemInMainHand().getType();
            return mainHand == Material.AIR &&
                    event.getClickedBlock() != null &&
                    event.getClickedBlock().getType() == Material.DECORATED_POT;
        }, Material.DECORATED_POT);

        this.advService = advService;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Open a visual selector to choose a pottery sherd for a decorated pot.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Empty hand + right click a decorated pot to open the selector. Select a sherd to apply it to the clicked pot and save it as your last choice for future placements.", NamedTextColor.GRAY);
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        //OPTIONAL
        if (event.getPlayer().isSneaking()) return; // keep sneak behaviour for other handlers

        if (!(event.getClickedBlock().getType() == Material.DECORATED_POT)) return;

        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        if (textureSelectionSessions.containsKey(event.getPlayer().getUniqueId())) return;

        // Only respond if hand is empty (predicate already ensures this), but double-check
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;

        // Open selector
        openTextureSelectionGUI(event.getPlayer(), event.getClickedBlock().getLocation());

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!textureSelectionSessions.containsKey(player.getUniqueId())) return;
        UUID uuid = player.getUniqueId();



        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= ROWS * COLS) return;

        ItemStack clicked = event.getCurrentItem();

        Material sherd = clicked.getType();
        if (clicked.getType() == Material.DECORATED_POT) {
            sherd = null;
        }
        Location potLoc = textureSelectionSessions.get(uuid);

        var block = potLoc.getBlock();
        BlockState state = block.getState();
        if (state instanceof DecoratedPot pot) {
            pot.setSherd(DecoratedPot.Side.FRONT, sherd);
            pot.setSherd(DecoratedPot.Side.RIGHT, sherd);
            pot.setSherd(DecoratedPot.Side.BACK, sherd);
            pot.setSherd(DecoratedPot.Side.LEFT, sherd);
            pot.update();
        }
        decoratedPotDesigns.put(potLoc, sherd);
        lastChosenSherds.put(uuid, sherd);

        if (sherd != null) {
            player.sendMessage(Component.text("[AdvBuild] ").color(NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text("Selected pottery sherd: " + sherd.name()).color(NamedTextColor.GRAY)));
        } else
            player.sendMessage(Component.text("[AdvBuild] ").color(NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text("Cleared pottery sherd (set to plain decorated pot).").color(NamedTextColor.GRAY)));

        player.closeInventory();
        textureSelectionSessions.remove(uuid);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof org.bukkit.entity.Player)) return;
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        // Rely on the session map rather than a Component title equality check (which often fails).
        textureSelectionSessions.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        textureSelectionSessions.remove(uuid);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // When a player places a plain decorated pot, if they have a saved last-chosen sherd, record it for the placed location
        if (!advService.isEnabledFor(event.getPlayer().getUniqueId())) return;
        if (event.getBlock().getType() != Material.DECORATED_POT) return;
        Player player = event.getPlayer();
        Material last = getLastChosenSherd(player.getUniqueId());
        if (last != null) {
            decoratedPotDesigns.put(event.getBlock().getLocation(), last);
        }
    }

    private void openTextureSelectionGUI(Player player, Location potLocation) {
        Inventory gui = Bukkit.createInventory(null, ROWS * COLS, Component.text("Select Pottery Sherd"));

        // Track session
        textureSelectionSessions.put(player.getUniqueId(), potLocation);

        // Populate the GUI using the manual grid.
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int slot = r * COLS + c;

                // Prefer the manual grid value if present
                Material mat = MANUAL_GRID[slot];

                if (mat != null) {
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        // Avoid deprecated setDisplayName; use Component via Adventure if available elsewhere.
                        meta.setDisplayName(mat.name());
                        item.setItemMeta(meta);
                    }
                    gui.setItem(slot, item);
                } else {
                    gui.setItem(slot, null);
                }
            }
        }

        player.openInventory(gui);
    }

    // Utility: get last chosen sherd for a player
    public Material getLastChosenSherd(UUID uuid) {
        return lastChosenSherds.get(uuid);
    }
}