package net.greenfieldmc.core.signmanager.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.signmanager.services.ISignManagerService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignEntryNameArgument extends AbstractStringTypedArgument<String> {

    private static final DynamicCommandExceptionType ENTRY_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Sign or group '" + o.toString() + "' not found");

    private final ISignManagerService signManagerService;

    public SignEntryNameArgument(ISignManagerService signManagerService) {
        this.signManagerService = signManagerService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        return signManagerService.getAllEntryNames();
    }

    @Override
    public String convertToNative(String entryName) {
        return entryName.toLowerCase();
    }

    @Override
    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var names = signManagerService.getAllEntryNames();
        var match = names.stream().filter(n -> n.equalsIgnoreCase(nativeType)).findFirst().orElse(null);
        if (match == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw ENTRY_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return match;
    }
}
