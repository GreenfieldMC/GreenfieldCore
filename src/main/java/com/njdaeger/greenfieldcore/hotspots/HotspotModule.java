package com.njdaeger.greenfieldcore.hotspots;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HotspotModule extends Module {

    private HotspotStorage storage;
    private MarkerAPI markerApi;

    public HotspotModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("dynmap") == null || Bukkit.getPluginManager().getPlugin("essentials") == null) {
            Bukkit.getLogger().warning("Unable to start HotspotModule. Dynmap or Essentials was not found.");
            return;
        }
        this.markerApi = ((DynmapAPI)Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI();
        this.storage = new HotspotStorage(plugin);
        new HotspotCommands(this, this.storage, plugin);

        loadHotspotsToDynmap();
    }

    public void loadHotspotsToDynmap() {

        //Loading possible icons from the jar file
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".png")) {
                    MarkerIcon icon = markerApi.getMarkerIcon(entry.getName().split("\\.")[0]);
                    if (icon == null) {
                        markerApi.createMarkerIcon(entry.getName().split("\\.")[0], entry.getName().split("\\.")[0], zipFile.getInputStream(entry));
                    } else {
                        icon.setMarkerIconImage(zipFile.getInputStream(entry));
                    }
                }
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        storage.getCategories().forEach((id, category) -> {

            MarkerIcon icon = markerApi.getMarkerIcon(category.getMarker().endsWith(".png") ? category.getMarker().split("\\.")[0] : category.getMarker());
            if (icon == null) {
                Bukkit.getLogger().warning("Unable to load marker " + category.getMarker() + ". Category " + id + " will not be loaded.");
                return;
            }

            MarkerSet set;
            if (markerApi.getMarkerSet(id) == null) set = markerApi.createMarkerSet(id, category.getName(), null, false);
            else set = markerApi.getMarkerSet(id);

            set.setHideByDefault(true);
            set.setDefaultMarkerIcon(icon);

        });
        storage.getHotspots().values().forEach(this::addHotspot);
    }

    public void addHotspot(Hotspot hs) {
        MarkerSet set = markerApi.getMarkerSet(hs.getCategory().getId());
        if (set == null) {
            plugin.getLogger().warning("Unable to load hotspot #" + hs.getId() + " (" + hs.getName() + "). Could not find marker set.");
        } else {
            MarkerIcon icon;
            if (hs.getCustomMarker() != null) {
                icon = markerApi.getMarkerIcon(hs.getCustomMarker().endsWith(".png") ? hs.getCustomMarker().split("\\.")[0] : hs.getCustomMarker());
                if (icon == null) {
                    Bukkit.getLogger().warning("Unable to hotspot #" + hs.getId() + ". Could not find icon " + hs.getCustomMarker());
                    return;
                }
            } else icon = markerApi.getMarkerIcon(hs.getCategory().getMarker().endsWith(".png") ? hs.getCategory().getMarker().split("\\.")[0] : hs.getCategory().getMarker());
            set.createMarker(hs.getId() + "", hs.getName(), hs.getLocation().getWorld().getName(), hs.getLocation().getX(), hs.getLocation().getY(), hs.getLocation().getZ(), icon, false);
        }
    }

    public void deleteHotspot(Hotspot hs) {
        MarkerSet set = markerApi.getMarkerSet(hs.getCategory().getId());
        if (set == null) plugin.getLogger().warning("Unable to delete hotspot #" + hs.getId() + " (" + hs.getName() + "). Could not find marker set.");
        else {
            Marker marker = set.findMarker(hs.getId() + "");
            if (marker == null) plugin.getLogger().warning("Unable to delete hotspot #" + hs.getId() + " (" + hs.getName() + "). Could not find marker.");
            else marker.deleteMarker();
        }
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.save();
    }

    public HotspotStorage getStorage() {
        return storage;
    }

    public MarkerAPI getMarkerApi() {
        return markerApi;
    }

}
