package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
import com.njdaeger.greenfieldcore.hotspots.HotspotMessages;
import com.njdaeger.greenfieldcore.hotspots.arguments.CategoryIdArgument;
import com.njdaeger.greenfieldcore.hotspots.arguments.CategoryNameArgument;
import com.njdaeger.greenfieldcore.hotspots.arguments.HotspotIdArgument;
import com.njdaeger.greenfieldcore.hotspots.arguments.HotspotNameArgument;
import com.njdaeger.greenfieldcore.hotspots.arguments.IconArgument;
import com.njdaeger.greenfieldcore.hotspots.paginators.CategoryPaginator;
import com.njdaeger.greenfieldcore.hotspots.paginators.HotspotPaginator;
import com.njdaeger.greenfieldcore.services.IDynmapService;
import com.njdaeger.greenfieldcore.services.IEssentialsService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class HotspotCommandService extends ModuleService<HotspotCommandService> implements IModuleService<HotspotCommandService> {

    private final ChatPaginator<Hotspot, Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext>> hotspotPaginator = new HotspotPaginator().build();
    private final ChatPaginator<Category, CategoryPaginator.CategoryPaginatorMode> categoryPaginator = new CategoryPaginator().build();

    private final IHotspotService hotspotService;
    private final IEssentialsService essentialsService;
    private final IDynmapService dynmapService;

    public HotspotCommandService(Plugin plugin, Module module, IDynmapService dynmapService, IHotspotService hotspotService, IEssentialsService essentialsService) {
        super(plugin, module);
        this.hotspotService = hotspotService;
        this.essentialsService = essentialsService;
        this.dynmapService = dynmapService;
    }

    // /hotspot goto "hotspot name"|hotspotId
    private void gotoHotspot(ICommandContext ctx) throws PDKCommandException {
        var hotspots = resolveHotspot(ctx);
        var player = ctx.asPlayer();
        if (hotspots.size() == 1) {
            essentialsService.setUserLastLocation(player, player.getLocation());
            player.teleport(hotspots.getFirst().getLocation());
            ctx.send(HotspotMessages.TELEPORT_SUCCESS.apply(hotspots.getFirst().getName()));
        } else {
            if (ctx.isLocatable()) {
                var location = ctx.getLocation();
                hotspots = hotspots.stream().filter(hs -> hs.getLocation().getWorld().getUID().equals(location.getWorld().getUID()))
                        .sorted(Comparator.comparingDouble(h -> h.getLocation().distanceSquared(location)))
                        .toList();
            }
            hotspotPaginator.generatePage(Pair.of(HotspotPaginator.HotspotPaginatorMode.LIST, ctx), hotspots, 1).sendTo(HotspotMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
            ctx.error(HotspotMessages.ERROR_MULTIPLE_RESULTS);
        }
    }

    // /hotspot create hotspot "hotspot name" <category> [customIcon]
    private void createHotspot(ICommandContext ctx) throws PDKCommandException {
        var hotspotName = ctx.getTyped("hotspotName", String.class);
        var category = resolveCategory(ctx);
        var customIcon = ctx.getTyped("customIcon", String.class, null);
        var location = ctx.asPlayer().getLocation();
        var hotspot = hotspotService.createHotspot(hotspotName, category, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                location.getYaw(), location.getPitch(), location.getWorld(), customIcon);
        ctx.send(HotspotMessages.HOTSPOT_CREATE_SUCCESS.apply(hotspot.getName()));
    }

    // /hsdelete hotspot "hotspot name"|hotspotId
    private void deleteHotspot(ICommandContext ctx) throws PDKCommandException {
        var hotspots = resolveHotspot(ctx);
        if (hotspots.size() == 1) {
            hotspotService.deleteHotspot(hotspots.getFirst());
            ctx.send(HotspotMessages.HOTSPOT_DELETE_SUCCESS.apply(hotspots.getFirst().getName()));
        } else {
            if (ctx.isLocatable()) {
                var location = ctx.getLocation();
                hotspots = hotspots.stream().filter(hs -> hs.getLocation().getWorld().getUID().equals(location.getWorld().getUID()))
                        .sorted(Comparator.comparingDouble(h -> h.getLocation().distanceSquared(location)))
                        .toList();
            }
            hotspotPaginator.generatePage(Pair.of(HotspotPaginator.HotspotPaginatorMode.DELETE, ctx), hotspots, 1).sendTo(HotspotMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
            ctx.error(HotspotMessages.ERROR_MULTIPLE_RESULTS);
        }

    }

    // /hsedit hotspot "hotspot name"|hotspotId category|icon|name <new value>
    private void editHotspot(ICommandContext ctx) throws PDKCommandException {
        var hotspots = resolveHotspot(ctx);
        if (hotspots.size() > 1) {
            if (ctx.isLocatable()) {
                var location = ctx.getLocation();
                hotspots = hotspots.stream().filter(hs -> hs.getLocation().getWorld().getUID().equals(location.getWorld().getUID()))
                        .sorted(Comparator.comparingDouble(h -> h.getLocation().distanceSquared(location)))
                        .toList();
            }
            hotspotPaginator.generatePage(Pair.of(HotspotPaginator.HotspotPaginatorMode.EDIT, ctx), hotspots, 1).sendTo(HotspotMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
            ctx.error(HotspotMessages.ERROR_MULTIPLE_RESULTS);
        }
        var hotspot = hotspots.getFirst();
        if (ctx.hasTyped("categoryId")) {
            var category = resolveCategory(ctx);
            if (!category.getId().equalsIgnoreCase(hotspot.getCategory())) {
                hotspotService.editHotspot(hotspot, null, category, null);
                ctx.send(HotspotMessages.HOTSPOT_EDIT_SUCCESS.apply(hotspot.getName(), "category", category.getName()));
                return;
            }
        }
        if (ctx.hasTyped("iconName")) {
            var icon = ctx.getTyped("iconName", String.class);
            if (!icon.equals(hotspot.getCustomMarker())) {
                hotspotService.editHotspot(hotspot, null, null, icon);
                ctx.send(HotspotMessages.HOTSPOT_EDIT_SUCCESS.apply(hotspot.getName(), "icon", icon));
                return;
            }
        }
        if (ctx.hasTyped("newName")) {
            var newName = ctx.getTyped("newName", String.class);
            if (!newName.equals(hotspot.getName())) {
                hotspotService.editHotspot(hotspot, newName, null, null);
                ctx.send(HotspotMessages.HOTSPOT_EDIT_SUCCESS.apply(hotspot.getName(), "name", newName));
                return;
            }
        }
        ctx.error(HotspotMessages.ERROR_EDIT_NO_CHANGE);
    }

    // /hslist hotspots categoryId -page <page>
    private void listHotspots(ICommandContext ctx) throws PDKCommandException {
        var hotspots = hotspotService.getHotspots(hs -> true);
        if (ctx.hasTyped("categoryName")) {
            var category = resolveCategory(ctx);
            hotspots = hotspots.stream().filter(hs -> hs.getCategory().equalsIgnoreCase(category.getId())).toList();
        }
        var page = ctx.getFlag("page", 1);
        var mode = ctx.hasFlag("deleteMode") ? HotspotPaginator.HotspotPaginatorMode.DELETE : HotspotPaginator.HotspotPaginatorMode.LIST;
        mode = ctx.hasFlag("editMode") ? HotspotPaginator.HotspotPaginatorMode.EDIT : mode;
        hotspotPaginator.generatePage(Pair.of(mode, ctx), hotspots, page).sendTo(HotspotMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    // /hotspot create category "category name" categoryId categoryIcon
    private void createCategory(ICommandContext ctx) {
        var categoryName = ctx.getTyped("categoryName", String.class);
        var categoryId = ctx.getTyped("categoryId", String.class);
        var categoryIcon = ctx.getTyped("categoryIcon", String.class, null);
        hotspotService.createCategory(categoryName, categoryIcon, categoryId);
        ctx.send(HotspotMessages.CATEGORY_CREATE_SUCCESS.apply(categoryName));
    }

    // /hsdelete category "category name"|categoryId "replacement category"|categoryId
    private void deleteCategory(ICommandContext ctx) throws PDKCommandException {
        var category = resolveCategory(ctx);
        var replacementCategory = resolveReplacementCategory(ctx);

        if (!hotspotService.getHotspots(hs -> hs.getCategory().equalsIgnoreCase(category.getId())).isEmpty() && replacementCategory == null)
            ctx.error(HotspotMessages.CATEGORY_DELETE_FAIL_NO_REPLACEMENT);

        hotspotService.deleteCategory(category, replacementCategory);
        if (replacementCategory != null)
            ctx.send(HotspotMessages.CATEGORY_DELETE_SUCCESS_REPLACEMENT.apply(category.getName(), replacementCategory.getName()));
        else ctx.send(HotspotMessages.CATEGORY_DELETE_SUCCESS.apply(category.getName()));
    }

    // /hsedit category "category name"|categoryId name|icon <new value>
    private void editCategory(ICommandContext ctx) {

    }

    // /hslist categories -page <page>
    private void listCategories(ICommandContext ctx) {

    }

    private @NotNull List<Hotspot> resolveHotspot(ICommandContext ctx) {
        if (ctx.hasTyped("hotspotId")) {
            var hotspot = ctx.getTyped("hotspotId", Hotspot.class);
            return List.of(hotspot);
        }
        return ctx.getTyped("hotspotName", List.class);
    }

    private @NotNull Category resolveCategory(ICommandContext ctx) {
        return ctx.getTyped("categoryId", Category.class);
    }

    private @Nullable Category resolveReplacementCategory(ICommandContext ctx) {
        if (ctx.hasTyped("replacementCategoryId")) return ctx.getTyped("replacementCategoryId", Category.class);
        return null;
    }


    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("hsgoto", "hstp", "hotspot", "hs")
                .description("Teleport to a Hotspot")
                .permission("greenfieldcore.hotspot.goto")
                .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true)).executes(this::gotoHotspot)
                .then("byId")
                    .then("hotspotId", new HotspotIdArgument(hotspotService, hs -> true)).executes(this::gotoHotspot)
                .end()
                .register(plugin);

        CommandBuilder.of("hscreate", "hsc")
                .description("Create a new Hotspot or Hotspot Category")
                .permission("greenfieldcore.hotspot.create")
                .then("hotspot")
                    .then("hotspotName", PdkArgumentTypes.quotedString(false, () -> "The new hotspot name"))
                        .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).canExecute(this::createHotspot)
                            .then("customIcon", new IconArgument(dynmapService)).executes(this::createHotspot)
                        .end()
                    .end()
                .end()
                .then("category")
                    .then("categoryName", PdkArgumentTypes.quotedString(false, () -> "The new category name"))
                        .then("categoryId", PdkArgumentTypes.string()).canExecute(this::createCategory)
                            .then("categoryIcon", new IconArgument(dynmapService)).executes(this::createCategory)
                        .end()
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("hsdelete", "hsd")
                .description("Delete a Hotspot or Hotspot Category")
                .permission("greenfieldcore.hotspot.delete")
                .then("hotspot")
                    .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true)).executes(this::deleteHotspot)
                    .then("byId")
                        .then("hotspotId", new HotspotIdArgument(hotspotService, hs -> true)).executes(this::deleteHotspot)
                    .end()
                .end()
                .then("category")
                    .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).canExecute(this::deleteCategory)
                        .then("replacementCategoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::deleteCategory)
                    .end()
                .end()
                .register(plugin);

        /*
        helptext:

        /hsedit hotspot (<hotspotName>|byId <hotspotId>) (category <categoryId>|icon <iconName>|name <newName>)
        /hsedit category (<categoryName>|byId <categoryId>) (name <newName>|icon <iconName>)
         */
        CommandBuilder.of("hsedit", "hse")
                .description("Edit a Hotspot or Hotspot Category")
                .permission("greenfieldcore.hotspot.edit")
                .then("hotspot")
                    .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true))
                        .then("category")
                            .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                        .end()
                        .then("icon")
                            .then("iconName", new IconArgument(dynmapService)).executes(this::editHotspot)
                        .end()
                        .then("name")
                            .then("newName", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editHotspot)
                        .end()
                    .end()
                    .then("byId")
                        .then("hotspotId", new HotspotIdArgument(hotspotService, hs -> true))
                            .then("category")
                                .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                            .end()
                            .then("icon")
                                .then("iconName", new IconArgument(dynmapService)).executes(this::editHotspot)
                            .end()
                            .then("name")
                                .then("newName", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editHotspot)
                            .end()
                        .end()
                    .end()
                .end()
                .then("category")
                    .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true))
                        .then("icon")
                            .then("iconName", new IconArgument(dynmapService)).executes(this::editCategory)
                        .end()
                        .then("name")
                            .then("newName", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editCategory)
                        .end()
                    .end()
                .end()
                .register(plugin);

        //helptext:
        /*
        helptext:
        /hslist hotspots [<categoryName>]|[byId <categoryId>]
        /hslist categories
         */
        CommandBuilder.of("hslist", "hsl")
                .description("List Hotspots or Hotspot Categories")
                .permission("greenfieldcore.hotspot.list")
                .flag("page", "The page number", PdkArgumentTypes.integer(1, () -> "The page number"))
                .hiddenFlag("deleteMode", "This list will show as deletable entries.")
                .hiddenFlag("editMode", "This list will show as editable entries.")
                .then("hotspots").canExecute(this::listHotspots)
                    .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::listHotspots)
                .end()
                .then("categories").executes(this::listCategories)
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
