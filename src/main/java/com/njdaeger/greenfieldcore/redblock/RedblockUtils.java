package com.njdaeger.greenfieldcore.redblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedblockUtils {

    public static Map<UUID, String> userNameMap = new ConcurrentHashMap<>();

    /**
     * Takes a content string and splits it up into lines that can be used to display the Redblock content with armorstands.
     * @param content The content string to split into lines
     * @return A list of lines that can be used to display the Redblock content with armorstands
     */
    static List<String> getContentLines(String content) {
        List<String> lineList = new ArrayList<>();

        //split the content string at every space and if a word is longer than 48 characters, split it into multiple strings
        List<String> words = Arrays.stream(content.split(" "))
                .map(s -> s.length() > 48 ? Arrays.stream(s.split("(?<=\\G.{48})")).map(String::trim).toArray(String[]::new) : new String[]{s.trim()})
                .flatMap(Arrays::stream)
                .toList();
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            //if a word is 48 characters long, it will only ever take up a single line and the rest of its text will be on the consecutive lines
            //or if the current line length + the current word length is greater than 48, the current word will be added to the next line
            if (word.length() == 48 || currentLine.length() + word.length() > 48) {
                lineList.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
            //if the current word ends with a \n, the words following it will be added to the next line
            currentLine.append(word.endsWith("\\n") ? word.replace("\\n", "") : word).append(" ");
            if (word.endsWith("\\n")) {
                lineList.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
        }
        lineList.add(currentLine.toString().trim());
        return lineList;
    }

    /**
     * Creates armorstands for the given location and content
     * @param lines The lines of content to display
     * @param redblockLocation The location the redblock is located at
Fi     * @param id The id of the redblock
     * @param minRank The minimum recommended rank for this redblock
     * @param assignedTo The player this redblock is assigned to
     * @return A list of armorstand UUIDs that were created
     */
    static List<UUID> spawnArmorstands(List<String> lines, Location redblockLocation, int id, String minRank, String assignedTo) {
        var textLocation = redblockLocation.clone();
        textLocation.add(.5, .75 + lines.size()*.25, .5);
        List<UUID> armorstands = new ArrayList<>();

        var restrictionLocation = textLocation.clone().add(0, .35, 0);

        if (assignedTo != null) {
            var assignStand = (ArmorStand) textLocation.getWorld().spawnEntity(restrictionLocation, EntityType.ARMOR_STAND);
            assignStand.setGravity(false);
            assignStand.setCustomName(ChatColor.GRAY + "" + ChatColor.BOLD + "Assigned To: " + ChatColor.BLUE + assignedTo);
            assignStand.setCustomNameVisible(true);
            assignStand.setInvisible(true);
            assignStand.setMarker(true);
            armorstands.add(assignStand.getUniqueId());
            restrictionLocation.add(0, .25, 0);
        }

        if (minRank != null) {
            var rankStand = (ArmorStand) redblockLocation.getWorld().spawnEntity(restrictionLocation, EntityType.ARMOR_STAND);
            rankStand.setGravity(false);
            rankStand.setCustomName(ChatColor.GRAY + "" + ChatColor.BOLD + "Recommended Rank: " + ChatColor.BLUE + minRank);
            rankStand.setCustomNameVisible(true);
            rankStand.setInvisible(true);
            rankStand.setMarker(true);
            armorstands.add(rankStand.getUniqueId());
        }

        //create one armorstand above the redblockLocation and set the custom name to the ID of the redblock
        var idStand = (ArmorStand) redblockLocation.getWorld().spawnEntity(redblockLocation.clone().add(.5, 0, .5), EntityType.ARMOR_STAND);
        idStand.setGravity(false);
        idStand.setCustomName(ChatColor.GRAY + "" + ChatColor.BOLD + "ID: " + ChatColor.BLUE + id);
        idStand.setCustomNameVisible(true);
        idStand.setInvisible(true);
        idStand.setMarker(true);
        armorstands.add(idStand.getUniqueId());


        //spawn an invisible armorstand entity at the location with no gravity and a custom name that is the content of the redblock
        for (String line : lines) {
            var armorStand = (ArmorStand) textLocation.getWorld().spawnEntity(textLocation, EntityType.ARMOR_STAND);
            armorStand.setGravity(false);
            armorStand.setCustomName(line);
            armorStand.setCustomNameVisible(true);
            armorStand.setInvisible(true);
            armorStand.setMarker(true);
            armorstands.add(armorStand.getUniqueId());
            textLocation.add(0, -0.25, 0);
        }
        return armorstands;
    }

    /**
     * Takes a list of armorstands and removes them from the world.
     * @param armorstands A list of armorstands to remove
     * @return An empty list
     */
    static List<UUID> removeArmorstands(List<UUID> armorstands) {
        armorstands.forEach(armorstandId -> Objects.requireNonNull(Bukkit.getEntity(armorstandId)).remove());
        return new ArrayList<>();
    }

    /**
     * Creates a cube with a sign on top at the given redblock location.
     * @param material The material of the cube
     * @param redblockLocation The location of the redblock
     * @param signLines The lines of text to display on the sign
     * NOTE: If the signLines is empty, or if the material is air, the redblock location will be set to air.
     */
    static void createCube(Material material, Location redblockLocation, String... signLines) {
        if (signLines.length > 4) throw new RuntimeException("A sign does not have more than 4 lines.");
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -3; y <= -1; y++) {
                    Location loc = redblockLocation.clone().add(x, y, z);
                    loc.getBlock().setType(material, false);
                }
            }
        }

        if (material == Material.AIR || signLines.length == 0) redblockLocation.getBlock().setType(Material.AIR, false);
        else {
            redblockLocation.getBlock().setType(Material.OAK_SIGN);
            Sign sign = (Sign) redblockLocation.getBlock().getState();
            for (int i = 0; i < signLines.length; i++) {
                sign.setLine(i, signLines[i] == null ? "" : signLines[i]);
            }
            sign.update(true, false);
        }
    }

    /**'
     * Search through the list of redblocks and return the one that is closest to the given location.
     * @param redblocks The list of redblocks to search through
     * @param location The location to search around
     * @param searchRadius The radius of the search
     * @return The redblock that is closest to the given location, or null if no redblocks are in the list
     */
    static Redblock getNearestRedblock(List<Redblock> redblocks, Location location, int searchRadius) {
        if (redblocks.isEmpty()) return null;
        Redblock nearestRedblock = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Redblock redblock : redblocks) {
            double distance = redblock.getLocation().distance(location);
            if (distance < nearestDistance && (searchRadius == -1 || distance <= searchRadius)) {
                nearestRedblock = redblock;
                nearestDistance = distance;
            }
        }
        return nearestRedblock;
    }

    static String getOfflinePlayer(UUID uuid) {
        if (userNameMap.containsKey(uuid)) return userNameMap.get(uuid);
        else return Bukkit.getOfflinePlayer(uuid).getName();
    }

}
