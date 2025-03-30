package net.greenfieldmc.core.chatformat;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.chatformat.services.ChatConfigServiceImpl;
import net.greenfieldmc.core.chatformat.services.ChatFormatCommandService;
import net.greenfieldmc.core.chatformat.services.DefaultChatService;
import net.greenfieldmc.core.chatformat.services.EssentialsChatService;
import net.greenfieldmc.core.chatformat.services.IChatConfigService;
import net.greenfieldmc.core.chatformat.unitconversions.ConverterManager;
import net.greenfieldmc.core.chatformat.unitconversions.IUnit;
import net.greenfieldmc.core.chatformat.unitconversions.IUnitConverter;
import com.njdaeger.pdk.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.CompactNumberFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ChatFormatModule extends Module {

    private static final NumberFormat compactFormat = CompactNumberFormat.getInstance();

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
                conversions.forEach((u, d) -> {
                    var formatted = compactFormat.format(d);
                    if (formatted.length() > 15) return;
                    hover.appendNewline().append(Component.text(formatted + " " + u.getName(), NamedTextColor.GRAY));
                });
                return hover.build();
            }))
            .build();

    private IChatConfigService config;

    public ChatFormatModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        this.config = enableIntegration(new ChatConfigServiceImpl(plugin, this), true);
        if (enableIntegration(new EssentialsChatService(plugin, this, config), false).isEnabled()) getLogger().info("Using EssentialsChat integration for chat formatting.");
        else if (enableIntegration(new DefaultChatService(plugin, this, config), true).isEnabled()) getLogger().info("Using DefaultChat integration for chat formatting.");
        enableIntegration(new ChatFormatCommandService(plugin, this, config), true);
    }

    @Override
    public void tryDisable() {
        disableIntegration(config);
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
        var skipChars = 0;
        for (int i = 0; i < chars.length; i++) {
            if (skipChars > 0) {
                skipChars--;
                continue;
            }
            if (linkIndices.containsKey(i) && !ignoreExtras) {
                if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                current = new StringBuilder();
                var link = linkIndices.get(i);//.replaceAll("§", "&");
                boolean isMarkdown = link.startsWith("[");
                if (!isMarkdown) {
                    base.append(Component.text(link, linkStyle.apply(link)));
                } else {
                    var linkText = link.substring(1, link.indexOf("]("));
                    var linkUrl = link.substring(link.indexOf("](") + 2, link.length() - 1);
                    base.append(Component.text(linkText, linkStyle.apply(linkUrl)));
                }
                skipChars += link.length() - 1;
                base.append();
            }
            if (mentionIndices.containsKey(i) && !ignoreExtras) {
                if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                current = new StringBuilder();
                var player = mentionIndices.get(i).getSecond();
                var mention = mentionIndices.get(i).getFirst();
                var displayName = player.getDisplayName().replaceAll("§.|&.", "");
                base.append(Component.text('@' + displayName, mentionStyle.apply(displayName)));
                skipChars += mention.length();
                base.append();
            }
            if (unitIndices.containsKey(i) && !ignoreExtras) {
                if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                current = new StringBuilder();
                var unit = unitIndices.get(i).getFirst();
                var conversions = unitIndices.get(i).getSecond();
                base.append(Component.text(unit, unitStyle.apply(conversions)));
                skipChars += unit.length() - 1;
                base.append();
            }
            if (chars[i] == '&' && i + 1 < chars.length) {
                char next = chars[i + 1];
                if (next == '#') {
                    if (i + 7 < chars.length) {
                        if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                        var color = TextColor.fromHexString(new String(chars, i + 1, 7));
                        if (color != null) {
                            currentStyle = currentStyle.color(color);
                            current = new StringBuilder();
                            skipChars += 7;
                        }
                    }
                } else if (isBukkitColorCode(next)) {
                    if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                    current = new StringBuilder();
                    currentStyle = currentStyle.color(fromColorCode(next));
                    skipChars += 1;
                } else {
                    if (!current.isEmpty()) base.append(Component.text(current.toString(), currentStyle));
                    current = new StringBuilder();
                    boolean wasFormat = true;
                    switch (next) {
                        case 'r', 'R' -> currentStyle = Style.empty();
                        case 'l', 'L' -> currentStyle = currentStyle.decorate(TextDecoration.BOLD);
                        case 'm', 'M' -> currentStyle = currentStyle.decorate(TextDecoration.STRIKETHROUGH);
                        case 'n', 'N' -> currentStyle = currentStyle.decorate(TextDecoration.UNDERLINED);
                        case 'o', 'O' -> currentStyle = currentStyle.decorate(TextDecoration.ITALIC);
                        case 'k', 'K' -> currentStyle = currentStyle.decorate(TextDecoration.OBFUSCATED);
                        default -> wasFormat = false;
                    }
                    if (wasFormat) skipChars += 1;
                }
            }
            if (skipChars == 0) current.append(chars[i]);
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
