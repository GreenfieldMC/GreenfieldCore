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

    private final ITestResultService testResultService;

    public AttemptingUserArgument(ITestResultService testResultService) {
        super();
        this.testResultService = testResultService;
    }

    @Override
    public List<UUID> listBasicSuggestions(ICommandContext commandContext) {
        return testResultService.getPendingAttemptSets().stream().map(TestSet::getUuid).toList();
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
                if (testResultService.getPendingAttemptSets().stream().noneMatch(ts -> ts.getUuid().equals(userId))) {
                    reader.setCursor(reader.getCursor() - nativeType.length());
                    throw USER_HAS_NO_INCOMPLETE_ATTEMPTS.createWithContext(reader, nativeType);
                }
                return userId;
            }
        }
        reader.setCursor(reader.getCursor() - nativeType.length());
        throw USER_NOT_FOUND.createWithContext(reader, nativeType);
    }

}
