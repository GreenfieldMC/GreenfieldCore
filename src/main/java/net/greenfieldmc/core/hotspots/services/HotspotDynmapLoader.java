package net.greenfieldmc.core.hotspots.services;

import net.greenfieldmc.core.shared.services.IDynmapService;
import org.dynmap.markers.MarkerIcon;
import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Loads the stored categories and hotspots into Dynmap on startup. Does nothing when Dynmap is not installed.
 */
public class HotspotDynmapLoader {

    private final Logger logger;
    private final IDynmapService dynmapService;
    private final IHotspotStorageService storageService;

    public HotspotDynmapLoader(Logger logger, @Nullable IDynmapService dynmapService, IHotspotStorageService storageService) {
        this.logger = logger;
        this.dynmapService = dynmapService;
        this.storageService = storageService;
        if (dynmapService == null) {
            logger.info("Not loading hotspots to Dynmap... no Dynmap Service is available.");
            return;
        }
        logger.info("Loading hotspots to Dynmap...");
        loadCategoriesToDynmap();
        loadHotspotsToDynmap();
    }

    private void loadCategoriesToDynmap() {
        var count = new AtomicInteger();
        storageService.getCategories().forEach(cat -> {
            var icon = dynmapService.getOrCreateMarkerIcon(cat.getMarker());
            if (icon == null) {
                logger.warning("Failed to load category " + cat.getId() + " with marker " + cat.getMarker() + ". Marker icon not found.");
                return;
            }

            var set = dynmapService.getOrCreateMarkerSet(cat.getId());
            set.setMarkerSetLabel(cat.getName());
            set.setHideByDefault(true);
            set.setDefaultMarkerIcon(icon);
            count.incrementAndGet();
        });
        logger.info("Loaded " + count.get() + " categories to Dynmap.");
    }

    private void loadHotspotsToDynmap() {
        var count = new AtomicInteger();
        storageService.getHotspots().forEach(hotspot -> {
            var set = dynmapService.getMarkerAPI().getMarkerSet(hotspot.getCategory());
            if (set == null) {
                logger.warning("Failed to load hotspot " + hotspot.getName() + " with category " + hotspot.getCategory() + ". Marker set not found.");
                return;
            }
            MarkerIcon icon;
            if (hotspot.getCustomMarker() != null) {
                icon = dynmapService.getMarkerAPI().getMarkerIcon(hotspot.getCustomMarker().endsWith(".png") ? hotspot.getCustomMarker().split("\\.")[0] : hotspot.getCustomMarker());
                if (icon == null) {
                    logger.warning("Failed to load hotspot " + hotspot.getName() + " with custom marker " + hotspot.getCustomMarker() + ". Marker icon not found.");
                    return;
                }
            } else {
                var marker = storageService.getCategory(hotspot.getCategory()).getMarker();
                icon = dynmapService.getMarkerAPI().getMarkerIcon(marker.endsWith(".png") ? marker.split("\\.")[0] : marker);
                if (icon == null) {
                    logger.warning("Failed to load hotspot " + hotspot.getName() + " with default marker " + marker + ". Marker icon not found.");
                    return;
                }
            }
            if (!dynmapService.tryCreateMarker(hotspot.getCategory(), hotspot.getId() + "",  hotspot.getName(), hotspot.getLocation(), icon.getMarkerIconID(), false)) {
                logger.warning("Failed to create marker for hotspot " + hotspot.getName() + ".");
                return;
            }
            count.incrementAndGet();
        });
        logger.info("Loaded " + count.get() + " hotspots to Dynmap.");
    }
}
