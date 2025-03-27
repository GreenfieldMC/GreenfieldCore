package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
import com.njdaeger.greenfieldcore.shared.services.IDynmapService;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Predicate;

public class HotspotServiceImpl extends ModuleService<IHotspotService> implements IHotspotService {

    private final IDynmapService dynmapService;
    private final IHotspotStorageService storageService;

    public HotspotServiceImpl(Plugin plugin, Module module, IDynmapService dynmapService, IHotspotStorageService storageService) {
        super(plugin, module);
        this.dynmapService = dynmapService;
        this.storageService = storageService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public Hotspot createHotspot(String name, Category category, int x, int y, int z, float yaw, float pitch, World world, String customMarker) {
        var hotspot = new Hotspot(name, category.getId(), storageService.getNextHotspotId(), x, y, z, yaw, pitch, world, customMarker);
        storageService.saveHotspot(hotspot);
        storageService.saveDatabase();
        if (dynmapService.isEnabled()) dynmapService.tryCreateMarker(category.getId(), hotspot.getId() + "", hotspot.getName(), hotspot.getLocation(), resolveMarker(hotspot), false);
        return hotspot;
    }

    @Override
    public void editHotspot(Hotspot hotspot, String name, Category category, String customMarker) {
        var oldCategory = hotspot.getCategory();
        if (name != null && !name.isBlank() && !name.equals(hotspot.getName())) {
            hotspot.setName(name);
        }
        if (category != null && !category.getId().equals(hotspot.getCategory())) {
            hotspot.setCategory(category.getId());
        }
        if (customMarker == null && hotspot.getCustomMarker() != null) {
            hotspot.setCustomMarker(null);
        } else if (customMarker != null && !customMarker.equals(hotspot.getCustomMarker())) {
            hotspot.setCustomMarker(customMarker);
        }
        storageService.saveHotspot(hotspot);
        storageService.saveDatabase();
        if (dynmapService.isEnabled()) {
            if (!dynmapService.tryDeleteMarker(oldCategory, hotspot.getId() + "")) {
                getModule().getLogger().warning("Failed to delete old marker for hotspot " + hotspot.getId());
            }
            if (!dynmapService.tryCreateMarker(hotspot.getCategory(), hotspot.getId() + "", hotspot.getName(), hotspot.getLocation(), resolveMarker(hotspot), false)) {
                getModule().getLogger().warning("Failed to create new marker for hotspot " + hotspot.getId());
            }
        }
    }

    @Override
    public void deleteHotspot(Hotspot hotspot) {
        storageService.deleteHotspot(hotspot.getId());
        storageService.saveDatabase();
        if (dynmapService.isEnabled()) {
            if (!dynmapService.tryDeleteMarker(hotspot.getCategory(), hotspot.getId() + "")) {
                getModule().getLogger().warning("Failed to delete marker for hotspot " + hotspot.getId());
            }
        }
    }

    @Override
    public Hotspot getHotspot(int id) {
        return storageService.getHotspot(id);
    }

    @Override
    public List<Hotspot> getHotspots(Predicate<Hotspot> filter) {
        return storageService.getHotspots(filter);
    }

    @Override
    public Category createCategory(String name, String marker, String id) {
        var category = new Category(name, marker, id);
        storageService.saveCategory(category);
        storageService.saveDatabase();
        if (dynmapService.isEnabled() && !dynmapService.tryCreateMarkerSet(id, name, marker)) {
            getModule().getLogger().warning("Failed to create marker for category " + name);
        }
        return category;
    }

    @Override
    public void editCategory(Category editedCategory, String name, String marker) {
        if (name != null && !name.isBlank() && !name.equals(editedCategory.getName())) {
            editedCategory.setName(name);
        }
        if (marker != null && !marker.equals(editedCategory.getMarker())) {
            editedCategory.setMarker(marker);
        }
        storageService.saveCategory(editedCategory);
        storageService.saveDatabase();
        if (dynmapService.isEnabled()) {
            var set = dynmapService.getMarkerSet(editedCategory.getId());
            if (set == null) {
                getModule().getLogger().warning("Marker set for category " + editedCategory.getName() + " does not exist. Cannot update marker.");
                return;
            }
            if (name != null) set.setMarkerSetLabel(name);
            if (marker != null) {
                var icon = dynmapService.getMarkerIcon(marker);
                if (icon == null) {
                    getModule().getLogger().warning("Marker icon " + marker + " for category " + editedCategory.getName() + " does not exist. Cannot update marker.");
                    return;
                }
                set.setDefaultMarkerIcon(icon);
            }
        }
    }

    @Override
    public void deleteCategory(Category category, Category replacement) {
        if (replacement != null) {
            var hotspots = storageService.getHotspots(hotspot -> hotspot.getCategory().equalsIgnoreCase(category.getId()));
            for (var hotspot : hotspots) {
                hotspot.setCategory(replacement.getId());
                storageService.saveHotspot(hotspot);
            }
        }
        storageService.deleteCategory(category.getId());
        storageService.saveDatabase();
        if (dynmapService.isEnabled()) {
            if (!dynmapService.tryDeleteMarkerSet(category.getId())) {
                getModule().getLogger().warning("Failed to delete marker set for category " + category.getId());
            }
        }
    }

    @Override
    public Category getCategory(String id) {
        return storageService.getCategory(id);
    }

    @Override
    public List<Category> getCategories(Predicate<Category> filter) {
        return storageService.getCategories(filter);
    }

    private String resolveMarker(Hotspot hotspot) {
        var marker = hotspot.getCustomMarker();
        if (marker != null) return marker;
        var category = getCategory(hotspot.getCategory());
        if (category != null) return category.getMarker();
        throw new IllegalStateException("Hotspot category " + hotspot.getCategory() + " does not exist. Could not resolve marker.");
    }

}
