package com.njdaeger.greenfieldcore.worldedit;

import com.njdaeger.greenfieldcore.Module;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;

public class WorldEditSafeyModule implements Module {
    @Override
    public void onEnable() {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            Bukkit.getLogger().warning("WorldEdit not found. Safety warning not enabled.");
            return;
        }
        else new WorldEditSafetyListener(worldEdit);
    }

    @Override
    public void onDisable() {

    }
}
