package com.njdaeger.greenfieldcore.paintingswitch;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.ModuleConfig;
import com.njdaeger.greenfieldcore.paintingswitch.services.IPaintingSwitchService;
import com.njdaeger.greenfieldcore.paintingswitch.services.PaintingSwitchCommandService;
import com.njdaeger.greenfieldcore.paintingswitch.services.PaintingSwitchServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;
import static java.lang.Math.*;

public class PaintingSwitchModule extends Module implements Listener {

    private IPaintingSwitchService paintingSwitchService;

    public PaintingSwitchModule(GreenfieldCore plugin, Predicate<ModuleConfig> canEnable) {
        super(plugin, canEnable);
    }

    @Override
    public void tryEnable() {
        paintingSwitchService = enableIntegration(new PaintingSwitchServiceImpl(plugin, this), true);
        enableIntegration(new PaintingSwitchCommandService(plugin, this, paintingSwitchService), true);
    }

    @Override
    public void tryDisable() {
        disableIntegration(paintingSwitchService);
    }

}
