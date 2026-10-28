package net.greenfieldmc.core.paintingswitch.services;

import java.util.List;
import java.util.UUID;

public interface IPaintingSwitchService {

    List<UUID> getDisabledUsers();

    default boolean isEnabledFor(UUID uuid) {
        return !getDisabledUsers().contains(uuid);
    }

    void setEnabledFor(UUID uuid, boolean enabled);
}
