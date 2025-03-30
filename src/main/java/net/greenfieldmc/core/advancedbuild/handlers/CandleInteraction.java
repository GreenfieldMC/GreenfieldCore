package net.greenfieldmc.core.advancedbuild.handlers;

import net.greenfieldmc.core.advancedbuild.InteractionHandler;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Candle;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CandleInteraction extends InteractionHandler {

    private final Map<UUID, CandleSession> sessions;

    public CandleInteraction(IWorldEditService worldEditService, ICoreProtectService coreProtectService) {
        super(worldEditService, coreProtectService, (event) -> {
                var player = event.getPlayer();
                var mainHand = player.getInventory().getItemInMainHand().getType();
                return !player.isSneaking() &&
                        mainHand == Material.AIR &&
                        event.getClickedBlock() != null &&
                        event.getClickedBlock().getBlockData() instanceof Candle;
            },
                Material.CANDLE,
                Material.BLACK_CANDLE,
                Material.BLUE_CANDLE,
                Material.BROWN_CANDLE,
                Material.CYAN_CANDLE,
                Material.GRAY_CANDLE,
                Material.GREEN_CANDLE,
                Material.LIGHT_BLUE_CANDLE,
                Material.LIGHT_GRAY_CANDLE,
                Material.LIME_CANDLE,
                Material.MAGENTA_CANDLE,
                Material.ORANGE_CANDLE,
                Material.PINK_CANDLE,
                Material.PURPLE_CANDLE,
                Material.RED_CANDLE,
                Material.WHITE_CANDLE,
                Material.YELLOW_CANDLE
        );
        this.sessions = new HashMap<>();
    }

    @Override
    public TextComponent getInteractionDescription() {
        return Component.text("Allow the placement of candles on any block face.");
    }

    @Override
    public TextComponent getInteractionUsage() {
        return Component.text("If hand is empty: right click a candle toggles the \"lit\" state of the candle.", NamedTextColor.GRAY)
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If hand is not empty: right clicking a candle cycles the candle amount of that candle.", NamedTextColor.GRAY))
                .append(Component.text(" ----- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("If hand is holding a candle and shifting: right clicking will place a candle in the desired location.", NamedTextColor.GRAY));
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        var clicked = event.getClickedBlock();
        if (clicked == null) throw new IllegalStateException("Clicked block was null");

        var face = event.getBlockFace();
        var placeableLocation = getPlaceableLocation(clicked.getLocation(), face);

        //if the user is not sneaking
        if (!event.getPlayer().isSneaking()) {
            if (!(clicked.getBlockData() instanceof Candle candle)) return;

            //and the user is holding air (and has clicked a candle)
            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                //and the candle is not lit
                if (!candle.isLit()) {
                    candle.setLit(true);
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_BRUSH_BRUSHING_SAND, 1.9f, 0.5f);
                } else {
                    //set unlit
                    candle.setLit(false);
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_CANDLE_EXTINGUISH, 1, 1);
                }
                //otherwise, the user is not holding air, they are holding a candle - so iterate through the candle amounts
            } else candle.setCandles(candle.getCandles() == candle.getMaximumCandles() ? 1 : candle.getCandles() + 1);

            getSession(event.getPlayer().getUniqueId()).updateCandle(event.getClickedBlock().getType(), candle);

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            placeBlockAt(event.getPlayer(), event.getClickedBlock().getLocation(), candle.getMaterial(), candle);

            //if the user is sneaking
        } else if (canPlaceAt(placeableLocation)){
            var session = getSession(event.getPlayer().getUniqueId());
            var handMat = event.getPlayer().getInventory().getItemInMainHand().getType();
            var data = (Candle) handMat.createBlockData();
            Candle candleSessionData;

            switch (handMat) {
                case CANDLE, WHITE_CANDLE, MAGENTA_CANDLE, LIGHT_BLUE_CANDLE, ORANGE_CANDLE, BROWN_CANDLE, GREEN_CANDLE, BLACK_CANDLE, BLUE_CANDLE, YELLOW_CANDLE -> {
                    candleSessionData = session.getCandleData(handMat, false);
                    if (candleSessionData == null) {
                        data.setLit(true);
                    } else data = candleSessionData;
                }
                case CYAN_CANDLE -> {
                    boolean northSouth = (event.getPlayer().getFacing() == BlockFace.NORTH || event.getPlayer().getFacing() == BlockFace.SOUTH);
                    candleSessionData = session.getCandleData(handMat, northSouth);
                    if (candleSessionData == null) {
                        data.setLit(true);
                    } else data = candleSessionData;
                    int candles = data.getCandles();
                    data.setCandles((northSouth ? 1 : 2) + (candles == 3 || candles == 4 ? 2 : 0));
                }
                case LIME_CANDLE, PINK_CANDLE, GRAY_CANDLE, RED_CANDLE, PURPLE_CANDLE, LIGHT_GRAY_CANDLE -> {
                    candleSessionData = session.getCandleData(handMat, false);
                    int candles = switch (event.getBlockFace()) {
                        case NORTH -> 2;
                        case WEST -> 3;
                        case SOUTH -> 4;
                        case UP, DOWN -> switch (event.getPlayer().getFacing()) {
                            case NORTH -> 4;
                            case EAST -> 3;
                            case SOUTH -> 2;
                            default -> 1;
                        };
                        default -> 1;
                    };
                    if (candleSessionData == null) {//session should only change the candle being on or off, since the direction of the candle will change depending on where is clicked.
                        data.setLit(true);

                    } else data = candleSessionData;
                    data.setCandles(candles);
                }
            }

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            placeBlockAt(event.getPlayer(), placeableLocation, handMat, data);
        }
    }

    private CandleSession getSession(UUID uuid) {
        if (!sessions.containsKey(uuid)) sessions.put(uuid, new CandleSession());
        return sessions.get(uuid);
    }

    public static class CandleSession {

        private final Map<Material, Candle> dataMap;

        public CandleSession() {
            dataMap = new HashMap<>();
        }

        public Candle getCandleData(Material material, boolean northSouth) {
            return dataMap.get(material);
        }

        public void updateCandle(Material candleMaterial, Candle candleData) {
            dataMap.put(candleMaterial, candleData);
        }

    }

}
