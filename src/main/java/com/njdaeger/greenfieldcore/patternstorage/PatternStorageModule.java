package com.njdaeger.greenfieldcore.patternstorage;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PatternStorageModule extends Module {

    private final Map<UUID, List<String>> userPatterns = new HashMap<>();

    public PatternStorageModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
