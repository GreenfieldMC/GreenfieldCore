package net.greenfieldmc.core.redblock.paginators;

import net.greenfieldmc.core.redblock.Redblock;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RedblockListPaginator extends ChatPaginatorBuilder<Redblock, ICommandContext> {

    public RedblockListPaginator() {
        super();
        addComponent(Component.text("RedBlock List", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        addComponent(new RedblockFilterComponent(), ComponentPosition.TOP_RIGHT);
        addComponent(new PageNavigationComponent<>(
                (ctx, _, pg, _) -> pageFunction(ctx, pg, 1),
                (ctx, _, pg, _) -> pageFunction(ctx, pg, pg - 1),
                (ctx, _, pg, _) -> pageFunction(ctx, pg, pg + 1),
                (ctx, _, pg, total) -> pageFunction(ctx, pg, (int) Math.ceil(total / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

    private static String pageFunction(ICommandContext context, int currentPage, int wantedPage) {
        var input = context.getRawInput();
        if (input.contains(" ")) {
            input = input.split(" ", 2)[1];
        } else {
            input = "";
        }
        input = input.replace("-page " + currentPage, "").strip();
        return "/" + context.getAlias() + " " + (input.isEmpty() ? "" : input + " ") + "-page " + wantedPage;
    }

}
