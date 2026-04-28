package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public interface ITemplateViewerService extends IModuleService<ITemplateViewerService> {

    /**
     * Start placement mode for the given player with the given template.
     * The model will follow the player's crosshair until confirmed or cancelled.
     *
     * @param player The player entering placement mode.
     * @param loadedTemplate The template to place (must already be loaded).
     * @param onConfirm Callback invoked with the final anchor location and rotation degrees (0/90/180/270)
     *                  when the player confirms placement.
     *                  The callback is responsible for performing any world operations (e.g. pasting).
     */
    void startPlacementMode(Player player, Template loadedTemplate, BiConsumer<Location, Integer> onConfirm);

    /**
     * Confirm the current placement, pasting the template at the current anchor location.
     *
     * @param player The player confirming the placement.
     * @return The anchor location where the template was placed, or null if not in placement mode.
     */
    Location confirmPlacement(Player player);

    /**
     * Cancel the current placement mode and despawn all display entities.
     *
     * @param player The player cancelling placement.
     */
    void cancelPlacement(Player player);

    /**
     * Check if the player is currently in placement mode.
     *
     * @param player The player to check.
     * @return True if the player is in placement mode, false otherwise.
     */
    boolean isInPlacementMode(Player player);

    /**
     * Create a template item that can be right-clicked to enter placement mode.
     * The item uses PersistentDataContainer to store the template name.
     *
     * @param template The template to create an item for.
     * @return An ItemStack representing the template.
     */
    ItemStack createTemplateItem(Template template);

}
