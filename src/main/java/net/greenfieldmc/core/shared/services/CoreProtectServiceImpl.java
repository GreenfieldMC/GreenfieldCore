package net.greenfieldmc.core.shared.services;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Throws on construction if CoreProtect is unavailable, so it is only registered when CoreProtect is present.
 */
public class CoreProtectServiceImpl implements ICoreProtectService {

    private final CoreProtectAPI coreApi;

    public CoreProtectServiceImpl() throws Exception {
        var coreProtectPlugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (coreProtectPlugin == null) {
            throw new Exception("CoreProtect not found");
        }
        var cp = (CoreProtect) coreProtectPlugin;
        if (!cp.getAPI().isEnabled() || cp.getAPI().APIVersion() < 6) throw new Exception("CoreProtect API is not enabled or version is too low");
        this.coreApi = cp.getAPI();
    }

    @Override
    public boolean isEnabled() {
        return coreApi.isEnabled();
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

    @Override
    public void logContainerTransaction(String player, Location location) {
        if (!isEnabled()) return;
        coreApi.logContainerTransaction(player, location);
    }
}
