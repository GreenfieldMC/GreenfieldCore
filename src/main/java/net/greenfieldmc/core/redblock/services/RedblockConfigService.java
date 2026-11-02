package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.pdk.serviceprovider.Nullable;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Loads redblock-config.yml, which is in the format:
 * <pre>
 * world-project-mappings:
 *   worldName: PROJECTKEY
 * </pre>
 */
public class RedblockConfigService implements IRedblockConfigService {

    private final Map<String, String> worldToProject = new HashMap<>();
    private final Map<String, String> projectToWorld = new HashMap<>();

    public RedblockConfigService(IGreenfieldCorePlugin plugin, Logger logger) {
        try {
            IConfig config = ConfigType.YML_CONFIGURATE.createNew(plugin, "redblock-config");

            if (config.hasSection("world-project-mappings")) {
                for (var worldName : config.getSection("world-project-mappings").getKeys(false)) {
                    var projectKey = config.getString("world-project-mappings." + worldName);
                    if (projectKey == null || projectKey.isBlank()) {
                        logger.warning("World " + worldName + " in redblock-config.yml has no project key.");
                        continue;
                    }
                    if (projectToWorld.containsKey(projectKey)) {
                        logger.warning("Project " + projectKey + " is mapped to more than one world in redblock-config.yml. Ignoring world " + worldName + ".");
                        continue;
                    }
                    worldToProject.put(worldName, projectKey);
                    projectToWorld.put(projectKey, worldName);
                }
            }

            if (worldToProject.isEmpty()) {
                logger.warning("No worlds are mapped to redblock projects. Please update redblock-config.yml");
                config.setEntry("world-project-mappings.example_world", "PRJKEY");
            }
            else logger.info("World-Project mappings: " + worldToProject.entrySet().stream().map(e -> e.getKey() + " -> " + e.getValue()).collect(Collectors.joining(", ")));

            config.save();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load redblock-config.yml", e);
        }
    }

    @Override
    public @Nullable String getProjectKey(World world) {
        return worldToProject.get(world.getName());
    }

    @Override
    public @Nullable World getWorld(String projectKey) {
        var worldName = projectToWorld.get(projectKey);
        return worldName == null ? null : Bukkit.getWorld(worldName);
    }

    @Override
    public Set<String> getMappedProjectKeys() {
        return Set.copyOf(projectToWorld.keySet());
    }
}
