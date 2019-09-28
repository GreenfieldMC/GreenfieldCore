package com.njdaeger.greenfieldcore.signs;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;

public final class SignEditorModule extends Module {

    public SignEditorModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        new EditSignCommand(plugin);
    }

    @Override
    public void onDisable() {

    }
}
