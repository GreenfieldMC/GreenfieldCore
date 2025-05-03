package net.greenfieldmc.core.templates.arguments;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.defaults.GreedyStringArgument;
import net.greenfieldmc.core.templates.services.ITemplateService;

import java.util.List;

public class CreationAttributesArgument extends GreedyStringArgument {

    private final ITemplateService templateService;

    public CreationAttributesArgument(ITemplateService templateService) {
        super(() -> "Add any attributes to this template.");
        this.templateService = templateService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        return templateService.getTemplates().stream()
                .flatMap(template -> template.getAttributes().stream())
                .distinct()
                .toList();
    }
}
