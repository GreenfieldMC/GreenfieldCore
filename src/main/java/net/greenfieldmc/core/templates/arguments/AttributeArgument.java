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

public class AttributeArgument extends AbstractStringTypedArgument<String> {

    private static final DynamicCommandExceptionType ATTRIBUTE_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Attribute " + o.toString() + " not found");
    private static final DynamicCommandExceptionType ATTRIBUTE_ALREADY_EXISTS = new DynamicCommandExceptionType(o -> () -> "Attribute " + o.toString() + " already exists");
    private static final DynamicCommandExceptionType TEMPLATE_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Template " + o.toString() + " not found");

    private final AttributeArgumentMode mode;
    private final ITemplateService templateService;

    public AttributeArgument(AttributeArgumentMode mode, ITemplateService templateService) {
        this.mode = mode;
        this.templateService = templateService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        var currentTemplateName = commandContext.argAtOrNull(1);
        if (currentTemplateName == null) return List.of();

        var template = templateService.getTemplate(currentTemplateName);
        if (template == null) return List.of();

        //suggest attributes that are not already in the template
        if (mode == AttributeArgumentMode.TEMPLATE_ADD) {
            return templateService.getTemplates().stream().map(Template::getAttributes)
                    .flatMap(List::stream)
                    .filter(attribute -> !template.getAttributes().contains(attribute))
                    .distinct()
                    .toList();
        }

        //suggest attributes that are already in the template
        return template.getAttributes();
    }

    @Override
    public String convertToNative(String s) {
        return s;
    }

    @Override
    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var currentTemplateName = reader.getString().split(" ")[1];
        var template = templateService.getTemplate(currentTemplateName);
        if (template == null) {
            reader.setCursor(reader.getCursor() - currentTemplateName.length());
            throw TEMPLATE_NOT_FOUND.createWithContext(reader, currentTemplateName);
        }

        if (mode == AttributeArgumentMode.TEMPLATE_ADD) {
            if (template.getAttributes().contains(nativeType)) {
                reader.setCursor(reader.getCursor() - nativeType.length());
                throw ATTRIBUTE_ALREADY_EXISTS.createWithContext(reader, nativeType);
            }
            return nativeType;
        }

        if (!template.getAttributes().contains(nativeType)) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw ATTRIBUTE_NOT_FOUND.createWithContext(reader, nativeType);
        }

        return nativeType;
    }

    public enum AttributeArgumentMode {

        TEMPLATE_ADD,
        TEMPLATE_REMOVE

    }

}
