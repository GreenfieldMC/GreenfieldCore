package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class DynmapServiceImpl extends ModuleService<IDynmapService> implements IDynmapService {

    protected MarkerAPI markerApi;

    public DynmapServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        var dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin != null) {
            markerApi = ((org.dynmap.DynmapCommonAPI) dynmapPlugin).getMarkerAPI();
        } else {
            throw new Exception("Dynmap not found");
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
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

    @Override
    public boolean tryCreateMarkerSet(String setId, String setName, String icon) {
        if (!isEnabled()) return false;
        var set = markerApi.getMarkerSet(setId);
        if (set != null) return false; // already exists
        set = markerApi.createMarkerSet(setId, setName, null, false);
        if (set == null) {
            getPlugin().getLogger().warning("Unable to create marker set " + setId);
            return false;
        }
        if (icon != null) {
            var markerIcon = getOrCreateMarkerIcon(icon);
            if (markerIcon != null) {
                set.setDefaultMarkerIcon(markerIcon);
            } else {
                getPlugin().getLogger().warning("Unable to load icon " + icon + " for marker set " + setId);
            }
        }
        return true;
    }

    @Override
    public boolean tryDeleteMarkerSet(String setId) {
        if (!isEnabled()) return false;
        var set = markerApi.getMarkerSet(setId);
        if (set == null) return false; // doesn't exist
        set.deleteMarkerSet();
        return true;
    }

    @Override
    public MarkerSet getMarkerSet(String setId) {
        if (!isEnabled()) return null;
        return markerApi.getMarkerSet(setId);
    }

    @Override
    public MarkerIcon getMarkerIcon(String iconName) {
        if (!isEnabled()) return null;
        return markerApi.getMarkerIcon(iconName);
    }

    @Override
    public MarkerAPI getMarkerAPI() {
        if (!isEnabled()) return null;
        return markerApi;
    }

    protected MarkerSet getOrCreateMarkerSet(String setName) {
        if (!isEnabled()) return null;
        var set = markerApi.getMarkerSet(setName);
        if (set == null) {
            set = markerApi.createMarkerSet(setName, setName, null, false);
            if (set == null) getPlugin().getLogger().warning("Unable to create marker set " + setName);
        }
        return set;
    }

    protected MarkerIcon getOrCreateMarkerIcon(String iconName) {
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
