package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.EntityInteractPredicate;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ItemFrameInteraction extends InteractionHandler {

    public ItemFrameInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (EntityInteractPredicate) (event) ->
                event.getPlayer().isSneaking() && event.getRightClicked() instanceof ItemFrame);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Alters the state of item frames when on right click.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click with an empty hand to toggle the visibility of item frames.").color(NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Shift and right click with the opposite item frame type in hand to swap the type of item frame.").color(NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Shift and right click with an item in hand matching the item of the to remove the name of the item in the frame.").color(NamedTextColor.GRAY));
    }

    @Override
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        ItemFrame itemFrame = (ItemFrame) entity;
        Player player = event.getPlayer();

        // Inside onRightClickEntity
        if (itemFrame.getItem() != null && getHandMat(event) == itemFrame.getItem().getType()) {
            var frameItem = itemFrame.getItem();
            var meta = frameItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                meta.setDisplayName(null);
                frameItem.setItemMeta(meta);
                itemFrame.setItem(frameItem);
                event.setCancelled(true);
            }
        }

        // Swap GlowItemFrame to ItemFrame
        if (entity instanceof GlowItemFrame && getHandMat(event) == Material.ITEM_FRAME) {
            coreProtectService.logRemoval(player.getName(), itemFrame.getLocation(), Material.GLOW_ITEM_FRAME, null);

            itemFrame.remove();
            ItemFrame newFrame = itemFrame.getWorld().spawn(itemFrame.getLocation(), ItemFrame.class);

            newFrame.setRotation(itemFrame.getRotation());
            newFrame.setFacingDirection(itemFrame.getAttachedFace().getOppositeFace());
            newFrame.setItem(itemFrame.getItem());
            newFrame.setItemDropChance(itemFrame.getItemDropChance());
            newFrame.setVisible(itemFrame.isVisible());
            newFrame.setFixed(itemFrame.isFixed());
            newFrame.setGlowing(itemFrame.isGlowing());

            coreProtectService.logPlacement(player.getName(), newFrame.getLocation(), Material.ITEM_FRAME, null);
            event.setCancelled(true);
        }
        // Swap ItemFrame to GlowItemFrame
        else if (entity instanceof ItemFrame && !(entity instanceof GlowItemFrame) && getHandMat(event) == Material.GLOW_ITEM_FRAME) {
            coreProtectService.logRemoval(player.getName(), itemFrame.getLocation(), Material.ITEM_FRAME, null);

            itemFrame.remove();
            GlowItemFrame newFrame = itemFrame.getWorld().spawn(itemFrame.getLocation(), GlowItemFrame.class);

            newFrame.setRotation(itemFrame.getRotation());
            newFrame.setFacingDirection(itemFrame.getAttachedFace().getOppositeFace());
            newFrame.setItem(itemFrame.getItem());
            newFrame.setItemDropChance(itemFrame.getItemDropChance());
            newFrame.setVisible(itemFrame.isVisible());
            newFrame.setFixed(itemFrame.isFixed());
            newFrame.setGlowing(itemFrame.isGlowing());

            coreProtectService.logPlacement(player.getName(), newFrame.getLocation(), Material.GLOW_ITEM_FRAME, null);
            event.setCancelled(true);
        }

        if (getHandMat(event) == Material.AIR) {
            itemFrame.setVisible(!itemFrame.isVisible());
            event.setCancelled(true);
        }

    }

}


