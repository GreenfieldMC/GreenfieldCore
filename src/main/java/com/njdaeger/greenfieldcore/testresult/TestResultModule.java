package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class TestResultModule extends Module {

    private Permission permission = null;
    private TestResultConfig config = null;

    public TestResultModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Could not enable TestResultModule. (Vault isn't installed)");
            return;
        }
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            this.permission = permissionProvider.getProvider();
            this.config = new TestResultConfig(plugin);
            new TestResultCommands(plugin, this);
        } else plugin.getLogger().warning("Could not enable TestResultModule, there was an issue getting the Permission system service provider.");
    }

    @Override
    public void onDisable() {
        if (config != null) config.save();
    }

    public Permission getPermissions() {
        return permission;
    }

    public TestResultConfig getConfig() {
        return config;
    }

}
