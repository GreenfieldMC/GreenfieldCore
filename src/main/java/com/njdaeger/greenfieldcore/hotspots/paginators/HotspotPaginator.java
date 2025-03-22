package com.njdaeger.greenfieldcore.hotspots.paginators;

import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
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
                (ctx, res, pg) -> "/hslist hotspots" + getCategoryName(ctx.getSecond()) + getListMode(ctx.getFirst()) + " -page 1",
                (ctx, res, pg) -> "/hslist hotspots" + getCategoryName(ctx.getSecond()) + getListMode(ctx.getFirst()) + " -page " + (pg - 1),
                (ctx, res, pg) -> "/hslist hotspots" + getCategoryName(ctx.getSecond()) + getListMode(ctx.getFirst()) + " -page " + (pg + 1),
                (ctx, res, pg) -> "/rblist hotspots" + getCategoryName(ctx.getSecond()) + getListMode(ctx.getFirst()) + " -page " + ((int) Math.ceil(res.size() / 8.0))
        ), ComponentPosition.BOTTOM_CENTER);
        setLineWrappingMode(LineWrappingMode.ELLIPSIS);
    }

    private static String getCategoryName(ICommandContext ctx) {
        if (ctx.hasTyped("categoryId")) {
            var category = ctx.getTyped("categoryId", Category.class);
            return " byId " + category.getId();
        } else if (ctx.hasTyped("categoryName")) {
            var category = ctx.getTyped("categoryName", Category.class);
            return " " + category.getName();
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
