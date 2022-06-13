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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.njdaeger.greenfieldcore.redblock.RedblockUtils.getNearestRedblock;
import static org.bukkit.ChatColor.*;

public class RedblockCommands {

    private final RedblockModule module;
    private final RedblockStorage storage;
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);

    public RedblockCommands(RedblockModule module, RedblockStorage storage, GreenfieldCore plugin) {
        this.module = module;
        this.storage = storage;

        //redblock create command
        CommandBuilder.of("rbcreate", "rbc")
                .description("Create a new redblock")
                .usage("/rbc <content>")
                .permissions("greenfieldcore.redblock.create")
                .min(1)
                .executor(this::create)
                .flag(new UUIDFlag("The user to assign this redblock to", "-assign <playerName>", "assign"))
                .flag(new RankFlag())
                .register(plugin);

        //redblock approve command
        CommandBuilder.of("rbapprove", "rba")
                .description("Approve a pending redblock")
                .usage("/rba")
                .permissions("greenfieldcore.redblock.approve")
                .max(0)
                .flag(new IdFlag(storage, Redblock::isPending))
                .executor(this::approve)
                .register(plugin);

        //redblock deny command
        CommandBuilder.of("rbdeny", "rbd")
                .description("Deny a pending redblock")
                .usage("/rbd")
                .permissions("greenfieldcore.redblock.deny.self", "greenfieldcore.redblock.deny.others")
                .max(0)
                .flag(new IdFlag(storage, Redblock::isPending))
                .executor(this::deny)
                .register(plugin);

        //redblock list command
        CommandBuilder.of("rblist", "rbl", "redblocks")
                .description("List all redblocks")
                .usage("/rbl")
                .permissions("greenfieldcore.redblock.list")
                .max(0)
                .executor(this::list)
                .flag(new PageFlag())
                .flag(new UUIDFlag("Filter who the redblock was approved by", "-approvedBy <playerName>", "approvedBy"))
                .flag(new UUIDFlag("Filter who the redblock was assigned to", "-assignedTo <playerName>", "assignedTo"))
                .flag(new UUIDFlag("Filter who the redblock was completed by", "-completedBy <playerName>", "completedBy"))
                .flag(new UUIDFlag("Filter who the redblock was created by", "-createdBy <playerName>", "createdBy"))
                .flag(new MaxRadiusFlag())
                .flag(new OptionalFlag("Show deleted redblocks", "-deleted", "deleted"))
                .flag(new OptionalFlag("Show pending redblocks", "-pending", "pending"))
                .flag(new OptionalFlag("Show approved redblocks", "-approved", "approved"))
                .flag(new OptionalFlag("Show incomplete redblocks", "-incomplete", "incomplete"))
                .flag(new OptionalFlag("Show redblocks assigned to you", "-mine", "mine"))
                .register(plugin);

        //redblock delete command
        CommandBuilder.of("rbdelete", "rbr", "rbremove", "rbrem", "rbdel")
                .description("Delete a pending or incomplete redblock")
                .usage("/rbd")
                .permissions("greenfieldcore.redblock.delete")
                .max(0)
                .flag(new IdFlag(storage, rb -> rb.isPending() || rb.isIncomplete()))
                .executor(this::delete)
                .register(plugin);

        //redblock edit command
        CommandBuilder.of("rbedit", "rbe")
                .description("Edit an incomplete redblock")
                .usage("/rbe [content]")
                .permissions("greenfieldcore.redblock.edit")
                .executor(this::edit)
                .flag(new UUIDFlag("The user to assign this redblock to", "-assign <playerName>", "assign"))
                .flag(new RankFlag())
                .flag(new IdFlag(storage, Redblock::isIncomplete))
                .flag(new OptionalFlag("Unassign the redblock from a user", "-unassign", "unassign"))
                .flag(new OptionalFlag("Unrank the redblock from a rank", "-unrank", "unrank"))
                .register(plugin);

        //redblock goto command
        CommandBuilder.of("rbtp", "rbgoto")
                .description("Teleport to a redblock")
                .usage("/rbtp")
                .permissions("greenfieldcore.redblock.goto")
                .max(0)
                .flag(new IdFlag(storage, rb -> true))
                .flag(new MaxRadiusFlag())
                .flag(new OptionalFlag("Go to the nearest redblock assigned to you", "-mine", "mine"))
                .executor(this::goTo)
                .register(plugin);

        //redblock complete command
        CommandBuilder.of("rbcomplete", "rbdone")
                .description("Complete an incomplete redblock")
                .usage("/rbcomplete")
                .permissions("greenfieldcore.redblock.complete")
                .max(0)
                .flag(new IdFlag(storage, Redblock::isIncomplete))
                .executor(this::complete)
                .register(plugin);
    }

    // /rbcreate <content...>
    // flags: [-assign <player>] [-rank <rank>] [-id <id>]
    private void create(CommandContext context) throws PDKCommandException {
        UUID assignedTo = context.getFlag("assign");
        String minRank = context.getFlag("rank");

        var rb = storage.createRedblock(context.joinArgs(), context.asPlayer(), context.getLocation().getBlock().getLocation(), assignedTo, minRank);
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock created with id " + rb.getId());
    }

    // /rbdelete
    // flags: [-id <id>]
    private void delete(CommandContext context) throws PDKCommandException {
        Redblock rb = context.hasFlag("id") ? context.getFlag("id") : getNearestRedblock(storage.getRedblocksFiltered(rdb -> rdb.isPending() || rdb.isIncomplete()), context.getLocation(), 10);

        if (rb == null) context.error(RED + "No pending or incomplete redblock found in a 10 block radius with the given ID.");

        storage.deleteRedblock(rb);
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " has been deleted.");
    }

    // /rbedit [content...]
    // flags: [-assign <player>] [-rank <rank>] [-unassign] [-unrank]
    private void edit(CommandContext context) throws PDKCommandException {
        Redblock rb = context.hasFlag("id") ? context.getFlag("id") : getNearestRedblock(storage.getRedblocksFiltered(Redblock::isIncomplete), context.getLocation(), 10);

        if (rb == null) context.error(RED + "No incomplete redblock found in a 10 block radius of you matching the given ID.");

        boolean unassgin = context.hasFlag("unassign");
        boolean unrank = context.hasFlag("unrank");
        UUID assignedTo = context.getFlag("assign");
        String minRank = context.getFlag("rank");

        if (unassgin && assignedTo != null) context.error(RED + "You cannot assign and unassign at the same time.");
        if (unrank && minRank != null) context.error(RED + "You cannot rank and unrank at the same time.");

        if (assignedTo == null && !unassgin) assignedTo = rb.getAssignedTo();
        if (minRank == null && !unrank) minRank = rb.getMinRank();

        storage.editRedblock(rb, context.joinArgs(), assignedTo, minRank);
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " has been edited.");
    }

    // /rbapprove
    // flags: [-id <id>]
    private void approve(CommandContext context) throws PDKCommandException {
        Redblock rb = context.hasFlag("id") ? context.getFlag("id") : getNearestRedblock(storage.getRedblocksFiltered(Redblock::isPending), context.getLocation(), 10);

        if (rb == null) context.error(RED + "No pending redblock found in a 10 block radius of you matching the given ID.");

        storage.approveRedblock(rb, context.asPlayer());
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " has been approved.");
    }

    // /rbcomplete
    // flags: [-id <id>]
    private void complete(CommandContext context) throws PDKCommandException {
        Redblock rb = context.hasFlag("id") ? context.getFlag("id") : getNearestRedblock(storage.getRedblocksFiltered(Redblock::isIncomplete), context.getLocation(), 10);

        if (rb == null) context.error(RED + "No incomplete redblock found in a 10 block radius of you matching the given ID.");

        storage.completeRedblock(rb, context.asPlayer());
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " is now pending approval.");
    }

    // /rbdeny
    // flags: [-id <id>]
    private void deny(CommandContext context) throws PDKCommandException {
        Redblock rb = context.hasFlag("id") ? context.getFlag("id") : getNearestRedblock(storage.getRedblocksFiltered(Redblock::isPending), context.getLocation(), 10);

        if (rb == null) context.error(RED + "No pending redblock found in a 10 block radius of you matching the given ID.");

        if (!context.hasPermission("greenfieldcore.redblock.deny.others") && !rb.getCompletedBy().equals(context.asPlayer().getUniqueId())) {
            context.error(RED + "You do not have permission to deny redblocks completed by other players.");
        }

        storage.denyRedblock(rb);
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Redblock #" + rb.getId() + " has been denied.");
    }

    // /rbtp
    // flags: [-id <id>] [-mine] [-radius <radius>]
    private void goTo(CommandContext context) throws PDKCommandException {
        if (context.hasFlag("id") && context.hasFlag("mine")) context.error(RED + "You cannot use both the -id and -mine flags at the same time.");

        int radius = context.hasFlag("radius") ? context.getFlag("radius") : context.hasFlag("mine") ? -1 : 100;

        //if we have the ID flag, we get the value of the flag and use that redblock
        //otherwise, we need to check if we have the mine flag and if we do, we use the nearest redblock assigned to us with a max raidus of -1
        //otherwise, we use the nearest redblock with a max raidus of 100

        Redblock rb;
        if (context.hasFlag("id")) rb = context.getFlag("id");
        else rb = context.hasFlag("mine")
                ? getNearestRedblock(storage.getRedblocksFiltered(rdb -> rdb.isIncomplete() && rdb.getAssignedTo() != null && rdb.getAssignedTo().equals(context.asPlayer().getUniqueId())), context.getLocation(), radius)
                : getNearestRedblock(storage.getIncompleteRedblocks(), context.getLocation(), radius);

        if (rb == null) context.error(RED + "No incomplete redblock found" + (radius > 0 ? " in a " + radius + " block radius of you" : "") + " matching the given criteria.");

        Essentials.getPlugin(Essentials.class).getUser(context.asPlayer().getUniqueId()).setLastLocation(context.getLocation());
        context.asPlayer().teleport(rb.getLocation().clone().add(.5,0,1.5));
        context.send(LIGHT_PURPLE + "[Redblock] " + GRAY + "Successfully teleported to redblock #" + rb.getId() + ".");

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

        //status filter flags
        var deleted = context.hasFlag("deleted");
        var incomplete = context.hasFlag("incomplete");
        var pending = context.hasFlag("pending");
        var approved = context.hasFlag("approved");

        //player filter flags
        UUID assignedTo = context.hasFlag("mine") ? context.asPlayer().getUniqueId() : context.getFlag("assignedTo");
        UUID createdBy = context.getFlag("createdBy");
        UUID completedBy = context.getFlag("completedBy");
        UUID approvedBy = context.getFlag("approvedBy");

        int radius = context.hasFlag("radius") ? context.getFlag("radius") : -1;

        int page = context.hasFlag("page") ? context.getFlag("page") : 1;

        var filtered = storage.getRedblocks().stream()
                //sort redblocks by closest to player to furthest from player
                .sorted(Comparator.comparingDouble(h ->
                        h.getLocation().distanceSquared(context.getLocation())))
                .filter(rb -> radius == -1 || rb.getLocation().distance(context.getLocation()) <= radius)
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
            if (!deleted && !incomplete && !pending && !approved) text = text.append("ALL").setColor(BLUE);
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
            if (radius != -1) text = text.append("\nRadius: ").setColor(GRAY).append(String.valueOf(radius)).setColor(BLUE);
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
            header.append("|<--").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, "/rblist " + context.getRawCommandString().replace("-page " + page, "") + " -page " + 1).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to first page").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("<-").setColor(GRAY).clickEvent(Text.ClickAction.RUN_COMMAND, "/rblist " + context.getRawCommandString().replace("-page " + page, "") + " -page " + (page - 1)).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to previous page").setColor(GRAY));
        } else {
            header.append("|<--").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("You are on the first page.").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("<-").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("There are no previous pages.").setColor(GRAY));
        }
        header.append(" ========== ").setColor(GRAY).clearEvents();
        header.append(" [" + String.format("%-4d/%4d", page, maxPage) + "] ").setColor(LIGHT_PURPLE);
        header.append(" =========== ").setColor(GRAY);

        if (page < maxPage) {
            header.append("->").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, "/rblist " + context.getRawCommandString().replace("-page " + page, "") + " -page " + (page + 1)).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to next page").setColor(GRAY));
            header.append(" ").setColor(GRAY).clearEvents();
            header.append("-->|").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, "/rblist " + context.getRawCommandString().replace("-page " + page, "") + " -page " + maxPage).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Go to last page").setColor(GRAY));
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
                    .append("Created by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getCreatedBy()).getName()).setColor(BLUE).append("\n")
                    .append("Created at: ").setColor(GRAY).append(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getCreatedOn())))).setColor(BLUE).append("\n");

            if (redblock.getCompletedBy() != null)
                text.append("Completed by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getCompletedBy()).getName()).setColor(BLUE).append("\n")
                    .append("Completed at: ").setColor(GRAY).append(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getCompletedOn())))).setColor(BLUE).append("\n");
            if (redblock.getApprovedBy() != null)
                text.append("Approved by: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getApprovedBy()).getName()).setColor(BLUE).append("\n")
                    .append("Approved at: ").setColor(GRAY).append(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getApprovedOn())))).setColor(BLUE).append("\n");
            if (redblock.getAssignedTo() != null)
                text.append("Assigned to: ").setColor(GRAY).append(Bukkit.getOfflinePlayer(redblock.getAssignedTo()).getName()).setColor(BLUE).append("\n")
                    .append("Assigned at: ").setColor(GRAY).append(DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(redblock.getAssignedOn())))).setColor(BLUE).append("\n");
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
                .clickEvent(Text.ClickAction.RUN_COMMAND, "/rbtp -id " + redblock.getId())
                .hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Teleport to this redblock").setColor(GRAY));

        rootText.append(" - ").setColor(GRAY).clearFormatting().clearEvents();

        if (MinecraftFont.Font.getWidth(redblock.getContent()) > 200) {
            rootText.append(redblock.getContent().substring(0, getSubstringIndex(200, redblock.getContent())) + "...")
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
