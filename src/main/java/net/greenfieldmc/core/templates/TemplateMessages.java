package net.greenfieldmc.core.templates;

import com.njdaeger.pdk.utils.TriFunction;
import net.greenfieldmc.core.templates.models.Template;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Function;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class TemplateMessages {

    public static final TextComponent ERROR_NO_TEMPLATES_SELECTED = Component.text("No templates are selected for your template brush. Please run /tbrush to select templates.", NamedTextColor.RED);
    public static final Function<String, TextComponent> ERROR_TEMPLATE_NOT_FOUND = (name) -> Component.text("Template " + name + " not found. It is being removed from your template list. This could indicate the template name has changed.", NamedTextColor.RED);

    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);

    public static final Function<Template, TextComponent> TEMPLATE_CREATED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' created.");
    public static final Function<Template, TextComponent> TEMPLATE_DELETED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' deleted.");

    public static final TriFunction<String, String, String, TextComponent> TEMPLATE_EDITED = (templateName, field, newValue) -> moduleMessage("Template", "Successfully edited the \"" + field + "\" attribute to \"" + newValue + "\" for template \"" + templateName + "\".");

    public static final TextComponent TEMPLATE_NEXT_RANDOMIZED = moduleMessage("Template", "Randomized the next template.");

    public static final String TEMPLATE_SESSION_NOT_FOUND = "Template session not found.";

}
