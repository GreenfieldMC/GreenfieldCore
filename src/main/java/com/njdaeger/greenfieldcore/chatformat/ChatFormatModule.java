package com.njdaeger.greenfieldcore.chatformat;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.LIGHT_PURPLE;

public class ChatFormatModule extends Module implements Listener {

    private static final Pattern linkFormat = Pattern.compile("\\[[^]]*]\\((https?://\\S+)\\)|\\b((?i)https?://\\S+)");
    private ChatConfig config;

    public ChatFormatModule(GreenfieldCore plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = new ChatConfig(plugin);

        CommandBuilder.of("togglepings", "togglementions")
                .executor(context -> {
                    if (context.hasArgAt(0) && context.hasPermission("greenfieldcore.chat.toggle-mentions.others")) {
                        var player = Bukkit.getPlayer(context.argAt(0));
                        if (player == null) {
                            context.send(LIGHT_PURPLE + "[Chat] " + GRAY + "That player is not online.");
                            return;
                        }
                        config.setAllowMentions(player, !config.allowsMentions(player));
                        context.send(LIGHT_PURPLE + "[Chat] " + GRAY + (config.allowsMentions(player) ? "Enabled Chat Pings for " + player.getName() : "Disabled Chat Pings for " + player.getName()));
                        return;
                    }
                    config.setAllowMentions(context.asPlayer(), !config.allowsMentions(context.asPlayer()));
                    context.send(LIGHT_PURPLE + "[Chat] " + GRAY + (config.allowsMentions(context.asPlayer()) ? "Enabled Chat Pings." : "Disabled Chat Pings."));
                })
                .completer((ctx) -> {
                    if (ctx.hasPermission("greenfieldcore.chat.toggle-mentions.others")) ctx.playerCompletionAt(0);
                })
                .permissions("greenfieldcore.chat.toggle-mentions", "greenfieldcore.chat.toggle-mentions.others")
                .max(0)
                .usage("/togglepings")
                .description("Enables the ping sounds when you are mentioned in chat.")
                .build().register(plugin);

        CommandBuilder.of("pings", "mentions")
                .executor(ctx -> {
                    if (ctx.first().equalsIgnoreCase("volume")) {
                        var vol = ctx.floatAt(1, 0.5f);
                        config.setVolume(ctx.asPlayer(), vol);
                        ctx.send(LIGHT_PURPLE + "[Chat] " + GRAY + "Your ping volume has been set to " + vol);
                    } else if (ctx.first().equalsIgnoreCase("sound")) {
                        var sound = Stream.of(Sound.values()).filter(s -> s.getKey().getKey().equalsIgnoreCase(ctx.argAt(1))).findFirst().orElse(Sound.BLOCK_NOTE_BLOCK_PLING);
                        config.setSound(ctx.asPlayer(), sound);
                        ctx.send(LIGHT_PURPLE + "[Chat] " + GRAY + "Your ping sound has been set to " + sound.getKey().getKey());
                    } else {
                        ctx.error("Incorrect command usage. /pings <volume|sound> <value>");
                    }
                })
                .completer((ctx) -> {
                    ctx.completionAt(0, "volume", "sound");
                    if (ctx.first().equalsIgnoreCase("volume")) ctx.completionAt(1, "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "2.0");
                    if (ctx.first().equalsIgnoreCase("sound")) ctx.completionAt(1, Stream.of(Sound.values()).map(Sound::getKey).map(NamespacedKey::getKey).toArray(String[]::new));
                })
                .permissions("greenfieldcore.chat.mention")
                .min(2)
                .max(2)
                .usage("/pings")
                .description("Set ping settings for chat pings.")
                .build().register(plugin);
    }

    @Override
    public void onDisable() {
        config.save();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        var combined = event.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage());
        var finalText = formatString(combined);
        if (event.getPlayer().hasPermission("greenfieldcore.chat.mention")) {
            var mentions = getMentionIndices(combined);
            mentions.values().stream().map(Pair::getSecond).filter(config::allowsMentions).forEach(player -> player.playSound(player.getLocation(), config.getSound(player), config.getVolume(player), 1));
        }
        var recipients = new ArrayList<CommandSender>(event.getRecipients());
        recipients.add(Bukkit.getConsoleSender());
        finalText.sendTo(recipients.toArray(new CommandSender[0]));
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

    public static Text.Section formatString(String word) {
        var base = Text.of("");
        StringBuilder current = new StringBuilder();
        char[] chars = word.toCharArray();
        var linkIndices = getLinkIndices(word);
        var mentionIndices = getMentionIndices(word);
        boolean hit = false;
        boolean lastProcessedNonColor = false;
        boolean firstColorOrFormat = true;
        for (int i = 0; i < chars.length; i++) {
            if (linkIndices.containsKey(i)) {
                var link = linkIndices.get(i).replaceAll("§", "&");
                boolean isMarkdown = link.startsWith("[");
                if (!isMarkdown) {
                    base = base.append(link)
                            .clearFormatting()
                            .setColor(199, 233, 255)
                            .setUnderlined(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(link).setColor(ChatColor.GRAY))
                            .setClickEvent(ClickAction.OPEN_URL, ClickString.of(link)).appendParent("");
                } else {
                    var linkText = link.substring(1, link.indexOf("]("));
                    var linkUrl = link.substring(link.indexOf("](") + 2, link.length() - 1);
                    base = base.append(linkText)
                            .clearFormatting()
                            .setColor(199, 233, 255)
                            .setUnderlined(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(linkUrl).setColor(ChatColor.GRAY))
                            .setClickEvent(ClickAction.OPEN_URL, ClickString.of(linkUrl)).appendParent("");
                }
                i += link.length();
                if (chars.length <= i) break;
                lastProcessedNonColor = true;
            }
            if (lastProcessedNonColor) {
                lastProcessedNonColor = false;
                current = new StringBuilder();
            }

            if (mentionIndices.containsKey(i)) {
                var player = mentionIndices.get(i).getSecond();
                var mention = mentionIndices.get(i).getFirst();
                var displayName = player.getDisplayName().replaceAll("§.", "");
                base = base.append("@" + displayName)
                        .clearFormatting()
                        .setColor(255, 0, 230)
                        .setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to teleport").setColor(ChatColor.GRAY))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/tp " + player.getName())).appendParent("");
                i += mention.length() + 1;
                if (chars.length <= i) break;
                lastProcessedNonColor = true;
            }

            if (lastProcessedNonColor) {
                lastProcessedNonColor = false;
                current = new StringBuilder();
            }

            char c = chars[i];
            if (c == '§') {
                if (firstColorOrFormat) {
                    base = base.append("");
                    current = new StringBuilder();
                    firstColorOrFormat = false;
                }
                if (i + 1 < chars.length) {
                    if (hit) {
                        base = base.append("");
                        current = new StringBuilder();
                    }
                    hit = true;
                    char next = chars[i + 1];
                    if (next == 'x') {
                        if (i + 8 < chars.length) {
                            base.setColor(Color.fromRGB(Integer.parseInt(new String(chars, i + 2, 12).replace("§", ""), 16)));
                            i += 13;
                        }
                    } else if (isBukkitColorCode(next)) {
                        var color = ChatColor.getByChar(next).asBungee().getColor();
                        base.setColor(color.getRed(), color.getGreen(), color.getBlue());
                        i++;
                    } else {
                        switch (next) {
                            case 'r', 'R' -> base = base.appendRoot("");
                            case 'l', 'L' -> base.setBold(true);
                            case 'm', 'M' -> base.setStrikethrough(true);
                            case 'n', 'N' -> base.setUnderlined(true);
                            case 'o', 'O' -> base.setItalic(true);
                            case 'k', 'K' -> base.setObfuscated(true);
                            default -> current.append(c);
                        }
                        i++;
                    }
                }
            } else current.append(c);
            if (current.length() > 0) {
                base = base.setText(current.toString());
            }
        }
        return base;
    }

    private static boolean isBukkitColorCode(char c) {
        return "0123456789abcdef".indexOf(c) != -1;
    }

}
