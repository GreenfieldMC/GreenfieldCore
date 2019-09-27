package com.njdaeger.greenfieldcore.customtree;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;

public class TreePlanterModule implements Module {

    private static WorldEditPlugin worldEdit;
    private final GreenfieldCore plugin;

    public TreePlanterModule(GreenfieldCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        if(Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            new TreePlanterCommand(plugin);
        } else plugin.getLogger().warning("Tree planter module not loaded. (WorldEdit is not installed)");
    }

    @Override
    public void onDisable() {

    }

    public static WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

}
