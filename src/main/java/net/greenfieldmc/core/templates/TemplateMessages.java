package net.greenfieldmc.core.templates;

import com.njdaeger.pdk.utils.TriFunction;
import net.greenfieldmc.core.templates.models.Template;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Function;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class TemplateMessages {

    public static final String ERROR_TEMPLATE_NOT_LOADED = "Template is not currently loaded.";
    public static final String ERROR_TEMPLATE_SESSION_NOT_FOUND = "Template session not found.";
    public static final String ERROR_TEMPLATE_FAILED_TO_LOAD = "Failed to load template. This could indicate the template schematic file has changed.";
    public static final String ERROR_TEMPLATE_NOT_BEING_VIEWED = "You are not currently viewing a template.";
    public static final String ERROR_TEMPLATE_TOO_LARGE = "Template is too large to be viewed. Copy it to your clipboard and paste it in WorldEdit to view it.";
    public static final TextComponent ERROR_NO_TEMPLATES_SELECTED = Component.text("No templates are selected for your template brush. Please run /tbrush to select templates.", NamedTextColor.RED);
    public static final Function<String, TextComponent> ERROR_TEMPLATE_NOT_FOUND = (name) -> Component.text("Template " + name + " not found. It is being removed from your template list. This could indicate the template name has changed.", NamedTextColor.RED);
    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);

    public static final Function<Template, TextComponent> TEMPLATE_CREATED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' created.");
    public static final Function<Template, TextComponent> TEMPLATE_DELETED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' deleted.");
    public static final TriFunction<String, String, String, TextComponent> TEMPLATE_EDITED = (templateName, field, newValue) -> moduleMessage("Template", "Successfully edited the \"" + field + "\" attribute to \"" + newValue + "\" for template \"" + templateName + "\".");
    public static final TextComponent TEMPLATE_NEXT_RANDOMIZED = moduleMessage("Template", "Randomized the next template.");
    public static final Function<Template, TextComponent> TEMPLATE_COPIED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' copied to your WorldEdit clipboard.");
    public static final TextComponent TEMPLATE_VIEW_LOADING = moduleMessage("Template", "Loading template view...");
    public static final Function<Template, TextComponent> TEMPLATE_VIEW_STARTED = template -> moduleMessage("Template", "Viewing template '" + template.getTemplateName() + "'.");
    public static final TextComponent TEMPLATE_VIEW_ENDED = moduleMessage("Template", "Template viewer stopped.");
    public static final Function<Template, TextComponent> TEMPLATE_LOADING = template -> moduleMessage("Template", "Loading template '" + template.getTemplateName() + "'...");
}
