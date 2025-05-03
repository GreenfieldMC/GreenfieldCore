package net.greenfieldmc.core.templates.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.services.ITemplateService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TemplateNameArgument extends AbstractStringTypedArgument<Template> {

    private static final DynamicCommandExceptionType TEMPLATE_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Template " + o.toString() + " not found");

    private final ITemplateService templateService;

    public TemplateNameArgument(ITemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public List<Template> listBasicSuggestions(ICommandContext commandContext) {
        return templateService.getTemplates();
    }

    @Override
    public String convertToNative(Template template) {
        return template.getTemplateName().toLowerCase();
    }

    @Override
    public Template convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var template = templateService.getTemplate(nativeType);
        if (template == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw TEMPLATE_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return template;
    }

}
