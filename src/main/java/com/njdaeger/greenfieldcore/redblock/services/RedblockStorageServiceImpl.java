package com.njdaeger.greenfieldcore.redblock.services;

import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.redblock.Redblock;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class RedblockStorageServiceImpl extends ModuleService<IRedblockStorageService> implements IRedblockStorageService {

    private IConfig config;
    private int redblockIndex = 0;
    private final Map<Integer, Redblock> redblockMap = new HashMap<>();

    public RedblockStorageServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "redblocks");

            if (config.hasSection("redblocks")) {
                for (String stringId : config.getSection("redblocks").getKeys(false)) {
                    int id = Integer.parseInt(stringId);
                    if (id >= redblockIndex) redblockIndex = id + 1;
                    var redblock = config.getSection("redblocks." + stringId);
                    var content = redblock.getString("content"); //notnull
                    var status = Redblock.Status.valueOf(redblock.getString("status")); //notnull
                    var createdBy = UUID.fromString(redblock.getString("createdBy")); //notnull
                    var createdOn = redblock.getLong("createdOn"); //notnull

                    //nullable/0able fields
                    var completedBy = redblock.getValue("completedBy") != null ? UUID.fromString(redblock.getString("completedBy")) : null; //nullable
                    var completedOn = redblock.getValue("completedOn") != null ? redblock.getLong("completedOn") : 0; //nullable
                    var approvedBy = redblock.getValue("approvedBy") != null ? UUID.fromString(redblock.getString("approvedBy")) : null; //nullable
                    var approvedOn = redblock.getValue("approvedOn") != null ? redblock.getLong("approvedOn") : 0; //nullable
                    var assignedTo = redblock.getValue("assignedTo") != null ? UUID.fromString(redblock.getString("assignedTo")) : null; //nullable
                    var assignedOn = redblock.getValue("assignedOn") != null ? redblock.getLong("assignedOn") : 0; //nullable

                    //location of redblock sign
                    var locationSection = redblock.getSection("location"); //notnull
                    var x = locationSection.getInt("x"); //notnull
                    var y = locationSection.getInt("y"); //notnull
                    int z = locationSection.getInt("z"); //notnull
                    var world = plugin.getServer().getWorld(locationSection.getString("world")); //notnull
                    if (world == null) plugin.getLogger().warning("Unable to load redblock " + id + " because the world " + locationSection.getString("world") + " does not exist.");
                    var location = new Location(world, x, y, z);
                    var minRank = redblock.getString("minRank"); //nullable

                    var armorstands = redblock.getStringList("armorstands"); //notnull, we should always have armorstands showing the content of this redblock
                    var uuids = armorstands.stream().map(UUID::fromString).toList(); //notnull

                    Redblock redblockObject = new Redblock(id, content, status, location, createdBy, createdOn, approvedBy, approvedOn, completedBy, completedOn, assignedTo, assignedOn, minRank, uuids);
                    redblockMap.put(id, redblockObject);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to load the RedblockStorageService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        saveDatabase();
    }

    @Override
    public int getNextId() {
        return redblockIndex;
    }

    @Override
    public int getNextAndIncrementId() {
        return redblockIndex++;
    }

    @Override
    public Redblock getRedblock(int id) {
        return redblockMap.get(id);
    }

    @Override
    public List<Redblock> getRedblocks(Predicate<Redblock> filter) {
        return redblockMap.values().stream().filter(filter).toList();
    }

    @Override
    public void saveRedblock(Redblock redblock) {
        var path = "redblocks." + redblock.getId();
        config.setEntry(path + ".content", redblock.getContent());
        config.setEntry(path + ".status", redblock.getStatus().name());
        config.setEntry(path + ".createdBy", redblock.getCreatedBy().toString());
        config.setEntry(path + ".createdOn", redblock.getCreatedOn());
        config.setEntry(path + ".completedBy", redblock.getCompletedBy() == null ? null : redblock.getCompletedBy().toString());
        config.setEntry(path + ".completedOn", redblock.getCompletedOn() == 0 ? null : redblock.getCompletedOn());
        config.setEntry(path + ".approvedBy", redblock.getApprovedBy() == null ? null : redblock.getApprovedBy().toString());
        config.setEntry(path + ".approvedOn", redblock.getApprovedOn() == 0 ? null : redblock.getApprovedOn());
        config.setEntry(path + ".assignedTo", redblock.getAssignedTo() == null ? null : redblock.getAssignedTo().toString());
        config.setEntry(path + ".assignedOn", redblock.getAssignedOn() == 0 ? null : redblock.getAssignedOn());
        config.setEntry(path + ".minRank", redblock.getMinRank());
        config.setEntry(path + ".location.x", redblock.getLocation().getBlockX());
        config.setEntry(path + ".location.y", redblock.getLocation().getBlockY());
        config.setEntry(path + ".location.z", redblock.getLocation().getBlockZ());
        config.setEntry(path + ".location.world", redblock.getLocation().getWorld().getName());
        config.setEntry(path + ".armorstands", redblock.getDisplayEntityIds().isEmpty() ? null : redblock.getDisplayEntityIds().stream().map(UUID::toString).toList());
    }

    @Override
    public void saveDatabase() {
        getPlugin().getLogger().info("Saving RedBlocks to the database...");
        var count = new AtomicInteger(0);
        redblockMap.values().stream().filter(Redblock::hasChanged).forEach(rb -> {
            saveRedblock(rb);
            rb.setChanged(false);
            count.incrementAndGet();
        });
        config.save();
        getPlugin().getLogger().info("Saved " + count.get() + " RedBlocks to the database.");
    }
}
