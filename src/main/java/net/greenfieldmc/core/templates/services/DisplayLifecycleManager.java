package net.greenfieldmc.core.templates.services;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.greenfieldmc.core.templates.models.PlacementSession;
import net.greenfieldmc.core.templates.models.Template;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Manages the lifecycle of block display entities that form a template model preview.
 * Handles spawning (with optional Y-rotation), teleporting, and despawning.
 */
public class DisplayLifecycleManager {

    private static final Display.Brightness DEFAULT_BRIGHTNESS = new Display.Brightness(15, 15);
    private static final Transformation UNIT_TRANSFORMATION = new Transformation(
            new Vector3f(), new Quaternionf(), new Vector3f(1f, 1f, 1f), new Quaternionf()
    );

    /** Maximum number of visible block display entities before refusing to render. */
    static final int MAX_DISPLAY_ENTITIES = 16000;

    // =========================================================
    //  spawnModel
    // =========================================================

    /**
     * Spawn a full model of the template at the given anchor location with optional Y-rotation.
     *
     * <p>The {@code anchor} is the world location corresponding to the clipboard's origin point.
     * Block positions and block-state orientation are both transformed according to
     * {@code rotationDegrees} using WorldEdit's {@link AffineTransform} and {@link BlockTransformExtent}.
     *
     * @param player         The player who should see the displays.
     * @param plugin         The owning plugin.
     * @param template       The loaded template.
     * @param anchor         World location of the clipboard origin after rotation.
     * @param originTeam     Scoreboard team for the origin marker display.
     * @param onConfirm      Callback invoked with the anchor location when the player confirms.
     * @param rotationDegrees Y-axis rotation in degrees (0 / 90 / 180 / 270).
     *                        Positive = counter-clockwise in WorldEdit convention
     *                        (scroll-down = +90, scroll-up = -90).
     * @return A {@link PlacementSession} ready for tick-loop tracking.
     * @throws IllegalArgumentException if the visible block count exceeds {@link #MAX_DISPLAY_ENTITIES}.
     */
    public static PlacementSession spawnModel(Player player, Plugin plugin, Template template,
                                              Location anchor, Team originTeam,
                                              BiConsumer<Location, Integer> onConfirm, int rotationDegrees)
            throws IllegalArgumentException {

        var clipboard = template.getClipboard();
        var transform = new AffineTransform().rotateY(rotationDegrees);
        // BlockTransformExtent rotates block-state orientation (stairs, slabs, etc.)
        var transformedExtent = new BlockTransformExtent(clipboard, transform);

        // ---- Pre-check visible count ----
        int visibleCount = 0;
        for (var pos : clipboard.getRegion()) {
            var rel = pos.subtract(clipboard.getMinimumPoint());
            if (isBlockHidden(clipboard, rel.x(), rel.y(), rel.z())) continue;
            if (isInvisibleBlockType(clipboard.getBlock(pos).getBlockType())) continue;
            if (++visibleCount > MAX_DISPLAY_ENTITIES) {
                throw new IllegalArgumentException(
                        "Template has too many visible blocks (" + visibleCount + ") to render. Maximum is " + MAX_DISPLAY_ENTITIES + ".");
            }
        }

        // ---- Spawn displays ----
        var entries = new ArrayList<PlacementSession.DisplayEntry>();

        for (var pos : clipboard.getRegion()) {
            var rel = pos.subtract(clipboard.getMinimumPoint());
            if (isBlockHidden(clipboard, rel.x(), rel.y(), rel.z())) continue;
            if (isInvisibleBlockType(clipboard.getBlock(pos).getBlockType())) continue;

            // Rotated block-state (stair/slab directions, etc.)
            var blockData = BukkitAdapter.adapt(transformedExtent.getFullBlock(pos));

            // Rotated position relative to the origin
            var relToOrigin = pos.subtract(clipboard.getOrigin()).toVector3();
            var rotatedRel  = transform.apply(relToOrigin).toBlockPoint();
            var offset      = new Vector3i(rotatedRel.x(), rotatedRel.y(), rotatedRel.z());

            var spawnLoc = anchor.clone().add(offset.x, offset.y, offset.z);
            var display  = spawnDisplay(spawnLoc, blockData, UNIT_TRANSFORMATION, DEFAULT_BRIGHTNESS, null);
            player.showEntity(plugin, display);
            entries.add(new PlacementSession.DisplayEntry(display, offset));
        }

        // Origin marker
        var originDisplay = spawnDisplay(anchor.clone(), Material.RED_WOOL.createBlockData(),
                UNIT_TRANSFORMATION, DEFAULT_BRIGHTNESS, originTeam);
        originDisplay.setGlowing(true);
        player.showEntity(plugin, originDisplay);

        return new PlacementSession(template, entries, originDisplay, new Vector3i(0, 0, 0),
                anchor, onConfirm, rotationDegrees);
    }

    // =========================================================
    //  teleportModel
    // =========================================================

    /**
     * Teleport all display entities in the session to a new anchor location.
     * Offsets are already in the rotated coordinate space — no re-rotation needed.
     */
    public static void teleportModel(PlacementSession session, Location newAnchor) {
        session.setAnchorLocation(newAnchor);
        for (var entry : session.getDisplayEntries()) {
            entry.display().teleport(newAnchor.clone().add(entry.offset().x, entry.offset().y, entry.offset().z));
        }
        session.getOriginDisplay().teleport(newAnchor);
    }

    // =========================================================
    //  destroyModel
    // =========================================================

    /**
     * Remove all display entities and clean up the scoreboard team entry.
     * Also cancels the tick task if one is running.
     */
    public static void destroyModel(Player player, Plugin plugin, PlacementSession session, Team originTeam) {
        for (var entry : session.getDisplayEntries()) {
            player.hideEntity(plugin, entry.display());
            entry.display().remove();
        }
        var originDisplay = session.getOriginDisplay();
        if (originTeam.hasEntry(originDisplay.getUniqueId().toString())) {
            originTeam.removeEntry(originDisplay.getUniqueId().toString());
        }
        player.hideEntity(plugin, originDisplay);
        originDisplay.remove();

        if (session.getTickTask() != null) session.getTickTask().cancel();
    }

    // =========================================================
    //  spawn helper
    // =========================================================

    private static BlockDisplay spawnDisplay(Location location, BlockData blockData,
                                              Transformation transformation,
                                              Display.Brightness brightness,
                                              @Nullable Team team) {
        var display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(blockData);
        display.setTransformation(transformation);
        display.setBrightness(brightness);
        display.setGlowing(true);
        display.setVisibleByDefault(false);
        if (team != null) team.addEntry(display.getUniqueId().toString());
        return display;
    }

    // =========================================================
    //  Visibility helpers
    // =========================================================

    static boolean isInvisibleBlockType(BlockType blockType) {
        return blockType == BlockTypes.AIR
                || blockType == BlockTypes.CAVE_AIR
                || blockType == BlockTypes.VOID_AIR
                || blockType == BlockTypes.WATER
                || blockType == BlockTypes.LAVA;
    }

    static boolean isBlockHidden(BlockArrayClipboard clipboard, int x, int y, int z) {
        var min = clipboard.getMinimumPoint();
        var north = clipboard.getBlock(BlockVector3.at(x - 1, y, z).add(min)).getBlockType();
        var south = clipboard.getBlock(BlockVector3.at(x + 1, y, z).add(min)).getBlockType();
        var west  = clipboard.getBlock(BlockVector3.at(x, y, z - 1).add(min)).getBlockType();
        var east  = clipboard.getBlock(BlockVector3.at(x, y, z + 1).add(min)).getBlockType();
        var up    = clipboard.getBlock(BlockVector3.at(x, y + 1, z).add(min)).getBlockType();
        var down  = clipboard.getBlock(BlockVector3.at(x, y - 1, z).add(min)).getBlockType();
        return List.of(north, south, west, east, up, down).stream()
                .noneMatch(t -> isInvisibleBlockType(t) || !t.getMaterial().isFullCube());
    }
}
