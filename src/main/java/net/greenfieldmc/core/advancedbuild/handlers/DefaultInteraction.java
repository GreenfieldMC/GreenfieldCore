package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import org.bukkit.event.player.PlayerInteractEvent;

public class DefaultInteraction extends InteractionHandler {

    public DefaultInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService);
    }

    @Override
    public boolean handles(PlayerInteractEvent event) {
        return false;
    }

//    @Override
//    public void onRightClickBlock(PlayerInteractEvent event) {
//        var handMat = getHandMat(event);
//        if (handMat != Material.AIR) {
//            System.out.println(handMat.data.getName());
//            System.out.println(handMat.createBlockData().getClass().getName());
//            System.out.println(handMat.createBlockData().getAsString());
//            System.out.println(event.getb);
//            var datacopy = handMat.
//        }
//    }
}
