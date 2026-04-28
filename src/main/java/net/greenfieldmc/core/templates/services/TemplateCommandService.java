package net.greenfieldmc.core.templates.services;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.TemplateMessages;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import net.greenfieldmc.core.templates.arguments.*;
import net.greenfieldmc.core.templates.models.AdjustableOption;
import net.greenfieldmc.core.templates.models.PasteOption;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.paginators.TemplatePaginator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateCommandService extends ModuleService<TemplateCommandService> implements IModuleService<TemplateCommandService> {

    private TemplatePaginator paginator;

    private final ITemplateService templateService;
    private final ITemplateWorldEditService worldEditService;
    private final ITemplateViewerService viewerService;

    public TemplateCommandService(Plugin plugin, Module module, ITemplateService templateService, ITemplateWorldEditService worldEditService, ITemplateViewerService viewerService) {
        super(plugin, module);
        this.templateService = templateService;
        this.worldEditService = worldEditService;
        this.viewerService = viewerService;
    }

    private void create(ICommandContext ctx) throws PDKCommandException {
        var templateName = ctx.getTyped("templateName", String.class);
        var mask = ctx.getTyped("mask", String.class, null);
        var attributes = ctx.getTyped("attributes", String.class, null);

        var player = ctx.asPlayer();

        // Get the item in the player's main hand as the display item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ItemStack displayItem = null;
        if (heldItem.getType() != Material.AIR) {
            displayItem = heldItem.clone();
            displayItem.setAmount(1);
        }

        // Run //copy (with optional mask) and //schematic save as the player
        java.nio.file.Path schematicPath;
        try {
            schematicPath = worldEditService.copySelectionToSchematic(player, templateName, mask);
        } catch (Exception e) {
            ctx.error(e.getMessage());
            return;
        }

        var createdTemplate = templateService.createTemplate(templateName, schematicPath.toString(), attributes == null ? List.of() : Arrays.stream(attributes.split(" ")).toList(), displayItem);
        ctx.send(TemplateMessages.TEMPLATE_CREATED.apply(createdTemplate));
        if (displayItem == null) {
            ctx.send(TemplateMessages.TEMPLATE_NO_DISPLAY_ITEM_WARNING);
        }
    }

    private void edit(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class);
        var newTemplateName = ctx.getTyped("newTemplateName", String.class, null);
        var newSchematicFile = ctx.getTyped("newSchematicFile", Path.class, null);
        var addedAttribute = ctx.getTyped("addedAttribute", String.class, null);
        var removedAttribute = ctx.getTyped("removedAttribute", String.class, null);
        var updateItem = ctx.getTyped("updateItem", Boolean.class, null);

        var editedField = "";
        var newValue = "";

        if (newTemplateName != null) {
            editedField = "name";
            newValue = newTemplateName;
            templateService.updateTemplate(template, newTemplateName, null, null);
        } else if (newSchematicFile != null) {
            editedField = "schematic";
            newValue = newSchematicFile.toString();
            templateService.updateTemplate(template, null, newSchematicFile.toString(), null);
        } else if (updateItem != null) {
            ItemStack heldItem = ctx.asPlayer().getInventory().getItemInMainHand();
            if (heldItem.getType() == Material.AIR) {
                ctx.error(TemplateMessages.ERROR_MUST_HOLD_ITEM);
                return;
            }
            ItemStack displayItem = heldItem.clone();
            displayItem.setAmount(1);
            templateService.updateDisplayItem(template, displayItem);
            editedField = "display item";
            newValue = heldItem.getType().name();
        } else if (addedAttribute != null) {
            var list = new ArrayList<>(template.getAttributes());
            list.add(addedAttribute);
            editedField = "attribute";
            newValue = list.stream().map(String::toString).reduce((a, b) -> a + ", " + b).orElse("");
            templateService.updateTemplate(template, null, null, list);
        } else if (removedAttribute != null) {
            var list = new ArrayList<>(template.getAttributes());
            list.remove(removedAttribute);
            editedField = "attribute";
            newValue = list.stream().map(String::toString).reduce((a, b) -> a + ", " + b).orElse("");
            templateService.updateTemplate(template, null, null, list);
        }

        ctx.send(TemplateMessages.TEMPLATE_EDITED.apply(template.getTemplateName(), editedField, newValue));
    }

    private void delete(ICommandContext ctx) {
        var template = ctx.getTyped("templateName", Template.class);
        templateService.deleteTemplate(template);
        ctx.send(TemplateMessages.TEMPLATE_DELETED.apply(template));
    }

    private void setItem(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class);
        ItemStack heldItem = ctx.asPlayer().getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            ctx.error(TemplateMessages.ERROR_MUST_HOLD_ITEM);
            return;
        }
        ItemStack displayItem = heldItem.clone();
        displayItem.setAmount(1);
        templateService.updateDisplayItem(template, displayItem);
        ctx.send(TemplateMessages.TEMPLATE_DISPLAY_ITEM_SET.apply(template.getTemplateName(), heldItem.getType().name()));
    }

    private void brush(ICommandContext ctx) throws PDKCommandException {
        var player = ctx.asPlayer();
        TemplateBrush templateBrush = silentResolveBrush(ctx);
        var page = ctx.getFlag("page", 1);

        if (templateBrush == null) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType() == Material.AIR) {
                ItemStack brushItem = new ItemStack(Material.BRUSH);
                player.getInventory().setItemInMainHand(brushItem);
                ctx.send(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("Given you a ", NamedTextColor.GRAY))
                        .append(Component.text("Brush", NamedTextColor.YELLOW))
                        .append(Component.text(" to use as your template brush.", NamedTextColor.GRAY)));
            }

            templateBrush = templateService.createBrush(ctx.asPlayer().getUniqueId());
            try {
                worldEditService.addBrush(ctx.asPlayer(), templateBrush.getBrushId());
            } catch (Exception e) {
                ctx.error(e.getMessage());
            }
        }

        showBrushModifyGui(ctx, templateBrush, templateService.getTemplates(), page);
    }

    private void brushAdd(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class, null);
        var pasteOption = ctx.getTyped("pasteOption", PasteOption.class, null);
        var page = ctx.getFlag("page", 1);
        var player = ctx.asPlayer();

        // If player's main hand is empty and no brush exists, give them a brush item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            var existingBrush = silentResolveBrush(ctx);
            if (existingBrush == null) {
                // Give player a blaze rod to use as a brush
                ItemStack brushItem = new ItemStack(Material.BLAZE_ROD);
                player.getInventory().setItemInMainHand(brushItem);
                ctx.send(Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text("Given you a ", NamedTextColor.GRAY))
                        .append(Component.text("Blaze Rod", NamedTextColor.YELLOW))
                        .append(Component.text(" to use as your template brush.", NamedTextColor.GRAY)));
            }
        }

        var templateBrush = resolveBrush(ctx);

        if (template != null) {
            templateBrush.addTemplate(template.getTemplateName());
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
                try {
                    template.loadClipboard();
                } catch (CommandSenderTypeException e) {
                    ctx.send(Component.text(e.getMessage(), NamedTextColor.RED));
                } catch (Exception e) {
                    if (e instanceof RuntimeException ex) throw ex;
                    ctx.send(Component.text(e.getMessage(), NamedTextColor.RED));
                }
            });
        }
        else if (pasteOption != null) templateBrush.addPasteOption(pasteOption);
        templateService.updateBrush(ctx.asPlayer().getUniqueId(), templateBrush);

        showBrushModifyGui(ctx, templateBrush, templateService.getTemplates(), page);
    }

    private void brushRemove(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class, null);
        var pasteOption = ctx.getTyped("pasteOption", PasteOption.class, null);
        var page = ctx.getFlag("page", 1);

        var templateBrush = resolveBrush(ctx);

        if (template != null) templateBrush.removeTemplate(template.getTemplateName());
        else if (pasteOption != null) templateBrush.removePasteOption(pasteOption);
        templateService.updateBrush(ctx.asPlayer().getUniqueId(), templateBrush);

        showBrushModifyGui(ctx, templateBrush, templateService.getTemplates(), page);
    }

    private void brushNext(ICommandContext ctx) throws PDKCommandException {
        var templateBrush = resolveBrush(ctx);
        templateBrush.randomizeNextTemplate();
        ctx.send(TemplateMessages.TEMPLATE_NEXT_RANDOMIZED);
    }

    private void view(ICommandContext ctx) throws PDKCommandException {
        if (!ctx.hasTyped("templateName")) {
            var cancelled = templateService.cancelPlacementMode(ctx.asPlayer());
            if (cancelled) ctx.send(TemplateMessages.TEMPLATE_VIEW_ENDED);
            else ctx.error(TemplateMessages.ERROR_TEMPLATE_NOT_BEING_VIEWED);
            return;
        }

        var template = ctx.getTyped("templateName", Template.class);
        var force = ctx.hasFlag("force");
        var player = ctx.asPlayer();

        if (force && !ctx.hasPermission("greenfieldcore.template.view.force")) ctx.error(TemplateMessages.ERROR_TEMPLATE_TOO_LARGE);

        if (!template.isLoaded()) ctx.send(TemplateMessages.TEMPLATE_LOADING.apply(template));

        templateService.startPlacementMode(player, template, force, ex -> {
            if (ex != null) {
                ctx.send(Component.text(ex.getMessage(), NamedTextColor.RED));
                return;
            }
            ctx.send(TemplateMessages.TEMPLATE_VIEW_STARTED.apply(template));
        }, (anchor, rotationDegrees) -> {
            // Invoked on the main thread (from PlayerInteractEvent).
            // WorldEdit EditSession / BukkitAdapter NMS calls MUST run on the main thread.
            try {
                boolean ignoreAir = templateService.isPasteIgnoreAir(player.getUniqueId());
                worldEditService.pasteTemplate(template, anchor, player, ignoreAir, rotationDegrees);
                player.sendMessage(
                        Component.text("[Template] ", NamedTextColor.LIGHT_PURPLE)
                                .append(Component.text("Template '", NamedTextColor.GRAY))
                                .append(Component.text(template.getTemplateName(), NamedTextColor.WHITE))
                                .append(Component.text("' placed at " + anchor.getBlockX() + ", " + anchor.getBlockY() + ", " + anchor.getBlockZ()
                                        + (rotationDegrees != 0 ? " (" + rotationDegrees + "° rotation)" : "") + ".", NamedTextColor.GRAY))
                );
            } catch (Exception e) {
                player.sendMessage(Component.text("[Template] Failed to paste: " + e.getMessage(), NamedTextColor.RED));
            }
        });
    }

    private void tagCreate(ICommandContext ctx) throws PDKCommandException {
        var tagName = ctx.getTyped("tagName", String.class);
        var heldItem = ctx.asPlayer().getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) ctx.error(TemplateMessages.ERROR_MUST_HOLD_ITEM_FOR_TAG);
        var item = heldItem.clone();
        item.setAmount(1);
        var existing = templateService.getTag(tagName);
        templateService.saveTag(tagName, item);
        ctx.send(existing == null
                ? TemplateMessages.TAG_CREATED.apply(tagName)
                : TemplateMessages.TAG_UPDATED.apply(tagName));
    }

    private void tagDelete(ICommandContext ctx) throws PDKCommandException {
        var tagName = ctx.getTyped("tagName", String.class);
        if (!templateService.deleteTag(tagName)) {
            ctx.send(TemplateMessages.ERROR_TAG_NOT_FOUND.apply(tagName));
            return;
        }
        ctx.send(TemplateMessages.TAG_DELETED.apply(tagName));
    }

    private void copy(ICommandContext ctx) {
        var template = ctx.getTyped("templateName", Template.class);
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                worldEditService.loadToClipboard(template, ctx.asPlayer());
                ctx.send(TemplateMessages.TEMPLATE_COPIED.apply(template));
            } catch (CommandSenderTypeException e) {
                ctx.send(Component.text(e.getMessage(), NamedTextColor.RED));
            } catch (Exception e) {
                if (e instanceof RuntimeException ex) throw ex;
                ctx.send(Component.text(e.getMessage(), NamedTextColor.RED));
            }
        });
    }

    private void list(ICommandContext ctx) throws PDKCommandException {
        var isBrushMode = ctx.hasFlag("brush");
        var filter = ctx.getTyped("filter", String.class, null);
        var page = ctx.getFlag("page", 1);
        var filters = (filter == null || filter.isEmpty()) ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(filter.split(" ")));

        var templates = templateService.getTemplates(template -> {
            if (filters.isEmpty()) return true;
            return filters.stream().anyMatch(f -> template.getTemplateName().toLowerCase().contains(f.toLowerCase())) || template.getAttributes().stream().anyMatch(attr -> filters.stream().anyMatch(f -> attr.toLowerCase().contains(f.toLowerCase())));
        });

        if (isBrushMode) {
            var templateBrush = resolveBrush(ctx);
            showBrushModifyGui(ctx, templateBrush, templates, page);
            return;
        }

        if (templates.isEmpty()) {
            ctx.send(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY);
            return;
        }

        paginator.open(ctx.asPlayer(), templates, TemplatePaginator.TemplatePaginatorMode.SELECT, null, filter, page, null);
    }

    private void showBrushModifyGui(ICommandContext ctx, TemplateBrush templateBrush, List<Template> templates, int page) throws PDKCommandException {
        paginator.open(ctx.asPlayer(), templates, TemplatePaginator.TemplatePaginatorMode.BRUSH, templateBrush, null, page, null);

        // Also send the adjustable options as chat text below the GUI
        var grayColor = NamedTextColor.DARK_GRAY;
        var highlightColor = NamedTextColor.LIGHT_PURPLE;
        var grayedOutColor = NamedTextColor.GRAY;

        var line = Component.text("= ", grayColor).toBuilder();
        line.append(generateAdjustableOption("paste", templateBrush.getPasteOptions(), PasteOption.class, highlightColor, grayColor, grayedOutColor));
        line.resetStyle().append(Component.text(" =", grayColor));

        ctx.send(line.build());
    }

    private <O, T extends Enum<T> & AdjustableOption<O>> TextComponent generateAdjustableOption(String adjustableOptionName, List<T> selectedOptions, Class<T> optionEnum, NamedTextColor highlightColor, NamedTextColor grayColor, NamedTextColor grayedOutColor) {
        TextComponent.Builder builder = Component.text("[", grayColor).decorate(TextDecoration.BOLD).toBuilder();

        // Get all possible enum values
        T[] allOptions = optionEnum.getEnumConstants();

        // Add each option with appropriate formatting
        for (int i = 0; i < allOptions.length; i++) {
            T option = allOptions[i];
            boolean isSelected = selectedOptions.contains(option);

            String commandAction = isSelected ? "remove" : "add";
            String hoverAction = isSelected ? "Click to remove" : "Click to add";

            // Create the option component with appropriate color and decoration
            Component optionComponent = Component.text(option.getChatName(), isSelected ? grayedOutColor : highlightColor)
                    .decorate(TextDecoration.BOLD)
                    .decorate(isSelected ? new TextDecoration[]{ TextDecoration.UNDERLINED } : new TextDecoration[] {} )
                    .hoverEvent(HoverEvent.showText(
                            Component.text(hoverAction, highlightColor).appendNewline().append(Component.text(option.getDescription(), grayColor))
                    ))
                    .clickEvent(ClickEvent.runCommand(
                            "/tbrush " + commandAction + " option " + adjustableOptionName + " " + option.name()
                    ));

            builder.append(optionComponent);

            // Add a space between options, but not after the last one
            if (i < allOptions.length - 1) {
                builder.append(Component.text(" ", grayColor).decorate(TextDecoration.BOLD));
            }
        }

        // Add right bracket
        builder.append(Component.text("]", grayColor).decorate(TextDecoration.BOLD));
        return builder.build();
    }

    private TemplateBrush silentResolveBrush(ICommandContext ctx) {
        try {
            return resolveBrush(ctx);
        } catch (PDKCommandException e) {
            return null;
        }
    }

    private TemplateBrush resolveBrush(ICommandContext ctx) throws PDKCommandException {
        WorldEditTemplateBrush worldEditBrush = null;
        try {
            worldEditBrush = worldEditService.getBrush(ctx.asPlayer());
        } catch (Exception e) {
            ctx.error(e.getMessage());
        }
        var session = templateService.getSession(ctx.asPlayer().getUniqueId());
        if (session == null) ctx.error(TemplateMessages.ERROR_TEMPLATE_SESSION_NOT_FOUND);
        var templateBrush = session.getBrush(worldEditBrush.getBrushId());
        if (templateBrush == null) ctx.error(TemplateMessages.ERROR_TEMPLATE_SESSION_NOT_FOUND);
        return templateBrush;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        this.paginator = new TemplatePaginator(plugin, templateService, viewerService);

        CommandBuilder.of("tcreate", "createtemplate", "ct")
                .permission("greenfieldcore.template.create")
                .description("Create a new template from your current WorldEdit selection. Hold an item to set it as the display item.")
                .then("templateName", new NewTemplateNameArgument(templateService))
                    .canExecute(this::create)
                    .then("-m")
                        .then("mask", PdkArgumentTypes.string(() -> "WorldEdit mask — blocks matching this are excluded from the copy (e.g. red_wool)."))
                            .canExecute(this::create)
                            .then("attributes", new CreationAttributesArgument(templateService)).executes(this::create)
                        .end()
                    .end()
                    .then("attributes", new CreationAttributesArgument(templateService)).executes(this::create)
                .end()
                .register(plugin);

        CommandBuilder.of("tedit", "edittemplate", "et")
                .permission("greenfieldcore.template.edit")
                .description("Edit an existing template. Hold an item when using 'item' to set the display icon.")
                .then("templateName", new TemplateNameArgument(templateService))
                    .then("schematic").then("newSchematicFile", new SchematicFileArgument(worldEditService)).executes(this::edit).end()
                    .then("name").then("newTemplateName", new NewTemplateNameArgument(templateService)).executes(this::edit).end()
                    .then("item").executes(this::edit).end()
                    .then("attributes")
                        .then("add").then("addedAttribute", new AttributeArgument(AttributeArgument.AttributeArgumentMode.TEMPLATE_ADD, templateService)).executes(this::edit).end()
                        .then("remove").then("removedAttribute", new AttributeArgument(AttributeArgument.AttributeArgumentMode.TEMPLATE_REMOVE, templateService)).executes(this::edit).end()
                    .end()
                .register(plugin);

        CommandBuilder.of("tdelete", "tdel", "deletetemplate")
                .permission("greenfieldcore.template.delete")
                .description("Delete an existing template.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::delete)
                .register(plugin);

        CommandBuilder.of("tsetitem", "templatesetitem", "tsi")
                .permission("greenfieldcore.template.edit")
                .description("Set the display item for a template. Hold the item in your main hand.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::setItem)
                .register(plugin);

        CommandBuilder.of("tbrush", "templatebrush", "tb")
                .permission("greenfieldcore.template.brush")
                .description("Edit the template brush.")
                .hiddenFlag("page", "The page to view.", PdkArgumentTypes.integer(1, () -> "The page to view."))
                .canExecute(this::brush)
                .then("next").executes(this::brushNext)
                .then("add")
                    .then("template")
                        .then("templateName", new TemplateNameArgument(templateService)).executes(this::brushAdd)
                    .end()
                    .then("option")
                        .then("paste")
                            .then("pasteOption", PdkArgumentTypes.enumArg(PasteOption.class)).executes(this::brushAdd)
                        .end()
                    .end()
                .end()
                .then("remove")
                    .then("template")
                        .then("templateName", new TemplateNameArgument(templateService)).executes(this::brushRemove)
                    .end()
                    .then("option")
                        .then("paste")
                            .then("pasteOption", PdkArgumentTypes.enumArg(PasteOption.class)).executes(this::brushRemove)
                        .end()
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("tview", "viewtemplate", "templateview", "vt")
                .permission("greenfieldcore.template.view")
                .description("Enter placement mode with a template, or cancel current placement.")
                .flag("force", "Force the template to be viewed even if it is too large.")
                .canExecute(this::view)
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::view)
                .register(plugin);

        CommandBuilder.of("tcopy", "copytemplate")
                .permission("greenfieldcore.template.copy")
                .description("Copy an existing template to your WorldEdit clipboard.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::copy)
                .register(plugin);

        CommandBuilder.of("ttag", "templatetag")
                .permission("greenfieldcore.template.tag")
                .description("Manage template attribute tags. Hold an item to set as the tag display item.")
                .then("create").then("tagName", PdkArgumentTypes.string(() -> "The tag name.")).executes(this::tagCreate).end()
                .then("delete").then("tagName", PdkArgumentTypes.string(() -> "The tag name.")).executes(this::tagDelete).end()
                .register(plugin);

        CommandBuilder.of("tlist", "listtemplates", "templatelist", "lt")
                .permission("greenfieldcore.template.list")
                .description("List all templates in a GUI. Click to view or copy.")
                .hiddenFlag("brush", "When the brush is being modified.")
                .flag("page", "The page to view.", PdkArgumentTypes.integer(1, () -> "The page to view."))
                .canExecute(this::list)
                .then("filter", new FilterArgument(templateService)).executes(this::list)
                .register(plugin);

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        if (paginator != null) {
            paginator.unregister();
        }
    }
}
