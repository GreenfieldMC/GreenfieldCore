package net.greenfieldmc.core.hotspots.paginators;

import net.greenfieldmc.core.hotspots.Category;
import net.greenfieldmc.core.hotspots.Hotspot;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.LineWrappingMode;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HotspotPaginator extends ChatPaginatorBuilder<Hotspot, Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext>> {

    public HotspotPaginator() {
        super();
        addComponent(Component.text("Hotspot List", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER);
        addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT);
        addComponent(new PageNavigationComponent<>(
                (ctx, res, pg) -> "/hslist hotspots" + getCategoryName(ctx.getSecond()) + " flags: -page 1" + getListMode(ctx.getFirst()),
                (ctx, res, pg) -> "/hslist hotspots" + getCategoryName(ctx.getSecond()) + " flags: -page " + (pg - 1) + getListMode(ctx.getFirst()),
                (ctx, res, pg) -> "/hslist hotspots" + getCategoryName(ctx.getSecond()) + " flags: -page " + (pg + 1) + getListMode(ctx.getFirst()),
                (ctx, res, pg) -> "/rblist hotspots" + getCategoryName(ctx.getSecond()) + " flags: -page " + ((int) Math.ceil(res.size() / 8.0)) + getListMode(ctx.getFirst())
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

    private static String getCategoryName(ICommandContext ctx) {
        if (ctx.hasTyped("categoryId")) {
            var category = ctx.getTyped("categoryId", Category.class);
            return " byId " + category.getId();
        }
        return "";
    }

    private static String getListMode(HotspotPaginatorMode mode) {
        return switch (mode) {
            case DELETE -> " -deleteMode";
            case EDIT -> " -editMode";
            default -> "";
        };
    }

    public enum HotspotPaginatorMode {
        LIST,
        DELETE,
        EDIT
    }

}
