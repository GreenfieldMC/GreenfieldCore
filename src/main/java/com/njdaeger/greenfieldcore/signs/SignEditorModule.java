package com.njdaeger.greenfieldcore.signs;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public class SignEditorModule implements Module {

    private final GreenfieldCore plugin;

    public SignEditorModule(GreenfieldCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        new EditSignCommand(plugin);
    }

    @Override
    public void onDisable() {

    }
}
