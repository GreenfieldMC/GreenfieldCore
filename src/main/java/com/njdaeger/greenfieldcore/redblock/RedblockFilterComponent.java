package com.njdaeger.greenfieldcore.redblock;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.components.IComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.GRAY;

public class RedblockFilterComponent implements IComponent<Redblock, CommandContext> {

    @Override
    public Text.Section getText(CommandContext context, ChatPaginator<Redblock, CommandContext> paginator, List<Redblock> results, int currentPage) {

        var deleted = context.hasFlag("deleted");
        var incomplete = context.hasFlag("incomplete");
        var pending = context.hasFlag("pending");
        var approved = context.hasFlag("approved");

        UUID assignedTo = context.hasFlag("mine") ? context.asPlayer().getUniqueId() : context.getFlag("assignedTo");
        UUID createdBy = context.getFlag("createdBy");
        UUID completedBy = context.getFlag("completedBy");
        UUID approvedBy = context.getFlag("approvedBy");

        int radius = context.hasFlag("radius") ? context.getFlag("radius") : -1;

        Supplier<Text.Section> hoverText = () -> {
            var text = Text.of("Status Shown: ").setColor(GRAY);
            if (!deleted && !incomplete && !pending && !approved) text = text.append("ALL").setColor(BLUE);
            else {
                if (deleted) text = text.append("\n- DELETED").setColor(BLUE);
                if (incomplete) text = text.append("\n- INCOMPLETE").setColor(BLUE);
                if (pending) text = text.append("\n- PENDING").setColor(BLUE);
                if (approved) text = text.append("\n- APPROVED").setColor(BLUE);
            }
            if (assignedTo != null) text = text.append("\nAssigned to: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(assignedTo).getName()).setColor(BLUE);
            if (createdBy != null) text = text.append("\nCreated by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(createdBy).getName()).setColor(BLUE);
            if (completedBy != null) text = text.append("\nCompleted by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(completedBy).getName()).setColor(BLUE);
            if (approvedBy != null) text = text.append("\nApproved by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(approvedBy).getName()).setColor(BLUE);
            if (radius != -1) text = text.append("\nRadius: ").setColor(GRAY).append(String.valueOf(radius)).setColor(BLUE);
            return text;
        };
        return Text.of("Filter").setColor(ChatColor.LIGHT_PURPLE).setHoverEvent(HoverAction.SHOW_TEXT, hoverText.get());
    }
}
