package com.njdaeger.greenfieldcore.shared.services;

import com.njdaeger.greenfieldcore.IModuleService;
import org.bukkit.Location;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public interface IDynmapService extends IModuleService<IDynmapService> {

    boolean tryCreateMarker(String markerSet, String markerId, String html, Location location, String iconName, boolean persistent);

    boolean tryDeleteMarker(String markerSet, String markerId);

    boolean tryCreateMarkerSet(String setId, String setName, String icon);

    boolean tryDeleteMarkerSet(String setId);

    MarkerSet getMarkerSet(String setId);

    MarkerIcon getMarkerIcon(String iconName);

    MarkerAPI getMarkerAPI();
}
