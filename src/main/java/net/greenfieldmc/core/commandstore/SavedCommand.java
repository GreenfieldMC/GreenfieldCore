package net.greenfieldmc.core.commandstore;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SavedCommand implements PageItem<ICommandContext> {

    private boolean hasChanged = false;

    private String command;
    private String description;
    private int used;
    private final int id;

    public SavedCommand(String command, String description, int used, int id) {
        this.command = command;
        this.description = description;
        this.used = used;
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
        this.hasChanged = true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.hasChanged = true;
    }

    public int getUsed() {
        return used;
    }

    public void incrementUsage() {
        used++;
        this.hasChanged = true;
    }

    public int getId() {
        return id;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setChanged(boolean changed) {
        this.hasChanged = changed;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext ctx) {
        var server = ctx.argAtOrDefault(0, "");
        var isServer = server.equalsIgnoreCase("server");

        var questionMark = Component.text("?", paginator.getHighlightColor(), TextDecoration.BOLD).toBuilder();
        var hoverText = Component.text();
        hoverText.append(Component.text("Command: ", paginator.getGrayColor()).append(Component.text(command, NamedTextColor.BLUE)));
        hoverText.append(Component.newline());
        hoverText.append(Component.text("Used: ", paginator.getGrayColor()).append(Component.text(used, NamedTextColor.BLUE)));
        hoverText.append(Component.newline());
        hoverText.append(Component.text("ID: ", paginator.getGrayColor()).append(Component.text(id, NamedTextColor.BLUE)));
        questionMark.hoverEvent(hoverText.build());

        var line = Component.text();
        line.append(questionMark.build());
        line.resetStyle().appendSpace();
        line.append(Component.text("[", NamedTextColor.GRAY, TextDecoration.BOLD));

        if (!isServer || ctx.hasPermission("greenfieldcore.commandstore.remove.server")) {
            line.append(Component.text("X", NamedTextColor.RED, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/rcmd " + server + " " + id))
                    .hoverEvent(Component.text("Click to remove this command", NamedTextColor.GRAY)));
            line.resetStyle().appendSpace();
        }

        if (!isServer || ctx.hasPermission("greenfieldcore.commandstore.edit.server")) {
            line.append(Component.text("E", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.suggestCommand("/ecmd " + server + " " + id + " "))
                    .hoverEvent(Component.text("Click to edit this command", NamedTextColor.GRAY)));
            line.resetStyle().appendSpace();
        }

        if (!isServer || ctx.hasPermission("greenfieldcore.commandstore.copy.server")) {
            line.append(Component.text("C", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.copyToClipboard(command))
                    .hoverEvent(Component.text("Click to copy this command", NamedTextColor.GRAY)));
            line.resetStyle().appendSpace();
        }

        if (!isServer || ctx.hasPermission("greenfieldcore.commandstore.run.server")) {
            line.append(Component.text("R", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/wcmd " + server + " " + id))
                    .hoverEvent(Component.text("Click to run this command", NamedTextColor.GRAY)));
            line.resetStyle();
        }

        line.append(Component.text("] - ", NamedTextColor.GRAY, TextDecoration.BOLD));
        line.resetStyle();
        line.append(Component.text(description, paginator.getHighlightColor()));
        return line.build();
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        return description;
    }
}
