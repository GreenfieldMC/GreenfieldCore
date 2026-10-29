package net.greenfieldmc.core.advancedbuild.services;

import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.advancedbuild.AdvBuildMessages;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.advancedbuild.arguments.InteractionHandlerArgument;
import net.greenfieldmc.core.advancedbuild.paginators.InteractionPaginator;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AdvBuildCommandService {

    private final ChatPaginator<InteractionHandler, ICommandContext> interactionPaginator = new InteractionPaginator().build();

    private final IAdvBuildService advBuildService;

    public AdvBuildCommandService(IGreenfieldCorePlugin plugin, IAdvBuildService advBuildService) {
        this.advBuildService = advBuildService;

        CommandBuilder.of("advbuild", "avb", "abm")
                .description("The advanced build mode commands.")
                .permission("greenfieldcore.advbuild")
                .then("help").canExecute(this::help)
                    .then("page", PdkArgumentTypes.integer(1, () -> "What page of interaction handlers to view")).executes(this::help)
                .end()
                .then("handler", new InteractionHandlerArgument(advBuildService)).executes(this::handlerInfo)
                .canExecute(this::toggle)
                .register(plugin);
    }

    @SuppressWarnings("DataFlowIssue")
    private void help(ICommandContext ctx) {
        var page = ctx.getTyped("page", Integer.class, 1);
        interactionPaginator.generatePage(ctx, advBuildService.getInteractionHandlers(), page).sendTo(AdvBuildMessages.ERROR_NO_RESULTS_TO_DISPLAY, ctx.getSender());
    }

    private void handlerInfo(ICommandContext ctx) {
        var handler = ctx.getTyped("handler", InteractionHandler.class);
        var message = Component.text(" ======== Handler: ", NamedTextColor.GRAY).toBuilder()
                .append(Component.text("[" + handler.getInteractionName() + "]", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" ========", NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("Description:", NamedTextColor.BLUE, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                .appendSpace()
                .append(handler.getInteractionDescription().color(NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("Usage:", NamedTextColor.BLUE, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                .appendSpace()
                .append(handler.getInteractionUsage().color(NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("Materials:", NamedTextColor.BLUE, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                .appendSpace()
                .append(handler.getMaterialListText().color(NamedTextColor.GRAY))
                .build();
        ctx.send(message);
    }

    private void toggle(ICommandContext ctx) throws PDKCommandException {
        var id = ctx.asPlayer().getUniqueId();
        var wasEnabled = advBuildService.isEnabledFor(id);
        advBuildService.setEnabledFor(id, !wasEnabled);
        ctx.send(!wasEnabled ? AdvBuildMessages.ENABLED_AVB : AdvBuildMessages.DISABLED_AVB);
    }
}
