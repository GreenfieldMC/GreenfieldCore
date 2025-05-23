package net.greenfieldmc.core.shared.services;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;

public class WorldEditServiceImpl extends ModuleService<IWorldEditService> implements IWorldEditService {

    protected WorldEditServiceActualImpl impl;

    public WorldEditServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && impl != null;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            var wePlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
            if (wePlugin != null) {
                this.impl = new WorldEditServiceActualImpl((WorldEditPlugin) wePlugin);
            } else throw new Exception("Failed to enable WorldEditService");

        } catch (NoClassDefFoundError e) {
            throw new Exception("Failed to enable WorldEditService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @Override
    public void setBlock(Location location, BlockData data) {
        if (!isEnabled()) return;
        //had to do this weird workaround due to getting a NoClassDefFoundError from some of the worldedit api classes if they were referenced in this class.
        if (impl != null) impl.setBlock(location, data);
    }


}
