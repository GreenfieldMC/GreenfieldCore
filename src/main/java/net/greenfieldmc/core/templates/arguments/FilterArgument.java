package net.greenfieldmc.core.templates.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractQuotedTypedArgument;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.services.ITemplateService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterArgument extends AbstractQuotedTypedArgument<String> {

    private final ITemplateService templateService;

    public FilterArgument(ITemplateService templateService) {
        super();
        this.templateService = templateService;
    }

    @Override
    public Map<String, Message> listSuggestions(ICommandContext commandContext) {
        var attributes = templateService.getTemplates().stream()
                .flatMap(template -> template.getAttributes().stream())
                .distinct()
                .toList();
        var templates = templateService.getTemplates().stream()
                .map(Template::getTemplateName)
                .distinct()
                .toList();

        var list = new ArrayList<>(attributes.stream()
                .map(attribute -> "attribute:" + attribute)
                .toList());
        list.addAll(templates);
        return list.stream()
                .collect(Collectors.toMap(s -> s, (s) -> () -> s.startsWith("attribute:") ? "Look for templates with attribute: " + s.substring(9) : "Look for templates with names that contain: " + s));
    }

    @Override
    public String convertToNative(String templatePredicate) {
        return templatePredicate;
    }

    @Override
    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        return null;
    }

}
