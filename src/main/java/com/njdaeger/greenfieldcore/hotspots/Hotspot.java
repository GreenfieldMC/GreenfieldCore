package com.njdaeger.greenfieldcore.hotspots;

import org.bukkit.Location;
import org.bukkit.World;

public class Hotspot {

    private String name;
    private Category category;
    private final int id;
    private int x;
    private int y;
    private int z;
    private float yaw;
    private float pitch;
    private World world;
    private String customMarker;

    public Hotspot(String name, Category category, int id, int x, int y, int z, float yaw, float pitch, World world, String customMarker) {
        this.name = name;
        this.category = category;
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.customMarker = customMarker;
    }

    public Hotspot(String name, Category category, int id, int x, int y, int z, float yaw, float pitch, World world) {
        this(name, category, id, x, y, z, yaw, pitch, world, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.world = location.getWorld();
    }

    public String getCustomMarker() {
        return customMarker;
    }

    public void setCustomMarker(String customMarker) {
        this.customMarker = customMarker;
    }
}

