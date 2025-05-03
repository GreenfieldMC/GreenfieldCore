package net.greenfieldmc.core.hotspots.paginators;

import net.greenfieldmc.core.hotspots.Category;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CategoryPaginator extends ChatPaginatorBuilder<Category, CategoryPaginator.CategoryPaginatorMode> {


    public CategoryPaginator() {
        super();
        addComponent(Component.text("Category List", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/hslist categories " + " flags: -page 1" + getListMode(ctx),
                (ctx, res, pg) -> "/hslist categories " + " flags: -page " + (pg - 1) + getListMode(ctx),
                (ctx, res, pg) -> "/hslist categories " + " flags: -page " + (pg + 1) + getListMode(ctx),
                (ctx, res, pg) -> "/rblist categories " + " flags: -page " + ((int) Math.ceil(res.size() / 8.0)) + getListMode(ctx)
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

    private static String getListMode(CategoryPaginator.CategoryPaginatorMode mode) {
        return switch (mode) {
            case DELETE -> " -deleteMode";
            case EDIT -> " -editMode";
            default -> "";
        };
    }

    public enum CategoryPaginatorMode {
        LIST,
        DELETE,
        EDIT
    }

}
