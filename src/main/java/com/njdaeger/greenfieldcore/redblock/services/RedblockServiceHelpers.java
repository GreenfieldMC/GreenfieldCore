package com.njdaeger.greenfieldcore.redblock.services;

import com.njdaeger.greenfieldcore.redblock.Redblock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.njdaeger.greenfieldcore.Util.resolvePlayerName;

public final class RedblockServiceHelpers {

    static String createMarkerHtml(Redblock redblock) {
        var html = new StringBuilder();
        html.append("<div style=\"display:flex; flex-direction: column; padding: .25rem; max-width: 12rem; min-width: 8rem;\">");

        if (redblock.isIncomplete()) html.append("<span><strong>Incomplete</strong></span>");
        else html.append("<span><strong>Pending</strong></span>");

        if (redblock.getAssignedTo() != null) html.append("<span>Assigned To: <strong>").append(resolvePlayerName(redblock.getAssignedTo())).append("</strong></span>");
        if (redblock.getMinRank() != null) html.append("<span>Recommended Rank: <strong>").append(redblock.getMinRank()).append("</strong></span>");
        html.append("<hr style=\"width: 100%;\"><span style=\"white-space: pre-wrap;\">").append(redblock.getContent()).append("</span>").append("</div>");
        return html.toString();

    }

    static UUID createDisplay(int id, String minRank, String assignedTo, Location location, String content) {
        var display = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        display.setGlowing(true);
        display.setLineWidth(200);
        display.setBackgroundColor(Color.fromARGB(190, 28, 28, 28));
        display.setBillboard(Display.Billboard.CENTER);
        display.setTextOpacity((byte) 0xFF);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        System.out.println(display.getDisplayHeight());

        var displayComponent = Component.text();
        displayComponent.append(Component.text("ID: ", NamedTextColor.GRAY, TextDecoration.BOLD).append(Component.text(id, NamedTextColor.BLUE)));
        if (minRank != null)
            displayComponent
                    .appendNewline()
                    .append(Component.text("Recommended Rank: ", NamedTextColor.GRAY, TextDecoration.BOLD)
                            .append(Component.text(minRank, NamedTextColor.BLUE)));

        if (assignedTo != null)
            displayComponent
                    .appendNewline()
                    .append(Component.text("Assigned To: ", NamedTextColor.GRAY, TextDecoration.BOLD)
                            .append(Component.text(assignedTo, NamedTextColor.BLUE)));

        displayComponent
                .appendNewline()
                .appendNewline()
                .append(Component.text(content));

        display.text(displayComponent.build());
        return display.getUniqueId();
    }

    static List<UUID> removeEntities(List<UUID> entityUuids) {
        entityUuids.forEach(uuid -> Objects.requireNonNull(Bukkit.getEntity(uuid)).remove());
        return new ArrayList<>();
    }

    static void createCube(Material material, Location location, TextComponent... components) {
        //create the actual cube
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    var loc = location.clone().add(x, y, z);
                    loc.getBlock().setType(material, false);
                }
            }
        }

        //apply the information to the cube
        if (material.isAir()) location.getBlock().setType(material, false);
        else {
            location.getBlock().setType(Material.OAK_SIGN);
            var sign = (Sign) location.getBlock().getState();
            for (int i = 0; i < components.length; i++) {
                var back = sign.getSide(Side.BACK);
                var front = sign.getSide(Side.FRONT);
                back.line(i, components[i] == null ? Component.empty() : components[i]);
                front.line(i, components[i] == null ? Component.empty() : components[i]);
            }
            sign.setWaxed(true);
            sign.update(true, false);
        }
    }
}
