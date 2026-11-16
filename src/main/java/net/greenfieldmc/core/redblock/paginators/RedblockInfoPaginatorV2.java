package net.greenfieldmc.core.redblock.paginators;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.GfRedblock;
import net.greenfieldmc.core.redblock.Redblock;
import net.greenfieldmc.core.shared.services.IUserService;
import net.kyori.adventure.text.Component;

public class RedblockInfoPaginatorV2 extends ChatPaginatorBuilder<GfRedblock.GfRedblockInfo, Pair<ICommandContext, IUserService>> {

    public RedblockInfoPaginatorV2() {
        super();
        addComponent((ctx, paginator, res, pg, maxResults) -> Component.text("Redblock Information", paginator.getHighlightColor()), ComponentPosition.TOP_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        setEqualSignCount(25);
        setResultsPerPage(16);
    }

}
