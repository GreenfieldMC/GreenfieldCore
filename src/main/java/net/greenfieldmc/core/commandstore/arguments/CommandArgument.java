package net.greenfieldmc.core.commandstore.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.arguments.defaults.QuotedStringArgument;
import org.bukkit.command.CommandSender;

public class CommandArgument extends QuotedStringArgument {

    private static final DynamicCommandExceptionType MISSING_FORWARD_SLASH = new DynamicCommandExceptionType(o -> () -> "Command must start with a forward slash (/)");

    public CommandArgument() {
        super(false, () -> "The command to execute");
    }

    @Override
    public String convertToCustom(CommandSender sender, String nativeType, StringReader reader) throws CommandSyntaxException {
        var maybeCommand = super.convertToCustom(sender, nativeType, reader);
        if (!maybeCommand.startsWith("/")) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw MISSING_FORWARD_SLASH.createWithContext(reader, nativeType);
        }
        return maybeCommand;
    }
}
