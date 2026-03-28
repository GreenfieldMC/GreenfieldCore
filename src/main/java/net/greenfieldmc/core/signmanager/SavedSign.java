package net.greenfieldmc.core.signmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SavedSign {

    private String name;
    private final Material signMaterial;
    private final List<String> frontLines; // GsonComponentSerializer JSON strings
    private final List<String> backLines;  // GsonComponentSerializer JSON strings
    private @Nullable SignFlag flag;

    public SavedSign(String name, Material signMaterial, List<String> frontLines, List<String> backLines, @Nullable SignFlag flag) {
        this.name = name;
        this.signMaterial = signMaterial;
        this.frontLines = frontLines;
        this.backLines = backLines;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getSignMaterial() {
        return signMaterial;
    }

    public List<String> getFrontLines() {
        return frontLines;
    }

    public List<String> getBackLines() {
        return backLines;
    }

    public @Nullable SignFlag getFlag() {
        return flag;
    }

    public void setFlag(@Nullable SignFlag flag) {
        this.flag = flag;
    }

    /**
     * Builds the lore lines representing the sign's front and back text content.
     * Used both for individual sign display and for group display signs.
     */
    public List<Component> buildLoreLines() {
        var lore = new ArrayList<Component>();
        lore.add(Component.text("── Front ──", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        for (var line : frontLines) {
            var component = deserializeLine(line);
            var plain = PlainTextComponentSerializer.plainText().serialize(component);
            if (!plain.isBlank()) {
                lore.add(Component.text("  ", NamedTextColor.WHITE).append(component).decoration(TextDecoration.ITALIC, false));
            }
        }
        lore.add(Component.text("── Back ──", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        for (var line : backLines) {
            var component = deserializeLine(line);
            var plain = PlainTextComponentSerializer.plainText().serialize(component);
            if (!plain.isBlank()) {
                lore.add(Component.text("  ", NamedTextColor.WHITE).append(component).decoration(TextDecoration.ITALIC, false));
            }
        }
        return lore;
    }

    /**
     * Converts the saved sign data back into a placeable sign ItemStack with
     * the original front/back text restored in the block entity NBT.
     */
    public ItemStack toItemStack() {
        var item = new ItemStack(signMaterial, 1);
        var meta = item.getItemMeta();

        if (meta instanceof BlockStateMeta blockStateMeta) {
            var state = blockStateMeta.getBlockState();
            if (state instanceof Sign sign) {
                var frontSide = sign.getSide(Side.FRONT);
                for (int i = 0; i < frontLines.size() && i < 4; i++) {
                    frontSide.line(i, deserializeLine(frontLines.get(i)));
                }
                var backSide = sign.getSide(Side.BACK);
                for (int i = 0; i < backLines.size() && i < 4; i++) {
                    backSide.line(i, deserializeLine(backLines.get(i)));
                }
                blockStateMeta.setBlockState(state);
            }
        }

        // Set display name
        meta.displayName(Component.text(name, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

        // Build lore: sign text lines + flag info
        var lore = buildLoreLines();
        if (flag != null) {
            lore.add(Component.empty());
            lore.add(Component.text("Flag: ", NamedTextColor.DARK_GRAY).append(Component.text(flag.getDisplayName(), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a SavedSign from a sign ItemStack held by a player.
     */
    public static @Nullable SavedSign fromItemStack(ItemStack item, String name, @Nullable SignFlag flag) {
        if (item == null || !isSignMaterial(item.getType())) return null;

        var meta = item.getItemMeta();
        var frontLines = new ArrayList<String>();
        var backLines = new ArrayList<String>();

        if (meta instanceof BlockStateMeta blockStateMeta) {
            var state = blockStateMeta.getBlockState();
            if (state instanceof Sign sign) {
                var frontSide = sign.getSide(Side.FRONT);
                for (int i = 0; i < 4; i++) {
                    frontLines.add(serializeLine(frontSide.line(i)));
                }
                var backSide = sign.getSide(Side.BACK);
                for (int i = 0; i < 4; i++) {
                    backLines.add(serializeLine(backSide.line(i)));
                }
            }
        }

        // If no block state meta, just use empty lines
        while (frontLines.size() < 4) frontLines.add(serializeLine(Component.empty()));
        while (backLines.size() < 4) backLines.add(serializeLine(Component.empty()));

        return new SavedSign(name, item.getType(), frontLines, backLines, flag);
    }

    /**
     * Returns the plain text content of all sign lines for search matching.
     */
    public String getPlainText() {
        var sb = new StringBuilder();
        sb.append(name).append(" ");
        for (var line : frontLines) {
            sb.append(PlainTextComponentSerializer.plainText().serialize(deserializeLine(line))).append(" ");
        }
        for (var line : backLines) {
            sb.append(PlainTextComponentSerializer.plainText().serialize(deserializeLine(line))).append(" ");
        }
        if (flag != null) sb.append(flag.getDisplayName()).append(" ");
        return sb.toString();
    }

    public static boolean isSignMaterial(Material material) {
        return material != null && material.name().endsWith("_SIGN");
    }

    private static String serializeLine(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    private static Component deserializeLine(String json) {
        try {
            return GsonComponentSerializer.gson().deserialize(json);
        } catch (Exception e) {
            return Component.text(json);
        }
    }
}
