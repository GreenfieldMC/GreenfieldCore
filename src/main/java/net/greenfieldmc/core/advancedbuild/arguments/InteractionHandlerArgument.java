package net.greenfieldmc.core.advancedbuild.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.advancedbuild.services.IAdvBuildService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InteractionHandlerArgument extends AbstractStringTypedArgument<InteractionHandler> {

    private static final DynamicCommandExceptionType INTERACTION_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Interaction handler " + o.toString() + " not found");

    private final IAdvBuildService advBuildService;

    public InteractionHandlerArgument(IAdvBuildService advBuildService) {
        this.advBuildService = advBuildService;
    }

    @Override
    public List<InteractionHandler> listBasicSuggestions(ICommandContext commandContext) {
        return advBuildService.getInteractionHandlers();
    }

    @Override
    public String convertToNative(InteractionHandler interactionHandler) {
        return interactionHandler.getInteractionName();
    }

    @Override
    public InteractionHandler convertToCustom(CommandSender sender, String nativeType, StringReader reader) throws CommandSyntaxException {
        var interaction = advBuildService.getInteractionHandlers().stream().filter(i -> i.getInteractionName().equalsIgnoreCase(nativeType)).findFirst();
        if (interaction.isEmpty()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw INTERACTION_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return interaction.get();
    }
}
