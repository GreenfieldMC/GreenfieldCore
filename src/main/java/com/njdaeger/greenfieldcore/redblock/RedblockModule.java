package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import org.bukkit.Bukkit;

public class RedblockModule extends Module {

    private RedblockStorage storage;

    public RedblockModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Unable to start RedblockModule. Vault was not found.");
            return;
        }
        this.storage = new RedblockStorage(plugin);
        new RedblockCommands(this, this.storage, plugin);
    }

    @Override
    public void onDisable() {
        storage.save();
    }
}
