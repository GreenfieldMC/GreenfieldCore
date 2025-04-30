package net.greenfieldmc.core.hotspots.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.hotspots.Category;
import net.greenfieldmc.core.hotspots.Hotspot;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class HotspotStorageServiceImpl extends ModuleService<IHotspotStorageService> implements IHotspotStorageService {

    private final Map<String, Category> categories = new HashMap<>();
    private final Map<Integer, Hotspot> hotspots = new HashMap<>();
    private int hotspotIdCounter = 0;

    private IConfig config;

    public HotspotStorageServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "hotspots");

            if (!config.hasSection("categories")) {
                config.addEntry("categories.apartments.name", "Apartments");
                config.addEntry("categories.apartments.default-marker", "apartments.png");
                config.addEntry("categories.houses.name", "Houses");
                config.addEntry("categories.houses.default-marker", "house.png");
                config.addEntry("categories.shopping.name", "Shopping");
                config.addEntry("categories.shopping.default-marker", "cart.png");
                config.addEntry("categories.athletics.name", "Athletics");
                config.addEntry("categories.athletics.default-marker", "athletics.png");
                config.addEntry("categories.offices.name", "Offices");
                config.addEntry("categories.offices.default-marker", "building.png");
                config.addEntry("categories.education.name", "Education");
                config.addEntry("categories.education.default-marker", "education.png");
                config.addEntry("categories.transit.name", "Public Transit");
                config.addEntry("categories.transit.default-marker", "subway.png");
                config.addEntry("categories.airports.name", "Airports");
                config.addEntry("categories.airports.default-marker", "plane.png");
                config.addEntry("categories.industry.name", "Industrial");
                config.addEntry("categories.industry.default-marker", "factory.png");
                config.addEntry("categories.utilities.name", "Utilities");
                config.addEntry("categories.utilities.default-marker", "wrench.png");
                config.addEntry("categories.construction.name", "Construction");
                config.addEntry("categories.construction.default-marker", "construction.png");
                config.addEntry("categories.hotels.name", "Hotels");
                config.addEntry("categories.hotels.default-marker", "bighouse.png");
                config.addEntry("categories.government.name", "Government");
                config.addEntry("categories.government.default-marker", "temple.png");
                config.addEntry("categories.religious.name", "Religious");
                config.addEntry("categories.religious.default-marker", "church.png");
                config.addEntry("categories.other.name", "Other");
                config.addEntry("categories.other.default-marker", "pin.png");
                config.addEntry("categories.healthcare.name", "Healthcare");
                config.addEntry("categories.healthcare.default-marker", "medical.png");
                config.addEntry("categories.entertainment.name", "Entertainment");
                config.addEntry("categories.entertainment.default-marker", "entertainment.png");
                config.addEntry("categories.publicspace.name", "Public Space");
                config.addEntry("categories.publicspace.default-marker", "publicspace.png");
                config.save();
            }

            for (var category : config.getSection("categories").getKeys(false)) {
                var section = config.getSection("categories." + category);
                categories.put(category, new Category(section.getString("name"), section.getString("default-marker"), category));
            }

            if (config.hasSection("hotspots")) {
                for (var hotspot : config.getSection("hotspots").getKeys(false)) {
                    var hsId = Integer.parseInt(hotspot);
                    var section = config.getSection("hotspots." + hotspot);
                    var category = getCategory(section.getString("category"));
                    if (hsId >= hotspotIdCounter) {
                        hotspotIdCounter = hsId + 1;
                    }
                    if (category == null) {
                        plugin.getLogger().warning("Hotspot #" + hotspot + " has an invalid category " + ". Skipping.");
                        continue;
                    }
                    hotspots.put(hsId, new Hotspot(
                            section.getString("name"),
                            category.getId(),
                            hsId,
                            section.getInt("x"),
                            section.getInt("y"),
                            section.getInt("z"),
                            (float) section.getDouble("yaw"),
                            (float) section.getDouble("pitch"),
                            plugin.getServer().getWorld(UUID.fromString(section.getString("world"))),
                            section.getString("custom-marker")
                    ));
                }
            }

        } catch (Exception e) {
            throw new Exception("Failed to enable HotspotStorageService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        saveDatabase();
    }

    @Override
    public List<Category> getCategories(Predicate<Category> filter) {
        return categories.values().stream().filter(filter).toList();
    }

    @Override
    public Category getCategory(String id) {
        return categories.get(id);
    }

    @Override
    public void saveCategory(Category category) {
        categories.put(category.getId(), category);
        config.setEntry("categories." + category.getId() + ".name", category.getName());
        config.setEntry("categories." + category.getId() + ".default-marker", category.getMarker());
    }

    @Override
    public void deleteCategory(String id) {
        var removed = categories.remove(id);
        if (removed != null) {
            config.setEntry("categories." + id, null);
        }
    }

    @Override
    public int getNextHotspotId() {
        return hotspotIdCounter;
    }

    @Override
    public List<Hotspot> getHotspots(Predicate<Hotspot> filter) {
        return hotspots.values().stream().filter(filter).toList();
    }

    @Override
    public Hotspot getHotspot(int id) {
        return hotspots.get(id);
    }

    @Override
    public void saveHotspot(Hotspot hotspot) {
        hotspots.put(hotspot.getId(), hotspot);
        if (hotspot.getId() >= hotspotIdCounter) hotspotIdCounter = hotspot.getId() + 1;
        config.setEntry("hotspots." + hotspot.getId() + ".name", hotspot.getName());
        config.setEntry("hotspots." + hotspot.getId() + ".category", hotspot.getCategory());
        config.setEntry("hotspots." + hotspot.getId() + ".x", hotspot.getLocation().getBlockX());
        config.setEntry("hotspots." + hotspot.getId() + ".y", hotspot.getLocation().getBlockY());
        config.setEntry("hotspots." + hotspot.getId() + ".z", hotspot.getLocation().getBlockZ());
        config.setEntry("hotspots." + hotspot.getId() + ".yaw", hotspot.getLocation().getYaw());
        config.setEntry("hotspots." + hotspot.getId() + ".pitch", hotspot.getLocation().getPitch());
        config.setEntry("hotspots." + hotspot.getId() + ".world", hotspot.getLocation().getWorld().getName());
        config.setEntry("hotspots." + hotspot.getId() + ".custom-marker", hotspot.getCustomMarker());
    }

    @Override
    public void deleteHotspot(int id) {
        var removed = hotspots.remove(id);
        if (removed != null) {
            config.setEntry("hotspots." + id, null);
        }
    }

    @Override
    public void saveDatabase() {
        hotspots.values().stream().filter(Hotspot::hasChanged).forEach(hs -> {
            saveHotspot(hs);
            hs.setChanged(false);
        });
        categories.values().stream().filter(Category::hasChanged).forEach(cat -> {
            saveCategory(cat);
            cat.setChanged(false);
        });
        config.save();
    }
}
