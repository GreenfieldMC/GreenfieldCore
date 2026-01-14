package net.greenfieldmc.core.armorstandeditor.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.armorstandeditor.ArmorStandEditorMessages;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.CommandSenderTypeException;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.greenfieldmc.core.armorstandeditor.handlers.Arms;
import net.greenfieldmc.core.armorstandeditor.handlers.BasePlate;
import net.greenfieldmc.core.armorstandeditor.handlers.Copy;
import net.greenfieldmc.core.armorstandeditor.handlers.Lock;
import net.greenfieldmc.core.armorstandeditor.handlers.Pose;
import net.greenfieldmc.core.armorstandeditor.handlers.Rotate;
import net.greenfieldmc.core.armorstandeditor.handlers.Size;
import net.greenfieldmc.core.armorstandeditor.handlers.Slot;
import net.greenfieldmc.core.armorstandeditor.handlers.Visible;
import net.greenfieldmc.core.armorstandeditor.storage.ArmorStandPosePreset;

public class ArmorStandEditorCommandService extends ModuleService<ArmorStandEditorCommandService> implements IModuleService<ArmorStandEditorCommandService> {

    private final IArmorStandEditorService armorStandEditorService;

    public ArmorStandEditorCommandService(Plugin plugin, Module module, IArmorStandEditorService armorStandEditorService) {
        super(plugin, module);
        this.armorStandEditorService = armorStandEditorService;
    }

    private void toggle(ICommandContext ctx) throws CommandSenderTypeException {
        var player = ctx.asPlayer();
        var enabled = armorStandEditorService.isEnabledFor(player.getUniqueId());
        armorStandEditorService.setEnabledFor(player.getUniqueId(), !enabled);
        ctx.send(!enabled ? ArmorStandEditorMessages.ENABLED : ArmorStandEditorMessages.DISABLED);
    }

    private enum HandlerType {
        ARMS,
        BASEPLATE,
        COPY,
        LOCK,
        POSE,
        ROTATE,
        SIZE,
        SLOT,
        VISIBLE
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        CommandBuilder.of("astoggle", "armorstandeditor")
                .permission("greenfieldcore.armorstandeditor.use")
                .description("Toggle the armor stand editor feature")
                .canExecute(this::toggle)
                .register(plugin);

        // /ase or /armorstandeditor give editing items
        CommandBuilder.of("asetool", "armorstandeditortool")
                .permission("greenfieldcore.armorstandeditor.use")
                .description("Give an armor-stand editor item")
                .then("handler", PdkArgumentTypes.enumArg(HandlerType.class))
                    .executes(ctx -> {
                        Player player = ctx.asPlayer();
                        HandlerType h = ctx.getTyped("handler", HandlerType.class);
                        ItemStack item = resolveHandlerItem(h);
                        if (item != null) player.getInventory().addItem(item);
                    })
                    .then("pose", PdkArgumentTypes.enumArg(ArmorStandPosePreset.class))
                        .executes(ctx -> {
                            Player player = ctx.asPlayer();
                            ArmorStandPosePreset preset = ctx.getTyped("pose", ArmorStandPosePreset.class);
                            // give the preset item (not the selector) to the player
                            player.getInventory().addItem(preset.toItemStack());
                        })
                .register(plugin);
    }

    private ItemStack resolveHandlerItem(HandlerType h) {
        if (h == null) return null;
        return switch (h) {
            case ARMS -> Arms.armsItem(null);
            case BASEPLATE -> BasePlate.basePlateItem(null);
            case COPY -> Copy.copyItem();
            case LOCK -> Lock.lockItem(null);
            case POSE -> Pose.poseItem();
            case ROTATE -> Rotate.rotateItem();
            case SIZE -> Size.sizeItem(null);
            case SLOT -> Slot.slotItem();
            case VISIBLE -> Visible.visibleItem(null);
        };
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }
}
