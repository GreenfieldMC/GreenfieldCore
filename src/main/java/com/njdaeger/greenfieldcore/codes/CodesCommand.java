package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.stream.IntStream;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class CodesCommand {

    private final CodesModule codes;
    private final CodesConfig config;

    public CodesCommand(GreenfieldCore plugin, CodesModule codes) {
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
                    .then("pageNumber", PdkArgumentTypes.integer(ctx -> IntStream.rangeClosed(1, (int)Math.ceil(codes.getConfig().getCodes().size() / 8.0)).boxed().toList(), () -> "What page would you like to view?")).executes().end()
                .register(plugin);


        this.codes = codes;
        this.config = codes.getConfig();
    }

    private void reload(ICommandContext context) {
        config.reload();
        context.send(moduleMessage("Codes").append(Component.text("Reloaded Codes config.", NamedTextColor.GRAY)));
    }

    private void codes(ICommandContext context) throws PDKCommandException {
        if (config.getCodes().isEmpty()) context.error("There are no codes to show.");
        int page = context.getTyped("pageNumber", 1);
        codes.getCodes().sendTo(context, page);
    }

    private void addCode(ICommandContext context) {
        var newCode = context.getTyped("newCode", String.class);
        config.addCode(newCode);
        var message = Component.text("Added code ", NamedTextColor.GRAY)
                .append(Component.text("#" + config.getCodes().size(), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" \"" + newCode + "\".", NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)));
        context.send(moduleMessage("Codes").append(message));
    }

    private void removeCode(ICommandContext context) throws PDKCommandException {
        int codeNumber = context.getTyped("codeNumber", Integer.class);

        var code = config.getCode(codeNumber - 1);
        if (code == null) context.error("Please specify a valid code to remove.");

        var message = Component.text("Removed code ")
                .append(Component.text("#" + codeNumber, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(". \"" + code + "\".", NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)));
        config.removeCode(codeNumber - 1);
        context.send(moduleMessage("Codes").append(message));
    }

}
