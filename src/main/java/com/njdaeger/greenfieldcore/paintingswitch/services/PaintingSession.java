package com.njdaeger.greenfieldcore.paintingswitch.services;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.entity.Painting;

class PaintingSession {

    private boolean justStarted;
    private boolean isSwitching;
    private Art lastArt;
    private Painting selected;

    PaintingSession() {
    }

    void setSwitching(boolean switching, Painting selected) {
        this.isSwitching = switching;
        this.selected = selected;
    }

    boolean isSwitching() {
        return isSwitching;
    }

    boolean hasLastArt() {
        return lastArt != null;
    }

    Art getLastArt() {
        return lastArt;
    }

    void setLastArt(Art art) {
        this.lastArt = art;
    }

    Painting getSelected() {
        return selected;
    }

    boolean justStarted() {
        return justStarted;
    }

    void setJustStarted(boolean justStarted) {
        this.justStarted = justStarted;
    }

    Location getSelectedLocation() {
        if (selected != null) return selected.getLocation();
        else return null;
    }

}
