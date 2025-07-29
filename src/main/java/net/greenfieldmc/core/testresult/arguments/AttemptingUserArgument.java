package net.greenfieldmc.core.testresult.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import net.greenfieldmc.core.Util;
import net.greenfieldmc.core.testresult.TestSet;
import net.greenfieldmc.core.testresult.services.ITestResultService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class AttemptingUserArgument extends AbstractStringTypedArgument<UUID> {

    private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "User " + o.toString() + " not found");
    private static final DynamicCommandExceptionType USER_HAS_NO_INCOMPLETE_ATTEMPTS = new DynamicCommandExceptionType(o -> () -> "User " + o.toString() + " has no incomplete attempts");
    private static final DynamicCommandExceptionType USER_HAS_NO_ATTEMPTS = new DynamicCommandExceptionType(o -> () -> "User " + o.toString() + " has no attempts");

    private final ITestResultService testResultService;
    private final ArgumentMode argumentMode;

    public AttemptingUserArgument(ITestResultService testResultService, ArgumentMode argumentMode) {
        super();
        this.testResultService = testResultService;
        this.argumentMode = argumentMode;
    }

    @Override
    public List<UUID> listBasicSuggestions(ICommandContext commandContext) {
        if (argumentMode == ArgumentMode.INCOMPLETE_ATTEMPTS)
            return testResultService.getPendingAttemptSets().stream().map(TestSet::getUuid).toList();
        else return testResultService.getAllAttemptSets().stream().map(TestSet::getUuid).toList();
    }

    @Override
    public String convertToNative(UUID uuid) {
        return Util.userNameMap.get(uuid);
    }

    @Override
    public UUID convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        if (nativeType == null || nativeType.isBlank()) throw USER_NOT_FOUND.createWithContext(reader, nativeType);
        for (var entry : Util.userNameMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(nativeType)) {
                var userId = entry.getKey();
                if (argumentMode == ArgumentMode.ANY_ATTEMPTS && testResultService.getAllAttemptSets().stream().noneMatch(ts -> ts.getUuid().equals(userId))) {
                    reader.setCursor(reader.getCursor() - nativeType.length());
                    throw USER_HAS_NO_ATTEMPTS.createWithContext(reader, nativeType);
                }
                if (argumentMode == ArgumentMode.INCOMPLETE_ATTEMPTS && testResultService.getPendingAttemptSets().stream().noneMatch(ts -> ts.getUuid().equals(userId))) {
                    reader.setCursor(reader.getCursor() - nativeType.length());
                    throw USER_HAS_NO_INCOMPLETE_ATTEMPTS.createWithContext(reader, nativeType);
                }
                return userId;
            }
        }
        reader.setCursor(reader.getCursor() - nativeType.length());
        throw USER_NOT_FOUND.createWithContext(reader, nativeType);
    }

    public enum ArgumentMode {
        INCOMPLETE_ATTEMPTS,
        ANY_ATTEMPTS
    }

}
