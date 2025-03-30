package net.greenfieldmc.core.codes;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class Code implements PageItem<ICommandContext> {

    private final String code;
    private final int codeNumber;

    public Code(String code, int codeNumber) {
        this.code = code;
        this.codeNumber = codeNumber;
    }

    public int getCodeNumber() {
        return codeNumber;
    }

    public String getCode() {
        return code;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var numberText = Component.text("#" + codeNumber, paginator.getHighlightColor());
        var codeText = Component.text(code, paginator.getGrayColor());
        return (TextComponent) numberText.appendSpace().append(codeText);
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        return getCode();
    }
}
