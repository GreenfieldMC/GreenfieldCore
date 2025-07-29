package net.greenfieldmc.core.commandstore.paginators;

import net.greenfieldmc.core.commandstore.SavedCommand;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class CommandPaginator extends ChatPaginatorBuilder<SavedCommand, ICommandContext> {

    public CommandPaginator(CommandPaginatorMode mode) {
        addComponent((ctx, paginator, results, pg) -> {
            var server = ctx.argAtOrDefault(0, "").equalsIgnoreCase("server");
            if (server) return Component.text("Server CommandStorage", paginator.getHighlightColor());
            return Component.text("User CommandStorage", paginator.getHighlightColor());
        }, ComponentPosition.TOP_CENTER);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/" + ctx.getAlias() + (ctx.argAtOrDefault(0, "").equalsIgnoreCase("server") ? " server" : "") + " flags:" + ctx.lastOrDefault("").replace("-page " + pg, "") + " -page " + 1,
                (ctx, res, pg) -> "/" + ctx.getAlias() + (ctx.argAtOrDefault(0, "").equalsIgnoreCase("server") ? " server" : "") + " flags:" + ctx.lastOrDefault("").replace("-page " + pg, "") + " -page " + (pg - 1),
                (ctx, res, pg) -> "/" + ctx.getAlias() + (ctx.argAtOrDefault(0, "").equalsIgnoreCase("server") ? " server" : "") + " flags:" + ctx.lastOrDefault("").replace("-page " + pg, "") + " -page " + (pg + 1),
                (ctx, res, pg) -> "/" + ctx.getAlias() + (ctx.argAtOrDefault(0, "").equalsIgnoreCase("server") ? " server" : "") + " flags:" + ctx.lastOrDefault("").replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        String searchButtonText;
        if (mode == CommandPaginatorMode.QUERY) {
            searchButtonText = "Search for another command.";
            addComponent((ctx, paginator, results, pg) -> Component.text("Query", paginator.getHighlightColor(), TextDecoration.UNDERLINED).hoverEvent(HoverEvent.showText(Component.text(ctx.getTyped("query", String.class), NamedTextColor.GRAY))), ComponentPosition.TOP_RIGHT);
        } else searchButtonText = "Search for a command.";
        addComponent((ctx, paginator, results, pg) -> Component.text("[☀]", paginator.getHighlightColor())
                .hoverEvent(HoverEvent.showText(Component.text(searchButtonText, NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.suggestCommand("/fcmd " + (ctx.argAtOrDefault(0, "").equalsIgnoreCase("server") ? "server " : ""))), ComponentPosition.BOTTOM_RIGHT);

    }

    public enum CommandPaginatorMode {
        LIST,
        QUERY
    }

}
