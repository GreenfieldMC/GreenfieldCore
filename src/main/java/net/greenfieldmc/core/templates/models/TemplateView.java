package net.greenfieldmc.core.templates.models;

import org.bukkit.entity.BlockDisplay;

import java.util.List;

public class TemplateView {

    private final List<BlockDisplay> spawnedDisplays;

    public TemplateView(List<BlockDisplay> spawnedDisplays) {
        this.spawnedDisplays = spawnedDisplays;
    }

    public List<BlockDisplay> getSpawnedDisplays() {
        return spawnedDisplays;
    }

}
