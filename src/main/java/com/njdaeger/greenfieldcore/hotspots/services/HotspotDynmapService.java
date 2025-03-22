package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.services.DynmapServiceImpl;
import org.bukkit.plugin.Plugin;
import org.dynmap.markers.MarkerIcon;

import java.util.concurrent.atomic.AtomicInteger;

public class HotspotDynmapService extends DynmapServiceImpl {

    private final IHotspotStorageService storageService;

    public HotspotDynmapService(Plugin plugin, Module module, IHotspotStorageService storageService) {
        super(plugin, module);
        this.storageService = storageService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        super.tryEnable(plugin, module);
        if (isEnabled()) {
            getModule().getLogger().info("Loading hotspots to Dynmap...");
            loadCategoriesToDynmap();
            loadHotspotsToDynmap();
        }
    }


    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        super.tryDisable(plugin, module);
    }

    private void loadCategoriesToDynmap() {
        var count = new AtomicInteger();
        storageService.getCategories().forEach(cat -> {
            var icon = getOrCreateMarkerIcon(cat.getMarker());
            if (icon == null) {
                getModule().getLogger().warning("Failed to load category " + cat.getId() + " with marker " + cat.getMarker() + ". Marker icon not found.");
                return;
            }

            var set = getOrCreateMarkerSet(cat.getId());
            set.setMarkerSetLabel(cat.getName());
            set.setHideByDefault(true);
            set.setDefaultMarkerIcon(icon);
            count.incrementAndGet();
        });
        getModule().getLogger().info("Loaded " + count.get() + " categories to Dynmap.");
    }

    private void loadHotspotsToDynmap() {
        var count = new AtomicInteger();
        storageService.getHotspots().forEach(hotspot -> {
            var set = markerApi.getMarkerSet(hotspot.getCategory());
            if (set == null) {
                getModule().getLogger().warning("Failed to load hotspot " + hotspot.getName() + " with category " + hotspot.getCategory() + ". Marker set not found.");
                return;
            }
            MarkerIcon icon;
            if (hotspot.getCustomMarker() != null) {
                icon = markerApi.getMarkerIcon(hotspot.getCustomMarker().endsWith(".png") ? hotspot.getCustomMarker().split("\\.")[0] : hotspot.getCustomMarker());
                if (icon == null) {
                    getModule().getLogger().warning("Failed to load hotspot " + hotspot.getName() + " with custom marker " + hotspot.getCustomMarker() + ". Marker icon not found.");
                    return;
                }
            } else {
                var marker = storageService.getCategory(hotspot.getCategory()).getMarker();
                icon = markerApi.getMarkerIcon(marker.endsWith(".png") ? marker.split("\\.")[0] : marker);
                if (icon == null) {
                    getModule().getLogger().warning("Failed to load hotspot " + hotspot.getName() + " with default marker " + marker + ". Marker icon not found.");
                    return;
                }
            }
            if (!tryCreateMarker(hotspot.getCategory(), hotspot.getId() + "",  hotspot.getName(), hotspot.getLocation(), icon.getMarkerIconID(), false)) {
                getModule().getLogger().warning("Failed to create marker for hotspot " + hotspot.getName() + ".");
                return;
            }
            count.incrementAndGet();
        });
        getModule().getLogger().info("Loaded " + count.get() + " hotspots to Dynmap.");
    }
}
