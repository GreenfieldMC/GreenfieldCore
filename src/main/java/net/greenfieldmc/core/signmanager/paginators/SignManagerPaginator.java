package net.greenfieldmc.core.signmanager.paginators;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.greenfieldmc.core.signmanager.SignManagerEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class SignManagerPaginator extends ChatPaginatorBuilder<SignManagerEntry, ICommandContext> {

    public SignManagerPaginator() {
        super();

        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        setResultsPerPage(8);

        // Title
        addComponent(Component.text("Sign Manager", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);

        // Result count
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);

        // Search query display in top right
        addComponent((ctx, paginator, results, page) -> {
            var query = ctx.getTyped("query", String.class, "");
            if (query == null || query.isBlank()) {
                return Component.text("No filter", NamedTextColor.GRAY);
            }
            return Component.text("Search: ", NamedTextColor.GRAY)
                    .append(Component.text(query, NamedTextColor.GOLD));
        }, ComponentPosition.TOP_RIGHT);

        // Page navigation
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> buildPageCommand(ctx, 1),
                (ctx, res, pg) -> buildPageCommand(ctx, pg - 1),
                (ctx, res, pg) -> buildPageCommand(ctx, pg + 1),
                (ctx, res, pg) -> buildPageCommand(ctx, (int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);

        // Search button at bottom right
        addComponent((ctx, paginator, results, pg) -> Component.text("[☀]", paginator.getHighlightColor())
                .hoverEvent(HoverEvent.showText(Component.text("Search for a sign", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.suggestCommand("/sm search "))
        , ComponentPosition.BOTTOM_RIGHT);
    }

    private static String buildPageCommand(ICommandContext ctx, int page) {
        var query = ctx.getTyped("query", String.class, "");
        if (query == null || query.isBlank()) {
            return "/sm page " + page;
        }
        return "/sm search " + query + " -page " + page;
    }
}

