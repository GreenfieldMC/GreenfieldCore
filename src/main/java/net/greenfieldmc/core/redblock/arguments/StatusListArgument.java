package net.greenfieldmc.core.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractDelimitedTypedArgument;
import net.greenfieldmc.core.redblock.RedblockConstants;
import org.bukkit.command.CommandSender;
import com.njdaeger.pdk.serviceprovider.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A comma separated list of redblock states to search for: any of the {@link RedblockConstants#STATUSES}, plus
 * {@link #DELETED}. Input is matched ignoring case; statuses are returned as their {@link RedblockConstants} names.
 */
public class StatusListArgument extends AbstractDelimitedTypedArgument<String> {

    /**
     * Not a status: selects deleted redblocks, whatever their status.
     */
    public static final String DELETED = "deleted";

    private static final List<String> VALUES = Stream.concat(RedblockConstants.STATUSES.stream(), Stream.of(DELETED)).toList();
    private static final List<String> SUGGESTIONS = VALUES.stream().map(String::toLowerCase).toList();
    private static final DynamicCommandExceptionType STATUS_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Unknown status " + o.toString() + ". Expected one of " + String.join(", ", SUGGESTIONS));

    @Override
    public Message getDefaultTooltipMessage() {
        return () -> "Which statuses should be listed? (Separate multiple statuses with a comma)";
    }

    @Override
    public String convertToNativeSingle(String s) {
        return s;
    }

    @Override
    public String convertSingleToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var input = nativeType.trim();
        var match = VALUES.stream().filter(v -> v.equalsIgnoreCase(input)).findFirst();
        if (match.isEmpty()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw STATUS_NOT_FOUND.createWithContext(reader, input);
        }
        return match.get();
    }

    @Override
    public List<String> listBasicDelimitedSuggestions(ICommandContext commandContext) {
        return new ArrayList<>(SUGGESTIONS);
    }
}
