package net.greenfieldmc.core.armorstandeditor;

import net.greenfieldmc.core.GreenfieldCore;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleConfig;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandEditorCommandService;
import net.greenfieldmc.core.armorstandeditor.services.ArmorStandEditorServiceImpl;
import net.greenfieldmc.core.armorstandeditor.services.IArmorStandEditorService;
import org.bukkit.event.HandlerList;

import java.util.function.Predicate;

public class ArmorStandEditorModule extends Module {

    private IArmorStandEditorService armorStandEditorService;
    private ArmorStandEditorCommandService commandService;

    public ArmorStandEditorModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    protected void tryEnable() throws Exception {
        armorStandEditorService = enableIntegration(new ArmorStandEditorServiceImpl(plugin, this), true);
        commandService = enableIntegration(new ArmorStandEditorCommandService(plugin, this, armorStandEditorService), true);
    }

    @Override
    protected void tryDisable() throws Exception {
        if (commandService != null) {
            disableIntegration(commandService);
        }
        if (armorStandEditorService != null) {
            disableIntegration(armorStandEditorService);
        }
    }
}
