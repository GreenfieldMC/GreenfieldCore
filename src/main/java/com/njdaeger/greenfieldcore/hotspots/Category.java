package com.njdaeger.greenfieldcore.hotspots;

public class Category {

    private boolean hasChanged = false;

    private String name;
    private String marker;
    private String id;

    public Category(String name, String marker, String id) {
        this.name = name;
        this.marker = marker;
        this.id = id;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setChanged(boolean changed) {
        this.hasChanged = changed;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
        hasChanged = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        hasChanged = true;
    }

    public String getId() {
        return id;
    }
}
