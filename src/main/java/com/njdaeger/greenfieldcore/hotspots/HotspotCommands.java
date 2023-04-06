package com.njdaeger.greenfieldcore.hotspots;

import com.earth2me.essentials.Essentials;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.commandstore.PageFlag;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import org.bukkit.map.MinecraftFont;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.njdaeger.greenfieldcore.Util.getSubstringIndex;
import static org.bukkit.ChatColor.*;

public class HotspotCommands {

    private final HotspotModule module;
    private final HotspotStorage storage;
    private final ChatPaginator<Hotspot, CommandContext> paginator;

    public HotspotCommands(HotspotModule module, HotspotStorage storage, GreenfieldCore plugin) {
        this.storage = storage;
        this.module = module;

        this.paginator = ChatPaginator.builder(this::lineGenerator)
                .addComponent((ctx, paginator, results, pg) -> {
                    if (ctx.first().equalsIgnoreCase("delete")) return Text.of("Delete Options").setColor(LIGHT_PURPLE);
                    else if (ctx.first().equalsIgnoreCase("teleport")) return Text.of("Teleport Options").setColor(LIGHT_PURPLE);
                    else return Text.of("Hotspot List").setColor(LIGHT_PURPLE);
                }, ComponentPosition.TOP_CENTER)
                .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/hs " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/hs " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/hs " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/hs " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .build();

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
            paginator.generatePage(context, hotspots.stream().sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).toList(), page).sendTo(Text.of("Page does not exist.").setColor(RED), context.asPlayer());
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
            paginator.generatePage(context, hotspots.stream().sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).toList(), page).sendTo(Text.of("Page does not exist.").setColor(RED), context.asPlayer());
        }
    }

    // /hotspot list <category>
    private void list(CommandContext context) throws PDKCommandException {
        if (!context.hasPermission("greenfieldcore.hotspots.list")) context.noPermission();
        Category category = storage.getCategory(context.joinArgs(1));
        List<Hotspot> hotspots;
        var senderWorldUid = context.asPlayer().getWorld().getUID();
        int page = context.hasFlag("page") ? context.getFlag("page") : 1;
        if (category != null) hotspots = storage.getHotspots().values().stream().filter(h -> h.getCategory().equals(category) && h.getLocation().getWorld().getUID().equals(senderWorldUid)).sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).collect(Collectors.toList());
        else hotspots = storage.getHotspots().values().stream().filter(hs -> hs.getLocation().getWorld().getUID().equals(senderWorldUid)).sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).collect(Collectors.toList());
        paginator.generatePage(context, hotspots.stream().sorted(Comparator.comparingDouble(h -> h.getLocation().distance(context.getLocation()))).toList(), page).sendTo(Text.of("Page does not exist.").setColor(RED), context.asPlayer());
    }

    private Text.Section lineGenerator(Hotspot hs, CommandContext ctx) {
        int mode = 0;
        if (ctx.first().equalsIgnoreCase("delete")) mode = -1;
        else if (ctx.first().equalsIgnoreCase("teleport")) mode = 1;

        var deleteButton = Text.of("[D]")
                .setBold(true)
                .setColor(RED)
                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Delete this Hotspot").setColor(GRAY))
                .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/hotspot delete " + hs.getId()));

        var teleportButton = Text.of("[T]")
                .setBold(true)
                .setColor(GREEN)
                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Teleport to this Hotspot").setColor(GRAY))
                .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/hotspot goto " + hs.getId()));

        var big = MinecraftFont.Font.getWidth(hs.getName()) > 200;
        var name = big ? hs.getName().substring(0, getSubstringIndex(200, hs.getName())) : hs.getName();

        var text = Text.of(" | ").setColor(GRAY);

        if (mode == -1) text = deleteButton.appendRoot(text);
        else if (mode == 1) text = teleportButton.appendRoot(text);
        else {
            if (ctx.hasPermission("greenfieldcore.hotspots.goto")) text = teleportButton.appendRoot(text);
            if (ctx.hasPermission("greenfieldcore.hotspots.delete")) text = deleteButton.appendRoot(text);
        }

        text.appendRoot(name)
                .setColor(GRAY)
                .setHoverEvent(HoverAction.SHOW_TEXT,
                        Text.of("Distance: " + String.format("%.2f", ctx.asPlayer().getLocation().distance(hs.getLocation()))).setColor(GRAY)
                                .append("\nCategory: " + hs.getCategory().getId())
                                .append("\nMarker: " + (hs.getCustomMarker() == null ? hs.getCategory().getMarker() : hs.getCustomMarker()))
                                .append("\nID: #" + hs.getId()));

        if (big) text.appendRoot("...").setColor(GRAY).setBold(true).setHoverEvent(HoverAction.SHOW_TEXT, Text.of(hs.getName()).setColor(GRAY));

        return text;
    }
}
