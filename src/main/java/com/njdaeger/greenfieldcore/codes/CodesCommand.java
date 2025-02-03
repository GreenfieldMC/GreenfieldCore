package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.pdk.command.brigadier.ICommandArgument;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.ICommandRoot;
import com.njdaeger.pdk.command.brigadier.arguments.types.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class CodesCommand {

    private final CodesModule codes;
    private final CodesConfig config;

    public CodesCommand(GreenfieldCore plugin, CodesModule codes) {
        ICommandRoot.of("codes", "buildcodes", "bcodes")
                .description("List or change the server build codes")
                .permission("greenfieldcore.codes")
                .canExecute(this::codes2)
                .then(ICommandArgument.of("add")
                        .then(ICommandArgument.of("newCode", PdkArgumentTypes.greedyString())
                                .canExecute(this::addCode2)))
                .then(ICommandArgument.of("remove")
                        .then(ICommandArgument.of("codeNumber", PdkArgumentTypes.integer())
                                .canExecute(this::removeCode2)))
                .then(ICommandArgument.of("reload")
                        .canExecute(this::reload))
                .then(ICommandArgument.of("page", PdkArgumentTypes.integer())
                        .canExecute(this::codes2))
                .build(plugin).register(plugin);

        this.codes = codes;
        this.config = codes.getConfig();
    }

    private void reload(ICommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.codes.reload")) context.noPermission();
        config.reload();
        context.send(moduleMessage("Codes").append(Component.text("Reloaded Codes config.", NamedTextColor.GRAY)));
    }

    private void codes2(ICommandContext context) throws PDKCommandException {
        if (config.getCodes().isEmpty()) context.error("There are no codes to show.");
        var page = context.getTyped("page", 1);
        codes.getCodes().sendTo(context.getSender(), page);
    }

    private void addCode2(ICommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.codes.add")) context.noPermission();
        var newCode = context.getTyped("newCode");
        config.addCode(newCode);
        var message = Component.text("Added code ")
                .append(Component.text("#" + config.getCodes().size(), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" \"" + newCode + "\".", NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)));
        context.send(moduleMessage("Codes").append(message));
    }

    private void removeCode2(ICommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.codes.remove")) context.noPermission();
        var codeNumber = context.getTyped("codeNumber", Integer.class);
        if (codeNumber == null) context.error("Invalid code number.");

        var code = config.getCode(codeNumber - 1);
        if (code == null) context.error("Please specify a valid code to remove.");

        var message = Component.text("Removed code ")
                .append(Component.text("#" + codeNumber, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(". \"" + code + "\".", NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)));
        config.removeCode(codeNumber - 1);
        context.send(moduleMessage("Codes").append(message));

    }


    /**
     * Tab completion
     */
//    private void completer(TabContext context) {
//        List<String> first = new ArrayList<>();
//        if (context.hasPermission("greenfieldcore.codes.add")) first.add("add");
//        if (context.hasPermission("greenfieldcore.codes.remove")) first.add("remove");
//        if (context.hasPermission("greenfieldcore.codes.reload")) first.add("reload");
//        if (!config.getCodes().isEmpty()) first.addAll(IntStream.range(1, codes.getCodes().getTotalPages() + 1).mapToObj(String::valueOf).toList());
//        context.completionIf(ctx -> ctx.isLength(1), (f) -> first);
//
//        if (context.hasPermission("greenfieldcore.codes.remove") && context.argAt(0).equalsIgnoreCase("remove")) {
//            String code = config.getCode(context.integerAt(1, 0) - 1);
//            if (code == null) {
//                if (context.isPlayer()) ActionBar.of(RED + "Please specify a valid code to remove").sendTo(context.asPlayer());
//                else context.send(RED + "Please specify a valid code to remove");
//            } else {
//                if (context.isPlayer()) ActionBar.of(LIGHT_PURPLE + "[Codes] " + BLACK + "Remove: \"" + (code.length() > 60 ? code.substring(0, 60).concat("...") : code) + "\"?").sendTo(context.asPlayer());
//                else context.send(LIGHT_PURPLE + "[Codes] " + GRAY + "Remove: \"" + code + "\"?");
//            }
//            context.completionAt(1, IntStream.range(1, config.getCodes().size() + 1).mapToObj(String::valueOf).toArray(String[]::new));
//        }
//    }

}
