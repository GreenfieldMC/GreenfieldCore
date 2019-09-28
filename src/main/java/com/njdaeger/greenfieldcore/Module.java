package com.njdaeger.greenfieldcore;

public abstract class Module {

    protected final GreenfieldCore plugin;

    public Module(GreenfieldCore plugin) {
        this.plugin = plugin;
    }

    public abstract void onEnable();

    public abstract void onDisable();

}
