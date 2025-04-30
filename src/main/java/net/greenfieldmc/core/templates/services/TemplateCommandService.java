package net.greenfieldmc.core.templates.services;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.Triple;
import net.greenfieldmc.core.templates.TemplateMessages;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import net.greenfieldmc.core.templates.arguments.AttributeArgument;
import net.greenfieldmc.core.templates.arguments.CreationAttributesArgument;
import net.greenfieldmc.core.templates.arguments.FilterArgument;
import net.greenfieldmc.core.templates.arguments.NewTemplateNameArgument;
import net.greenfieldmc.core.templates.arguments.SchematicFileArgument;
import net.greenfieldmc.core.templates.arguments.TemplateNameArgument;
import net.greenfieldmc.core.templates.arguments.ViewScaleArgument;
import net.greenfieldmc.core.templates.models.AdjustableOption;
import net.greenfieldmc.core.templates.models.FlipOption;
import net.greenfieldmc.core.templates.models.PasteOption;
import net.greenfieldmc.core.templates.models.RotationOption;
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
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateCommandService extends ModuleService<TemplateCommandService> implements IModuleService<TemplateCommandService> {

    private final ChatPaginator<Template, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush>> BRUSH_MODIFY_PAGINATOR = new TemplatePaginator(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY).build();
    private final ChatPaginator<Template, Triple<TemplatePaginator.TemplatePaginatorMode, ICommandContext, TemplateBrush>> LIST_PAGINATOR = new TemplatePaginator(TemplatePaginator.TemplatePaginatorMode.LIST).build();

    private final ITemplateService templateService;
    private final ITemplateWorldEditService worldEditService;

    public TemplateCommandService(Plugin plugin, Module module, ITemplateService templateService, ITemplateWorldEditService worldEditService) {
        super(plugin, module);
        this.templateService = templateService;
        this.worldEditService = worldEditService;
    }

    private void create(ICommandContext ctx) {
        var templateName = ctx.getTyped("templateName", String.class);
        var schematicFile = ctx.getTyped("schematicFile", Path.class);
        var attributes = ctx.getTyped("attributes", String.class, null);
        var createdTemplate = templateService.createTemplate(templateName, schematicFile.toString(), attributes == null ? List.of() : Arrays.stream(attributes.split(" ")).toList());
        ctx.send(TemplateMessages.TEMPLATE_CREATED.apply(createdTemplate));
    }

    private void edit(ICommandContext ctx) {
        var template = ctx.getTyped("templateName", Template.class);
        var newTemplateName = ctx.getTyped("newTemplateName", String.class, null);
        var newSchematicFile = ctx.getTyped("newSchematicFile", Path.class, null);
        var addedAttribute = ctx.getTyped("addedAttribute", String.class, null);
        var removedAttribute = ctx.getTyped("removedAttribute", String.class, null);

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

    private void brush(ICommandContext ctx) throws PDKCommandException {
        TemplateBrush templateBrush = silentResolveBrush(ctx);
        var page = ctx.getFlag("page", 1);

        if (templateBrush == null) {
            templateBrush = templateService.createBrush(ctx.asPlayer().getUniqueId());
            try {
                worldEditService.addBrush(ctx.asPlayer(), templateBrush.getBrushId());
            } catch (Exception e) {
                ctx.error(e.getMessage());
            }
        }

        showBrushModifyPages(ctx, templateBrush, templateService.getTemplates(), page);
    }

    private void brushAdd(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class, null);
        var flipOption = ctx.getTyped("flipOption", FlipOption.class, null);
        var rotateOption = ctx.getTyped("rotateOption", RotationOption.class, null);
        var pasteOption = ctx.getTyped("pasteOption", PasteOption.class, null);
        var page = ctx.getFlag("page", 1);

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
        else if (flipOption != null) templateBrush.addFlipOption(flipOption);
        else if (rotateOption != null) templateBrush.addRotationOption(rotateOption);
        else if (pasteOption != null) templateBrush.addPasteOption(pasteOption);
        templateService.updateBrush(ctx.asPlayer().getUniqueId(), templateBrush);

        showBrushModifyPages(ctx, templateBrush, templateService.getTemplates(), page);
    }

    private void brushRemove(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class, null);
        var flipOption = ctx.getTyped("flipOption", FlipOption.class, null);
        var rotateOption = ctx.getTyped("rotateOption", RotationOption.class, null);
        var pasteOption = ctx.getTyped("pasteOption", PasteOption.class, null);
        var page = ctx.getFlag("page", 1);

        var templateBrush = resolveBrush(ctx);

        if (template != null) templateBrush.removeTemplate(template.getTemplateName());
        else if (flipOption != null) templateBrush.removeFlipOption(flipOption);
        else if (rotateOption != null) templateBrush.removeRotationOption(rotateOption);
        else if (pasteOption != null) templateBrush.removePasteOption(pasteOption);
        templateService.updateBrush(ctx.asPlayer().getUniqueId(), templateBrush);

        showBrushModifyPages(ctx, templateBrush, templateService.getTemplates(), page);
    }

    private void brushNext(ICommandContext ctx) throws PDKCommandException {
        var templateBrush = resolveBrush(ctx);
        templateBrush.randomizeNextTemplate();
        ctx.send(TemplateMessages.TEMPLATE_NEXT_RANDOMIZED);
    }

    private void view(ICommandContext ctx) throws PDKCommandException {
        if (!ctx.hasTyped("templateName")) {
            var destroyed = templateService.destroyTemplateView(ctx.asPlayer());
            if (destroyed) ctx.send(TemplateMessages.TEMPLATE_VIEW_ENDED);
            else ctx.error(TemplateMessages.ERROR_TEMPLATE_NOT_BEING_VIEWED);
            return;
        }

        var template = ctx.getTyped("templateName", Template.class);
        var scale = ctx.getTyped("scale", Double.class, 1 / 16.0);
        var force = ctx.hasFlag("force");

        if (force && !ctx.hasPermission("greenfieldcore.template.view.force")) ctx.error(TemplateMessages.ERROR_TEMPLATE_TOO_LARGE);

        if (!template.isLoaded()) ctx.send(TemplateMessages.TEMPLATE_LOADING.apply(template));
        templateService.startTemplateView(ctx.asPlayer(), template, scale, force, ex -> {
            if (ex != null) {
                ctx.send(Component.text(ex.getMessage(), NamedTextColor.RED));
                return;
            }
            ctx.send(TemplateMessages.TEMPLATE_VIEW_STARTED.apply(template));
        });
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
            showBrushModifyPages(ctx, templateBrush, templates, page);
            return;
        }

        LIST_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.LIST, ctx, null), templates, page).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void showBrushModifyPages(ICommandContext ctx, TemplateBrush templateBrush, List<Template> templates, int page) {
        var grayColor = BRUSH_MODIFY_PAGINATOR.getGrayColor();

        var line = Component.text("= ", grayColor).toBuilder();
        line.append(generateAdjustableOption("rotate", templateBrush.getRotationOptions(), RotationOption.class));
        line.resetStyle().append(Component.text(" ==== ", grayColor));
        line.append(generateAdjustableOption("flip", templateBrush.getFlipOptions(), FlipOption.class));
        line.resetStyle().append(Component.text(" ==== ", grayColor));
        line.append(generateAdjustableOption("paste", templateBrush.getPasteOptions(), PasteOption.class));
        line.resetStyle().append(Component.text(" =", grayColor));

        BRUSH_MODIFY_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY, ctx, templateBrush), templates, page).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
        ctx.send(line.build());
    }

    private <O, T extends Enum<T> & AdjustableOption<O>> TextComponent generateAdjustableOption(String adjustableOptionName, List<T> selectedOptions, Class<T> optionEnum) {
        var highlightColor = BRUSH_MODIFY_PAGINATOR.getHighlightColor();
        var grayColor = BRUSH_MODIFY_PAGINATOR.getGrayColor();
        var grayedOutColor = BRUSH_MODIFY_PAGINATOR.getGrayedOutColor();

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
        CommandBuilder.of("tcreate", "createtemplate", "ct")
                .permission("greenfieldcore.template.create")
                .description("Create a new template.")
                .then("schematicFile", new SchematicFileArgument(worldEditService))
                    .then("templateName", new NewTemplateNameArgument(templateService)).canExecute(this::create)
                        .then("attributes", new CreationAttributesArgument(templateService)).executes(this::create)
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("tedit", "edittemplate", "et")
                .permission("greenfieldcore.template.edit")
                .description("Edit an existing template.")
                .then("templateName", new TemplateNameArgument(templateService))
                    .then("schematic").then("newSchematicFile", new SchematicFileArgument(worldEditService)).executes(this::edit).end()
                    .then("name").then("newTemplateName", new NewTemplateNameArgument(templateService)).executes(this::edit).end()
                    .then("attributes")
                        .then("add").then("addedAttribute", new AttributeArgument(AttributeArgument.AttributeArgumentMode.TEMPLATE_ADD, templateService)).executes(this::edit).end()
                        .then("remove").then("removedAttribute", new AttributeArgument(AttributeArgument.AttributeArgumentMode.TEMPLATE_REMOVE, templateService)).executes(this::edit).end()
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("tdel", "deletetemplate")
                .permission("greenfieldcore.template.delete")
                .description("Delete an existing template.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::delete)
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
                        .then("flip")
                            .then("flipOption", PdkArgumentTypes.enumArg(FlipOption.class)).executes(this::brushAdd)
                        .end()
                        .then("rotate")
                            .then("rotateOption", PdkArgumentTypes.enumArg(RotationOption.class)).executes(this::brushAdd)
                        .end()
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
                        .then("flip")
                            .then("flipOption", PdkArgumentTypes.enumArg(FlipOption.class)).executes(this::brushRemove)
                        .end()
                        .then("rotate")
                            .then("rotateOption", PdkArgumentTypes.enumArg(RotationOption.class)).executes(this::brushRemove)
                        .end()
                        .then("paste")
                            .then("pasteOption", PdkArgumentTypes.enumArg(PasteOption.class)).executes(this::brushRemove)
                        .end()
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("tview", "viewtemplate", "templateview", "vt")
                .permission("greenfieldcore.template.view")
                .description("View an existing template.")
                .flag("force", "Force the template to be viewed even if it is too large.")
                .canExecute(this::view)
                .then("templateName", new TemplateNameArgument(templateService)).canExecute(this::view)
                    .then("scale", new ViewScaleArgument()).executes(this::view)
                .end()
                .register(plugin);

        CommandBuilder.of("tcopy", "copytemplate")
                .permission("greenfieldcore.template.copy")
                .description("Copy an existing template to your WorldEdit clipboard.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::copy)
                .register(plugin);

        CommandBuilder.of("tlist", "listtemplates", "templatelist", "lt")
                .permission("greenfieldcore.template.list")
                .description("List all templates.")
                .hiddenFlag("brush", "When the brush is being modified.")
                .flag("page", "The page to view.", PdkArgumentTypes.integer(1, () -> "The page to view."))
                .canExecute(this::list)
                .then("filter", new FilterArgument(templateService)).executes(this::list)
                .register(plugin);

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
