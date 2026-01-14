package net.greenfieldmc.core.armorstandeditor.services;

import net.greenfieldmc.core.IModuleService;

import java.util.List;
import java.util.UUID;

public interface IArmorStandEditorService extends IModuleService<IArmorStandEditorService> {

    List<UUID> getDisabledUsers();

    default boolean isEnabledFor(UUID uuid) {
        return !getDisabledUsers().contains(uuid);
    }

    void setEnabledFor(UUID uuid, boolean enabled);
}

