package net.greenfieldmc.core.advancedbuild.paginators;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class InteractionPaginator extends ChatPaginatorBuilder<InteractionHandler, ICommandContext> {

    public InteractionPaginator() {
        super();
        addComponent(Component.text("Advanced Build Mode Interactions", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/avb help " + 1,
                (ctx, res, pg) -> "/avb help " + (pg - 1),
                (ctx, res, pg) -> "/avb help " + (pg + 1),
                (ctx, res, pg) -> "/avb help " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

}
