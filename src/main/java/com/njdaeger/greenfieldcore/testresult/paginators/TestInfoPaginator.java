package com.njdaeger.greenfieldcore.testresult.paginators;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestInfoPaginator extends ChatPaginatorBuilder<PageItem<ICommandContext>, ICommandContext> {

    public TestInfoPaginator() {
        super();
        addComponent(Component.text("Test Build Rules", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        setLineWrappingMode(LineWrappingMode.FIXED_ITEMS_WRAP);
        setResultsPerPage(30);
        setEqualSignCount(25);
    }

}
