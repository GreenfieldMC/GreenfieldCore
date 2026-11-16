package net.greenfieldmc.core.redblock.paginators;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.ResultListMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.redblock.services.IRedblockConfigService;
import net.greenfieldmc.core.shared.services.IUserService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class RedblockListPaginatorV2 extends ChatPaginatorBuilder<GfRedblock, Pair<ICommandContext, IUserService>> {

    public RedblockListPaginatorV2(IRedblockConfigService configService, IUserService userService) {
        super();
        addComponent(Component.text("RedBlock List", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        addComponent(new RedblockFilterComponentV2(configService, userService), ComponentPosition.TOP_RIGHT);
        addComponent(new PageNavigationComponent<>(
                (ctx, _, pg, _) -> pageFunction(ctx, pg, 1),
                (ctx, _, pg, _) -> pageFunction(ctx, pg, pg - 1),
                (ctx, _, pg, _) -> pageFunction(ctx, pg, pg + 1),
                (ctx, _, pg, total) -> pageFunction(ctx, pg, (int) Math.ceil(total / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        setResultListMode(ResultListMode.CONTAINS_PAGE_ONLY);
    }

    private static String pageFunction(Pair<ICommandContext, IUserService> context, int currentPage, int wantedPage) {
        var input = context.getFirst().getRawInput();
        if (input.contains(" ")) {
            input = input.split(" ", 2)[1];
        } else {
            input = "";
        }
        return "/rblist " + input.replace("-page " + currentPage, "") + "-page " + wantedPage;
    }

}
