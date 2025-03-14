package com.njdaeger.greenfieldcore.services;

import com.earth2me.essentials.IEssentials;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsServiceImpl extends ModuleService<IEssentialsService> implements IEssentialsService {

    private IEssentials essentials;

    public EssentialsServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        } else {
            throw new Exception("Essentials not found");
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void setUserLastLocation(Player player, Location location) {
        if (!isEnabled()) return;
        essentials.getUser(player).setLastLocation(location);
    }
}
