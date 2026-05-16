package net.greenfieldmc.core.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractDelimitedTypedArgument;
import net.greenfieldmc.core.shared.services.IVaultService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoleListArgument extends AbstractDelimitedTypedArgument<String> {

    private static final DynamicCommandExceptionType ROLE_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Role " + o.toString() + " not found");

    private final IVaultService vaultService;

    public RoleListArgument(IVaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Override
    public Message getDefaultTooltipMessage() {
        return () -> "What role should this redblock be assigned to? (Separate multiple roles with a comma)";
    }

    @Override
    public String convertToNativeSingle(String s) {
        return s;
    }

    @Override
    public String convertSingleToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var role = nativeType.trim();
        if (vaultService == null) return role;
        if (vaultService.getGroupList().stream().noneMatch(r -> r.equalsIgnoreCase(role))) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw ROLE_NOT_FOUND.createWithContext(reader, role);
        }
        return role;
    }

    @Override
    public List<String> listBasicDelimitedSuggestions(ICommandContext commandContext) {
        if (vaultService == null) return List.of();
        return vaultService.getGroupList();
    }
}
