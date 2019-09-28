package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class TestResultModule extends Module {

    private Permission permission = null;

    public TestResultModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
    }

    @Override
    public void onDisable() {

    }

}
