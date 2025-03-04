package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CodesPaginator extends ChatPaginatorBuilder<Code, ICommandContext> {

    public CodesPaginator() {
        super();
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        addComponent(Component.text("Build Codes", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/codes page 1",
                (ctx, res, pg) -> "/codes page " + (pg - 1),
                (ctx, res, pg) -> "/codes page " + (pg + 1),
                (ctx, res, pg) -> "/codes page " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
    }

}
