package net.greenfieldmc.core.advancedbuild.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.advancedbuild.handlers.*;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Candle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdvBuildServiceImpl extends ModuleService<IAdvBuildService> implements IAdvBuildService, Listener {

    private IConfig config;
    private List<UUID> enabled;
    private List<InteractionHandler> interactionHandlers;

    private final IWorldEditService worldEditService;
    private final ICoreProtectService coreProtectService;

    public AdvBuildServiceImpl(Plugin plugin, Module module, IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(plugin, module);
        this.worldEditService = worldEditService;
        this.coreProtectService = coreProtectService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "advancedbuildmode");
            config.addEntry("enabled", new ArrayList<>());
            this.enabled = new ArrayList<>(config.getStringList("enabled").stream().map(UUID::fromString).toList());
            loadInteractionHandlers();
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } catch (Exception e) {
            throw new Exception("Failed to load AdvBuildService", e);
        }

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        config.setEntry("enabled", enabled.stream().map(UUID::toString).toList());
        config.save();
    }

    @Override
    public List<UUID> getEnabledUsers() {
        return enabled;
    }

    @Override
    public List<InteractionHandler> getInteractionHandlers() {
        return interactionHandlers;
    }

    private void loadInteractionHandlers() {
        this.interactionHandlers = List.of(
                new DefaultInteraction(worldEditService, coreProtectService),
                new CandleInteraction(worldEditService, coreProtectService),
                new AmethystInteraction(worldEditService, coreProtectService),
                new ChiseledBookshelfInteraction(worldEditService, coreProtectService, getPlugin(), this),
                new DoorInteraction(worldEditService, coreProtectService),
                new JigsawInteraction(worldEditService, coreProtectService),
                new PinkPetalsInteraction(worldEditService, coreProtectService),
                new MultipleFacingInteraction(worldEditService, coreProtectService),
                new RandomBlockInteractions(worldEditService, coreProtectService),
                new ChorusInteraction(worldEditService, coreProtectService),
                new PlantInteraction(worldEditService, coreProtectService),
                new BisectedInteraction(worldEditService, coreProtectService),
                new SignInteraction(worldEditService, coreProtectService),
                new WallInteraction(worldEditService, coreProtectService),
                new ObserverInteraction(worldEditService, coreProtectService),
                new MangroveRootsInteraction(worldEditService, coreProtectService),
                new DirectionalInteraction(worldEditService, coreProtectService),
                new PitcherPodInteraction(worldEditService, coreProtectService),
                new CommandBlockInteraction(worldEditService, coreProtectService),
                new TorchInteraction(worldEditService, coreProtectService),
                new SwitchInteraction(worldEditService, coreProtectService),
                new CoralInteraction(worldEditService, coreProtectService),
                new RailInteraction(worldEditService, coreProtectService),
                new BrushableInteraction(worldEditService, coreProtectService),
                new NetherWartInteraction(worldEditService, coreProtectService),
                new CocoaBeanInteraction(worldEditService, coreProtectService),
                new LightningRodInteraction(worldEditService, coreProtectService),
                new MaterialMappingInteraction(worldEditService, coreProtectService),
                new ItemFrameInteraction(worldEditService, coreProtectService),
                new SeaPickleInteraction(worldEditService, coreProtectService),
                new TurtleEggInteraction(worldEditService, coreProtectService),
                new FlowerPotInteraction(worldEditService, coreProtectService),
                new PressurePlateInteraction(worldEditService, coreProtectService),
                new IronDoorsInteraction(worldEditService, coreProtectService)
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("greenfieldcore.advbuild")) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND && isEnabledFor(e.getPlayer().getUniqueId()) && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getBlockData() instanceof Candle && e.getPlayer().getInventory().getItemInMainHand().getType().isAir()) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            return;
        }
        if (e.getHand() == EquipmentSlot.OFF_HAND || e.getAction() == Action.PHYSICAL || !isEnabledFor(e.getPlayer().getUniqueId())) return;

        var mainHand = e.getPlayer().getInventory().getItemInMainHand();
        //if hand is worldedit wand, ignore
        if (mainHand.getType() == Material.WOODEN_AXE && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        InteractionHandler handler = interactionHandlers.stream().filter(h -> h.handles(e)).findFirst().orElse(interactionHandlers.get(0));

        switch (e.getAction()) {
            case RIGHT_CLICK_BLOCK -> handler.onRightClickBlock(e);
            case LEFT_CLICK_BLOCK -> handler.onLeftClickBlock(e);
            case RIGHT_CLICK_AIR -> handler.onRightClickAir(e);
            case LEFT_CLICK_AIR -> handler.onLeftClickAir(e);
            default -> {}
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (!e.getPlayer().hasPermission("greenfieldcore.advbuild")) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND || !isEnabledFor(e.getPlayer().getUniqueId())) return;

        InteractionHandler handler = interactionHandlers.stream().filter(h -> h.handles(e)).findFirst().orElse(interactionHandlers.get(0));
        handler.onRightClickEntity(e);
    }
}
