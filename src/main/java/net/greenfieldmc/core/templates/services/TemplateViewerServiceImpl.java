package net.greenfieldmc.core.templates.services;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemplateViewerServiceImpl extends ModuleService<ITemplateViewerService> implements ITemplateViewerService, Listener {

    private Team originTeam;
    private final Map<UUID, TemplateView> templateViewers = new HashMap<>();
    private final Display.Brightness defaultBrightness = new Display.Brightness(15, 15);

    public TemplateViewerServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.originTeam = plugin.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("template_origin");
        originTeam.color(NamedTextColor.RED);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        for (var viewer : templateViewers.values()) {
            for (var entity : viewer.getSpawnedDisplays()) {
                if (originTeam.hasEntry(entity.getUniqueId().toString())) originTeam.removeEntry(entity.getUniqueId().toString());
                entity.remove();
            }
        }
        templateViewers.clear();
        originTeam.unregister();
    }

    @Override
    public void startTemplateView(Player player, Template loadedTemplate, Location location, double scale) {
        var displays = new ArrayList<BlockDisplay>();
        var clipboard = loadedTemplate.getClipboard();
        var dimensionVector = clipboard.getDimensions();
        var originVector = clipboard.getOrigin().subtract(clipboard.getMinimumPoint());

        //adjust the location to make the location and origin point the same when full scale
        if (scale >= 1.0) location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        location = location.clone().subtract(originVector.x() * scale, originVector.y() * scale, originVector.z() * scale);

        var transformation = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f((float)scale, (float)scale, (float)scale), new Quaternionf());

        for (var x = 0; x < dimensionVector.x(); x++) {
            for (var y = 0; y < dimensionVector.y(); y++) {
                for (var z = 0; z < dimensionVector.z(); z++) {

                    if (isBlockHidden(clipboard, x, y, z)) continue;

                    var block = clipboard.getBlock(BlockVector3.at(x, y, z).add(clipboard.getMinimumPoint()));

                    if (isInvisibleBlockType(block.getBlockType())) continue;

                    var computedSpawnLocation = location.clone().add(x * scale, y * scale, z * scale);
                    var spawnLocation = new Location(computedSpawnLocation.getWorld(), computedSpawnLocation.getX(), computedSpawnLocation.getY(), computedSpawnLocation.getZ());
                    var display = spawnDisplay(spawnLocation, BukkitAdapter.adapt(block), transformation, defaultBrightness, null);
                    player.showEntity(getPlugin(), display);
                    displays.add(display);
                }
            }
        }

        var computedOrigin = location.clone().add(originVector.x() * scale, originVector.y() * scale, originVector.z() * scale);
        var originLocation = new Location(computedOrigin.getWorld(), computedOrigin.getX(), computedOrigin.getY(), computedOrigin.getZ());
        var originDisplay = spawnDisplay(originLocation, Material.RED_WOOL.createBlockData(), transformation, defaultBrightness, originTeam);
        if (scale >= 1.0) {
            originDisplay.customName(Component.text("Origin Point", NamedTextColor.RED));
            originDisplay.setCustomNameVisible(true);
        }
        player.showEntity(getPlugin(), originDisplay);
        displays.add(originDisplay);

        templateViewers.put(player.getUniqueId(), new TemplateView(displays));
    }

    @Override
    public void destroyTemplateView(Player player) {
        var viewer = templateViewers.get(player.getUniqueId());
        for (var entity : viewer.getSpawnedDisplays()) {
            if (originTeam.hasEntry(entity.getUniqueId().toString())) originTeam.removeEntry(entity.getUniqueId().toString());
            player.hideEntity(getPlugin(), entity);
            entity.remove();
        }
        templateViewers.remove(player.getUniqueId());
    }

    @Override
    public boolean isTemplateViewActive(Player player) {
        return templateViewers.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (templateViewers.containsKey(player.getUniqueId())) {
            destroyTemplateView(player);
        }
    }

    /**
     * Spawn a block display entity at the specified location with the given parameters. This display is not visible by default.
     * @param location The location to spawn the block display.
     * @param blockData The block data to set for the display.
     * @param transformation The transformation to apply to the display.
     * @param brightness The brightness settings for the display.
     * @param team The team to which the display belongs. If null, the display will not be assigned to any team.
     * @return The spawned block display entity.
     */
    private static BlockDisplay spawnDisplay(Location location, BlockData blockData, Transformation transformation, Display.Brightness brightness, @Nullable Team team) {
        var display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(blockData);
        display.setTransformation(transformation);
        display.setBrightness(brightness);
        display.setGlowing(true);
        display.setVisibleByDefault(false);
        if (team != null) team.addEntry(display.getUniqueId().toString());
        return display;
    }

    /**
     * Check if a block type is considered invisible.
     * @param blockType The block type to check.
     * @return True if the block type is invisible, false otherwise.
     */
    private static boolean isInvisibleBlockType(BlockType blockType) {
        return blockType == BlockTypes.AIR
                || blockType == BlockTypes.CAVE_AIR
                || blockType == BlockTypes.VOID_AIR
                || blockType == BlockTypes.WATER
                || blockType == BlockTypes.LAVA;
    }

    /**
     * Check if a block is hidden by other surrounding blocks in the clipboard.
     * @param clipboard The clipboard to check against.
     * @param x The x coordinate of the block.
     * @param y The y coordinate of the block.
     * @param z The z coordinate of the block.
     * @return True if the block is hidden, false otherwise.
     */
    private static boolean isBlockHidden(BlockArrayClipboard clipboard, int x, int y, int z) {
        var north = clipboard.getBlock(BlockVector3.at(x - 1, y, z).add(clipboard.getMinimumPoint())).getBlockType();
        var south = clipboard.getBlock(BlockVector3.at(x + 1, y, z).add(clipboard.getMinimumPoint())).getBlockType();
        var west = clipboard.getBlock(BlockVector3.at(x, y, z - 1).add(clipboard.getMinimumPoint())).getBlockType();
        var east = clipboard.getBlock(BlockVector3.at(x, y, z + 1).add(clipboard.getMinimumPoint())).getBlockType();
        var up = clipboard.getBlock(BlockVector3.at(x, y + 1, z).add(clipboard.getMinimumPoint())).getBlockType();
        var down = clipboard.getBlock(BlockVector3.at(x, y - 1, z).add(clipboard.getMinimumPoint())).getBlockType();
        var list = List.of(north, south, west, east, up, down);

        return list.stream().noneMatch(type -> isInvisibleBlockType(type) || !type.getMaterial().isFullCube());
    }

}
