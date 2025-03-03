package com.njdaeger.greenfieldcore.redblock.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.redblock.Redblock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import static com.njdaeger.greenfieldcore.redblock.services.RedblockServiceHelpers.createMarkerHtml;

public class DynmapServiceImpl extends ModuleService<IDynmapService> implements IDynmapService {

    private MarkerAPI markerApi;
    private final IRedblockStorageService storageService;

    public DynmapServiceImpl(Plugin plugin, Module module, IRedblockStorageService storageService) {
        super(plugin, module);
        this.storageService = storageService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        var dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin != null) {
            markerApi = ((org.dynmap.DynmapCommonAPI) dynmapPlugin).getMarkerAPI();
            loadRedblocksToDynmap();
        } else {
            throw new Exception("Dynmap not found");
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        if (isEnabled()) {
            var incompleteSet = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
            if (incompleteSet != null) incompleteSet.deleteMarkerSet();
            var pendingSet = markerApi.getMarkerSet(PENDING_MARKER_SET);
            if (pendingSet != null) pendingSet.deleteMarkerSet();
        }
    }

    @Override
    public boolean isEnabled() {
        return markerApi != null && super.isEnabled();
    }

    @Override
    public boolean tryCreateMarker(String markerSet, String markerId, String html, Location location, String iconName, boolean persistent) {
        if (!isEnabled()) return false;
        var set = getOrCreateMarkerSet(markerSet);
        var icon = getOrCreateMarkerIcon(iconName);
        if (set == null || icon == null) return false;
        var marker = set.createMarker(markerId, html, true, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icon, persistent);
        return marker != null;
    }

    @Override
    public boolean tryDeleteMarker(String markerSet, String markerId) {
        if (!isEnabled()) return false;
        var set = getOrCreateMarkerSet(markerSet);
        if (set == null) return false;
        var marker = set.findMarker(markerId);
        if (marker == null) return false;

        marker.deleteMarker();
        return true;
    }

    private void loadRedblocksToDynmap() {
        if (!isEnabled()) return;
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

    private MarkerSet getOrCreateMarkerSet(String setName) {
        if (!isEnabled()) return null;
        var set = markerApi.getMarkerSet(setName);
        if (set == null) {
            set = markerApi.createMarkerSet(setName, setName, null, false);
            if (set == null) getPlugin().getLogger().warning("Unable to create marker set " + setName);
        }
        return set;
    }

    private MarkerIcon getOrCreateMarkerIcon(String iconName) {
        if (!isEnabled()) return null;
        var icon = markerApi.getMarkerIcon(iconName);
        var stream = getPlugin().getResource(iconName + ".png");
        if (icon == null) {
            if (stream != null) icon = markerApi.createMarkerIcon(iconName, iconName, stream);
            else getPlugin().getLogger().warning("Unable to load icon " + iconName);
        } else if (stream != null) icon.setMarkerIconImage(stream);
        return icon;
    }
}
