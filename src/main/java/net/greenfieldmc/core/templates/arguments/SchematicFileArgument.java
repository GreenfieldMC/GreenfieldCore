package net.greenfieldmc.core.templates.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.templates.services.ITemplateWorldEditService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class SchematicFileArgument extends AbstractStringTypedArgument<Path> {

    private static final DynamicCommandExceptionType SCHEMATIC_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Schematic " + o.toString() + " not found");

    private final ITemplateWorldEditService worldEditService;

    public SchematicFileArgument(ITemplateWorldEditService worldEditService) {
        this.worldEditService = worldEditService;
    }

    @Override
    public List<Path> listBasicSuggestions(ICommandContext commandContext) {
        return worldEditService.getSchematicFiles();
    }

    @Override
    public String convertToNative(Path path) {
        return path.getFileName().toString();
    }

    @Override
    public Path convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var path = worldEditService.getSchematicFiles().stream()
                .filter(p -> p.getFileName().toString().equalsIgnoreCase(nativeType))
                .findFirst();

        if (path.isEmpty()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw SCHEMATIC_NOT_FOUND.createWithContext(reader, nativeType);
        }

        return path.get();
    }
}
