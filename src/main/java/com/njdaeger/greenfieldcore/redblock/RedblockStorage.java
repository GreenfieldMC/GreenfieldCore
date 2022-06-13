package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import com.njdaeger.pdk.config.ISection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.njdaeger.greenfieldcore.redblock.RedblockUtils.*;
import static org.bukkit.ChatColor.*;

public class RedblockStorage extends Configuration {

    private final Map<Integer, Redblock> redblocks;
    private int redblockIndex = 0;

    public RedblockStorage(Plugin plugin) {
        super(plugin, ConfigType.YML, "redblocks");

        this.redblocks = new HashMap<>();

        if (hasSection("redblocks")) {
            for (String stringId : getSection("redblocks").getKeys(false)) {
                int id = Integer.parseInt(stringId);
                if (id >= redblockIndex) redblockIndex = id + 1;
                ISection redblock = getSection("redblocks." + stringId);
                String content = redblock.getString("content"); //notnull
                Redblock.Status status = Redblock.Status.valueOf(redblock.getString("status")); //notnull
                UUID createdBy = UUID.fromString(redblock.getString("createdBy")); //notnull
                long createdOn = redblock.getLong("createdOn"); //notnull

                //nullable/0able fields
                UUID completedBy = redblock.getValue("completedBy") != null ? UUID.fromString(redblock.getString("completedBy")) : null; //nullable
                long completedOn = redblock.getValue("completedOn") != null ? redblock.getLong("completedOn") : 0; //nullable
                UUID approvedBy = redblock.getValue("approvedBy") != null ? UUID.fromString(redblock.getString("approvedBy")) : null; //nullable
                long approvedOn = redblock.getValue("approvedOn") != null ? redblock.getLong("approvedOn") : 0; //nullable
                UUID assignedTo = redblock.getValue("assignedTo") != null ? UUID.fromString(redblock.getString("assignedTo")) : null; //nullable
                long assignedOn = redblock.getValue("assignedOn") != null ? redblock.getLong("assignedOn") : 0; //nullable

                //location of redblock sign
                ISection locationSection = redblock.getSection("location"); //notnull
                int x = locationSection.getInt("x"); //notnull
                int y = locationSection.getInt("y"); //notnull
                int z = locationSection.getInt("z"); //notnull
                World world = plugin.getServer().getWorld(locationSection.getString("world")); //notnull
                if (world == null) plugin.getLogger().warning("Unable to load redblock " + id + " because the world " + locationSection.getString("world") + " does not exist.");
                Location location = new Location(world, x, y, z);
                String minRank = redblock.getString("minRank"); //nullable

                List<String> armorstands = redblock.getStringList("armorstands"); //notnull, we should always have armorstands showing the content of this redblock
                List<UUID> uuids = armorstands.stream().map(UUID::fromString).toList(); //notnull

                Redblock redblockObject = new Redblock(id, content, status, location, createdBy, createdOn, approvedBy, approvedOn, completedBy, completedOn, assignedTo, assignedOn, minRank, uuids);
                redblocks.put(id, redblockObject);
            }
        }
    }

    public Redblock getRedblock(int id) {
        return redblocks.get(id);
    }

    public List<Redblock> getRedblocks() {
        return redblocks.values().stream().toList();
    }

    public List<Redblock> getIncompleteRedblocks() {
        return redblocks.values().stream().filter(redblock -> redblock.getStatus() == Redblock.Status.INCOMPLETE).toList();
    }

    public List<Redblock> getPendingRedblocks() {
        return redblocks.values().stream().filter(redblock -> redblock.getStatus() == Redblock.Status.PENDING).toList();
    }

    public List<Redblock> getRedblocksFiltered(Predicate<Redblock> filter) {
        return redblocks.values().stream().filter(filter).toList();
    }

    public Redblock createRedblock(String content, Player createdBy, Location location, UUID assignedTo, String minRank) {

        //spawn the actual block
        createCube(Material.RED_WOOL, location, null, BLUE.toString() + UNDERLINE + BOLD + "[CLICK THIS]", BLUE.toString() + UNDERLINE + BOLD + "[IF COMPLETED]", null);
        var armorstands = spawnArmorstands(getContentLines(content), location, redblockIndex);

        //load the redblock into memory
        Redblock redblock = new Redblock(redblockIndex++, content, Redblock.Status.INCOMPLETE, location, createdBy.getUniqueId(), System.currentTimeMillis(), null, 0, null, 0, assignedTo, assignedTo != null ? System.currentTimeMillis() : 0, minRank, armorstands);
        redblocks.put(redblock.getId(), redblock);
        return redblock;
    }

    public void deleteRedblock(Redblock redblock) {
        redblock.setStatus(Redblock.Status.DELETED);
        redblock.setArmorstands(removeArmorstands(redblock.getArmorstands()));
        createCube(Material.AIR, redblock.getLocation());
    }

    public void editRedblock(Redblock redblock, String content, UUID assignedTo, String minRank) {
        //if the redblock content does not match the content variable, set the content of the redblock to the new content, and update the armorstands
        if (content != null && !content.isEmpty() && !redblock.getContent().equals(content)) {
            redblock.setContent(content);
            removeArmorstands(redblock.getArmorstands());
            redblock.setArmorstands(spawnArmorstands(getContentLines(content), redblock.getLocation(), redblock.getId()));
        }
        //if  assignedTo is null and the redblock is assigned to someone, unassign the redblock and set the assignedOn to 0, otherwise update the assignedOn and assignedTo
        if (assignedTo == null && redblock.getAssignedTo() != null) {
            redblock.setAssignedTo(null);
            redblock.setAssignedOn(0);
        } else if (assignedTo != null) {
            redblock.setAssignedTo(assignedTo);
            redblock.setAssignedOn(System.currentTimeMillis());
        }
        redblock.setMinRank(minRank);
    }

    public void completeRedblock(Redblock redblock, Player completedBy) {
        redblock.setStatus(Redblock.Status.PENDING);
        redblock.setCompletedBy(completedBy.getUniqueId());
        redblock.setCompletedOn(System.currentTimeMillis());
        createCube(Material.LIME_WOOL, redblock.getLocation(), GREEN + "Left click for", GREEN + "" + BOLD + "[APPROVE]", RED + "Right click for", RED + "" + BOLD + "[DENY]");
    }

    public void denyRedblock(Redblock redblock) {
        redblock.setStatus(Redblock.Status.INCOMPLETE);
        redblock.setCompletedBy(null);
        redblock.setCompletedOn(0);
        createCube(Material.RED_WOOL, redblock.getLocation(), null, BLUE.toString() + UNDERLINE + BOLD + "[CLICK THIS]", BLUE.toString() + UNDERLINE + BOLD + "[IF COMPLETED]", null);
    }

    public void approveRedblock(Redblock redblock, Player approvedBy) {
        redblock.setStatus(Redblock.Status.APPROVED);
        redblock.setApprovedBy(approvedBy.getUniqueId());
        redblock.setApprovedOn(System.currentTimeMillis());
        createCube(Material.AIR, redblock.getLocation());
        redblock.setArmorstands(removeArmorstands(redblock.getArmorstands()));
    }

    private void saveRedblock(Redblock redblock) {
        //saves a redblock into memory, not into the file yet
        var path = "redblocks." + redblock.getId();
        setEntry(path + ".content", redblock.getContent());
        setEntry(path + ".status", redblock.getStatus().name());
        setEntry(path + ".createdBy", redblock.getCreatedBy().toString());
        setEntry(path + ".createdOn", redblock.getCreatedOn());
        setEntry(path + ".completedBy", redblock.getCompletedBy() == null ? null : redblock.getCompletedBy().toString());
        setEntry(path + ".completedOn", redblock.getCompletedOn() == 0 ? null : redblock.getCompletedOn());
        setEntry(path + ".approvedBy", redblock.getApprovedBy() == null ? null : redblock.getApprovedBy().toString());
        setEntry(path + ".approvedOn", redblock.getApprovedOn() == 0 ? null : redblock.getApprovedOn());
        setEntry(path + ".assignedTo", redblock.getAssignedTo() == null ? null : redblock.getAssignedTo().toString());
        setEntry(path + ".assignedOn", redblock.getAssignedOn() == 0 ? null : redblock.getAssignedOn());
        setEntry(path + ".minRank", redblock.getMinRank());
        setEntry(path + ".location.x", redblock.getLocation().getBlockX());
        setEntry(path + ".location.y", redblock.getLocation().getBlockY());
        setEntry(path + ".location.z", redblock.getLocation().getBlockZ());
        setEntry(path + ".location.world", redblock.getLocation().getWorld().getName());
        setEntry(path + ".armorstands", redblock.getArmorstands().isEmpty() ? null : redblock.getArmorstands().stream().map(UUID::toString).toList());
    }

    @Override
    public void save() {
        //save all redblocks into the config memory
        for (Redblock redblock : redblocks.values()) {
            saveRedblock(redblock);
        }
        super.save();
    }
}
