package net.greenfieldmc.core.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractDelimitedTypedArgument;
import net.greenfieldmc.core.greenfieldapi.models.users.GfUser;
import net.greenfieldmc.core.shared.services.IUserService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class UserListArgument extends AbstractDelimitedTypedArgument<GfUser> {

    private static final DynamicCommandExceptionType USER_SERVICE_NOT_INITIALIZED = new DynamicCommandExceptionType(o -> () -> "User service is not initialized.");
    private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "User " + o.toString() + " not found");

    private final IUserService userService;

    public UserListArgument(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public String convertToNativeSingle(GfUser thing) {
        return thing.getUsername();
    }

    @Override
    public GfUser convertSingleToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        var username = nativeType.trim();
        if (userService == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw USER_SERVICE_NOT_INITIALIZED.createWithContext(reader, nativeType);
        }
        var potentialUser = userService.getUserByUsername(username);
        if (potentialUser == null) {

            reader.setCursor(reader.getCursor() - nativeType.length());
            throw USER_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return potentialUser;
    }

    @Override
    public Map<GfUser, Message> listDelimitedSuggestions(ICommandContext commandContext) {
            var users = userService.getUsers();
            return users.stream().collect(java.util.stream.Collectors.toMap(user -> user, user -> user.getMinecraftUuid()::toString));
    }
}
