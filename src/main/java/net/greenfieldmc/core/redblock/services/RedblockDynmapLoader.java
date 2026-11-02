package net.greenfieldmc.core.redblock.services;

import net.greenfieldmc.core.shared.services.IDynmapService;
import org.dynmap.markers.Marker;
import com.njdaeger.pdk.serviceprovider.Nullable;

/**
 * Creates the redblock marker sets in Dynmap on startup (clearing any markers left over from the last run) and removes
 * them on shutdown. Services that add redblock markers should depend on this so the sets exist first. Does nothing when
 * Dynmap is not installed.
 */
public class RedblockDynmapLoader implements AutoCloseable {

    public final static String PENDING_MARKER_ICON = "yellowblock";
    public final static String INCOMPLETE_MARKER_ICON = "redblock";

    public final static String PENDING_MARKER_SET = "Pending";
    public final static String INCOMPLETE_MARKER_SET = "Incomplete";

    private final @Nullable IDynmapService dynmapService;

    public RedblockDynmapLoader(@Nullable IDynmapService dynmapService) {
        this.dynmapService = dynmapService;
        if (dynmapService != null) setupMarkerSets(dynmapService);
    }

    @Override
    public void close() {
        if (dynmapService != null) removeMarkerSets(dynmapService);
    }

    private static void setupMarkerSets(IDynmapService dynmap) {
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

        pendingSet.getMarkers().forEach(Marker::deleteMarker);
        incompleteSet.getMarkers().forEach(Marker::deleteMarker);

        incompleteSet.setLayerPriority(-11);
        incompleteSet.setHideByDefault(true);
        incompleteSet.setDefaultMarkerIcon(incompleteIcon);

        pendingSet.setLayerPriority(-10);
        pendingSet.setHideByDefault(true);
        pendingSet.setDefaultMarkerIcon(pendingIcon);
    }

    private static void removeMarkerSets(IDynmapService dynmap) {
        var markerApi = dynmap.getMarkerAPI();
        var incompleteSet = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
        if (incompleteSet != null) incompleteSet.deleteMarkerSet();
        var pendingSet = markerApi.getMarkerSet(PENDING_MARKER_SET);
        if (pendingSet != null) pendingSet.deleteMarkerSet();
    }

}
