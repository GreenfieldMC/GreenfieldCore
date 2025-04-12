package net.greenfieldmc.core.templates.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ViewScaleArgument extends AbstractStringTypedArgument<Double> {

    private static final DynamicCommandExceptionType INVALID_SCALE = new DynamicCommandExceptionType(o -> () -> "Invalid view scale: " + o);

    @Override
    public List<Double> listBasicSuggestions(ICommandContext commandContext) {
        return List.of(0.0625, 1.0);
    }

    @Override
    public String convertToNative(Double aDouble) {
        if (aDouble == 0.0625) return "mini";
        else if (aDouble == 1.0) return "full";
        else return String.valueOf(aDouble);
    }

    @Override
    public Double convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        if (nativeType.equalsIgnoreCase("mini")) return 0.0625;
        else if (nativeType.equalsIgnoreCase("full")) return 1.0;
        else {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw INVALID_SCALE.createWithContext(reader, nativeType);
        }
    }
}
