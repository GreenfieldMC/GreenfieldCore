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

    public SavedSign(String name, Material signMaterial, List<String> frontLines, List<String> backLines) {
        this.name = name;
        this.signMaterial = signMaterial;
        this.frontLines = frontLines;
        this.backLines = backLines;
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

    /**
     * Builds hover text showing the sign's front and back text content.
     */
    public List<Component> buildHoverLines() {
        var lines = new ArrayList<Component>();
        lines.add(Component.text("── Front ──", NamedTextColor.GRAY));
        for (var line : frontLines) {
            var component = deserializeLine(line);
            var plain = PlainTextComponentSerializer.plainText().serialize(component);
            if (!plain.isBlank()) {
                lines.add(Component.text("  ").append(component));
            }
        }
        lines.add(Component.text("── Back ──", NamedTextColor.GRAY));
        for (var line : backLines) {
            var component = deserializeLine(line);
            var plain = PlainTextComponentSerializer.plainText().serialize(component);
            if (!plain.isBlank()) {
                lines.add(Component.text("  ").append(component));
            }
        }
        return lines;
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

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a SavedSign from a sign ItemStack held by a player.
     */
    public static @Nullable SavedSign fromItemStack(ItemStack item, String name) {
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

        return new SavedSign(name, item.getType(), frontLines, backLines);
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
        return sb.toString();
    }

    /**
     * Returns a human-readable sign type description based on the material name.
     * e.g. "Oak Sign", "Birch Hanging Sign", "Dark Oak Wall Sign"
     */
    public String getSignTypeDescription() {
        var name = signMaterial.name();
        // Remove _SIGN suffix and convert underscores to spaces, title case
        name = name.replace("_SIGN", "").replace("_", " ");
        var words = name.toLowerCase().split(" ");
        var sb = new StringBuilder();
        for (var word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        sb.append("Sign");
        return sb.toString().trim();
    }

    /**
     * Returns true if the sign material is a hanging sign type.
     */
    public boolean isHangingSign() {
        return signMaterial.name().contains("HANGING");
    }

    /**
     * Returns true if the sign material is a wall sign type.
     */
    public boolean isWallSign() {
        return signMaterial.name().contains("WALL");
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
