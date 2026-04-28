package net.greenfieldmc.core.templates;

import com.njdaeger.pdk.utils.TriFunction;
import net.greenfieldmc.core.templates.models.Template;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.BiFunction;
import java.util.function.Function;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class TemplateMessages {

    public static final String ERROR_TEMPLATE_NOT_LOADED = "Template is not currently loaded.";
    public static final String ERROR_TEMPLATE_SESSION_NOT_FOUND = "Template session not found.";
    public static final String ERROR_TEMPLATE_FAILED_TO_LOAD = "Failed to load template. This could indicate the template schematic file has changed.";
    public static final String ERROR_TEMPLATE_NOT_BEING_VIEWED = "You are not currently in placement mode.";
    public static final String ERROR_TEMPLATE_TOO_LARGE = "Template is too large to be viewed. Copy it to your clipboard and paste it in WorldEdit to view it.";
    public static final String ERROR_MUST_HOLD_ITEM = "You must be holding an item in your main hand to set as the display item.";
    public static final TextComponent ERROR_NO_TEMPLATES_SELECTED = Component.text("No templates are selected for your template brush. Please run /tbrush to select templates.", NamedTextColor.RED);
    public static final Function<String, TextComponent> ERROR_TEMPLATE_NOT_FOUND = (name) -> Component.text("Template " + name + " not found. It is being removed from your template list. This could indicate the template name has changed.", NamedTextColor.RED);
    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);

    public static final Function<Template, TextComponent> TEMPLATE_CREATED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' created.");
    public static final Function<Template, TextComponent> TEMPLATE_DELETED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' deleted.");
    public static final TriFunction<String, String, String, TextComponent> TEMPLATE_EDITED = (templateName, field, newValue) -> moduleMessage("Template", "Successfully edited the \"" + field + "\" attribute to \"" + newValue + "\" for template \"" + templateName + "\".");
    public static final TextComponent TEMPLATE_NEXT_RANDOMIZED = moduleMessage("Template", "Randomized the next template.");
    public static final Function<Template, TextComponent> TEMPLATE_COPIED = template -> moduleMessage("Template", "Template '" + template.getTemplateName() + "' copied to your WorldEdit clipboard.");
    public static final TextComponent TEMPLATE_VIEW_LOADING = moduleMessage("Template", "Loading template view...");
    public static final Function<Template, TextComponent> TEMPLATE_VIEW_STARTED = template -> moduleMessage("Template", "Entering placement mode with template '" + template.getTemplateName() + "'. Right-click to place, left-click to cancel.");
    public static final TextComponent TEMPLATE_VIEW_ENDED = moduleMessage("Template", "Placement mode cancelled.");
    public static final Function<Template, TextComponent> TEMPLATE_LOADING = template -> moduleMessage("Template", "Loading template '" + template.getTemplateName() + "'...");
    public static final TextComponent TEMPLATE_NO_DISPLAY_ITEM_WARNING = moduleMessage("Template", "No item was held. Template will use a default paper icon in the GUI. Use /tsetitem to set a display item later.");
    public static final BiFunction<String, String, TextComponent> TEMPLATE_DISPLAY_ITEM_SET = (templateName, itemType) -> moduleMessage("Template", "Display item for '" + templateName + "' set to " + itemType + ".");

    public static final Function<String, TextComponent> TAG_CREATED = name -> moduleMessage("Template", "Tag '" + name + "' created.");
    public static final Function<String, TextComponent> TAG_DELETED = name -> moduleMessage("Template", "Tag '" + name + "' deleted.");
    public static final Function<String, TextComponent> TAG_UPDATED = name -> moduleMessage("Template", "Tag '" + name + "' display item updated.");
    public static final String ERROR_MUST_HOLD_ITEM_FOR_TAG = "You must hold an item in your main hand to set as the tag display item.";
    public static final Function<String, TextComponent> ERROR_TAG_NOT_FOUND = name -> Component.text("Tag '" + name + "' not found.", NamedTextColor.RED);
}
