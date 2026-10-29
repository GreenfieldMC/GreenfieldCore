package net.greenfieldmc.core.shared.services;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

/**
 * Throws on construction if WorldEdit is unavailable, so it is only registered when WorldEdit is present.
 */
public class WorldEditServiceImpl implements IWorldEditService {

    protected final WorldEditServiceActualImpl impl;

    public WorldEditServiceImpl() throws Exception {
        var wePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) throw new Exception("WorldEdit not found");
        try {
            this.impl = new WorldEditServiceActualImpl((WorldEditPlugin) wePlugin);
        } catch (NoClassDefFoundError e) {
            //the service provider only catches Exceptions, so rethrow as one
            throw new Exception("Failed to enable WorldEditService", e);
        }
    }

    @Override
    public void setBlock(Location location, BlockData data) {
        //had to do this weird workaround due to getting a NoClassDefFoundError from some of the worldedit api classes if they were referenced in this class.
        impl.setBlock(location, data);
    }

}
