package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import net.greenfieldmc.core.templates.models.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public interface ITemplateWorldEditService extends IWorldEditService {

    /**
     * Get a list of all schematic files in the WorldEdit save directory
     *
     * @return a list of all schematic files. Will return empty list if WorldEdit is not enabled or no schematic files are found.
     */
    List<Path> getSchematicFiles();

    /**
     * A callable that loads a template to the clipboard
     *
     * @param template the template to load
     * @param player the player to load the template for
     */
    void loadToClipboard(Template template, Player player) throws Exception;

    /**
     * Get a brush from the given player's hand.
     * @param player the player to get the brush from
     * @return the brush from the given player's hand
     * @throws Exception if the brush could not be retrieved
     */
    WorldEditTemplateBrush getBrush(Player player) throws Exception;

    /**
     * Assign a brush to a players item in hand
     * @param player the player to assign the brush to
     * @param brushId the id of the brush to assign
     */
    void addBrush(Player player, int brushId) throws Exception;

    /**
     * Paste a template's clipboard at the given location in the world.
     *
     * @param template      The template to paste. Must already be loaded.
     * @param anchor        The world location to paste the clipboard origin at.
     * @param player        The player performing the paste (used for WorldEdit history).
     * @param ignoreAir     When {@code true} air blocks in the clipboard are skipped during paste.
     * @param rotationDegrees Y-axis rotation to apply before pasting (0 / 90 / 180 / 270).
     *                        Must match the rotation the player confirmed during placement mode.
     * @throws Exception if WorldEdit is unavailable or the paste fails.
     */
    void pasteTemplate(Template template, Location anchor, Player player, boolean ignoreAir, int rotationDegrees) throws Exception;

    /**
     * Runs {@code //copy} (with an optional material mask) and {@code //schematic save} as the given player,
     * then returns the {@link Path} where the schematic will be written.
     * <p>
     * Both WorldEdit commands are dispatched synchronously on the calling thread. The schematic file
     * write itself is queued asynchronously by WorldEdit, so the returned path may not exist
     * immediately — but will be ready before a typical user action requires it.
     *
     * @param player        the player whose current selection should be copied
     * @param schematicName the filename (without extension) to save the schematic as
     * @param mask          optional WorldEdit mask expression (e.g. {@code "stone,gravel"}); pass
     *                      {@code null} or blank to copy without a mask
     * @return the full path of the schematic file that will be written
     * @throws Exception if WorldEdit is unavailable, the player has no complete selection, or
     *                   the dispatch fails
     */
    Path copySelectionToSchematic(Player player, String schematicName, @Nullable String mask) throws Exception;

}
