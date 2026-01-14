package net.greenfieldmc.core.armorstandeditor.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import net.greenfieldmc.core.armorstandeditor.listeners.ArmorStandEditorListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArmorStandEditorServiceImpl extends ModuleService<IArmorStandEditorService> implements IArmorStandEditorService {

    private IConfig config;
    private List<UUID> disabledUsers;
    private final Map<UUID, ArmorStandSessionService.ArmorStandSession> sessions = new HashMap<>();
    private ArmorStandSessionService sessionService;
    private ArmorStandHotbarService hotbarService;
    private ArmorStandEditorListener listener;

    public ArmorStandEditorServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "armorstandeditor");
            config.addEntry("disabled", new ArrayList<>());
            this.disabledUsers = new ArrayList<>(config.getStringList("disabled").stream().map(UUID::fromString).toList());

            // initialize session and hotbar services
            this.sessionService = new ArmorStandSessionService();
            this.hotbarService = new ArmorStandHotbarService();

            // create and register the listener which wires handler instances
            this.listener = new ArmorStandEditorListener(this, sessions, hotbarService, plugin, sessionService);
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        } catch (Exception e) {
            throw new Exception("Failed to load ArmorStandEditorService.", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        config.setEntry("disabled", disabledUsers.stream().map(UUID::toString).toList());
        config.save();
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }

    @Override
    public List<UUID> getDisabledUsers() {
        return disabledUsers;
    }

    @Override
    public void setEnabledFor(UUID uuid, boolean enabled) {
        if (enabled) disabledUsers.remove(uuid);
        else {
            disabledUsers.add(uuid);
        }
    }
}
