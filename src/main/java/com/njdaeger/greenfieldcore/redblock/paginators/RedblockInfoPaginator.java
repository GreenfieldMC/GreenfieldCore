package com.njdaeger.greenfieldcore.redblock.paginators;

import com.njdaeger.greenfieldcore.redblock.Redblock;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import net.kyori.adventure.text.Component;

public class RedblockInfoPaginator extends ChatPaginatorBuilder<Redblock.RedblockInfo, ICommandContext> {

    public RedblockInfoPaginator() {
        super();
        addComponent((ctx, paginator, res, pg) -> Component.text("Redblock Information"), ComponentPosition.TOP_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

}
