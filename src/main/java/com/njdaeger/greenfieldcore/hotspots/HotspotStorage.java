package com.njdaeger.greenfieldcore.hotspots;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import com.njdaeger.pdk.config.ISection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class HotspotStorage extends Configuration {

    private final Map<String, Category> categories;
    private final Map<Integer, Hotspot> hotspots;
    private int hotspotIndex = 0;

    public HotspotStorage(Plugin plugin) {
        super(plugin, ConfigType.YML, "hotspots");

        this.categories = new HashMap<>();
        this.hotspots = new HashMap<>();

        if (!hasSection("categories")) {
            addEntry("categories.apartments.name", "Apartments");
            addEntry("categories.apartments.default-marker", "apartments.png");
            addEntry("categories.houses.name", "Houses");
            addEntry("categories.houses.default-marker", "house.png");
            addEntry("categories.shopping.name", "Shopping");
            addEntry("categories.shopping.default-marker", "cart.png");
            addEntry("categories.athletics.name", "Athletics");
            addEntry("categories.athletics.default-marker", "athletics.png");
            addEntry("categories.offices.name", "Offices");
            addEntry("categories.offices.default-marker", "building.png");
            addEntry("categories.education.name", "Education");
            addEntry("categories.education.default-marker", "education.png");
            addEntry("categories.transit.name", "Public Transit");
            addEntry("categories.transit.default-marker", "subway.png");
            addEntry("categories.airports.name", "Airports");
            addEntry("categories.airports.default-marker", "plane.png");
            addEntry("categories.industry.name", "Industrial");
            addEntry("categories.industry.default-marker", "factory.png");
            addEntry("categories.utilities.name", "Utilities");
            addEntry("categories.utilities.default-marker", "wrench.png");
            addEntry("categories.construction.name", "Construction");
            addEntry("categories.construction.default-marker", "construction.png");
            addEntry("categories.hotels.name", "Hotels");
            addEntry("categories.hotels.default-marker", "bighouse.png");
            addEntry("categories.government.name", "Government");
            addEntry("categories.government.default-marker", "temple.png");
            addEntry("categories.religious.name", "Religious");
            addEntry("categories.religious.default-marker", "church.png");
            addEntry("categories.other.name", "Other");
            addEntry("categories.other.default-marker", "pin.png");
            addEntry("categories.healthcare.name", "Healthcare");
            addEntry("categories.healthcare.default-marker", "medical.png");
            addEntry("categories.entertainment.name", "Entertainment");
            addEntry("categories.entertainment.default-marker", "entertainment.png");
            addEntry("categories.publicspace.name", "Public Space");
            addEntry("categories.publicspace.default-marker", "publicspace.png");
            super.save();
        }

        for (String category : getSection("categories").getKeys(false)) {
            if (categories.containsKey(category)) {
                plugin.getLogger().warning("Unable to load category " + category + ". It is a duplicate.");
                continue;
            }
            ISection section = getSection("categories." + category);
            categories.put(category, new Category(section.getString("name"), section.getString("default-marker").replaceAll("\\.png", ""), category));
        }

        if (hasSection("hotspots")) {
            for (String name : getSection("hotspots").getKeys(false)) {
                ISection hotspot = getSection("hotspots." + name);
                int id = Integer.parseInt(name);
                if (hotspotIndex <= id) hotspotIndex = id + 1;
                Category category = getCategory(hotspot.getString("category"));
                int x = hotspot.getInt("x");
                int y = hotspot.getInt("y");
                int z = hotspot.getInt("z");
                float yaw = hotspot.getFloat("yaw");
                float pitch = hotspot.getFloat("pitch");
                World world = Bukkit.getWorld(UUID.fromString(hotspot.getString("world")));
                String niceName = hotspot.getString("name");
                String marker = hotspot.getString("custom-marker");

                if (category == null || world == null) {
                    plugin.getLogger().warning("Unable to load hotspot #" + hotspot.getName() + " (" + hotspot.getString("name") + ").");
                    continue;
                }
                hotspots.put(Integer.parseInt(hotspot.getName()), new Hotspot(niceName, category, id, x, y, z, yaw, pitch, world, marker));
            }
        }
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public Category getCategory(String name) {
        return categories.get(name);
    }

    public Map<Integer, Hotspot> getHotspots() {
        return hotspots;
    }

    public Hotspot getHotspot(int id) {
        return hotspots.get(id);
    }

    public Hotspot createHotspot(String name, Category category, Location location) {
        return createHotspot(name, category, location, null);
    }

    public Hotspot createHotspot(String name, Category category, Location location, String customMarker) {
        Hotspot hs = new Hotspot(name, category, hotspotIndex, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld(), customMarker);
        hotspots.put(hotspotIndex, hs);
        hotspotIndex++;
        return hs;
    }

    public Hotspot deleteHotspot(int id) {
        setEntry("hotspots." + id, null);
        return hotspots.remove(id);
    }

    public List<Hotspot> getHotspots(String name) {
        return hotspots.values().stream().filter(hs -> hs.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
    }

    @Override
    public void save() {
        hotspots.forEach((id, hotspot) -> {
            setEntry("hotspots." + id + ".name", hotspot.getName());
            setEntry("hotspots." + id + ".custom-marker", hotspot.getCustomMarker());
            setEntry("hotspots." + id + ".category", hotspot.getCategory().getId());
            setEntry("hotspots." + id + ".x", hotspot.getLocation().getBlockX());
            setEntry("hotspots." + id + ".y", hotspot.getLocation().getBlockY());
            setEntry("hotspots." + id + ".z", hotspot.getLocation().getBlockZ());
            setEntry("hotspots." + id + ".yaw", hotspot.getLocation().getYaw());
            setEntry("hotspots." + id + ".pitch", hotspot.getLocation().getPitch());
            setEntry("hotspots." + id + ".world", hotspot.getLocation().getWorld().getUID().toString());
        });
        super.save();
    }
}
