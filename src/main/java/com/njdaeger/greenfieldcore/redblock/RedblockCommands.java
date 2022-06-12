package com.njdaeger.greenfieldcore.redblock;

import com.earth2me.essentials.Essentials;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.redblock.flags.*;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.OptionalFlag;
import com.njdaeger.pdk.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.map.MinecraftFont;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.njdaeger.greenfieldcore.redblock.RedblockUtils.getNearestRedblock;
import static org.bukkit.ChatColor.*;

public class RedblockCommands {

    private final RedblockModule module;
    private final RedblockStorage storage;

    public RedblockCommands(RedblockModule module, RedblockStorage storage, GreenfieldCore plugin) {
        this.module = module;
        this.storage = storage;

        CommandBuilder.of("redblock", "rb", "rblock")
                .usage("/redblock <delete|edit|approve|complete|deny> <id> " +
                        "OR /redblock create <content...> " +
                        "OR /redblock get [id] " +
                        "OR /redblock list")
                .description("Redblock commands")
                .min(1)
                .permissions(
                        "greenfieldcore.redblock.create",
                        "greenfieldcore.redblock.delete",
                        "greenfieldcore.redblock.edit",
                        "greenfieldcore.redblock.approve",
                        "greenfieldcore.redblock.complete",
                        "greenfieldcore.redblock.deny",
                        "greenfieldcore.redblock.goto",
                        "greenfieldcore.redblock.list")
                .executor(this::redblockCommand)
                .completer(this::redblockCompleter)
                .flag(new AssignFlag())
                .flag(new RankFlag())
                .flag(new PageFlag())
                .flag(new ApprovedByFlag())
                .flag(new AssignedToFlag())
                .flag(new CompletedByFlag())
                .flag(new CreatedByFlag())
                .flag(new OptionalFlag((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Show deleted redblocks", "-deleted", "deleted"))
                .flag(new OptionalFlag((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Show incomplete redblocks", "-incomplete", "incomplete"))
                .flag(new OptionalFlag((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Show pending redblocks", "-pending", "pending"))
                .flag(new OptionalFlag((ctx) -> ctx.hasArgAt(0) && ctx.argAt(0).equalsIgnoreCase("list"), "Show approved redblocks", "-approved", "approved"))
                .register(plugin);


    }

    private void redblockCompleter(TabContext context) throws PDKCommandException {
        if (!context.isPlayer()) context.error(RED + "You must be a player to use this command.");
        context.completionAt(0, "create", "delete", "edit", "approve", "complete", "deny", "goto", "list");
        if (context.isLength(2) && context.first().equalsIgnoreCase("edit") || context.first().equalsIgnoreCase("complete")) {
            context.completion(storage.getIncompleteRedblocks().stream()
                    .sorted(Comparator.comparingDouble(h ->
                            h.getLocation().distanceSquared(context.getLocation())))
                    .map(Redblock::getId)
                    .map(String::valueOf)
                    .toArray(String[]::new));
        }
        if (context.isLength(2) && context.first().equalsIgnoreCase("approve") || context.first().equalsIgnoreCase("deny")) {
            //set the completion to an array of all hotspots sorted by the closest to the player running the command
            context.completion(storage.getPendingRedblocks().stream()
                    .sorted(Comparator.comparingDouble(h ->
                            h.getLocation().distanceSquared(context.getLocation())))
                    .map(Redblock::getId)
                    .map(String::valueOf)
                    .toArray(String[]::new));
        }
        if (context.isLength(2) && context.first().equalsIgnoreCase("goto")) {
            context.completion(storage.getAllRedblocks().stream()
                    .filter(rb -> rb.getStatus() == Redblock.Status.PENDING || rb.getStatus() == Redblock.Status.INCOMPLETE)
                    .sorted(Comparator.comparingDouble(h ->
                            h.getLocation().distanceSquared(context.getLocation())))
                    .map(Redblock::getId)
                    .map(String::valueOf)
                    .toArray(String[]::new));

        }
        if (context.isLength(2) && context.first().equalsIgnoreCase("delete")) {
            context.completion(storage.getAllRedblocks().stream()
                    .filter(rb -> rb.getStatus() != Redblock.Status.DELETED)
                    .sorted(Comparator.comparingDouble(h ->
                            h.getLocation().distanceSquared(context.getLocation())))
                    .map(Redblock::getId)
                    .map(String::valueOf)
                    .toArray(String[]::new));
        }
    }

    private void redblockCommand(CommandContext context) throws PDKCommandException {
        if (!context.isPlayer()) context.error(RED + "You must be a player to use this command.");
        if (!context.subCommandAt(0, "create", true, this::create) &&
                !context.subCommandAt(0, "delete", true, this::delete) &&
                !context.subCommandAt(0, "edit", true, this::edit) &&
                !context.subCommandAt(0, "approve", true, this::approve) &&
                !context.subCommandAt(0, "complete", true, this::complete) &&
                !context.subCommandAt(0, "deny", true, this::deny) &&
                !context.subCommandAt(0, "goto", true, this::gotoCmd) &&
                !context.subCommandAt(0, "list", true, this::list)
        ) context.error(GRAY + context.first() + RED + " is not a valid subcommand.");
    }

    // /redblock create <content...>
    // flags: [-a <player>] [-r <rank>]
    private void create(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.create")) context.noPermission();
        if (context.isLess(2)) context.notEnoughArgs();
        UUID assignedTo = context.getFlag("a");
        String minRank = context.getFlag("r");
        var rb = storage.createRedblock(context.joinArgs(1), context.asPlayer(), context.getLocation().getBlock().getLocation(), assignedTo, minRank);
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock created with id " + rb.getId());
    }

    // /redblock delete <id>
    private void delete(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.delete")) context.noPermission();
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.isIntegerAt(1)) context.error(RED + "The id must be an integer.");
        var id = context.integerAt(1);
        var rb = storage.getRedblock(id);
        if (rb == null) context.error(RED + "Unknown redblock ID: " + GRAY + "#" + id);
        else if (rb.getStatus() == Redblock.Status.DELETED) context.error(RED + "You cannot delete a deleted redblock.");
        else {
            storage.deleteRedblock(rb);
            context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " deleted.");
        }
    }

    // /redblock edit <id> <content...>
    // flags: [-a <player>] [-r <rank>]
    private void edit(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.edit")) context.noPermission();
        if (context.isLess(3)) context.notEnoughArgs();
        if (!context.isIntegerAt(1)) context.error(RED + "The id must be an integer.");
        var id = context.integerAt(1);
        var rb = storage.getRedblock(id);
        if (rb == null) context.error(RED + "Unknown redblock ID: " + GRAY + "#" + id);
        else if (rb.getStatus() == Redblock.Status.DELETED) context.error(RED + "You cannot delete a deleted redblock.");
        else {
            UUID assignedTo = context.getFlag("a");
            String minRank = context.getFlag("r");
            storage.editRedblock(rb, context.joinArgs(2), assignedTo, minRank);
            context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " edited.");
        }
    }

    // /redblock approve <id>
    private void approve(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.approve")) context.noPermission();
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.isIntegerAt(1)) context.error(RED + "The id must be an integer.");
        var id = context.integerAt(1);
        var rb = storage.getRedblock(id);
        if (rb == null) context.error(RED + "Unknown redblock ID: " + GRAY + "#" + id);
        else if (rb.getStatus() != Redblock.Status.PENDING) context.error(RED + "You cannot approve a redblock that is not marked as pending.");
        else {
            storage.approveRedblock(rb, context.asPlayer());
            context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " approved.");
        }
    }

    // /redblock complete <id>
    private void complete(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.complete")) context.noPermission();
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.isIntegerAt(1)) context.error(RED + "The id must be an integer.");
        var id = context.integerAt(1);
        var rb = storage.getRedblock(id);
        if (rb == null) context.error(RED + "Unknown redblock ID: " + GRAY + "#" + id);
        else if (rb.getStatus() != Redblock.Status.INCOMPLETE) context.error(RED + "You cannot complete a redblock that is not marked as incomplete.");
        else {
            storage.completeRedblock(rb, context.asPlayer());
            context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " is now pending approval.");
        }
    }

    // /redblock deny <id>
    private void deny(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.deny")) context.noPermission();
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.isIntegerAt(1)) context.error(RED + "The id must be an integer.");
        var id = context.integerAt(1);
        var rb = storage.getRedblock(id);
        if (rb == null) context.error(RED + "Unknown redblock ID: " + GRAY + "#" + id);
        else if (rb.getStatus() != Redblock.Status.PENDING) context.error(RED + "You cannot deny a redblock that is not marked as pending.");
        else {
            storage.denyRedblock(rb);
            context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " denied.");
        }
    }

    private void gotoCmd(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.goto")) context.noPermission();
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.isIntegerAt(1)) context.error(RED + "The id must be an integer.");

        var incomplete = storage.getIncompleteRedblocks();
        if (incomplete.size() == 0) context.error(RED + "There are no incomplete redblocks.");

        var id = context.integerAt(1, -1);
        var rb = id == -1 ? getNearestRedblock(incomplete, context.getLocation()) : storage.getRedblock(id);
        if (rb == null) context.error(RED + "Unknown redblock ID: " + GRAY + "#" + id);

        else if (rb.getStatus() == Redblock.Status.DELETED) context.error(RED + "You cannot get a deleted redblock.");
        else {
            Essentials.getPlugin(Essentials.class).getUser(context.asPlayer().getUniqueId()).setLastLocation(context.getLocation());
            context.asPlayer().teleport(rb.getLocation().clone().add(.5,0,1.5));
            context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Successfully teleported to redblock #" + rb.getId() + ".");
        }
    }

    // /redblock list
    // -deleted
    // -incomplete
    // -pending
    // -approved
    // -assignedTo <player>
    // -createdBy <player>
    // -completedBy <player>
    // -rank <rank>
    // -page <page>
    private void list(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.redblock.list")) context.noPermission();

        //status filter flags
        var deleted = context.hasFlag("deleted");
        var incomplete = context.hasFlag("incomplete");
        var pending = context.hasFlag("pending");
        var approved = context.hasFlag("approved");

        //player filter flags
        UUID assignedTo = context.getFlag("assignedTo");
        UUID createdBy = context.getFlag("createdBy");
        UUID completedBy = context.getFlag("completedBy");
        UUID approvedBy = context.getFlag("approvedBy");

        int page = context.hasFlag("page") ? context.getFlag("page") : 1;

        var filtered = storage.getAllRedblocks().stream()
                //sort redblocks by closest to player to furthest from player
                .sorted(Comparator.comparingDouble(h ->
                        h.getLocation().distanceSquared(context.getLocation())))
                .filter(rb -> {
                    if (!deleted && !incomplete && !pending && !approved) return true;
                    if (deleted && rb.getStatus() == Redblock.Status.DELETED) return true;
                    if (incomplete && rb.getStatus() == Redblock.Status.INCOMPLETE) return true;
                    if (pending && rb.getStatus() == Redblock.Status.PENDING) return true;
                    return approved && rb.getStatus() == Redblock.Status.APPROVED;
                })
                .filter(rb -> assignedTo == null || rb.getAssignedTo() != null && rb.getAssignedTo().equals(assignedTo))
                .filter(rb -> createdBy == null || rb.getCreatedBy() != null && rb.getCreatedBy().equals(createdBy))
                .filter(rb -> completedBy == null || rb.getCompletedBy() != null && rb.getCompletedBy().equals(completedBy))
                .filter(rb -> approvedBy == null || rb.getApprovedBy() != null && rb.getApprovedBy().equals(approvedBy))
                .toList();

        Supplier<Text.TextSection> hoverText = () -> {
            var text = Text.of("Status Shown: ").setColor(GRAY);
            if (!deleted && !incomplete && !pending && !approved) text = text.append("ALL").setColor(GRAY);
            else {
                if (deleted) text = text.append("\n- DELETED").setColor(BLUE);
                if (incomplete) text = text.append("\n- INCOMPLETE").setColor(BLUE);
                if (pending) text = text.append("\n- PENDING").setColor(BLUE);
                if (approved) text = text.append("\n- APPROVED").setColor(BLUE);
            }
            if (assignedTo != null) text = text.append("\nAssigned to: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(assignedTo).getName()).setColor(BLUE);
            if (createdBy != null) text = text.append("\nCreated by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(createdBy).getName()).setColor(BLUE);
            if (completedBy != null) text = text.append("\nCompleted by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(completedBy).getName()).setColor(BLUE);
            if (approvedBy != null) text = text.append("\nApproved by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(approvedBy).getName()).setColor(BLUE);
            return text;
        };

        var textHeader = Text.of("== ").setColor(GRAY)
                .append(String.format("%-5d", filtered.size())).setColor(LIGHT_PURPLE)
                .append(" Matches =======").setColor(GRAY)
                .append(" Redblock List ").setColor(LIGHT_PURPLE)
                .append("======= ").setColor(GRAY)
                .append("Filter").setColor(LIGHT_PURPLE).setUnderlined(true).hoverEvent(Text.HoverAction.SHOW_TEXT, hoverText.get())
                .append(" ==").setColor(GRAY).clearEvents().clearFormatting();

        printRedblocks(page, filtered, context, textHeader);
    }

    private void printRedblocks(int page, List<Redblock> redblocks, CommandContext context, Text.TextSection header) throws PDKCommandException {

        int maxPage = (int) Math.ceil(redblocks.size() / 8.0);
        if (page < 1 || page > maxPage) context.error(RED + "No more results to show.");
        redblocks = redblocks.stream().skip((page - 1) * 8L).limit(8).toList();

        redblocks.forEach(rb -> getRedblockLine(rb, context.getLocation(), header));
        header.append("\n== ").setColor(GRAY);
        if (page > 1) {
            header.append("|<--").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, "/redblock " + context.joinArgs().replace("-page " + page, "") + " -page " + 1).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to first page").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("<-").setColor(GRAY).clickEvent(Text.ClickAction.RUN_COMMAND, "/redblock " + context.joinArgs().replace("-page " + page, "") + " -page " + (page - 1)).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to previous page").setColor(GRAY));
        } else {
            header.append("|<--").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("You are on the first page.").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("<-").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("There are no previous pages.").setColor(GRAY));
        }
        header.append(" ========== ").setColor(GRAY).clearEvents();
        header.append(" [" + String.format("%-4d/%4d", page, maxPage) + "] ").setColor(LIGHT_PURPLE);
        header.append(" =========== ").setColor(GRAY);

        if (page < maxPage) {
            header.append("->").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, "/redblock " + context.joinArgs().replace("-page " + page, "") + " -page " + (page + 1)).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to next page").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("-->|").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, "/redblock " + context.joinArgs().replace("-page " + page, "") + " -page " + maxPage).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to last page").setColor(GRAY));
        } else {
            header.append("->").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("There are no more pages.").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("-->|").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("You are on the last page.").setColor(GRAY));
        }
        header.append(" ==").setColor(GRAY).clearEvents();
        Text.sendTo(header, context.asPlayer());
    }

    private void getRedblockLine(Redblock redblock, Location locationOfSender, Text.TextSection rootText) {
        Supplier<Text.TextSection> hoverText = () -> {
            var text = Text.of("Status: ").setColor(GRAY).append(redblock.getStatus().name()).setColor(BLUE).append("\n")
                    .append("Created by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getCreatedBy()).getName()).setColor(BLUE).append("\n");
            if (redblock.getCompletedBy() != null)
                text.append("Completed by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getCompletedBy()).getName()).setColor(BLUE).append("\n");
            if (redblock.getApprovedBy() != null)
                text.append("Approved by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getApprovedBy()).getName()).setColor(BLUE).append("\n");
            if (redblock.getAssignedTo() != null)
                text.append("Assigned to: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getAssignedTo()).getName()).setColor(BLUE).append("\n");
            if (redblock.getMinRank() != null)
                text.append("Minimum Rank: ").setColor(GRAY).append(redblock.getMinRank()).setColor(BLUE).append("\n");
            text.append("Distance: ").setColor(GRAY).append(String.format("%.2f", locationOfSender.distance(redblock.getLocation()))).setColor(BLUE).append("\n");
            text.append("ID: ").setColor(GRAY).append(redblock.getId() + "").setColor(BLUE);
            return text;
        };

        rootText.append("\n?")
                .setColor(
                    switch (redblock.getStatus()) {
                        case DELETED -> DARK_RED;
                        case INCOMPLETE -> RED;
                        case PENDING -> GOLD;
                        case APPROVED -> GREEN;
                    })
                .setBold(true)
                .hoverEvent(Text.HoverAction.SHOW_TEXT, hoverText.get());

        rootText.append(" ").clearEvents();
        rootText.append("[T]")
                .setColor(BLUE)
                .setBold(true)
                .clickEvent(Text.ClickAction.RUN_COMMAND, "/redblock goto " + redblock.getId())
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Teleport to this redblock").setColor(GRAY));

        rootText.append(" - ").setColor(GRAY).clearFormatting().clearEvents();

        if (MinecraftFont.Font.getWidth(redblock.getContent()) > 250) {
            rootText.append(redblock.getContent().substring(0, getSubstringIndex(250, redblock.getContent())) + "...")
                    .setBold(false)
                    .setColor(GRAY)
                    .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of(redblock.getContent()).setColor(GRAY));
        } else rootText.append(redblock.getContent()).setColor(GRAY).setBold(false).clearEvents();
    }

    private int getSubstringIndex(int maxPixelWidth, String text) {
        int currentWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            if (currentWidth >= maxPixelWidth) return i - 1;
            else currentWidth += MinecraftFont.Font.getChar(text.charAt(i)).getWidth();
        }
        return text.length();
    }
}
