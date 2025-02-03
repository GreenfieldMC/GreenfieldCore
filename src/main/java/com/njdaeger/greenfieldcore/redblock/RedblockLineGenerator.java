package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.map.MinecraftFont;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

import static com.njdaeger.greenfieldcore.Util.getSubstringIndex;
import static org.bukkit.ChatColor.*;

public class RedblockLineGenerator {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);

    static TextComponent getRedblockLine(Redblock redblock, CommandContext context) {
        var locationOfSender = context.getLocation();

        var createdByName = RedblockUtils.getOfflinePlayer(redblock.getCreatedBy());
        if (createdByName == null) GreenfieldCore.logger().info("Could not resolve createdBy playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCreatedBy());
        var text2 = Component.text("Status: ", NamedTextColor.GRAY).toBuilder()
                .append(Component.text(redblock.getStatus().name(), NamedTextColor.BLUE))
                .appendNewline()
                .append(Component.text("Created by: ", NamedTextColor.GRAY))
                .append(Component.text(createdByName == null ? "null" : createdByName, NamedTextColor.BLUE))
                .appendNewline()
                .append(Component.text("Created at: ", NamedTextColor.GRAY))
                .append(Component.text(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getCreatedOn()))), NamedTextColor.BLUE))
                .appendNewline();

        if (redblock.getCompletedBy() != null) {
            var completedByName = RedblockUtils.getOfflinePlayer(redblock.getCompletedBy());
            if (completedByName == null) GreenfieldCore.logger().info("Could not resolve completedBy playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCompletedBy());

            text2.append(Component.text("Completed by: ", NamedTextColor.GRAY))
                    .append(Component.text(completedByName == null ? "null" : completedByName, NamedTextColor.BLUE))
                    .appendNewline()
                    .append(Component.text("Completed at: ", NamedTextColor.GRAY))
                    .append(Component.text(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getCompletedOn()))), NamedTextColor.BLUE))
                    .appendNewline();
        }
        if (redblock.getApprovedBy() != null) {
            var approvedByName = RedblockUtils.getOfflinePlayer(redblock.getApprovedBy());
            if (approvedByName == null) GreenfieldCore.logger().info("Could not resolve approvedBy playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCompletedBy());

            text2.append(Component.text("Approved by: ", NamedTextColor.GRAY))
                    .append(Component.text(approvedByName == null ? "null" : approvedByName, NamedTextColor.BLUE))
                    .appendNewline()
                    .append(Component.text("Approved at: ", NamedTextColor.GRAY))
                    .append(Component.text(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getApprovedOn()))), NamedTextColor.BLUE))
                    .appendNewline();
        }
        if (redblock.getAssignedTo() != null) {
            var assignedToName = RedblockUtils.getOfflinePlayer(redblock.getAssignedTo());
            if (assignedToName == null) GreenfieldCore.logger().info("Could not resolve assignedTo playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCompletedBy());

            text2.append(Component.text("Assigned to: ", NamedTextColor.GRAY))
                    .append(Component.text(assignedToName == null ? "null" : assignedToName, NamedTextColor.BLUE))
                    .appendNewline()
                    .append(Component.text("Assigned at: ", NamedTextColor.GRAY))
                    .append(Component.text(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getAssignedOn()))), NamedTextColor.BLUE))
                    .appendNewline();
        }
        if (redblock.getMinRank() != null) {
            text2.append(Component.text("Minimum Rank: ", NamedTextColor.GRAY))
                    .append(Component.text(redblock.getMinRank(), NamedTextColor.BLUE))
                    .appendNewline();
        }

        text2.append(Component.text("Distance: ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.2f", locationOfSender.distance(redblock.getLocation())), NamedTextColor.BLUE))
                .appendNewline();

        text2.append(Component.text("ID: ", NamedTextColor.GRAY))
                .append(Component.text(redblock.getId() + "", NamedTextColor.BLUE));

        var lineStart = Component.text("?", switch (redblock.getStatus()) {
            case DELETED -> NamedTextColor.DARK_RED;
            case INCOMPLETE -> NamedTextColor.RED;
            case PENDING -> NamedTextColor.GOLD;
            case APPROVED -> NamedTextColor.GREEN;
        }, TextDecoration.BOLD).hoverEvent(HoverEvent.showText(text2.build())).toBuilder();

        lineStart.appendSpace();
        lineStart.append(Component.text("[T]", NamedTextColor.BLUE, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/rbtp -id " + redblock.getId()))
                .hoverEvent(HoverEvent.showText(Component.text("Teleport to this redblock", NamedTextColor.GRAY))));

        lineStart.append(Component.text(" - ", NamedTextColor.GRAY));

        if (MinecraftFont.Font.getWidth(redblock.getContent()) > 200) {
            lineStart.append(Component.text(redblock.getContent().substring(0, getSubstringIndex(200, redblock.getContent())) + "...", NamedTextColor.GRAY, TextDecoration.BOLD)
                    .hoverEvent(HoverEvent.showText(Component.text(redblock.getContent(), NamedTextColor.GRAY))));
        } else lineStart.append(Component.text(redblock.getContent(), NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(redblock.getContent(), NamedTextColor.GRAY))));

        return lineStart.build();
    }

}
