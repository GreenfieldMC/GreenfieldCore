package net.greenfieldmc.core.signmanager.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.signmanager.SignFlag;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SignFlagArgument extends AbstractStringTypedArgument<SignFlag> {

    private static final DynamicCommandExceptionType INVALID_FLAG = new DynamicCommandExceptionType(o -> () -> "Invalid sign flag '" + o.toString() + "'. Valid flags: construction, road, rail, transit, municipal");

    @Override
    public List<SignFlag> listBasicSuggestions(ICommandContext commandContext) {
        return Arrays.asList(SignFlag.values());
    }

    @Override
    public String convertToNative(SignFlag flag) {
        return flag.name().toLowerCase();
    }

    @Override
    public SignFlag convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var flag = SignFlag.fromString(nativeType);
        if (flag == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw INVALID_FLAG.createWithContext(reader, nativeType);
        }
        return flag;
    }
}

