package net.greenfieldmc.core.signmanager.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.services.ISignManagerService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignNameArgument extends AbstractStringTypedArgument<SavedSign> {

    private static final DynamicCommandExceptionType SIGN_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Sign '" + o.toString() + "' not found");

    private final ISignManagerService signManagerService;

    public SignNameArgument(ISignManagerService signManagerService) {
        this.signManagerService = signManagerService;
    }

    @Override
    public List<SavedSign> listBasicSuggestions(ICommandContext commandContext) {
        return signManagerService.getAllSigns();
    }

    @Override
    public String convertToNative(SavedSign sign) {
        return sign.getName().toLowerCase();
    }

    @Override
    public SavedSign convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var sign = signManagerService.getSign(nativeType);
        if (sign == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw SIGN_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return sign;
    }
}

