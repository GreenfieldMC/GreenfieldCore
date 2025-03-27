package com.njdaeger.greenfieldcore.codes.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleService;
import com.njdaeger.greenfieldcore.codes.Code;
import com.njdaeger.greenfieldcore.codes.paginators.CodesPaginator;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.plugin.Plugin;

import java.util.stream.IntStream;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class CodesCommandService extends ModuleService<CodesCommandService> implements IModuleService<CodesCommandService> {

    private final ICodesService codesService;

    private final ChatPaginator<Code, ICommandContext> codePaginator = new CodesPaginator().build();

    public CodesCommandService(Plugin plugin, Module module, ICodesService codesService) {
        super(plugin, module);
        this.codesService = codesService;
    }

    private void reload(ICommandContext context) {
        codesService.reload();
        context.send(moduleMessage("Codes").append(Component.text("Reloaded Codes config.", NamedTextColor.GRAY)));
    }

    private void codes(ICommandContext context) throws PDKCommandException {
        if (codesService.getCodes().isEmpty()) context.error("There are no codes to show.");
        int page = context.getTyped("pageNumber", 1);
        codePaginator.generatePage(context, codesService.getCodes(), page).sendTo(context.getSender());
    }

    private void addCode(ICommandContext context) {
        var newCode = context.getTyped("newCode", String.class);
        codesService.addCode(newCode);
        var message = Component.text("Added code ", NamedTextColor.GRAY)
                .append(Component.text("#" + codesService.getCodes().size(), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" \"" + newCode + "\".", NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)));
        context.send(moduleMessage("Codes").append(message));
    }

    private void removeCode(ICommandContext context) throws PDKCommandException {
        int codeNumber = context.getTyped("codeNumber", Integer.class);

        var code = codesService.getCode(codeNumber);
        if (code == null) context.error("Please specify a valid code to remove.");

        var message = Component.text("Removed code ")
                .append(Component.text("#" + codeNumber, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(". \"" + code + "\".", NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)));
        codesService.removeCode(codeNumber - 1);
        context.send(moduleMessage("Codes").append(message));
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("codes", "buildcodes", "bcodes")
                .description("List or change the server build codes")
                .permission("greenfieldcore.codes")
                .defaultExecutor(this::codes)
                .canExecute()
                .then("add").permission("greenfieldcore.codes.modify")
                    .then("newCode", PdkArgumentTypes.greedyString()).executes(this::addCode).end()
                .then("remove").permission("greenfieldcore.codes.modify")
                    .then("codeNumber", PdkArgumentTypes.integer()).executes(this::removeCode).end()
                .then("reload").permission("greenfieldcore.codes.modify").executes(this::reload)
                .then("page")
                    .then("pageNumber", PdkArgumentTypes.integer(ctx -> IntStream.rangeClosed(1, (int)Math.ceil(codesService.getCodes().size() / 8.0)).boxed().toList(), () -> "What page would you like to view?")).executes().end()
                .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
