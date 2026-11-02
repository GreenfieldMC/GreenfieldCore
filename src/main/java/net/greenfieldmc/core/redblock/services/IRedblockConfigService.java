package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.serviceprovider.Nullable;
import org.bukkit.World;

import java.util.Set;

/**
 * Configuration for the redblock module. Backed by redblock-config.yml.
 */
public interface IRedblockConfigService {

    /**
     * Get the project key mapped to the given world.
     * @param world The world
     * @return The project key, or null if the world is not mapped to a project
     */
    @Nullable String getProjectKey(World world);

    /**
     * Get the world mapped to the given project key.
     * @param projectKey The project key
     * @return The world, or null if no loaded world is mapped to the project
     */
    @Nullable World getWorld(String projectKey);

    /**
     * Get the project key of a redblock from its PROJECT-KEY.
     * @param redblockKey The PROJECT-KEY of the redblock
     * @return The project portion of the key
     */
    default String getProjectKeyOf(String redblockKey) {
        var separator = redblockKey.lastIndexOf('-');
        return separator == -1 ? redblockKey : redblockKey.substring(0, separator);
    }

    /**
     * Get all project keys that are mapped to a world.
     * @return The mapped project keys
     */
    Set<String> getMappedProjectKeys();

}
