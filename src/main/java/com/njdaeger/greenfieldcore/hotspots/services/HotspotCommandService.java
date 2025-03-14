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
import com.njdaeger.greenfieldcore.services.IEssentialsService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotspotCommandService extends ModuleService<HotspotCommandService> implements IModuleService<HotspotCommandService> {

    private final IHotspotService hotspotService;
    private final IEssentialsService essentialsService;

    public HotspotCommandService(Plugin plugin, Module module, IHotspotService hotspotService, IEssentialsService essentialsService) {
        super(plugin, module);
        this.hotspotService = hotspotService;
        this.essentialsService = essentialsService;
    }

    // /hotspot goto "hotspot name"|hotspotId
    private void gotoHotspot(ICommandContext ctx) throws PDKCommandException {
        var hotspot = resolveHotspot(ctx);
        var player = ctx.asPlayer();
        essentialsService.setUserLastLocation(player, player.getLocation());
        player.teleport(hotspot.getLocation());
        ctx.send(HotspotMessages.TELEPORT_SUCCESS.apply(hotspot.getName()));
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

    // /hotspot delete hotspot "hotspot name"|hotspotId
    private void deleteHotspot(ICommandContext ctx) {
        var hotspot = resolveHotspot(ctx);
        hotspotService.deleteHotspot(hotspot);
        ctx.send(HotspotMessages.HOTSPOT_DELETE_SUCCESS.apply(hotspot.getName()));
    }

    // /hotspot edit hotspot "hotspot name"|hotspotId category|icon|name <new value>
    private void editHotspot(ICommandContext ctx) {
        var hotspot = resolveHotspot(ctx);

    }

    // /hotspot list hotspots <page> "category name"|categoryId
    private void listHotspots(ICommandContext ctx) {

    }

    // /hotspot create category "category name" categoryId categoryIcon
    private void createCategory(ICommandContext ctx) {
        var categoryName = ctx.getTyped("categoryName", String.class);
        var categoryId = ctx.getTyped("categoryId", String.class);
        var categoryIcon = ctx.getTyped("categoryIcon", String.class, null);
        hotspotService.createCategory(categoryName, categoryIcon, categoryId);
        ctx.send(HotspotMessages.CATEGORY_CREATE_SUCCESS.apply(categoryName));
    }

    // /hotspot delete category "category name"|categoryId "replacement category"|categoryId
    private void deleteCategory(ICommandContext ctx) throws PDKCommandException {
        var category = resolveCategory(ctx);
        var replacementCategory = resolveReplacementCategory(ctx);

        if (!hotspotService.getHotspots(hs -> hs.getCategory().equals(category)).isEmpty() && replacementCategory == null)
            ctx.error(HotspotMessages.CATEGORY_DELETE_FAIL_NO_REPLACEMENT);

        hotspotService.deleteCategory(category, replacementCategory);
        if (replacementCategory != null)
            ctx.send(HotspotMessages.CATEGORY_DELETE_SUCCESS_REPLACEMENT.apply(category.getName(), replacementCategory.getName()));
        else ctx.send(HotspotMessages.CATEGORY_DELETE_SUCCESS.apply(category.getName()));
    }

    // /hotspot edit category "category name"|categoryId name|icon <new value>
    private void editCategory(ICommandContext ctx) {

    }

    // /hotspot list categories <page>
    private void listCategories(ICommandContext ctx) {

    }

    private @NotNull Hotspot resolveHotspot(ICommandContext ctx) {
        if (ctx.hasTyped("hotspotName")) return ctx.getTyped("hotspotName", Hotspot.class);
        return ctx.getTyped("hotspotId", Hotspot.class);
    }

    private @NotNull Category resolveCategory(ICommandContext ctx) {
        if (ctx.hasTyped("categoryName")) return ctx.getTyped("categoryName", Category.class);
        return ctx.getTyped("categoryId", Category.class);
    }

    private @Nullable Category resolveReplacementCategory(ICommandContext ctx) {
        if (ctx.hasTyped("replacementCategory")) return ctx.getTyped("replacementCategory", Category.class);
        if (ctx.hasTyped("replacementCategoryId")) return ctx.getTyped("replacementCategoryId", Category.class);
        return null;
    }


    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("hotspot")
                .description("Hotspot commands")
                .permission("greenfieldcore.hotspot")
                .then("goto")
                    .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true)).executes(this::gotoHotspot)
                    .then("hotspotId", new HotspotIdArgument(hotspotService, hs -> true)).executes(this::gotoHotspot)
                .end()
                .then("create").permission("greenfieldcore.hotspot.create")
                    .then("hotspot")
                        .then("hotspotName", PdkArgumentTypes.quotedString(false, () -> "The new hotspot name"))
                            .then("categoryName", new CategoryNameArgument(hotspotService, cat -> true)).canExecute(this::createHotspot)
                                .then("customIcon", PdkArgumentTypes.string()).executes(this::createHotspot)
                            .end()
                            .then("categoryId", PdkArgumentTypes.string()).canExecute(this::createHotspot)
                                .then("customIcon", PdkArgumentTypes.string()).executes(this::createHotspot)
                            .end()
                        .end()
                    .end()
                    .then("category")
                        .then("categoryName", PdkArgumentTypes.quotedString(false, () -> "The new category name"))
                            .then("categoryId", PdkArgumentTypes.string()).canExecute(this::createCategory)
                                .then("categoryIcon", PdkArgumentTypes.string()).executes(this::createCategory)
                            .end()
                        .end()
                    .end()
                .end()
                .then("delete")
                    .then("hotspot")
                        .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true)).executes(this::deleteHotspot)
                        .then("hotspotId", new HotspotIdArgument(hotspotService, hs -> true)).executes(this::deleteHotspot)
                    .end()
                    .then("category")
                        .then("categoryName", new CategoryNameArgument(hotspotService, cat -> true)).canExecute(this::deleteCategory)
                            .then("replacementCategory", new CategoryNameArgument(hotspotService, cat -> true)).executes(this::deleteCategory)
                            .then("replacementCategoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::deleteCategory)
                        .end()
                    .end()
                .end()
                .then("edit")
                    .then("hotspot")
                        .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true))
                            .then("category")
                                .then("categoryName", new CategoryNameArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                                .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                            .end()
                            .then("icon")
                                .then("iconName", PdkArgumentTypes.string()).executes(this::editHotspot)
                            .end()
                            .then("name")
                                .then("newName", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editHotspot)
                            .end()
                        .end()
                        .then("hotspotId", new HotspotIdArgument(hotspotService, hs -> true))
                            .then("category")
                                .then("categoryName", new CategoryNameArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                                .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                            .end()
                            .then("icon")
                                .then("iconName", PdkArgumentTypes.string()).executes(this::editHotspot)
                            .end()
                            .then("name")
                                .then("newName", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editHotspot)
                            .end()
                        .end()
                    .end()
                    .then("category")
                        .then("categoryName", new CategoryNameArgument(hotspotService, cat -> true))
                            .then("name", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editCategory)
                            .then("icon", PdkArgumentTypes.string()).executes(this::editCategory)
                        .end()
                        .then("categoryId", PdkArgumentTypes.integer(() -> "The id of the category"))
                            .then("name", PdkArgumentTypes.quotedString(false, () -> "The new name")).executes(this::editCategory)
                            .then("icon", PdkArgumentTypes.string()).executes(this::editCategory)
                        .end()
                    .end()
                .end()
                .then("list")
                    .then("hotspots").canExecute(this::listHotspots)
                        .then("page", PdkArgumentTypes.integer(() -> "The page number")).canExecute(this::listHotspots)
                            .then("categoryName", new CategoryNameArgument(hotspotService, cat -> true)).executes(this::listHotspots)
                            .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::listHotspots)
                        .end()
                    .end()
                    .then("categories").canExecute(this::listCategories)
                        .then("page", PdkArgumentTypes.integer(() -> "The page number")).executes(this::listCategories)
                    .end()
                .end()
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
