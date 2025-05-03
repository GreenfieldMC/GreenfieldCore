package net.greenfieldmc.core.templates.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractTokenizedQuotedTypedArgument;
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.services.ITemplateService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FilterArgument extends AbstractTokenizedQuotedTypedArgument<String> {

    private final ITemplateService templateService;

    public FilterArgument(ITemplateService templateService) {
        super();
        this.templateService = templateService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        var attributes = templateService.getTemplates().stream()
                .flatMap(template -> template.getAttributes().stream())
                .distinct()
                .toList();
        var templates = templateService.getTemplates().stream()
                .map(Template::getTemplateName)
                .distinct()
                .toList();

        var list = new ArrayList<>(attributes);
        list.addAll(templates);
        return list;
    }

    @Override
    public String convertToNative(String templatePredicate) {
        return templatePredicate;
    }

    @Override
    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        return nativeType;
    }

}
