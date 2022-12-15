package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class RedblockModule extends Module {

    private RedblockStorage storage;
    private MarkerAPI markerApi;

    private static final String PENDING_MARKER_SET = "Pending";
    private static final String INCOMPLETE_MARKER_SET = "Incomplete";

    public RedblockModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null || Bukkit.getPluginManager().getPlugin("Essentials") == null) {
            Bukkit.getLogger().warning("Unable to start RedblockModule. Vault or Essentials was not found.");
            return;
        }
        this.storage = new RedblockStorage(plugin, this);
        new RedblockCommands(this, this.storage, plugin);
        Bukkit.getPluginManager().registerEvents(new RedblockListener(storage), plugin);

        Plugin dynmap;
        if ((dynmap = Bukkit.getPluginManager().getPlugin("dynmap")) != null) {
            this.markerApi = ((DynmapAPI)dynmap).getMarkerAPI();
            loadRedblocksToDynmap();
        }

        //TODO enable this for 1.18+
        Bukkit.getWhitelistedPlayers().forEach(op -> op.getPlayerProfile().update().thenAcceptAsync(result -> {
            if (result.isComplete()) RedblockUtils.userNameMap.put(result.getUniqueId(), result.getName());
        }));
    }

    private MarkerIcon createOrGetMarkerIcon(String iconName) {
        MarkerIcon icon = markerApi.getMarkerIcon(iconName);
        InputStream stream = plugin.getResource(iconName + ".png");
        if (icon != null) {
            if (stream != null) icon.setMarkerIconImage(stream);
        } else {
            if (stream == null) {
                plugin.getLogger().warning("Unable to find the icon " + iconName + ".png");
                return null;
            }
            icon = markerApi.createMarkerIcon(iconName, iconName, stream);
        }
        return icon;
    }

    public void loadRedblocksToDynmap() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Adding redblock markers to dynmap...");
            MarkerIcon pendingIcon = createOrGetMarkerIcon("yellowblock");
            if (pendingIcon == null) throw new RuntimeException("Unable to find the pending icon needed for pending redblocks");
            MarkerIcon incompleteIcon = createOrGetMarkerIcon("redblock");
            if (incompleteIcon == null) throw new RuntimeException("Unable to find the incomplete icon needed for incomplete redblocks");

            MarkerSet pending;
            MarkerSet incomplete;
            if (markerApi.getMarkerSet(PENDING_MARKER_SET) == null) pending = markerApi.createMarkerSet(PENDING_MARKER_SET, PENDING_MARKER_SET, null, false);
            else {
                pending = markerApi.getMarkerSet(PENDING_MARKER_SET);
                plugin.getLogger().info("Removing old pending markers...");
                pending.getMarkers().forEach(Marker::deleteMarker);
                plugin.getLogger().info("Done");
            }
            if (markerApi.getMarkerSet(INCOMPLETE_MARKER_SET) == null) incomplete = markerApi.createMarkerSet(INCOMPLETE_MARKER_SET, INCOMPLETE_MARKER_SET, null, false);
            else {
                incomplete = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
                plugin.getLogger().info("Removing old incomplete markers...");
                incomplete.getMarkers().forEach(Marker::deleteMarker);
                plugin.getLogger().info("Done");
            }

            incomplete.setLayerPriority(-11);
            pending.setLayerPriority(-10);

            incomplete.setHideByDefault(true);
            incomplete.setDefaultMarkerIcon(incompleteIcon);

            pending.setHideByDefault(true);
            pending.setDefaultMarkerIcon(pendingIcon);

            storage.getPendingRedblocks().forEach(this::createMarker);

            storage.getIncompleteRedblocks().forEach(this::createMarker);
            plugin.getLogger().info("Redblock marker load complete!");
        });
    }

    private Marker createMarker(Redblock rb) {
        StringBuilder html = new StringBuilder();
        MarkerSet set;
        MarkerIcon icon;

        html.append("<div style=\"display:flex; flex-direction: column; padding: .25rem; max-width: 12rem; min-width: 8rem;\">");
        if (rb.isIncomplete()) {
            set = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
            icon = createOrGetMarkerIcon("redblock");
            html.append("<span><strong>Incomplete</strong></span>");

        } else {
            set = markerApi.getMarkerSet(PENDING_MARKER_SET);
            icon = createOrGetMarkerIcon("yellowblock");
            html.append("<span><strong>Pending</strong></span>");
        }
        if (rb.getAssignedTo() != null) html.append("<span>Assigned To: <strong>").append(Bukkit.getOfflinePlayer(rb.getAssignedTo()).getName()).append("</strong></span>");
        if (rb.getMinRank() != null) html.append("<span>Recommended Rank: <strong>").append(rb.getMinRank()).append("</strong></span>");
        html.append("<hr style=\"width: 100%;\"><span style=\"white-space: pre-wrap;\">").append(rb.getContent()).append("</span>").append("</div>");
        return set.createMarker(rb.getId() + "_redblock", html.toString(), true, rb.getLocation().getWorld().getName(), rb.getLocation().getX(), rb.getLocation().getY(), rb.getLocation().getZ(), icon, false);
    }

    public void updateRedblock(Redblock updatedRb, boolean isEdit) {
        if (isEdit) {
            Marker marker = markerApi.getMarkerSet(PENDING_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker == null) marker = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker != null) marker.deleteMarker();
            createMarker(updatedRb);
        }
        if (updatedRb.isApproved()) {
            Marker marker = markerApi.getMarkerSet(PENDING_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker != null) marker.deleteMarker();
        } else if (updatedRb.isIncomplete()) {
            //if we get an incomplete redblock passed to this function, it will have either gone from pending approval back to incomplete, or it was newly created.
            Marker marker = markerApi.getMarkerSet(PENDING_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker != null) marker.deleteMarker();

            createMarker(updatedRb);
        } else if (updatedRb.isDeleted()) {
            Marker marker = markerApi.getMarkerSet(PENDING_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker == null) marker = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker != null) marker.deleteMarker();
        } else {
            Marker marker = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET).findMarker(updatedRb.getId() + "_redblock");
            if (marker != null) marker.deleteMarker();
            createMarker(updatedRb);
        }
    }

    @Override
    public void onDisable() {
        if (markerApi != null) {
            var set = markerApi.getMarkerSet(INCOMPLETE_MARKER_SET);
            if (set != null) set.deleteMarkerSet();
            set = markerApi.getMarkerSet(PENDING_MARKER_SET);
            if (set != null) set.deleteMarkerSet();
        }
        storage.save();
    }
}
