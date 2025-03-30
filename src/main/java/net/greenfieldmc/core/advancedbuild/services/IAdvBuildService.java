package net.greenfieldmc.core.advancedbuild.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;

import java.util.List;
import java.util.UUID;

public interface IAdvBuildService extends IModuleService<IAdvBuildService> {

    List<UUID> getEnabledUsers();

    List<InteractionHandler> getInteractionHandlers();

    default boolean isEnabledFor(UUID uuid) {
        return getEnabledUsers().contains(uuid);
    }

    default void setEnabledFor(UUID uuid, boolean enabled) {
        if (enabled) getEnabledUsers().add(uuid);
        else getEnabledUsers().remove(uuid);
    }

}
