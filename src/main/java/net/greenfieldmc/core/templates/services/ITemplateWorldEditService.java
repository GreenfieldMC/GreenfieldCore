package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import org.bukkit.entity.Player;

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

}
