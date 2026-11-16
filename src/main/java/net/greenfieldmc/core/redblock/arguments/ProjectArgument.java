package net.greenfieldmc.core.redblock.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import com.njdaeger.pdk.utils.Pair;
import net.greenfieldmc.core.greenfieldapi.models.Result;
import net.greenfieldmc.core.redblock.services.IRedblockConfigService;
import net.greenfieldmc.core.redblock.services.IRedblockServiceV2;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProjectArgument extends AbstractStringTypedArgument<World> {

    private static final DynamicCommandExceptionType PROJECT_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Project " + o.toString() + " not found");

    private final IRedblockConfigService configService;
    private final IRedblockServiceV2 redblockService;

    public ProjectArgument(IRedblockConfigService configService, IRedblockServiceV2 redblockService) {
        this.configService = configService;
        this.redblockService = redblockService;
    }


    @Override
    public CompletableFuture<Map<World, Message>> listSuggestionsAsync(ICommandContext commandContext) {
        return redblockService.getProjects()
                .thenApply(Result::getData)
                .thenApply(projects -> projects
                        .stream()
                        .map(project -> Pair.of(configService.getWorld(project.getProjectKey()), (Message) project::getProjectName))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }

    @Override
    public String convertToNative(World world) {
        return configService.getProjectKey(world);
    }

    @Override
    public World convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
        return Optional.of(configService.getWorld(nativeType)).orElseThrow(() -> {
            reader.setCursor(reader.getCursor() - nativeType.length());
            return PROJECT_NOT_FOUND.create(nativeType);
        });
    }

    //
//    @Override
//    public String convertToCustom(@Nullable CommandSender source, String nativeType, StringReader reader) throws CommandSyntaxException {
//        var found = redblockService.getProjects().join().getData().stream()
//                .filter(project -> project.getProjectKey().equalsIgnoreCase(nativeType))
//                .findFirst();
//        if (found.isEmpty()) {
//            reader.setCursor(reader.getCursor() - nativeType.length());
//            throw PROJECT_NOT_FOUND.create(nativeType);
//        }
//        return found.get().getProjectKey();
//    }
}
