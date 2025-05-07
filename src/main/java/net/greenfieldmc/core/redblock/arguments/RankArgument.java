package net.greenfieldmc.core.redblock.arguments;

import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.defaults.StringArgument;
import net.greenfieldmc.core.shared.services.IVaultService;

import java.util.List;

public class RankArgument extends StringArgument {

    private final IVaultService vaultService;

    public RankArgument(IVaultService vaultService) {
        super(() -> "What rank should this redblock be assigned to?");
        this.vaultService = vaultService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        return vaultService.getGroupList();
    }
}
