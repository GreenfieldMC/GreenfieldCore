package com.njdaeger.greenfieldcore.advancedbuild.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;

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
