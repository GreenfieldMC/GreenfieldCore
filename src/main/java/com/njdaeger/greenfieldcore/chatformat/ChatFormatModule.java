package com.njdaeger.greenfieldcore.chatformat;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.chatformat.unitconversions.ConverterManager;
import com.njdaeger.greenfieldcore.chatformat.unitconversions.IUnit;
import com.njdaeger.greenfieldcore.chatformat.unitconversions.IUnitConverter;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.utils.Pair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class ChatFormatModule extends Module implements Listener {

    private static final DecimalFormat format = new DecimalFormat("#.##");

    private static final Pattern linkFormat = Pattern.compile("\\[[^]]*]\\((https?://\\S+)\\)|\\b((?i)https?://\\S+)");
    private static final Function<String, Style> linkStyle = (link) -> Style.style()
                    .color(TextColor.color(199, 233, 255))
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.openUrl(link))
                    .hoverEvent(HoverEvent.showText(Component.text(link, NamedTextColor.GRAY)))
                    .build();
    private static final Function<String, Style> mentionStyle = (player) -> Style.style()
            .color(TextColor.fromHexString("#FF00E6"))
            .decorate(TextDecoration.ITALIC)
            .clickEvent(ClickEvent.runCommand("/tp " + player))
            .hoverEvent(HoverEvent.showText(Component.text("Click to teleport", NamedTextColor.GRAY)))
            .build();
    private static final Function<Map<IUnit, Double>, Style> unitStyle = (conversions) -> Style.style()
            .color(TextColor.color(199, 233, 255))
            .hoverEvent(HoverEvent.showText(() -> {
                var hover = Component.text("Conversions:").toBuilder();
                conversions.forEach((u, d) -> hover.appendNewline().append(Component.text(format.format(d) + " " + u.getName(), NamedTextColor.GRAY)));
                return hover.build();
            }))
            .build();


    private ChatConfig config;

    public ChatFormatModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = new ChatConfig(plugin);

        CommandBuilder.of("togglepings", "togglementions")
                .description("Enables the ping sounds when you are mentioned in chat.")
                .permission("greenfieldcore.chat.toggle-mentions")
                .canExecute(ctx -> {
                    var player = ctx.asPlayer();
                    config.setAllowMentions(player, !config.allowsMentions(player));
                    ctx.send(moduleMessage("Chat").append(Component.text(config.allowsMentions(player) ? "Enabled Chat Pings." : "Disabled Chat Pings.", NamedTextColor.GRAY)));
                })
                .then("forUser", PdkArgumentTypes.player()).permission("greenfieldcore.chat.toggle-mentions.others").executes(ctx -> {
                    var player = ctx.getTyped("forUser", Player.class);
                    config.setAllowMentions(player, !config.allowsMentions(player));
                    ctx.send(moduleMessage("Chat").append(Component.text(config.allowsMentions(player) ? "Enabled Chat Pings for " + player.getName() : "Disabled Chat Pings for " + player.getName(), NamedTextColor.GRAY)));
                })
                .register(plugin);

        CommandBuilder.of("pings", "mentions")
                .description("Set ping settings for chat pings.")
                .permission("greenfieldcore.chat.mention")
                .then("volume").then("volumeLevel", PdkArgumentTypes.floatArg(0.1f, 2.0f)).executes(ctx -> {
                    var vol = ctx.getTyped("volumeLevel", Float.class);
                    config.setVolume(ctx.asPlayer(), vol);
                    ctx.send(moduleMessage("Chat").append(Component.text("Your ping volume has been set to " + vol, NamedTextColor.GRAY)));
                }).end()
                .then("sound").then("soundChoice", PdkArgumentTypes.enumArg(Sound.class)).executes(ctx -> {
                    var sound = ctx.getTyped("soundChoice", Sound.class);
                    config.setSound(ctx.asPlayer(), sound);
                    ctx.send(moduleMessage("Chat").append(Component.text("Your ping sound has been set to " + sound.getKey().getKey(), NamedTextColor.GRAY)));
                }).end()
                .register(plugin);
    }

    @Override
    public void onDisable() {
        config.save();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        var combined = event.getFormat().replace("%1$s", event.getPlayer().getDisplayName().replace('&','§')).replace("%2$s", event.getMessage().replace('&','§'));
        var formattedMessage = formatString(combined, false);
        if (event.getPlayer().hasPermission("greenfieldcore.chat.mention")) {
            var mentions = getMentionIndices(event.getMessage());
            mentions.values().stream().map(Pair::getSecond).filter(config::allowsMentions).forEach(player -> player.playSound(player.getLocation(), config.getSound(player), config.getVolume(player), 1));
        }
        var recipients = new ArrayList<Audience>(event.getRecipients());
        recipients.add(Bukkit.getConsoleSender());
        recipients.forEach(p -> p.sendMessage(formattedMessage));
        event.setCancelled(true);
    }

    public static Map<Integer, String> getLinkIndices(String str) {
       var indices = new HashMap<Integer, String>();
        var matcher = linkFormat.matcher(str);
        while (matcher.find()) {
            indices.put(matcher.start(), matcher.group());
        }
        return indices;
    }

    public static Map<Integer, Pair<String, Player>> getMentionIndices(String str) {
        var indices = new HashMap<Integer, Pair<String, Player>>();
        var matcher = Pattern.compile("@(\\w+)").matcher(str);
        while (matcher.find()) {

            var match = matcher.group(1);

            var player = Bukkit.getPlayerExact(match);
            if (player == null) player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getDisplayName().equalsIgnoreCase(match)).findFirst().orElse(null);
            if (player == null) player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().toLowerCase().contains(match.toLowerCase())).findFirst().orElse(null);
            if (player == null) player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getDisplayName().toLowerCase().contains(match.toLowerCase())).findFirst().orElse(null);
            if (player != null) indices.put(matcher.start(), Pair.of(match, player));
        }
        return indices;
    }

    public static Map<Integer, Pair<String, Map<IUnit, Double>>> getUnitConversionIndices(String str) {
        var indices = new HashMap<Integer, Pair<String, Map<IUnit, Double>>>();

        var converters = ConverterManager.getConverters();

        for (IUnitConverter<?> converter : converters) {
            for (IUnit unit : converter.getUnits()) {
                var pattern = unit.getPattern();
                var matcher = pattern.matcher(str);
                while (matcher.find()) {
                    var match = matcher.group();
                    var value = matcher.group(1);
                    if (value == null || value.isBlank() || match == null || match.isBlank()) continue;
                    try {
                        double parsedValue = Double.parseDouble(value);
                        var conversions = converter.getConversions(parsedValue, unit);
                        if (conversions != null && !conversions.isEmpty()) indices.put(matcher.start(), Pair.of(match, conversions));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return indices;
    }

    public static TextComponent formatString(String message, boolean ignoreExtras) {
        var base = Component.text();

        var current = new StringBuilder();
        var chars = message.toCharArray();

        var linkIndices = getLinkIndices(message);
        var mentionIndices = getMentionIndices(message);
        var unitIndices = getUnitConversionIndices(message);

        var currentStyle = Style.empty();
        for (int i = 0; i < chars.length; i++) {
            if (linkIndices.containsKey(i) && !ignoreExtras) {
                if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                current = new StringBuilder();
                var link = linkIndices.get(i).replaceAll("§", "&");
                boolean isMarkdown = link.startsWith("[");
                if (!isMarkdown) {
                    base.append(Component.text(link, linkStyle.apply(link)));
                } else {
                    var linkText = link.substring(1, link.indexOf("]("));
                    var linkUrl = link.substring(link.indexOf("](") + 2, link.length() - 1);
                    base.append(Component.text(linkText, linkStyle.apply(linkUrl)));
                }
                i += link.length();
                base.append();
                if (chars.length <= i) break;
            }
            if (mentionIndices.containsKey(i) && !ignoreExtras) {
                if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                current = new StringBuilder();
                var player = mentionIndices.get(i).getSecond();
                var mention = mentionIndices.get(i).getFirst();
                var displayName = player.getDisplayName().replaceAll("§.", "");
                base.append(Component.text('@' + displayName, mentionStyle.apply(displayName)));
                i += mention.length() + 1;
                base.append();
                if (chars.length <= i) break;
            }
            if (unitIndices.containsKey(i) && !ignoreExtras) {
                if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                current = new StringBuilder();
                var unit = unitIndices.get(i).getFirst();
                var conversions = unitIndices.get(i).getSecond();
                base.append(Component.text(unit, unitStyle.apply(conversions)));
                i += unit.length();
                base.append();
                if (chars.length <= i) break;
            }
            if (chars[i] == '§' && i + 1 < chars.length) {
                char next = chars[i + 1];
                if (next == '#') {
                    if (i + 7 < chars.length) {
                        if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                        currentStyle = Style.style(TextColor.fromHexString(new String(chars, i + 1, 7)));
                        current = new StringBuilder();
                        i += 8;
                    }
                } else if (isBukkitColorCode(next)) {
                    if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                    current = new StringBuilder();
                    currentStyle = Style.style(fromColorCode(next));
                    i += 2;
                } else {
                    if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                    current = new StringBuilder();
                    switch (next) {
                        case 'r', 'R' -> currentStyle = Style.empty();
                        case 'l', 'L' -> currentStyle = currentStyle.decorate(TextDecoration.BOLD);
                        case 'm', 'M' -> currentStyle = currentStyle.decorate(TextDecoration.STRIKETHROUGH);
                        case 'n', 'N' -> currentStyle = currentStyle.decorate(TextDecoration.UNDERLINED);
                        case 'o', 'O' -> currentStyle = currentStyle.decorate(TextDecoration.ITALIC);
                        case 'k', 'K' -> currentStyle = currentStyle.decorate(TextDecoration.OBFUSCATED);
                    }
                    i += 2;
                }

            }
            if (i < chars.length) current.append(chars[i]);
        }
        if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
        return base.build();
    }

    private static NamedTextColor fromColorCode(char c) {
        return switch (c) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a', 'A' -> NamedTextColor.GREEN;
            case 'b', 'B' -> NamedTextColor.AQUA;
            case 'c', 'C' -> NamedTextColor.RED;
            case 'd', 'D' -> NamedTextColor.LIGHT_PURPLE;
            case 'e', 'E' -> NamedTextColor.YELLOW;
            default -> NamedTextColor.WHITE;
        };
    }

    private static boolean isBukkitColorCode(char c) {
        return "0123456789abcdef".indexOf(c) != -1;
    }

}
