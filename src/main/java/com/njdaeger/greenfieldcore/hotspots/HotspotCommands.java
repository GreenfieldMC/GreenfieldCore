package com.njdaeger.greenfieldcore.hotspots;

import com.earth2me.essentials.Essentials;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.commandstore.PageFlag;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Text;
import org.bukkit.map.MinecraftFont;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

public class HotspotCommands {

    private final HotspotModule module;
    private final HotspotStorage storage;

    public HotspotCommands(HotspotModule module, HotspotStorage storage, GreenfieldCore plugin) {
        this.storage = storage;
        this.module = module;

        CommandBuilder.of("hotspot", "hotspots", "hs")
                .flag(new PageFlag(module))
                .flag(new IconFlag(module))
                .min(1)
                .description("Hotspot commands.")
                .usage("/hotspot <goto|delete> <name...|id> OR /hotspot create <category> <name...> [customicon] OR /hotspot list [category]")
                .permissions("greenfieldcore.hotspots.goto", "greenfieldcore.hotspots.delete", "greenfieldcore.hotspots.list", "greenfieldcore.hotspots.create")
                .executor(this::hotspotCommand)
                .completer(this::hotspotCompleter)
                .register(plugin);


    }

    private void hotspotCompleter(TabContext context) throws PDKCommandException {
        if (!context.isPlayer()) context.error(RED + "Only players can run the hotspot commands.");
        context.completionAt(0, "goto", "delete", "create", "list");
        if (context.isLength(2) && context.argAt(0).equalsIgnoreCase("goto")) {
            context.completion(storage.getHotspots().values().stream().map(Hotspot::getName).toArray(String[]::new));
        }
        if (context.isLength(2) && context.argAt(0).equalsIgnoreCase("delete")) {
            context.completion(storage.getHotspots().values().stream().map(Hotspot::getName).toArray(String[]::new));
        }
        if (context.isLength(2) && context.argAt(0).equalsIgnoreCase("list")) {
            context.completion(storage.getCategories().keySet().toArray(new String[0]));
        }
        if (context.isLength(2) && context.argAt(0).equalsIgnoreCase("create")) {
            context.completion(storage.getCategories().keySet().toArray(new String[0]));
        }
    }

    private void hotspotCommand(CommandContext context) throws PDKCommandException {
        if (!context.isPlayer()) context.error(RED + "Only players can run the hotspot commands.");
        if (!context.subCommandAt(0, "goto", true, this::goTo) && !context.subCommandAt(0, "create", true, this::create) && !context.subCommandAt(0, "delete", true, this::delete) && !context.subCommandAt(0, "list", true, this::list)) {
            context.error(GRAY + context.argAt(0) + RED + " is not a valid subcommand.");
        }
    }

    // /hotspot goto <name... | id>
    private void goTo(CommandContext context) throws PDKCommandException {
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.hasPermission("greenfieldcore.hotspots.goto")) context.noPermission();
        if (context.isLength(2) && context.isIntegerAt(1)) { //Just assuming it is an ID
            Hotspot hs = storage.getHotspot(context.integerAt(1));
            if (hs == null) context.error(RED + "Unknown hotspot ID: " + GRAY + "#" + context.integerAt(1));
            else {
                Essentials.getPlugin(Essentials.class).getUser(context.asPlayer().getUniqueId()).setLastLocation(context.getLocation());
                context.asPlayer().teleport(hs.getLocation());
                context.send(LIGHT_PURPLE + "[Hotspots] " + GRAY + "Successfully teleported to " + hs.getName());
                return;
            }
        }
        //is name
        List<Hotspot> hotspots = storage.getHotspots(context.joinArgs(1));
        if (hotspots.isEmpty()) context.error(RED + "Unknown hotspot name: " + GRAY + context.joinArgs(1));
        else if (hotspots.size() == 1) {
            Essentials.getPlugin(Essentials.class).getUser(context.asPlayer().getUniqueId()).setLastLocation(context.getLocation());
            context.asPlayer().teleport(hotspots.get(0).getLocation());
            context.send(LIGHT_PURPLE + "[Hotspots] " + GRAY + "Successfully teleported to " + hotspots.get(0).getName());
        } else {
            int page = context.hasFlag("page") ? context.getFlag("page") : 1;
            printHotspotList(0, page, hotspots.stream().sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).collect(Collectors.toList()), context);
        }
    }

    // /hotspot create <category> <name...> [customIcon]
    private void create(CommandContext context) throws PDKCommandException {
        if (context.isLessOrEqual(2)) context.notEnoughArgs();
        if (!context.hasPermission("greenfieldcore.hotspots.create")) context.noPermission();
        Category category = storage.getCategory(context.argAt(1));
        if (category == null) context.error(RED + "Category " + context.argAt(1) + " does not exist.");
        String icon = context.hasFlag("icon") ? context.getFlag("icon") : null;
        Hotspot hs = storage.createHotspot(context.joinArgs(2), category, context.getLocation(), icon);
        module.addHotspot(hs);
        context.send(LIGHT_PURPLE + "[Hotspots] " + GRAY + "Successfully added hotspot.");
    }

    // /hotspot delete <name... | id>
    private void delete(CommandContext context) throws PDKCommandException {
        if (context.isLess(2)) context.notEnoughArgs();
        if (!context.hasPermission("greenfieldcore.hotspots.delete")) context.noPermission();
        if (context.isLength(2) && context.isIntegerAt(1)) {
            Hotspot hs = storage.getHotspot(context.integerAt(1));
            if (hs == null) context.error(RED + "Unknown hotspot ID: " + GRAY + "#" + context.integerAt(1));
            else {
                module.deleteHotspot(hs);
                storage.deleteHotspot(context.integerAt(1));
                context.send(LIGHT_PURPLE + "[Hotspots] " + GRAY + "Successfully deleted " + hs.getName());
                return;
            }
        }
        List<Hotspot> hotspots = storage.getHotspots(context.joinArgs(1));
        if (hotspots.isEmpty()) context.error(RED + "Unknown hotspot name: " + GRAY + context.joinArgs(1));
        else if (hotspots.size() == 1) {
            module.deleteHotspot(hotspots.get(0));
            storage.deleteHotspot(hotspots.get(0).getId());
            context.send(LIGHT_PURPLE + "[Hotspots] " + GRAY + "Successfully deleted " + hotspots.get(0).getName());
        } else {
            int page = context.hasFlag("page") ? context.getFlag("page") : 1;
            printHotspotList(-1, page, hotspots.stream().sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).collect(Collectors.toList()), context);
        }
    }

    // /hotspot list <category>
    private void list(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.hotspots.list")) context.noPermission();
        Category category = storage.getCategory(context.joinArgs(1));
        List<Hotspot> hotspots;
        int page = context.hasFlag("page") ? context.getFlag("page") : 1;
        if (category != null) hotspots = storage.getHotspots().values().stream().filter(h -> h.getCategory().equals(category)).sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).collect(Collectors.toList());
        else hotspots = storage.getHotspots().values().stream().sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).collect(Collectors.toList());
        printHotspotList(1, page, hotspots, context);
    }

    private void printHotspotList(int mode, int page, List<Hotspot> hotspots, CommandContext context) throws PDKCommandException {

        int maxPage = (int) Math.ceil(hotspots.size()/8.);
        if (page < 1 || maxPage < page) context.error(RED + "There are no more pages to display.");
        hotspots = hotspots.stream().skip((page - 1) * 8).limit(8).collect(Collectors.toList());

        Text.TextSection text = Text.of("========= ").setColor(GRAY)
                .append("Hotspots").setColor(LIGHT_PURPLE)
                .append(" --- ").setColor(GRAY);

        if (mode == -1) { //Delete mode
            text.append("Delete Options").setColor(LIGHT_PURPLE).append(" ===============");
            for (Hotspot hs : hotspots) {
                text.append("\n[D]").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Delete this Hotspot").setColor(GRAY)).clickEvent(Text.ClickAction.RUN_COMMAND, "/hotspot delete " + hs.getId()).setBold(true);
                appendHotspotText(context, text, hs);
            }
        }
        else if (mode == 0) { //Teleport mode
            text.append("Teleport Options").setColor(LIGHT_PURPLE).append(" ===============");
            for (Hotspot hs : hotspots) {
                text.append("\n[T]").setColor(GREEN).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Teleport to this Hotspot").setColor(GRAY)).clickEvent(Text.ClickAction.RUN_COMMAND, "/hotspot goto " + hs.getId()).setBold(true);
                appendHotspotText(context, text, hs);
            }
        } else { //List mode
            text.append("Hotspot List").setColor(LIGHT_PURPLE).append(" ===============");
            for (Hotspot hs : hotspots) {
                text.append("\n[D]").setColor(RED).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Delete this Hotspot").setColor(GRAY)).clickEvent(Text.ClickAction.RUN_COMMAND, "/hotspot delete " + hs.getId()).setBold(true);
                text.append("[T]").setColor(GREEN).hoverEvent(Text.HoverAction.SHOW_TEXT, Text.of("Teleport to this Hotspot").setColor(GRAY)).clickEvent(Text.ClickAction.RUN_COMMAND, "/hotspot goto " + hs.getId()).setBold(true);
                appendHotspotText(context, text, hs);
            }
        }

        text.append("\n========= ").setColor(GRAY);

        String command;
        if (mode == -1) command = "/hotspot delete " + context.joinArgs(1) + " -page ";
        else if (mode == 0) command = "/hotspot goto " + context.joinArgs(1) + " -page ";
        else command = "/hotspot list " + context.joinArgs(1) + " -page ";

        if (page > 1) text.append("|<--").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, command + 1).append(" ").append("<-").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, command + (page - 1));
        else text.append("|<--").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, command + 0).append(" ").append("<-").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, command + 0);

        text.append(" ==== ").setColor(GRAY);
        text.append(" [" + String.format("%-4d/%4d", page, maxPage) + "] ").setColor(LIGHT_PURPLE);
        text.append(" ==== ").setColor(GRAY);

        if (maxPage <= page) text.append("->").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, command + 0).append(" ").append("-->|").setColor(RED).clickEvent(Text.ClickAction.RUN_COMMAND, command + 0);
        else text.append("->").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, command + (page + 1)).append(" ").append("-->|").setColor(LIGHT_PURPLE).clickEvent(Text.ClickAction.RUN_COMMAND, command + maxPage);

        text.append(" =========");
        Text.sendTo(text, context.asPlayer());
    }

    private void appendHotspotText(CommandContext context, Text.TextSection text, Hotspot hs) {
        text.append(" | ");

        if (MinecraftFont.Font.getWidth(hs.getName()) > 250) text.append(hs.getName().substring(0, getSubstringIndex(250, hs.getName())) + "...").setColor(GRAY).hoverEvent(Text.HoverAction.SHOW_TEXT,
                Text.of("Distance: " + String.format("%.2f", context.asPlayer().getLocation().distance(hs.getLocation()))).setColor(GRAY)
                        .append("\nCategory: " + hs.getCategory().getId()).setColor(GRAY)
                        .append("\nMarker: " + (hs.getCustomMarker() == null ? hs.getCategory().getMarker() : hs.getCustomMarker()))
                        .append("\nID: #" + hs.getId()).setColor(GRAY)
        );
        else text.append(hs.getName()).setColor(GRAY).hoverEvent(Text.HoverAction.SHOW_TEXT,
                Text.of("Distance: " + String.format("%.2f", context.asPlayer().getLocation().distance(hs.getLocation()))).setColor(GRAY)
                        .append("\nCategory: " + hs.getCategory().getId()).setColor(GRAY)
                        .append("\nMarker: " + (hs.getCustomMarker() == null ? hs.getCategory().getMarker() : hs.getCustomMarker()))
                        .append("\nID: #" + hs.getId()).setColor(GRAY)
        );
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
