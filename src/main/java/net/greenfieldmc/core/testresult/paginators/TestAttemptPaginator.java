package net.greenfieldmc.core.testresult.paginators;

import net.greenfieldmc.core.testresult.TestAttempt;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestAttemptPaginator extends ChatPaginatorBuilder<TestAttempt, ICommandContext> {

    public TestAttemptPaginator() {
        super();
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
        addComponent(Component.text("User Test Build Attempts", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, _, pg, _) -> pageFunction(ctx, pg, 1),
                (ctx, _, pg, _) -> pageFunction(ctx, pg, pg - 1),
                (ctx, _, pg, _) -> pageFunction(ctx, pg, pg + 1),
                (ctx, _, pg, total) -> pageFunction(ctx, pg, (int) Math.ceil(total / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
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
