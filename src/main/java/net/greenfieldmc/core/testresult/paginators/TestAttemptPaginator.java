package net.greenfieldmc.core.testresult.paginators;

import net.greenfieldmc.core.Util;
import net.greenfieldmc.core.testresult.TestAttempt;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class TestAttemptPaginator extends ChatPaginatorBuilder<TestAttempt, ICommandContext> {

    public TestAttemptPaginator() {
        super();
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        addComponent(Component.text("User Test Build Attempts", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/attempts user " + userFromContext(ctx) + " -page 1",
                (ctx, res, pg) -> "/attempts user " + userFromContext(ctx) + " -page " + (pg - 1),
                (ctx, res, pg) -> "/attempts user " + userFromContext(ctx) + " -page " + (pg + 1),
                (ctx, res, pg) -> "/attempts user " + userFromContext(ctx) + " -page " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
    }

    private String userFromContext(ICommandContext ctx) {
        return Util.resolvePlayerName(ctx.getTyped("user", UUID.class));
    }

}
