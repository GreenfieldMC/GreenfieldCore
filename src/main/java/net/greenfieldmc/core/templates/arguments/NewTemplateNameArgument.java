package net.greenfieldmc.core.templates.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.templates.services.ITemplateService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class NewTemplateNameArgument extends AbstractStringTypedArgument<String> {

    private static final DynamicCommandExceptionType TEMPLATE_ALREADY_EXISTS = new DynamicCommandExceptionType(o -> () -> "Template " + o.toString() + " already exists");

    private final ITemplateService templateService;

    public NewTemplateNameArgument(ITemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public String convertToNative(String s) {
        return s;
    }

    @Override
    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        if (templateService.getTemplate(nativeType) != null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw TEMPLATE_ALREADY_EXISTS.createWithContext(reader, nativeType);
        }
        return nativeType;
    }
}
