package com.njdaeger.greenfieldcore.commandstore;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Function;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class CommandStoreMessages {

    public static final TextComponent MODULE = moduleMessage("CommandStorage");

    public static final Function<String, String> ERROR_COMMAND_EXECUTION = (error) -> "There was an error executing the command: " + error;
    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);

    public static final TextComponent ADD_COMMAND_SUCCESS = MODULE.append(Component.text("Successfully added command.", NamedTextColor.GRAY));
    public static final TextComponent ADD_COMMAND_CONFIRM = MODULE.append(Component.text("Confirm you wish to add this command.", NamedTextColor.GRAY));
    public static final TextComponent ADD_COMMAND_CONFIRM_CONSOLE = MODULE.append(Component.text("Run the same command again except with '-confirm' at the end of the command to confirm the addition."));
    public static final Function<ICommandContext, TextComponent> ADD_COMMAND_CONFIRM_PLAYER = (ctx) -> MODULE.append(Component.text("Press ", NamedTextColor.GRAY).append(Component.text("[CONFIRM]", NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/scmd " + ctx.joinArgs() + " -confirm"))).append(Component.text(" to confirm the addition of this command. Ignore this message if you do not want to add this command.", NamedTextColor.GRAY)));

    public static final TextComponent REMOVE_COMMAND_SUCCESS = MODULE.append(Component.text("Successfully removed command.", NamedTextColor.GRAY));
    public static final TextComponent REMOVE_COMMAND_CONFIRM = MODULE.append(Component.text("Confirm you wish to remove this command.", NamedTextColor.GRAY));
    public static final TextComponent REMOVE_COMMAND_CONFIRM_CONSOLE = MODULE.append(Component.text("Run the same command again except with '-confirm' at the end of the command to confirm the removal."));
    public static final Function<ICommandContext, TextComponent> REMOVE_COMMAND_CONFIRM_PLAYER = (ctx) -> MODULE.append(Component.text("Press ", NamedTextColor.GRAY).append(Component.text("[CONFIRM]", NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/rcmd " + ctx.joinArgs() + " -confirm"))).append(Component.text(" to confirm the removal of this command. Ignore this message if you do not want to remove this command.", NamedTextColor.GRAY)));

    public static final TextComponent EDIT_COMMAND_SUCCESS = MODULE.append(Component.text("Successfully edited command.", NamedTextColor.GRAY));
    public static final TextComponent EDIT_COMMAND_CONFIRM = MODULE.append(Component.text("Confirm you wish to edit this command.", NamedTextColor.GRAY));
    public static final TextComponent EDIT_COMMAND_CONFIRM_CONSOLE = MODULE.append(Component.text("Run the same command again except with '-confirm' at the end of the command to confirm the edit."));
    public static final Function<ICommandContext, TextComponent> EDIT_COMMAND_CONFIRM_PLAYER = (ctx) -> MODULE.append(Component.text("Press ", NamedTextColor.GRAY).append(Component.text("[CONFIRM]", NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/ecmd " + ctx.joinArgs() + " -confirm"))).append(Component.text(" to confirm the edit of this command. Ignore this message if you do not want to edit this command.", NamedTextColor.GRAY)));
}
