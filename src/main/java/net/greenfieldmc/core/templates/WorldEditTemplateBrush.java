package net.greenfieldmc.core.templates;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.greenfieldmc.core.templates.models.RotationOption;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WorldEditTemplateBrush implements Brush {

    private final int brushId;
    private final UUID uuid;
    private final ITemplateService templateService;

    public WorldEditTemplateBrush(ITemplateService templateService, UUID user, int brushId) {
        this.uuid = user;
        this.brushId = brushId;
        this.templateService = templateService;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        var brush = templateService.getSession(uuid).getBrush(brushId);
        if (brush == null) return;

        var templateInstance = brush.getNextTemplate();
        var template = templateService.getTemplate(templateInstance.getCurrentTemplateName());
        var player = Bukkit.getPlayer(uuid);
        if (player == null) throw new IllegalStateException("Player is null");
        if (template == null) {
            player.sendMessage(Component.text("Template " + templateInstance.getCurrentTemplateName() + " not found. It is being removed from your template list. This could indicate the template name has changed.", NamedTextColor.RED));
            brush.removeTemplate(templateInstance.getCurrentTemplateName());
            return;
        }

        brush.randomizeNextTemplate();
        if (!template.isLoaded()) {
            try {
                template.loadClipboard();
            } catch (Exception e) {
                player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
            }
        }

        var clipboard = template.getClipboard();

        var transform = new AffineTransform();
        if (templateInstance.hasRotationOption()) {
            if (templateInstance.getCurrentRotationOption() == RotationOption.SELF) transform.rotateY(getRotationFromPlayer(player));
            else transform.rotateY(templateInstance.getCurrentRotationOption().getRotation());
        }

        if (templateInstance.hasFlipOption()) {
            var blockVector = BukkitAdapter.adapt(templateInstance.getCurrentFlipOption().getFlipDirection()).toBlockVector();
            transform.scale(blockVector.abs().multiply(-2).add(1,1,1).toVector3());
        }

        var operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(clipboard.getOrigin())
                .ignoreAirBlocks(templateInstance.ignoreAirBlocks())
                .copyBiomes(templateInstance.pasteBiomes())
                .copyEntities(templateInstance.pasteEntities())
                .maskSource(editSession.getMask())
                .build();

        Operations.completeLegacy(operation);
    }

    private static int getRotationFromPlayer(Player player) {
        var facing = BukkitAdapter.adapt(player.getFacing());
        return switch (facing) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        };
    }

}
