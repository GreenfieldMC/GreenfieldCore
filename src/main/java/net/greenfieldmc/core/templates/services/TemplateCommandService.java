package net.greenfieldmc.core.templates.services;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.Triple;
import net.greenfieldmc.core.templates.TemplateMessages;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import net.greenfieldmc.core.templates.arguments.AttributeArgument;
import net.greenfieldmc.core.templates.arguments.FilterArgument;
import net.greenfieldmc.core.templates.arguments.NewTemplateNameArgument;
import net.greenfieldmc.core.templates.arguments.SchematicFileArgument;
import net.greenfieldmc.core.templates.arguments.TemplateNameArgument;
import net.greenfieldmc.core.templates.models.FlipOption;
import net.greenfieldmc.core.templates.models.PasteOption;
import net.greenfieldmc.core.templates.models.RotationOption;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateBrush;
import net.greenfieldmc.core.templates.paginators.TemplatePaginator;
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
        var attributes = ctx.getTyped("attributes", String.class);
        var createdTemplate = templateService.createTemplate(templateName, schematicFile.toString(), Arrays.stream(attributes.split(" ")).toList());
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

        if (templateBrush == null) {
            templateBrush = templateService.createBrush(ctx.asPlayer().getUniqueId());
            try {
                worldEditService.addBrush(ctx.asPlayer(), templateBrush.getBrushId());
            } catch (Exception e) {
                ctx.error(e.getMessage());
            }
        }

        BRUSH_MODIFY_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY, ctx, templateBrush), templateService.getTemplates(), 1).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.asPlayer());
    }

    private void brushAdd(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class, null);
        var flipOption = ctx.getTyped("flipOption", FlipOption.class, null);
        var rotateOption = ctx.getTyped("rotateOption", RotationOption.class, null);
        var pasteOption = ctx.getTyped("pasteOption", PasteOption.class, null);

        var templateBrush = resolveBrush(ctx);

        if (template != null) templateBrush.addTemplate(template.getTemplateName());
        else if (flipOption != null) templateBrush.addFlipOption(flipOption);
        else if (rotateOption != null) templateBrush.addRotationOption(rotateOption);
        else if (pasteOption != null) templateBrush.addPasteOption(pasteOption);
        templateService.updateBrush(ctx.asPlayer().getUniqueId(), templateBrush);

        BRUSH_MODIFY_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY, ctx, templateBrush), templateService.getTemplates(), 1).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.asPlayer());
    }

    private void brushRemove(ICommandContext ctx) throws PDKCommandException {
        var template = ctx.getTyped("templateName", Template.class, null);
        var flipOption = ctx.getTyped("flipOption", FlipOption.class, null);
        var rotateOption = ctx.getTyped("rotateOption", RotationOption.class, null);
        var pasteOption = ctx.getTyped("pasteOption", PasteOption.class, null);

        var templateBrush = resolveBrush(ctx);

        if (template != null) templateBrush.removeTemplate(template.getTemplateName());
        else if (flipOption != null) templateBrush.removeFlipOption(flipOption);
        else if (rotateOption != null) templateBrush.removeRotationOption(rotateOption);
        else if (pasteOption != null) templateBrush.removePasteOption(pasteOption);
        templateService.updateBrush(ctx.asPlayer().getUniqueId(), templateBrush);

        BRUSH_MODIFY_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY, ctx, templateBrush), templateService.getTemplates(), 1).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.asPlayer());
    }

    private void brushNext(ICommandContext ctx) throws PDKCommandException {
        var templateBrush = resolveBrush(ctx);
        templateBrush.randomizeNextTemplate();
        ctx.send(TemplateMessages.TEMPLATE_NEXT_RANDOMIZED);
    }

    private void view(ICommandContext ctx) {

    }

    private void copy(ICommandContext ctx) {

    }

    private void list(ICommandContext ctx) throws PDKCommandException {
        var isBrushMode = ctx.hasFlag("brush");
        var filter = ctx.getTyped("filter", String.class, null);
        var templateNameFilters = new ArrayList<String>();
        var attributeFilters = new ArrayList<String>();

        if (filter != null && !filter.isBlank()) {
            Arrays.stream(filter.split(" ")).forEach(f -> {
                if (f.toLowerCase().startsWith("attribute:")) {
                    attributeFilters.add(f.substring(10));
                } else {
                    templateNameFilters.add(f);
                }
            });
        }

        var templates = templateService.getTemplates(template -> {
            if (filter == null || filter.isEmpty()) return true;
            if (!templateNameFilters.isEmpty()) {
                if (templateNameFilters.stream().anyMatch(name -> template.getTemplateName().toLowerCase().contains(name.toLowerCase()))) {
                    return true;
                }
            }

            if (!attributeFilters.isEmpty()) {
                if (attributeFilters.stream().anyMatch(attribute -> template.getAttributes().stream().anyMatch(attr -> attr.toLowerCase().contains(attribute.toLowerCase())))) {
                    return true;
                }
            }
            return false;
        });

        if (isBrushMode) {
            var templateBrush = resolveBrush(ctx);
            BRUSH_MODIFY_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY, ctx, templateBrush), templates, 1).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
            return;
        }

        LIST_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.LIST, ctx, null), templates, 1).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void showBrushModifyPages(ICommandContext ctx, TemplateBrush templateBrush, List<Template> templates, int page) {
        BRUSH_MODIFY_PAGINATOR.generatePage(Triple.of(TemplatePaginator.TemplatePaginatorMode.BRUSH_MODIFY, ctx, templateBrush), templates, page).sendTo(TemplateMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
//        ctx.send(); todo: write the option buttons
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
        if (session == null) ctx.error(TemplateMessages.TEMPLATE_SESSION_NOT_FOUND);
        var templateBrush = session.getBrush(worldEditBrush.getBrushId());
        if (templateBrush == null) ctx.error(TemplateMessages.TEMPLATE_SESSION_NOT_FOUND);
        return templateBrush;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("tcreate", "createtemplate")
                .permission("greenfieldcore.template.create")
                .description("Create a new template.")
                .then("templateName", new NewTemplateNameArgument(templateService))
                    .then("schematicFile", new SchematicFileArgument(worldEditService)).canExecute(this::create)
                        .then("attributes", PdkArgumentTypes.greedyString((ctx) -> templateService.getTemplates().stream().map(Template::getAttributes).flatMap(List::stream).distinct().toList(), () -> "Add any attributes to this template.")).executes(this::create)
                    .end()
                .end()
                .register(plugin);

        CommandBuilder.of("tedit", "edittemplate")
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

        CommandBuilder.of("tbrush", "templatebrush")
                .permission("greenfieldcore.template.brush")
                .description("Edit the template brush.")
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

        CommandBuilder.of("tview", "viewtemplate")
                .permission("greenfieldcore.template.view")
                .description("View an existing template.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::view)
                .register(plugin);

        CommandBuilder.of("tcopy", "copytemplate")
                .permission("greenfieldcore.template.copy")
                .description("Copy an existing template.")
                .then("templateName", new TemplateNameArgument(templateService)).executes(this::copy)
                .register(plugin);

        CommandBuilder.of("tlist", "listtemplates")
                .permission("greenfieldcore.template.list")
                .description("List all templates.")
                .hiddenFlag("brush", "When the brush is being modified.")
                .flag("page", "The page to view.", PdkArgumentTypes.integer(1, () -> "The page to view."))
                .canExecute(this::list)
                .then("filter", new FilterArgument(templateService)).executes()
                .register(plugin);

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
