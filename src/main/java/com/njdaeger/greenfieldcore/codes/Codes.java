package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Codes {

    private List<Code> codes;
    private final ChatPaginator<Code, ICommandContext> paginator;

    public Codes(CodesConfig config) {
        reload(config.getCodes());

        this.paginator = ChatPaginator.<Code, ICommandContext>builder()
                .setLineWrappingMode(LineWrappingMode.ELLIPSIS)
                .addComponent(Component.text("Build Codes", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER)
                .addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/codes page 1",
                        (ctx, res, pg) -> "/codes page " + (pg - 1),
                        (ctx, res, pg) -> "/codes page " + (pg + 1),
                        (ctx, res, pg) -> "/codes page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .build();
    }

    public void reload(List<String> codes) {
        var num = new AtomicInteger(1);
        this.codes = codes.stream().map(code -> new Code(num.getAndIncrement(), code)).toList();
    }

    public void sendTo(ICommandContext context, int page) {
        paginator.generatePage(context, codes, page).sendTo(Component.text("No more pages to display.", NamedTextColor.RED), context.getSender());
    }

    private record Code(int codeNumber, String code) implements PageItem<ICommandContext> {

        @Override
        public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
            var numberText = Component.text("#" + codeNumber, paginator.getHighlightColor());
            var codeText = Component.text(code, paginator.getGrayColor());
            return (TextComponent) numberText.appendSpace().append(codeText);
        }

        @Override
        public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
            return code;
        }
    }
}
