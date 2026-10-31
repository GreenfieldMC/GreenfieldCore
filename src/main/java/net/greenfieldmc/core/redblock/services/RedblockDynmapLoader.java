package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.shared.services.IDynmapService;
import org.bukkit.Bukkit;
import org.dynmap.markers.Marker;
import com.njdaeger.pdk.serviceprovider.Nullable;

import static net.greenfieldmc.core.redblock.services.RedblockServiceHelpers.createMarkerHtml;

/**
 * Syncs the stored redblocks into Dynmap on startup and removes the redblock marker sets on shutdown. Does nothing when
 * Dynmap is not installed.
 */
public class RedblockDynmapLoader implements AutoCloseable {

    public final static String PENDING_MARKER_ICON = "yellowblock";
    public final static String INCOMPLETE_MARKER_ICON = "redblock";

    public final static String PENDING_MARKER_SET = "Pending";
    public final static String INCOMPLETE_MARKER_SET = "Incomplete";

    private final IGreenfieldCorePlugin plugin;
    private final @Nullable IDynmapService dynmapService;
    private final IRedblockStorageService storageService;

    public RedblockDynmapLoader(IGreenfieldCorePlugin plugin, @Nullable IDynmapService dynmapService, IRedblockStorageService storageService) {
        this.plugin = plugin;
        this.dynmapService = dynmapService;
        this.storageService = storageService;
        if (dynmapService != null) loadRedblocksToDynmap(dynmapService);
    }

    @Override
    public void close() {
        if (dynmapService == null) return;
        var markerApi = dynmapService.getMarkerAPI();
        var incompleteSet = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
        if (incompleteSet != null) incompleteSet.deleteMarkerSet();
        var pendingSet = markerApi.getMarkerSet(PENDING_MARKER_SET);
        if (pendingSet != null) pendingSet.deleteMarkerSet();
    }

    private void loadRedblocksToDynmap(IDynmapService dynmap) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Adding RedBlock markers to Dynmap...");

            var pendingIcon = dynmap.getOrCreateMarkerIcon(PENDING_MARKER_ICON);
            if (pendingIcon == null) throw new RuntimeException("Unable to find the pending icon necessary for pending RedBlock markers on the Dynmap.");
            var incompleteIcon = dynmap.getOrCreateMarkerIcon(INCOMPLETE_MARKER_ICON);
            if (incompleteIcon == null) throw new RuntimeException("Unable to find the incomplete icon necessary for incomplete RedBlock markers on the Dynmap.");

            var pendingSet = dynmap.getOrCreateMarkerSet(PENDING_MARKER_SET);
            var incompleteSet = dynmap.getOrCreateMarkerSet(INCOMPLETE_MARKER_SET);

            if (pendingSet == null)
                throw new RuntimeException("Unable to create the Dynmap MarkerSet for pending RedBlocks.");

            if (incompleteSet == null)
                throw new RuntimeException("Unable to create the Dynmap MarkerSet for incomplete RedBlocks.");

            plugin.getLogger().info("Removing old pending markers...");
            pendingSet.getMarkers().forEach(Marker::deleteMarker);
            plugin.getLogger().info("Removing old incomplete markers...");
            incompleteSet.getMarkers().forEach(Marker::deleteMarker);

            incompleteSet.setLayerPriority(-11);
            incompleteSet.setHideByDefault(true);
            incompleteSet.setDefaultMarkerIcon(incompleteIcon);

            pendingSet.setLayerPriority(-10);
            pendingSet.setHideByDefault(true);
            pendingSet.setDefaultMarkerIcon(pendingIcon);

            plugin.getLogger().info("Recreating all RedBlock markers...");
            storageService.getRedblocks(Redblock::isPending).forEach(rb -> {
                dynmap.tryCreateMarker(PENDING_MARKER_SET, rb.getId() + "_redblock", createMarkerHtml(rb), rb.getLocation(), PENDING_MARKER_ICON, true);
            });

            storageService.getRedblocks(Redblock::isIncomplete).forEach(rb -> {
                dynmap.tryCreateMarker(INCOMPLETE_MARKER_SET, rb.getId() + "_redblock", createMarkerHtml(rb), rb.getLocation(), INCOMPLETE_MARKER_ICON, true);
            });

            plugin.getLogger().info("Done adding RedBlock markers to Dynmap.");
        });
    }

}
