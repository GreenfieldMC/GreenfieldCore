package net.greenfieldmc.core.hotspots.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.hotspots.Category;
import net.greenfieldmc.core.hotspots.Hotspot;
import net.greenfieldmc.core.hotspots.HotspotMessages;
import net.greenfieldmc.core.hotspots.arguments.CategoryIdArgument;
import net.greenfieldmc.core.hotspots.arguments.HotspotIdArgument;
import net.greenfieldmc.core.hotspots.arguments.HotspotNameArgument;
import net.greenfieldmc.core.hotspots.arguments.IconArgument;
import net.greenfieldmc.core.hotspots.paginators.CategoryPaginator;
import net.greenfieldmc.core.hotspots.paginators.HotspotPaginator;
import net.greenfieldmc.core.shared.services.IDynmapService;
import net.greenfieldmc.core.shared.services.IEssentialsService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("DataFlowIssue")
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

    private void createHotspot(ICommandContext ctx) throws PDKCommandException {
        var hotspotName = ctx.getTyped("hotspotName", String.class);
        var category = resolveCategory(ctx);
        var customIcon = ctx.getTyped("customIcon", String.class, null);
        var location = ctx.asPlayer().getLocation();
        var hotspot = hotspotService.createHotspot(hotspotName, category, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                location.getYaw(), location.getPitch(), location.getWorld(), customIcon);
        ctx.send(HotspotMessages.HOTSPOT_CREATE_SUCCESS.apply(hotspot.getName()));
    }

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
        if (ctx.last().equalsIgnoreCase("icon") || ctx.hasTyped("iconName")) {
            String icon = null;
            if (ctx.hasTyped("iconName")) icon = ctx.getTyped("iconName", String.class);
            if ((hotspot.getCustomMarker() == null && icon != null) || (hotspot.getCustomMarker() != null && icon == null)) {
                hotspotService.editHotspot(hotspot, null, null, icon);
                ctx.send(HotspotMessages.HOTSPOT_EDIT_SUCCESS.apply(hotspot.getName(), "icon", icon == null ? "none" : icon));
                return;
            }
        }
        if (ctx.hasTyped("newName")) {
            var newName = ctx.getTyped("newName", String.class);
            if (!newName.equals(hotspot.getName())) {
                ctx.send(HotspotMessages.HOTSPOT_EDIT_SUCCESS.apply(hotspot.getName(), "name", newName));
                hotspotService.editHotspot(hotspot, newName, null, null);
                return;
            }
        }
        ctx.error(HotspotMessages.ERROR_EDIT_NO_CHANGE);
    }

    private void listHotspots(ICommandContext ctx) throws CommandSenderTypeException {
        var hotspots = hotspotService.getHotspots(hs -> true);
        if (ctx.hasTyped("categoryName")) {
            var category = resolveCategory(ctx);
            hotspots = hotspots.stream().filter(hs -> hs.getCategory().equalsIgnoreCase(category.getId())).toList();
        }
        if (ctx.isLocatable()) {
            var location = ctx.getLocation();
            hotspots = hotspots.stream().filter(hs -> hs.getLocation().getWorld().getUID().equals(location.getWorld().getUID()))
                    .sorted(Comparator.comparingDouble(h -> h.getLocation().distanceSquared(location)))
                    .toList();
        }
        var page = ctx.getFlag("page", 1);
        var mode = ctx.hasFlag("deleteMode") ? HotspotPaginator.HotspotPaginatorMode.DELETE : HotspotPaginator.HotspotPaginatorMode.LIST;
        mode = ctx.hasFlag("editMode") ? HotspotPaginator.HotspotPaginatorMode.EDIT : mode;
        hotspotPaginator.generatePage(Pair.of(mode, ctx), hotspots, page).sendTo(HotspotMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void createCategory(ICommandContext ctx) {
        var categoryName = ctx.getTyped("categoryName", String.class);
        var categoryId = ctx.getTyped("categoryId", String.class);
        var categoryIcon = ctx.getTyped("categoryIcon", String.class, null);
        hotspotService.createCategory(categoryName, categoryIcon, categoryId);
        ctx.send(HotspotMessages.CATEGORY_CREATE_SUCCESS.apply(categoryName));
    }

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

    private void editCategory(ICommandContext ctx) throws PDKCommandException {
        var category = resolveCategory(ctx);
        if (ctx.hasTyped("iconName")) {
            var icon = ctx.getTyped("iconName", String.class);
            if (!icon.equals(category.getMarker())) {
                hotspotService.editCategory(category, null, icon);
                ctx.send(HotspotMessages.CATEGORY_EDIT_SUCCESS.apply(category.getName(), "icon", icon));
                return;
            }
        }
        if (ctx.hasTyped("newName")) {
            var newName = ctx.getTyped("newName", String.class);
            if (!newName.equals(category.getName())) {
                hotspotService.editCategory(category, newName, null);
                ctx.send(HotspotMessages.CATEGORY_EDIT_SUCCESS.apply(category.getName(), "name", newName));
                return;
            }
        }
        ctx.error(HotspotMessages.ERROR_EDIT_NO_CHANGE);
    }

    private void listCategories(ICommandContext ctx) {
        var categories = hotspotService.getCategories(cat -> true);
        var page = ctx.getFlag("page", 1);
        categoryPaginator.generatePage(CategoryPaginator.CategoryPaginatorMode.LIST, categories, page).sendTo(HotspotMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    @SuppressWarnings("unchecked")
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

        CommandBuilder.of("hsdelete", "hsd", "hsdel")
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

        CommandBuilder.of("hsedit", "hse")
                .description("Edit a Hotspot or Hotspot Category")
                .permission("greenfieldcore.hotspot.edit")
                .then("hotspot")
                    .then("hotspotName", new HotspotNameArgument(hotspotService, hs -> true))
                        .then("category")
                            .then("categoryId", new CategoryIdArgument(hotspotService, cat -> true)).executes(this::editHotspot)
                        .end()
                        .then("icon").canExecute(this::editHotspot)
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
                            .then("icon").canExecute(this::editHotspot)
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

        CommandBuilder.of("hslist", "hsl", "hotspots")
                .description("List Hotspots or Hotspot Categories")
                .permission("greenfieldcore.hotspot.list")
                .flag("page", "The page number", PdkArgumentTypes.integer(1, () -> "The page number"))
                .hiddenFlag("deleteMode", "This list will show as deletable entries.")
                .hiddenFlag("editMode", "This list will show as editable entries.")
                .canExecute(this::listHotspots)
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
