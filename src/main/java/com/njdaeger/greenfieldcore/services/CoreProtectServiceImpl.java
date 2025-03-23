package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;

public class CoreProtectServiceImpl extends ModuleService<ICoreProtectService> implements ICoreProtectService {

    private CoreProtectAPI coreApi;

    public CoreProtectServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public boolean isEnabled() {
        return coreApi != null && coreApi.isEnabled() && super.isEnabled();
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            var coreProtectPlugin = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
            if (coreProtectPlugin == null) {
                throw new Exception("CoreProtect not found");
            }
            var cp = (CoreProtect) coreProtectPlugin;
            if (!cp.getAPI().isEnabled() || cp.getAPI().APIVersion() < 6) throw new Exception("CoreProtect API is not enabled or version is too low");
            this.coreApi = cp.getAPI();
        } catch (Exception e) {
            throw new Exception("Failed to enable CoreProtectService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
    }

    @Override
    public void logPlacement(String player, Location location, Material type, BlockData blockdata) {
        if (!isEnabled()) return;
        coreApi.logPlacement(player, location, type, blockdata);
    }

    @Override
    public void logRemoval(String player, Location location, Material type, BlockData blockdata) {
        if (!isEnabled()) return;
        coreApi.logRemoval(player, location, type, blockdata);
    }
}
