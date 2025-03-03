package com.njdaeger.greenfieldcore.redblock.paginators;

import com.njdaeger.greenfieldcore.redblock.Redblock;
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
                (ctx, res, pg) -> "/rblist page 1",
                (ctx, res, pg) -> "/rblist page " + (pg - 1),
                (ctx, res, pg) -> "/rblist page " + (pg + 1),
                (ctx, res, pg) -> "/rblist page " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

}
