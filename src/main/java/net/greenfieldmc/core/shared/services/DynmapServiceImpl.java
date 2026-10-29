package net.greenfieldmc.core.shared.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import org.bukkit.Location;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.logging.Logger;

public class DynmapServiceImpl implements IDynmapService {

    private final IGreenfieldCorePlugin plugin;
    private final Logger logger;
    private final MarkerAPI markerApi;

    public DynmapServiceImpl(IGreenfieldCorePlugin plugin, Logger logger) throws Exception {
        this.plugin = plugin;
        this.logger = logger;
        var dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) throw new Exception("Dynmap not found");
        this.markerApi = ((org.dynmap.DynmapCommonAPI) dynmapPlugin).getMarkerAPI();
    }

    @Override
    public boolean tryCreateMarker(String markerSet, String markerId, String html, Location location, String iconName, boolean persistent) {
        var set = getOrCreateMarkerSet(markerSet);
        var icon = getOrCreateMarkerIcon(iconName);
        if (set == null || icon == null) return false;
        var marker = set.createMarker(markerId, html, true, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icon, persistent);
        return marker != null;
    }

    @Override
    public boolean tryDeleteMarker(String markerSet, String markerId) {
        var set = getOrCreateMarkerSet(markerSet);
        if (set == null) return false;
        var marker = set.findMarker(markerId);
        if (marker == null) return false;

        marker.deleteMarker();
        return true;
    }

    @Override
    public boolean tryCreateMarkerSet(String setId, String setName, String icon) {
        var set = markerApi.getMarkerSet(setId);
        if (set != null) return false; // already exists
        set = markerApi.createMarkerSet(setId, setName, null, false);
        if (set == null) {
            logger.warning("Unable to create marker set " + setId);
            return false;
        }
        if (icon != null) {
            var markerIcon = getOrCreateMarkerIcon(icon);
            if (markerIcon != null) {
                set.setDefaultMarkerIcon(markerIcon);
            } else {
                logger.warning("Unable to load icon " + icon + " for marker set " + setId);
            }
        }
        return true;
    }

    @Override
    public boolean tryDeleteMarkerSet(String setId) {
        var set = markerApi.getMarkerSet(setId);
        if (set == null) return false; // doesn't exist
        set.deleteMarkerSet();
        return true;
    }

    @Override
    public MarkerSet getMarkerSet(String setId) {
        return markerApi.getMarkerSet(setId);
    }

    @Override
    public MarkerIcon getMarkerIcon(String iconName) {
        return markerApi.getMarkerIcon(iconName);
    }

    @Override
    public MarkerAPI getMarkerAPI() {
        return markerApi;
    }

    @Override
    public MarkerSet getOrCreateMarkerSet(String setName) {
        var set = markerApi.getMarkerSet(setName);
        if (set == null) {
            set = markerApi.createMarkerSet(setName, setName, null, false);
            if (set == null) logger.warning("Unable to create marker set " + setName);
        }
        return set;
    }

    @Override
    public MarkerIcon getOrCreateMarkerIcon(String iconName) {
        var icon = markerApi.getMarkerIcon(iconName.endsWith(".png") ? iconName.split("\\.")[0] : iconName);
        var stream = plugin.getResource(iconName);
        if (icon != null) {
            if (stream != null) {
                icon.setMarkerIconImage(stream);
            }
        } else {
            if (stream == null) {
                logger.warning("Unable to load icon " + iconName);
                return null;
            }
            icon = markerApi.createMarkerIcon(iconName, iconName, stream);
        }
        return icon;
    }
}
