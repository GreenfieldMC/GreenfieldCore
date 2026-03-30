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

public class SignGroupNameArgument extends AbstractStringTypedArgument<String> {

    private static final DynamicCommandExceptionType GROUP_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Sign group '" + o.toString() + "' not found");

    private final ISignManagerService signManagerService;

    public SignGroupNameArgument(ISignManagerService signManagerService) {
        this.signManagerService = signManagerService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        return signManagerService.getGroupNames();
    }

    @Override
    public String convertToNative(String groupName) {
        return groupName.toLowerCase();
    }

    @Override
    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var groups = signManagerService.getGroupNames();
        var match = groups.stream().filter(g -> g.equalsIgnoreCase(nativeType)).findFirst().orElse(null);
        if (match == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw GROUP_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return match;
    }
}

