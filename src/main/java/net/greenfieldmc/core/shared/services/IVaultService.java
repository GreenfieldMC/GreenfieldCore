package net.greenfieldmc.core.shared.services;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IVaultService {

    /**
     * @return true if Vault and its permission provider are available. When false, the group methods fail or return empty results.
     */
    boolean isEnabled();

    CompletableFuture<Boolean> addUserToGroup(String world, UUID uuid, String group);

    CompletableFuture<Boolean> removeUserFromGroup(String world, UUID uuid, String group);

    List<String> getGroupList();

}
