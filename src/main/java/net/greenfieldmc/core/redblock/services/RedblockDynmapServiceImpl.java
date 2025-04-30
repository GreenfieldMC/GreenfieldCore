package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.shared.services.DynmapServiceImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.markers.Marker;

import static net.greenfieldmc.core.redblock.services.RedblockServiceHelpers.createMarkerHtml;

public class RedblockDynmapServiceImpl extends DynmapServiceImpl {

    public final static String PENDING_MARKER_ICON = "yellowblock";
    public final static String INCOMPLETE_MARKER_ICON = "redblock";

    public final static String PENDING_MARKER_SET = "Pending";
    public final static String INCOMPLETE_MARKER_SET = "Incomplete";

    private final IRedblockStorageService storageService;

    public RedblockDynmapServiceImpl(Plugin plugin, Module module, IRedblockStorageService storageService) {
        super(plugin, module);
        this.storageService = storageService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        super.tryEnable(plugin, module);
    }

    @Override
    public void postTryEnable(Plugin plugin, Module module) throws Exception {
        loadRedblocksToDynmap();
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        super.tryDisable(plugin, module);
        if (isEnabled()) {
            var incompleteSet = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
            if (incompleteSet != null) incompleteSet.deleteMarkerSet();
            var pendingSet = markerApi.getMarkerSet(PENDING_MARKER_SET);
            if (pendingSet != null) pendingSet.deleteMarkerSet();
        }
    }

    private void loadRedblocksToDynmap() {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            getPlugin().getLogger().info("Adding RedBlock markers to Dynmap...");

            var pendingIcon = getOrCreateMarkerIcon(PENDING_MARKER_ICON);
            if (pendingIcon == null) throw new RuntimeException("Unable to find the pending icon necessary for pending RedBlock markers on the Dynmap.");
            var incompleteIcon = getOrCreateMarkerIcon(INCOMPLETE_MARKER_ICON);
            if (incompleteIcon == null) throw new RuntimeException("Unable to find the incomplete icon necessary for incomplete RedBlock markers on the Dynmap.");

            var pendingSet = getOrCreateMarkerSet(PENDING_MARKER_SET);
            var incompleteSet = getOrCreateMarkerSet(INCOMPLETE_MARKER_SET);

            if (pendingSet == null)
                throw new RuntimeException("Unable to create the Dynmap MarkerSet for pending RedBlocks.");

            if (incompleteSet == null)
                throw new RuntimeException("Unable to create the Dynmap MarkerSet for incomplete RedBlocks.");

            getPlugin().getLogger().info("Removing old pending markers...");
            pendingSet.getMarkers().forEach(Marker::deleteMarker);
            getPlugin().getLogger().info("Removing old incomplete markers...");
            incompleteSet.getMarkers().forEach(Marker::deleteMarker);

            incompleteSet.setLayerPriority(-11);
            incompleteSet.setHideByDefault(true);
            incompleteSet.setDefaultMarkerIcon(incompleteIcon);

            pendingSet.setLayerPriority(-10);
            pendingSet.setHideByDefault(true);
            pendingSet.setDefaultMarkerIcon(pendingIcon);

            getPlugin().getLogger().info("Recreating all RedBlock markers...");
            storageService.getRedblocks(Redblock::isPending).forEach(rb -> {
                tryCreateMarker(PENDING_MARKER_SET, rb.getId() + "_redblock", createMarkerHtml(rb), rb.getLocation(), PENDING_MARKER_ICON, true);
            });

            storageService.getRedblocks(Redblock::isIncomplete).forEach(rb -> {
                tryCreateMarker(INCOMPLETE_MARKER_SET, rb.getId() + "_redblock", createMarkerHtml(rb), rb.getLocation(), INCOMPLETE_MARKER_ICON, true);
            });

            getPlugin().getLogger().info("Done adding RedBlock markers to Dynmap.");
        });
    }

}
