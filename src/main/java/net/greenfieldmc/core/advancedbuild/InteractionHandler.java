package net.greenfieldmc.core.advancedbuild;

import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class InteractionHandler implements PageItem<ICommandContext> {

    protected final List<Material> materials;
    private final InteractPredicate interactPredicate;
    private final EntityInteractPredicate entityPredicate;
    private final IWorldEditService worldEditService;
    protected final ICoreProtectService coreProtectService;

    /**
     * Create an interaction handler for given materials.
     * @param heldMaterials The materials to look for in the main hand of the player when interacting.
     */
    public InteractionHandler(IWorldEditService worldEditService, ICoreProtectService coreProtectService, Material... heldMaterials) {
        this.interactPredicate = null;
        this.materials = heldMaterials == null ? new ArrayList<>() : Arrays.asList(heldMaterials);
        this.entityPredicate = null;
        this.worldEditService = worldEditService;
        this.coreProtectService = coreProtectService;
    }

    /**
     * Create an interaction handler for given materials or scenarios.
     * @param predicate The predicate to test for to determine if the interaction should be handled by this interaction handler.
     * @param materials The materials to look for in the main hand of the player when interacting.
     */
    public InteractionHandler(IWorldEditService worldEditService, ICoreProtectService coreProtectService, InteractPredicate interactPredicate, Material...  materials) {
        this.interactPredicate = interactPredicate;
        this.materials = Arrays.asList(materials);
        this.entityPredicate = null;
        this.worldEditService = worldEditService;
        this.coreProtectService = coreProtectService;
    }

    /**
     * Create an interaction handler for given materials or scenarios.
     * @param entityPredicate The predicate to test for to determine if the interaction should be handled by this interaction handler.
     * @param materials The materials to look for in the main hand of the player when interacting.
     */
    public InteractionHandler(IWorldEditService worldEditService, ICoreProtectService coreProtectService, EntityInteractPredicate entityPredicate, Material...  materials) {
        this.interactPredicate = null;
        this.materials = Arrays.asList(materials);
        this.entityPredicate = entityPredicate;
        this.worldEditService = worldEditService;
        this.coreProtectService = coreProtectService;
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
     *
     * @return The description of this interaction handler.
     */
    public TextComponent getInteractionDescription() {
        return Component.text("No description provided.");
    }

    /**
     * Get the usage of this interaction handler.
     *
     * @return The usage of this interaction handler.
     */
    public TextComponent getInteractionUsage() {
        return Component.text("No usage provided.");
    }

    /**
     * Get the materials this interaction handler handles as a text section.
     * @return The materials this interaction handler handles as a text section.
     */
    public TextComponent getMaterialListText() {
        var base2 = Component.text("[", NamedTextColor.GRAY).toBuilder();

        for (int i = 0; i < materials.size(); i++) {
            var mat = materials.get(i);
            base2.append(Component.text(mat.getKey().getKey(), NamedTextColor.GRAY));
            if (i != materials.size() - 1) base2.append(Component.text(", ", NamedTextColor.BLUE, TextDecoration.BOLD));
        }
        base2.append(Component.text("]", NamedTextColor.GRAY));
        return materials.isEmpty()
                ? Component.text("No materials specified.", NamedTextColor.RED)
                : base2.build();
    }

    /**
     * Check if this interaction handler handles the given event.
     * @param event The event to check.
     * @return True if this interaction handler handles the event.
     */
    public boolean handles(PlayerInteractEvent event) {
        return materials.contains(event.getPlayer().getInventory().getItemInMainHand().getType()) || (interactPredicate != null && interactPredicate.test(event));
    }

    public boolean handles(PlayerInteractEntityEvent event) {
        return materials.contains(event.getPlayer().getInventory().getItemInMainHand().getType()) || (entityPredicate != null && entityPredicate.test(event));
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
     * Called when the player right clicks an entity.
     * @param event The event.
     */
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        // Default implementation does nothing
    }

    /**
     * Called when the player left clicks an entity.
     * @param event The event.
     */
    public void onLeftClickEntity(PlayerInteractEntityEvent event) {
        // Default implementation does nothing
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
     * Get the material in the player calling the event's hand.
     * @param event The event.
     * @return The material in the player's hand.
     */
    public final Material getHandMat(PlayerInteractEntityEvent event) {
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
        if (!coreProtectService.isEnabled()) return;
        if (placement) coreProtectService.logPlacement(player.getName(), changedBlock.getLocation(), changedBlock.getType(), changedBlock.getBlockData());
        else coreProtectService.logRemoval(player.getName(), changedBlock.getLocation(), changedBlock.getType(), changedBlock.getBlockData());
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
        worldEditService.setBlock(location, data);
        log(true, player, location.getBlock());
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        return getInteractionName();
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var questionMark = Component.text("?", paginator.getHighlightColor(), TextDecoration.BOLD).toBuilder();
        questionMark.hoverEvent(HoverEvent.showText(Component.text("Click to view detailed information about this interaction handler.", paginator.getGrayColor())));
        questionMark.clickEvent(ClickEvent.runCommand("/avb " + getInteractionName()));

        var line = Component.text();
        line.append(questionMark);
        line.resetStyle().appendSpace();
        line.append(Component.text("| ", paginator.getGrayColor()).append(Component.text(getInteractionName()).hoverEvent(HoverEvent.showText(getInteractionDescription().color(paginator.getGrayColor())))));
        return line.build();
    }
}
