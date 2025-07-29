package net.greenfieldmc.core.redblock.paginators;

import net.greenfieldmc.core.redblock.Redblock;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.components.IComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class RedblockFilterComponent implements IComponent<Redblock, ICommandContext> {

    @Override
    public TextComponent getText(ICommandContext context, ChatPaginator<Redblock, ICommandContext> paginator, List<Redblock> results, int currentPage) {

        var deleted = context.hasFlag("deleted");
        var incomplete = context.hasFlag("incomplete");
        var pending = context.hasFlag("pending");
        var approved = context.hasFlag("approved");
        var unassigned = context.hasFlag("unassigned");

        UUID assignedTo;
        if (context.isPlayer() && context.hasFlag("mine")) {
            try {
                assignedTo = context.asPlayer().getUniqueId();
            } catch (PDKCommandException e) {
                throw new RuntimeException(e);
            }
        } else if (context.hasFlag("assignedTo")) {
            assignedTo = context.getFlag("assignedTo");
        } else assignedTo = null;

        UUID createdBy = context.getFlag("createdBy");
        UUID completedBy = context.getFlag("completedBy");
        UUID approvedBy = context.getFlag("approvedBy");

        @SuppressWarnings("DataFlowIssue")
        int radius = context.getFlag("radius", -1);

        Supplier<TextComponent> hoverText = () -> {
            var text = Component.text("Status Shown: ", NamedTextColor.GRAY).toBuilder();
            if (!deleted && !incomplete && !pending && !approved) text.append(Component.text("ALL", NamedTextColor.BLUE));
            else {
                if (deleted) text.appendNewline().append(Component.text("- DELETED", NamedTextColor.BLUE));
                if (incomplete) text.appendNewline().append(Component.text("- INCOMPLETE", NamedTextColor.BLUE));
                if (pending) text.appendNewline().append(Component.text("- PENDING", NamedTextColor.BLUE));
                if (approved) text.appendNewline().append(Component.text("- APPROVED", NamedTextColor.BLUE));
                if (unassigned) text.appendNewline().append(Component.text("- UNASSIGNED", NamedTextColor.BLUE));
            }
            if (assignedTo != null) text.appendNewline().append(Component.text("Assigned to: ", NamedTextColor.GRAY)).append(Component.text(Bukkit.getOfflinePlayer(assignedTo).getName(), NamedTextColor.BLUE));
            if (createdBy != null) text.appendNewline().append(Component.text("Created by: ", NamedTextColor.GRAY)).append(Component.text(Bukkit.getOfflinePlayer(createdBy).getName(), NamedTextColor.BLUE));
            if (completedBy != null) text.appendNewline().append(Component.text("Completed by: ", NamedTextColor.GRAY)).append(Component.text(Bukkit.getOfflinePlayer(completedBy).getName(), NamedTextColor.BLUE));
            if (approvedBy != null) text.appendNewline().append(Component.text("Approved by: ", NamedTextColor.GRAY)).append(Component.text(Bukkit.getOfflinePlayer(approvedBy).getName(), NamedTextColor.BLUE));
            if (radius != -1) text.appendNewline().append(Component.text("Radius: ", NamedTextColor.GRAY)).append(Component.text(String.valueOf(radius), NamedTextColor.BLUE));
            return text.build();
        };
        return Component.text("Filter", NamedTextColor.LIGHT_PURPLE).hoverEvent(HoverEvent.showText(hoverText.get()));
    }
}
