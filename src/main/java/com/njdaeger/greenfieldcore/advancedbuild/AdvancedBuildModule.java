package com.njdaeger.greenfieldcore.advancedbuild;

import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.Module;
import com.njdaeger.greenfieldcore.advancedbuild.handlers.*;
import com.njdaeger.greenfieldcore.commandstore.PageFlag;
import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.LIGHT_PURPLE;

public class AdvancedBuildModule extends Module implements Listener {

    private static final java.awt.Color temp = ChatColor.BLUE.asBungee().getColor().brighter().brighter();
//    public static final Color LIGHT_BLUE = Color.fromRGB(temp.getRed(), temp.getGreen(), temp.getBlue());
    public static final TextColor LIGHT_BLUE = TextColor.color(temp.getRed(), temp.getGreen(), temp.getBlue());
    private final ChatPaginator<InteractionHandler, CommandContext> paginator;
    private AdvBuildConfig config;

    private List<InteractionHandler> interactionHandlers;

    public AdvancedBuildModule(GreenfieldCore plugin) {
        super(plugin);

        this.paginator = ChatPaginator.builder(this::lineGenerator)
                .addComponent(Component.text("Advanced Build Mode Handler List", NamedTextColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER)
                .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/avb " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/avb " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/avb " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/avb " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .build();
    }

    private TextComponent lineGenerator(InteractionHandler handler, CommandContext context) {
        var text = Component.text("?", LIGHT_BLUE, TextDecoration.BOLD).toBuilder()
                .clickEvent(ClickEvent.runCommand("/avb " + handler.getInteractionName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to view detailed information about this interaction handler.", NamedTextColor.GRAY)));
        text.append(Component.text(" | ", NamedTextColor.GRAY));

        text.append(Component.text(handler.getInteractionName(), NamedTextColor.BLUE).toBuilder()
                        .hoverEvent(HoverEvent.showText(handler.getInteractionDescription().color(NamedTextColor.GRAY))));

        return text.build();
    }

    @Override
    public void onEnable() {
        this.config = new AdvBuildConfig(plugin);

        this.interactionHandlers = List.of(
                new DefaultInteraction(),
                new CandleInteraction(),
                new AmethystInteraction(),
                new ChiseledBookshelfInteraction(plugin, config),
                new DoorInteraction(),
                new JigsawInteraction(),
                new PinkPetalsInteraction(),
                new MultipleFacingInteraction(),
                new RandomBlockInteractions(),
                new ChorusInteraction(),
                new PlantInteraction(),
                new BisectedInteraction(),
                new SignInteraction(),
                new WallInteraction(),
                new ObserverInteraction(),
                new MangroveRootsInteraction(),
                new DirectionalInteraction(),
                new PitcherPodInteraction(),
                new VineInteraction(),
                new CommandBlockInteraction(),
                new SherdInteraction(),
                new TorchInteraction(),
                new SwitchInteraction(),
                new CoralInteraction(),
                new RailInteraction(),
                new BrushableInteraction(),
                new NetherWartInteraction(),
                new CocoaBeanInteraction(),
                new LightningRodInteraction()
                //sus sand and gravel handler
        );

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        CommandBuilder.of("advbuild", "avb")
                .executor(context -> {
                    if (context.hasArgAt(0)) {
                        if (context.argAt(0).equalsIgnoreCase("help")) {
                            int page = context.getFlag("page", 1);
                            paginator.generatePage(context, interactionHandlers, page).sendTo(Component.text("Page does not exist.", NamedTextColor.RED), context.asPlayer());
                        } else {
                            var h = interactionHandlers.stream().filter(handler -> handler.getInteractionName().equalsIgnoreCase(context.argAt(0))).findFirst();
                            if (h.isPresent()) {
                                var text = Component.text(" ======== Handler: ", NamedTextColor.GRAY).toBuilder()
                                        .append(Component.text("[" + h.get().getInteractionName() + "]", NamedTextColor.BLUE))
                                        .append(Component.text(" ========", NamedTextColor.GRAY))
                                        .appendNewline()
                                        .append(Component.text("Description:", NamedTextColor.GRAY, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                                        .appendSpace()
                                        .append(h.get().getInteractionDescription().color(LIGHT_BLUE))
                                        .appendNewline()
                                        .append(Component.text("Usage:", NamedTextColor.GRAY, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                                        .appendSpace()
                                        .append(h.get().getInteractionUsage().color(LIGHT_BLUE))
                                        .appendNewline()
                                        .append(Component.text("Materials:", NamedTextColor.GRAY, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                                        .appendSpace()
                                        .append(h.get().getMaterialListText().color(LIGHT_BLUE));
                                context.asPlayer().sendMessage(text.build());
                            } else context.error("Unknown interaction handler '" + context.argAt(0) + "'. Do /avb help for a list of Interaction Handlers.");
                        }
                        return;
                    }
                    config.setEnabled(context.asPlayer(), !config.isEnabledFor(context.asPlayer()));
                    context.send(LIGHT_PURPLE + "[AdvBuild] " + GRAY + (config.isEnabledFor(context.asPlayer()) ? "Enabled Advanced Building." : "Disabled Advanced Building."));
                })
                .completer(context -> {
                    var list = new java.util.ArrayList<>(interactionHandlers.stream().map(InteractionHandler::getInteractionName).toList());
                    list.add("help");
                    context.completionAt(0, list.toArray(new String[0]));
                })
                .flag(new PageFlag(this))
                .permissions("greenfieldcore.advbuild")
                .max(1)
                .usage("/advbuild [help|<interaction handler>]")
                .description("Enables advanced building mode. (Placing blocks that aren't normally placeable without specific surfaces etc)")
                .build().register(plugin);
    }

    @Override
    public void onDisable() {
        config.save();
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == Material.BUDDING_AMETHYST) {
            e.setBuild(false);
            e.setCancelled(true);
            return;
        }

        Material newType = switch (e.getBlockPlaced().getType()) {
            case COPPER_BLOCK -> Material.WAXED_COPPER_BLOCK;
            case EXPOSED_COPPER -> Material.WAXED_EXPOSED_COPPER;
            case WEATHERED_COPPER -> Material.WAXED_WEATHERED_COPPER;
            case OXIDIZED_COPPER -> Material.WAXED_OXIDIZED_COPPER;
            case CUT_COPPER -> Material.WAXED_CUT_COPPER;
            case EXPOSED_CUT_COPPER ->  Material.WAXED_EXPOSED_CUT_COPPER;
            case WEATHERED_CUT_COPPER -> Material.WAXED_WEATHERED_CUT_COPPER;
            case OXIDIZED_CUT_COPPER -> Material.WAXED_OXIDIZED_CUT_COPPER;
            case CUT_COPPER_STAIRS -> Material.WAXED_CUT_COPPER_STAIRS;
            case EXPOSED_CUT_COPPER_STAIRS -> Material.WAXED_EXPOSED_CUT_COPPER_STAIRS;
            case WEATHERED_CUT_COPPER_STAIRS -> Material.WAXED_WEATHERED_CUT_COPPER_STAIRS;
            case OXIDIZED_CUT_COPPER_STAIRS -> Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS;
            case CUT_COPPER_SLAB -> Material.WAXED_CUT_COPPER_SLAB;
            case EXPOSED_CUT_COPPER_SLAB -> Material.WAXED_EXPOSED_CUT_COPPER_SLAB;
            case WEATHERED_CUT_COPPER_SLAB -> Material.WAXED_WEATHERED_CUT_COPPER_SLAB;
            case OXIDIZED_CUT_COPPER_SLAB -> Material.WAXED_OXIDIZED_CUT_COPPER_SLAB;
            default -> null;
        };
        if (newType != null) {
            BlockData newData = Bukkit.createBlockData(e.getBlockPlaced().getBlockData().getAsString().replace("minecraft:", "minecraft:waxed_"));
            Location loc = e.getBlock().getLocation().clone();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (switch (loc.getBlock().getType()) {
                    case COPPER_BLOCK, EXPOSED_COPPER, WEATHERED_COPPER, OXIDIZED_COPPER -> false;
                    case CUT_COPPER, EXPOSED_CUT_COPPER, WEATHERED_CUT_COPPER, OXIDIZED_CUT_COPPER -> false;
                    case CUT_COPPER_STAIRS, EXPOSED_CUT_COPPER_STAIRS, WEATHERED_CUT_COPPER_STAIRS, OXIDIZED_CUT_COPPER_STAIRS -> false;
                    case CUT_COPPER_SLAB, EXPOSED_CUT_COPPER_SLAB, WEATHERED_CUT_COPPER_SLAB, OXIDIZED_CUT_COPPER_SLAB -> false;
                    default -> true;
                }) return;
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logRemoval(e.getPlayer().getName(), loc, loc.getBlock().getType(), loc.getBlock().getBlockData());
                loc.getBlock().setType(newType, false);
                loc.getBlock().setBlockData(newData, false);
                if (plugin.isCoreProtectEnabled()) plugin.getCoreApi().logPlacement(e.getPlayer().getName(), e.getBlock().getLocation(), newType, newData);
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND && config.isEnabledFor(e.getPlayer()) && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getBlockData() instanceof Candle && e.getPlayer().getInventory().getItemInMainHand().getType().isAir()) {
            e.setCancelled(true);
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            return;
        }
        if (e.getHand() == EquipmentSlot.OFF_HAND || e.getAction() == Action.PHYSICAL || !config.isEnabledFor(e.getPlayer())) return;

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
}
