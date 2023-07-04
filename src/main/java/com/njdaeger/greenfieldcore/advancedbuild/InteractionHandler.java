package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.utils.text.Text;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.njdaeger.greenfieldcore.advancedbuild.AdvancedBuildModule.LIGHT_BLUE;

public abstract class InteractionHandler {

    protected final List<Material> materials;
    private final Predicate<PlayerInteractEvent> predicate;
    protected final GreenfieldCore plugin;

    /**
     * Create an interaction handler for given materials.
     * @param heldMaterials The materials to look for in the main hand of the player when interacting.
     */
    public InteractionHandler(Material... heldMaterials) {
        this.materials = heldMaterials == null ? new ArrayList<>() : Arrays.asList(heldMaterials);
        this.plugin = GreenfieldCore.getPlugin(GreenfieldCore.class);
        this.predicate = null;
    }

    /**
     * Create an interaction handler for given materials or scenarios.
     * @param predicate The predicate to test for to determine if the interaction should be handled by this interaction handler.
     * @param materials The materials to look for in the main hand of the player when interacting.
     */
    public InteractionHandler(Predicate<PlayerInteractEvent> predicate, Material...  materials) {
        this.materials = Arrays.asList(materials);
        this.plugin = GreenfieldCore.getPlugin(GreenfieldCore.class);
        this.predicate = predicate;
    }

    /**
     * Get the name of this interaction handler.
     * @return The name of this interaction handler.
     */
    public String getInteractionName() {
        return getClass().getSimpleName();
    }

    /**
     * Get the description of this interaction handler.
     * @return The description of this interaction handler.
     */
    public Text.Section getInteractionDescription() {
        return Text.of("No description provided.");
    }

    /**
     * Get the usage of this interaction handler.
     * @return The usage of this interaction handler.
     */
    public Text.Section getInteractionUsage() {
        return Text.of("No usage provided.");
    }

    /**
     * Get the materials this interaction handler handles as a text section.
     * @return The materials this interaction handler handles as a text section.
     */
    public Text.Section getMaterialListText() {
        var base = Text.of("[").setColor(ChatColor.GRAY);
        for (int i = 0; i < materials.size(); i++) {
            var mat = materials.get(i);
            base.appendRoot(mat.getKey().getKey()).setColor(LIGHT_BLUE.getRed(), LIGHT_BLUE.getGreen(), LIGHT_BLUE.getBlue());
            if (i != materials.size() - 1) base.appendRoot(", ").setColor(ChatColor.BLUE);
        }
        base.appendRoot("]").setColor(ChatColor.GRAY);
        return materials.isEmpty()
                ? Text.of("No materials specified.").setColor(ChatColor.RED)
                : base;
    }

    /**
     * Check if this interaction handler handles the given event.
     * @param event The event to check.
     * @return True if this interaction handler handles the event.
     */
    public boolean handles(PlayerInteractEvent event) {
        return materials.contains(event.getPlayer().getInventory().getItemInMainHand().getType()) || (predicate != null && predicate.test(event));
    }

    /**
     * Called when a player right clicks a block.
     * @param event The event.
     */
    public void onRightClickBlock(PlayerInteractEvent event) {

    }

    /**
     * Called when a player left clicks a block.
     * @param event The event.
     */
    public void onLeftClickBlock(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking() || event.getClickedBlock() == null) return;
        event.setCancelled(true);
        event.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
        event.setUseItemInHand(PlayerInteractEvent.Result.DENY);
        var clicked = event.getClickedBlock();
        placeBlockAt(event.getPlayer(), clicked.getLocation(), Material.AIR);
    }

    /**
     * Called when a player right clicks air.
     * @param event The event.
     */
    public void onRightClickAir(PlayerInteractEvent event) {

    }

    /**
     * Called when a player left clicks air.
     * @param event The event.
     */
    public void onLeftClickAir(PlayerInteractEvent event) {

    }

    /**
     * Get the material in the player calling the event's hand.
     * @param event The event.
     * @return The material in the player's hand.
     */
    public final Material getHandMat(PlayerInteractEvent event) {
        return event.getPlayer().getInventory().getItemInMainHand().getType();
    }

    /**
     * Get the location a block should be placed at based on the block location and the face clicked.
     * @param blockLocation The location of the block.
     * @param face The face clicked.
     * @return The location the block should be placed at.
     */
    public final Location getPlaceableLocation(Location blockLocation, BlockFace face) {
        return new Location(blockLocation.getWorld(), blockLocation.getBlockX() + face.getDirection().getBlockX(), blockLocation.getBlockY() + face.getDirection().getBlockY(), blockLocation.getBlockZ() + face.getDirection().getBlockZ());
    }

    /**
     * Log a placement or removal to CoreProtect.
     * @param placement True if the event is a placement, false if it is a removal.
     * @param player The player who placed or removed the block.
     * @param changedBlock The block that was placed or removed.
     */
    public final void log(boolean placement, Player player, Block changedBlock) {
        var plugin = GreenfieldCore.getPlugin(GreenfieldCore.class);
        if (!plugin.isCoreProtectEnabled()) return;
        if (placement) plugin.getCoreApi().logPlacement(player.getName(), changedBlock.getLocation(), changedBlock.getType(), changedBlock.getBlockData());
        else plugin.getCoreApi().logRemoval(player.getName(), changedBlock.getLocation(), changedBlock.getType(), changedBlock.getBlockData());
    }

    /**
     * Play a sound for a player.
     * @param placement True if the event is a placement, false if it is a removal.
     * @param player The player to play the sound for.
     * @param material The material to play the sound of.
     */
    public final void playSoundFor(boolean placement, Player player, Material material) {
        player.playSound(player.getLocation(), placement ? material.createBlockData().getSoundGroup().getPlaceSound() : material.createBlockData().getSoundGroup().getBreakSound(), .1f, 2.f);
    }

    /**
     * Check if a block can be placed at a location. (if the supplied location is an air or non-solid block)
     * @param location The location to check.
     * @return True if the block can be placed at the location.
     */
    public final boolean canPlaceAt(Location location) {
        return location.getBlock().getType().isAir() || !location.getBlock().getType().isSolid() && location.isWorldLoaded();
    }

    /**
     * Get the location a block should be placed at based on the block location and the face clicked.
     * @param event The event.
     * @return The location the block should be placed at, or null if it cannot be placed at the location.
     */
    public final Location getPlaceableLocation(PlayerInteractEvent event) {
        var location = getPlaceableLocation(event.getClickedBlock().getLocation(), event.getBlockFace());
        return canPlaceAt(location) ? location : null;
    }

    public final void placeBlockAt(Player player, Location location, Material material, BlockData blockData, Sound sound) {
        log(false, player, location.getBlock());
        location.getBlock().setType(material, false);
        location.getBlock().setBlockData(blockData, false);
        if (sound != null) player.playSound(player.getLocation(), sound, 1f, 1f);
        log(true, player, location.getBlock());
    }

    public final void placeBlockAt(Player player, Location location, Material material, BlockData blockData) {
        log(false, player, location.getBlock());
        location.getBlock().setType(material, false);
        location.getBlock().setBlockData(blockData, false);
        playSoundFor(true, player, material);
        log(true, player, location.getBlock());
    }

    public final void placeBlockAt(Player player, Location location, Material material) {
        placeBlockAt(player, location, material, material.createBlockData());
    }

    public void placeBlockNatively(Player player, Location location, Material material) {
        placeBlockNatively(player, location, material.createBlockData());
    }

    public void placeBlockNatively(Player player, Location location, BlockData data) {
        log(false, player, location.getBlock());
        WorldEditPlugin worldedit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        var world = worldedit.getWorldEdit().getPlatformManager().getWorldForEditing(BukkitAdapter.adapt(location.getWorld()));
        var blockstate = BukkitAdapter.adapt(data);

        try {
            world.setBlock(BukkitAdapter.asBlockVector(location), blockstate.toBaseBlock(), SideEffectSet.none());
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
        log(true, player, location.getBlock());
    }

}
