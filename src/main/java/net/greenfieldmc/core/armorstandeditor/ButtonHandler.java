package net.greenfieldmc.core.armorstandeditor;

import net.greenfieldmc.core.armorstandeditor.services.ArmorStandSessionService;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ButtonHandler {

    private List<ItemStack> heldItemStacks = null;
    private ArmorStand armorStand = null;
    private ArmorStandSessionService sessionManager = null;
    private Player player = null;
    private final Event event = null;

    /**
     * @param heldItemStacks //itemstack to look for in player's hand
     */
    public ButtonHandler(ItemStack... heldItemStacks) {
        if (heldItemStacks == null) throw new IllegalArgumentException("heldItemStack");
        this.heldItemStacks = heldItemStacks == null ? Arrays.asList(heldItemStacks) : List.of();
    }

    /**
     * @param heldItemStacks //itemstack to look for in player's hand
     * @param armorStand    //armorstand to manipulate
     */
    public ButtonHandler(ArmorStand armorStand, ItemStack... heldItemStacks) {
        if (heldItemStacks == null) throw new IllegalArgumentException("heldItemStack");
        this.heldItemStacks = heldItemStacks == null ? Arrays.asList(heldItemStacks) : List.of();
        this.armorStand = armorStand;
    }

    /**
     * @param heldItemStacks //itemstack to look for in player's hand
     * @param armorStand    //armorstand to manipulate
     * @param sessionManager //session manager to get the session from
     * @param player       //player who is editing the armorstand
     */
    public ButtonHandler(ArmorStand armorStand, Player player, ArmorStandSessionService sessionManager, ItemStack... heldItemStacks) {
        if (heldItemStacks == null) throw new IllegalArgumentException("heldItemStack");
        this.heldItemStacks = heldItemStacks == null ? Arrays.asList(heldItemStacks) : List.of();
        this.armorStand = armorStand;
        this.player = player;
        this.sessionManager = sessionManager;
    }
}
