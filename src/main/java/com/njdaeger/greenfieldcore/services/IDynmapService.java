package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.IModuleService;
import org.bukkit.Location;

public interface IDynmapService extends IModuleService<IDynmapService> {

    boolean tryCreateMarker(String markerSet, String markerId, String html, Location location, String iconName, boolean persistent);

    boolean tryDeleteMarker(String markerSet, String markerId);

    boolean tryCreateMarkerSet(String setId, String setName, String icon);

    boolean tryDeleteMarkerSet(String setId);
}
