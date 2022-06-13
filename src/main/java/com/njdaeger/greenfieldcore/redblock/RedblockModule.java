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
        if (Bukkit.getPluginManager().getPlugin("Vault") == null || Bukkit.getPluginManager().getPlugin("Essentials") == null) {
            Bukkit.getLogger().warning("Unable to start RedblockModule. Vault or Essentials was not found.");
            return;
        }
        this.storage = new RedblockStorage(plugin);
        new RedblockCommands(this, this.storage, plugin);
        Bukkit.getPluginManager().registerEvents(new RedblockListener(storage), plugin);
    }

    @Override
    public void onDisable() {
        storage.save();
    }
}
