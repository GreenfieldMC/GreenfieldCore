package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import org.bukkit.Bukkit;
import org.bukkit.map.MinecraftFont;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

import static com.njdaeger.greenfieldcore.Util.getSubstringIndex;
import static org.bukkit.ChatColor.*;

public class RedblockLineGenerator {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);

    static Text.Section getRedblockLine(Redblock redblock, CommandContext context) {
        var locationOfSender = context.getLocation();

        var createdByName = RedblockUtils.getOfflinePlayer(redblock.getCreatedBy());
        if (createdByName == null) GreenfieldCore.logger().info("Could not resolve createdBy playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCreatedBy());
        var text = Text.of("Status: ").setColor(GRAY)
                .appendRoot(redblock.getStatus().name()).setColor(BLUE)
                .appendRoot("\n")
                .appendRoot("Created by: ").setColor(GRAY)
                .appendRoot(createdByName == null ? "null" : createdByName).setColor(BLUE)
                .appendRoot("\n")
                .appendRoot("Created at: ").setColor(GRAY)
                .appendRoot(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getCreatedOn())))).setColor(BLUE)
                .appendRoot("\n");

        if (redblock.getCompletedBy() != null) {
            var completedByName = RedblockUtils.getOfflinePlayer(redblock.getCompletedBy());
            if (completedByName == null) GreenfieldCore.logger().info("Could not resolve completedBy playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCompletedBy());

            text.appendRoot("Completed by: ").setColor(GRAY)
                    .appendRoot(completedByName == null ? "null" : completedByName).setColor(BLUE)
                    .appendRoot("\n")
                    .appendRoot("Completed at: ").setColor(GRAY)
                    .appendRoot(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getCompletedOn())))).setColor(BLUE)
                    .appendRoot("\n");
        }
        if (redblock.getApprovedBy() != null) {
            var approvedByName = RedblockUtils.getOfflinePlayer(redblock.getApprovedBy());
            if (approvedByName == null) GreenfieldCore.logger().info("Could not resolve approvedBy playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCompletedBy());

            text.appendRoot("Approved by: ").setColor(GRAY)
                    .appendRoot(approvedByName == null ? "null" : approvedByName).setColor(BLUE)
                    .appendRoot("\n")
                    .appendRoot("Approved at: ").setColor(GRAY)
                    .appendRoot(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getApprovedOn())))).setColor(BLUE)
                    .appendRoot("\n");
        }
        if (redblock.getAssignedTo() != null) {
            var assignedToName = RedblockUtils.getOfflinePlayer(redblock.getAssignedTo());
            if (assignedToName == null) GreenfieldCore.logger().info("Could not resolve assignedTo playername for redblock " + redblock.getId() + ". UUID: " + redblock.getCompletedBy());

            text.appendRoot("Assigned to: ").setColor(GRAY)
                    .appendRoot(assignedToName == null ? "null" : assignedToName).setColor(BLUE)
                    .appendRoot("\n")
                    .appendRoot("Assigned at: ").setColor(GRAY)
                    .appendRoot(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getAssignedOn())))).setColor(BLUE)
                    .appendRoot("\n");
        }
        if (redblock.getMinRank() != null) {
            text.appendRoot("Minimum Rank: ").setColor(GRAY)
                    .appendRoot(redblock.getMinRank()).setColor(BLUE)
                    .appendRoot("\n");
        }

        text.appendRoot("Distance: ").setColor(GRAY)
                .appendRoot(String.format("%.2f", locationOfSender.distance(redblock.getLocation()))).setColor(BLUE)
                .appendRoot("\n");

        text.appendRoot("ID: ").setColor(GRAY)
                .appendRoot(redblock.getId() + "").setColor(BLUE);


        var root = Text.of("?")
                .setColor(
                        switch (redblock.getStatus()) {
                            case DELETED -> DARK_RED;
                            case INCOMPLETE -> RED;
                            case PENDING -> GOLD;
                            case APPROVED -> GREEN;
                        })
                .setBold(true)
                .setHoverEvent(HoverAction.SHOW_TEXT, text);

        root.appendRoot(" ");
        root.appendRoot("[T]")
                .setColor(BLUE)
                .setBold(true)
                .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/rbtp -id " + redblock.getId()))
                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Teleport to this redblock").setColor(GRAY));

        root.appendRoot(" - ").setColor(GRAY);

        if (MinecraftFont.Font.getWidth(redblock.getContent()) > 200) {
            root.appendRoot(redblock.getContent().substring(0, getSubstringIndex(200, redblock.getContent())) + "...")
                    .setBold(false)
                    .setColor(GRAY)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(redblock.getContent()).setColor(GRAY));
        } else root.appendRoot(redblock.getContent()).setColor(GRAY);

        return root;
    }

}
