package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ItemFrameInteraction extends InteractionHandler {

    public ItemFrameInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService,
                Material.ITEM_FRAME,
                Material.GLOW_ITEM_FRAME);
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Alters the state of item frames when on right click.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("Shift and right click with and empty hand to toggle the visibility of item frames.").color(NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("or with an item frame in hand to swap the type of item frame.").color(NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Shift and right click with an item in hand matching the item of the to toggle the name of the item in the frame.").color(NamedTextColor.GRAY));
    }

    @Override
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!event.getPlayer().isSneaking()) {
            return;
        }

        if (entity instanceof ItemFrame itemFrame) {

            Player player = event.getPlayer();
            ItemStack itemFrameItem = itemFrame.getItem();
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType() == Material.GLOW_ITEM_FRAME) {
                swapGlowItemFrame(itemFrame, player);
            }

            //remove name function
            if (itemFrameItem.getType() == itemInMainHand.getType()) {
                toggleItemName(itemFrame);
            }

            //toggle itemframe invisibility
            else if (itemInMainHand.isEmpty()) {
                toggleItemFrameVisibility(itemFrame);
            }

        }
        if (entity instanceof GlowItemFrame itemFrame) {

            Player player = event.getPlayer();
            ItemStack itemFrameItem = itemFrame.getItem();
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType() == Material.ITEM_FRAME) {
                swapItemFrame(itemFrame, player);
            }

            //remove name function
            if (itemFrameItem.getType() == itemInMainHand.getType()) {
                toggleItemName(itemFrame);
            }

            //toggle itemframe invisibility
            else if (itemInMainHand.isEmpty()) {
                toggleItemFrameVisibility(itemFrame);
            }
        }

    }

    private void toggleItemName (ItemFrame itemFrame){
        ItemStack item = itemFrame.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (!storedNames.containsKey(itemFrame)) {
            if (meta.hasDisplayName()) {
                itemFrame.setRotation(itemFrame.getRotation().rotateCounterClockwise());
                storedNames.put(itemFrame, meta.getDisplayName());
                meta.setDisplayName(null);
                item.setItemMeta(meta);
                itemFrame.setItem(item);
            }
        } else {
            itemFrame.setRotation(itemFrame.getRotation().rotateCounterClockwise());
            meta.setDisplayName(storedNames.get(itemFrame));
            item.setItemMeta(meta);
            itemFrame.setItem(item);
            storedNames.remove(itemFrame);
        }
    }

    private void swapGlowItemFrame (ItemFrame itemFrame, Player player){
        ItemFrameAttributes attrs = new ItemFrameAttributes(itemFrame);
        coreProtectService.logRemoval(player.getName(), itemFrame.getLocation(), Material.ITEM_FRAME, null);
        itemFrame.remove();

        GlowItemFrame newFrame = itemFrame.getWorld().spawn(attrs.location, GlowItemFrame.class);
        coreProtectService.logPlacement(player.getName(), newFrame.getLocation(), Material.GLOW_ITEM_FRAME, null);

        newFrame.setRotation(attrs.rotation);
        newFrame.setFacingDirection(attrs.facing);
        newFrame.setItem(attrs.item);
        newFrame.setItemDropChance(attrs.itemDropChance);
        newFrame.setVisible(attrs.visible);
        newFrame.setFixed(attrs.fixed);
        newFrame.setGlowing(attrs.glowing);
    }

    private void swapItemFrame (GlowItemFrame itemFrame, Player player){
        ItemFrameAttributes attrs = new ItemFrameAttributes(itemFrame);

        coreProtectService.logRemoval(player.getName(), itemFrame.getLocation(), Material.GLOW_ITEM_FRAME, null);
        itemFrame.remove();

        ItemFrame newFrame = itemFrame.getWorld().spawn(attrs.location, ItemFrame.class);
        coreProtectService.logPlacement(player.getName(), newFrame.getLocation(), Material.ITEM_FRAME, null);

        newFrame.setRotation(attrs.rotation);
        newFrame.setFacingDirection(attrs.facing);
        newFrame.setItem(attrs.item);
        newFrame.setItemDropChance(attrs.itemDropChance);
        newFrame.setVisible(attrs.visible);
        newFrame.setFixed(attrs.fixed);
        newFrame.setGlowing(attrs.glowing);
    }

    private void toggleItemFrameVisibility(ItemFrame itemFrame) {
        itemFrame.setVisible(!itemFrame.isVisible());
        itemFrame.setRotation(itemFrame.getRotation().rotateCounterClockwise());
    }

    private static final Map<ItemFrame, String> storedNames = new HashMap<>();

    public static class ItemFrameAttributes {
        public final Location location;
        public final Rotation rotation;
        public final BlockFace facing;
        public final ItemStack item;
        public final float itemDropChance;
        public final boolean visible;
        public final boolean fixed;
        public final boolean glowing;

        public ItemFrameAttributes(ItemFrame frame) {
            this.location = frame.getLocation();
            this.rotation = frame.getRotation();
            this.facing = frame.getAttachedFace();
            this.item = frame.getItem();
            this.itemDropChance = frame.getItemDropChance();
            this.visible = frame.isVisible();
            this.fixed = frame.isFixed();
            this.glowing = frame.isGlowing();
        }
    }
}


