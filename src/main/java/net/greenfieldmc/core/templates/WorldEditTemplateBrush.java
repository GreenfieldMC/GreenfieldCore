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
import net.greenfieldmc.core.templates.models.Template;
import net.greenfieldmc.core.templates.models.TemplateInstance;
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

    public int getBrushId() {
        return brushId;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        var brush = templateService.getSession(uuid).getBrush(brushId);
        if (brush == null) return;
        var player = Bukkit.getPlayer(uuid);
        if (player == null) throw new IllegalStateException("Player is null");

        if (!brush.hasNextTemplate()) brush.randomizeNextTemplate();
        if (!brush.hasNextTemplate()) {
            player.sendMessage(TemplateMessages.ERROR_NO_TEMPLATES_SELECTED);
            return;
        }

        var templateInstance = brush.getNextTemplate();
        var template = templateService.getTemplate(templateInstance.getCurrentTemplateName());

        if (template == null) {
            player.sendMessage(TemplateMessages.ERROR_TEMPLATE_NOT_FOUND.apply(templateInstance.getCurrentTemplateName()));
            brush.removeTemplate(templateInstance.getCurrentTemplateName());
            return;
        }

        brush.randomizeNextTemplate();
        if (!template.isLoaded()) {
            Bukkit.getScheduler().runTaskAsynchronously(templateService.getPlugin(), () -> {
                player.sendMessage(TemplateMessages.TEMPLATE_LOADING.apply(template));
                try {
                    template.loadClipboard();
                } catch (Exception e) {
                    player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                    return;
                }
                try {
                    performOperation(templateInstance, template, editSession, position, player);
                } catch (MaxChangedBlocksException e) {
                    player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                }
            });
            return;
        }

        performOperation(templateInstance, template, editSession, position, player);
    }

    private void performOperation(TemplateInstance templateInstance, Template template, EditSession editSession, BlockVector3 position, Player player) throws MaxChangedBlocksException {
        var clipboard = template.getClipboard();

        var transform = new AffineTransform();
        if (templateInstance.hasRotationOption()) {
            if (templateInstance.getCurrentRotationOption() == RotationOption.SELF) transform = transform.rotateY(getRotationFromPlayer(player));
            else transform = transform.rotateY(templateInstance.getCurrentRotationOption().getAdjustmentValue());
        }

        if (templateInstance.hasFlipOption()) {
            var blockVector = BukkitAdapter.adapt(templateInstance.getCurrentFlipOption().getAdjustmentValue()).toBlockVector();
            transform = transform.scale(blockVector.abs().multiply(-2).add(1,1,1).toVector3());
        }

        var holder = new ClipboardHolder(clipboard);
        holder.setTransform(transform);

        var operation = holder
                .createPaste(editSession)
                .to(position)
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
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        };
    }

}
