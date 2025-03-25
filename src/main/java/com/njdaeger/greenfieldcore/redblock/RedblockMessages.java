package com.njdaeger.greenfieldcore.redblock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class RedblockMessages {

    public static final TextComponent MODULE = moduleMessage("RedBlock");

    // Error messages
    public static final BiFunction<String, Integer, String> ERROR_REDBLOCK_NOT_FOUND_RADIUS = (type, radius) -> "No " + type + " RedBlock found within a radius of " + radius + " blocks.";
    public static final BiFunction<String, String, String> ERROR_REDBLOCK_NOT_FOUND_ID = (type, id) -> "No " + type + " RedBlock found with id #" + id + ".";
    public static final String ERROR_EDIT_ASSIGN_UNASSIGN = "You cannot assign and unassign a RedBlock simultaneously.";
    public static final String ERROR_EDIT_RANK_UNRANK = "You cannot rank and unrank a RedBlock simultaneously.";
    public static final String ERROR_GOTO_ID_MINE = "You cannot use -id and -mine flags simultaneously.";
    public static final Function<Integer, String> ERROR_GOTO_NO_REDBLOCK_FOUND = (radius) -> "No incomplete RedBlock found" + (radius > 0 ? " in a " + radius + " block radius of you" : "") + " matching the given criteria.";
    public static final TextComponent ERROR_NO_RESULTS_TO_DISPLAY = Component.text("There are no results to display.", NamedTextColor.RED);


    // All other messages
    public static final Function<Integer, TextComponent> REDBLOCK_CREATED = id -> MODULE.append(Component.text("Created RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_DENIED = id -> MODULE.append(Component.text("Denied RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_APPROVED = id -> MODULE.append(Component.text("Approved RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_COMPLETED = id -> MODULE.append(Component.text("Completed RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_DELETED = id -> MODULE.append(Component.text("Deleted RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_EDITED = id -> MODULE.append(Component.text("Edited RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_GOTO = id -> MODULE.append(Component.text("Teleported to RedBlock #" + id, NamedTextColor.GRAY));
    public static final Function<Integer, TextComponent> REDBLOCK_JOIN_NOTIFICATION = count -> MODULE.append(Component.text("You have ", NamedTextColor.GRAY))
            .append(Component.text(count, NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" RedBlocks assigned to you. ", NamedTextColor.GRAY))
            .append(Component.text("\n[Click this to view them]", NamedTextColor.LIGHT_PURPLE, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/rbl -mine -incomplete")));

    // Redblock sign messages
    public static final TextComponent[] SIGN_CLICK_THIS_IF_COMPLETED = new TextComponent[] {
            null,
            Component.text("[CLICK THIS]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD, TextDecoration.UNDERLINED),
            Component.text("[IF COMPLETED]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD, TextDecoration.UNDERLINED),
            null
    };
    public static final TextComponent[] SIGN_CLICK_TO_APPROVE_OR_DENY = new TextComponent[] {
            Component.text("Left click this to", NamedTextColor.GRAY),
            Component.text("[APPROVE]", NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED),
            Component.text("Right click this to", NamedTextColor.GRAY),
            Component.text("[DENY]", NamedTextColor.RED, TextDecoration.BOLD, TextDecoration.UNDERLINED)
    };
}
