package com.njdaeger.greenfieldcore.redblock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.*;

public class RedblockUtils {

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
     * @return A list of armorstand UUIDs that were created
     */
    static List<UUID> spawnArmorstands(List<String> lines, Location redblockLocation) {
        var standLocation = redblockLocation.clone();
        standLocation.add(.5, .5 + lines.size()*.25, -.5);

        //spawn an invisible armorstand entity at the location with no gravity and a custom name that is the content of the redblock
        List<UUID> armorstands = new ArrayList<>();
        for (String line : lines) {
            ArmorStand armorStand = (ArmorStand) standLocation.getWorld().spawnEntity(standLocation, EntityType.ARMOR_STAND);
            armorStand.setGravity(false);
            armorStand.setCustomName(line);
            armorStand.setCustomNameVisible(true);
            armorStand.setInvisible(true);
            armorStand.setMarker(true);
            armorstands.add(armorStand.getUniqueId());
            standLocation.add(0, -0.25, 0);
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
     * @return The redblock that is closest to the given location, or null if no redblocks are in the list
     */
    static Redblock getNearestRedblock(List<Redblock> redblocks, Location location) {
        if (redblocks.isEmpty()) return null;
        Redblock nearestRedblock = redblocks.get(0);
        double nearestDistance = Double.MAX_VALUE;
        for (Redblock redblock : redblocks) {
            double distance = redblock.getLocation().distanceSquared(location);
            if (distance < nearestDistance) {
                nearestRedblock = redblock;
                nearestDistance = distance;
            }
        }
        return nearestRedblock;
    }

}
