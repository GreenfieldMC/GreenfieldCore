package com.njdaeger.greenfieldcore.hotspots;

public class Category {

    private final String name;
    private final String marker;
    private final String id;

    public Category(String name, String marker, String id) {
        this.name = name;
        this.marker = marker;
        this.id = id;
    }

    public String getMarker() {
        return marker;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
