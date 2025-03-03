package com.njdaeger.greenfieldcore.redblock.services;

import com.njdaeger.greenfieldcore.IModuleService;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public interface IDynmapService extends IModuleService<IDynmapService> {

    String PENDING_MARKER_ICON = "yellowblock";
    String INCOMPLETE_MARKER_ICON = "redblock";

    String PENDING_MARKER_SET = "Pending";
    String INCOMPLETE_MARKER_SET = "Incomplete";

    boolean tryCreateMarker(String markerSet, String markerId, String html, Location location, String iconName, boolean persistent);

    boolean tryDeleteMarker(String markerSet, String markerId);
}
