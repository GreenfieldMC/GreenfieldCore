package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Predicate;

public class HotspotServiceImpl extends ModuleService<IHotspotService> implements IHotspotService {

    public HotspotServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public Hotspot createHotspot(String name, Category category, int x, int y, int z, float yaw, float pitch, World world, String customMarker) {
        return null;
    }

    @Override
    public void editHotspot(Hotspot editedHotspot) {

    }

    @Override
    public void deleteHotspot(Hotspot hotspot) {

    }

    @Override
    public Hotspot getHotspot(int id) {
        return null;
    }

    @Override
    public List<Hotspot> getHotspots(Predicate<Hotspot> filter) {
        return List.of();
    }

    @Override
    public Category createCategory(String name, String marker, String id) {
        return null;
    }

    @Override
    public void editCategory(Category editedCategory) {

    }

    @Override
    public void deleteCategory(Category category, Category replacement) {

    }

    @Override
    public Category getCategory(String name) {
        return null;
    }

    @Override
    public List<Category> getCategories(Predicate<Category> filter) {
        return List.of();
    }
}
