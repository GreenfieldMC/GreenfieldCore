package net.greenfieldmc.core.shared.services;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.greenfieldmc.core.IModuleService;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public interface IWorldEditService extends IModuleService<IWorldEditService> {

    /**
     * Set a block at the given location to the given block data with no attached edit session.
     *
     * @param location the location to set the block at
     * @param data     the block data to set
     */
    void setBlock(Location location, BlockData data);

    default EditSession createEditSession(Player player) {
        return WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player));
    }

    default LocalSession getLocalSession(Player player) {
        return WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
    }

}
