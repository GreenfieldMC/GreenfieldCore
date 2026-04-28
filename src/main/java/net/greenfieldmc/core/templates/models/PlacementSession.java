package net.greenfieldmc.core.templates.models;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Vector3i;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Represents an active placement session where a player is positioning a template model
 * constructed of block display entities before confirming placement.
 */
public class PlacementSession {

    private final Template template;
    private final List<DisplayEntry> displayEntries;
    private final BlockDisplay originDisplay;
    private final Vector3i originOffset;
    private Location anchorLocation;
    private BukkitTask tickTask;
    private final BiConsumer<Location, Integer> onConfirm;
    private int rotationDegrees;

    /** A single display entity and its relative offset from the anchor point. */
    public record DisplayEntry(BlockDisplay display, Vector3i offset) {}

    public PlacementSession(Template template, List<DisplayEntry> displayEntries, BlockDisplay originDisplay, Vector3i originOffset, Location anchorLocation, BiConsumer<Location, Integer> onConfirm, int rotationDegrees) {
        this.template = template;
        this.displayEntries = displayEntries;
        this.originDisplay = originDisplay;
        this.originOffset = originOffset;
        this.anchorLocation = anchorLocation;
        this.onConfirm = onConfirm;
        this.rotationDegrees = rotationDegrees;
    }

    public Template getTemplate() {
        return template;
    }

    public List<DisplayEntry> getDisplayEntries() {
        return displayEntries;
    }

    public BlockDisplay getOriginDisplay() {
        return originDisplay;
    }

    public Vector3i getOriginOffset() {
        return originOffset;
    }

    public Location getAnchorLocation() {
        return anchorLocation;
    }

    public void setAnchorLocation(Location anchorLocation) {
        this.anchorLocation = anchorLocation;
    }

    public BukkitTask getTickTask() {
        return tickTask;
    }

    public void setTickTask(BukkitTask tickTask) {
        this.tickTask = tickTask;
    }

    public BiConsumer<Location, Integer> getOnConfirm() {
        return onConfirm;
    }

    public int getRotationDegrees() {
        return rotationDegrees;
    }

    public void setRotationDegrees(int rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
    }
}

