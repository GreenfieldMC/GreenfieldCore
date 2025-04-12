package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.templates.models.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ITemplateViewerService extends IModuleService<ITemplateViewerService> {

    void startTemplateView(Player player, Template loadedTemplate, Location location, double scale);

    void destroyTemplateView(Player player);

    boolean isTemplateViewActive(Player player);

}
