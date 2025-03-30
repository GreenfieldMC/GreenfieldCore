package net.greenfieldmc.core.testresult.paginators;

import net.greenfieldmc.core.testresult.TestSet;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestSetPaginator extends ChatPaginatorBuilder<TestSet, ICommandContext> {

    public TestSetPaginator() {
        super();
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        addComponent(Component.text("All Conducted Tests", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/attempts all -page 1",
                (ctx, res, pg) -> "/attempts all -page " + (pg - 1),
                (ctx, res, pg) -> "/attempts all -page " + (pg + 1),
                (ctx, res, pg) -> "/attempts all -page " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
    }

}
