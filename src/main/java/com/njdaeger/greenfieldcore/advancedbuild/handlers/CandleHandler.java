package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.BlockHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CandleHandler extends BlockHandler {

    public static final Map<UUID, CandleSession> CANDLE_SESSIONS = new HashMap<>();

    public CandleHandler() {
        super(
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
    }

    @Override
    public boolean handleBlock(Player player, Location clickedBlockLocation, Location placementLocation, BlockFace clickedFace, Material placeMaterial) {
        CandleSession session;
        if (!CANDLE_SESSIONS.containsKey(player.getUniqueId())) CANDLE_SESSIONS.put(player.getUniqueId(), new CandleSession());
        session = CANDLE_SESSIONS.get(player.getUniqueId());

        Candle data = (Candle) placeMaterial.createBlockData();
        Candle candleSessionData;

        switch (placeMaterial) {
            case CANDLE, WHITE_CANDLE, MAGENTA_CANDLE, LIGHT_BLUE_CANDLE, ORANGE_CANDLE, YELLOW_CANDLE, BROWN_CANDLE, GREEN_CANDLE, BLACK_CANDLE, BLUE_CANDLE -> {
                candleSessionData = session.getCandleData(placeMaterial, false);
                if (candleSessionData == null) {
                    data.setLit(true);
                } else data = candleSessionData;
            }
            case CYAN_CANDLE -> {
                boolean northSouth = (player.getFacing() == BlockFace.NORTH || player.getFacing() == BlockFace.SOUTH);
                candleSessionData = session.getCandleData(placeMaterial, northSouth);
                if (candleSessionData == null) {
                    data.setLit(true);
                } else data = candleSessionData;
                int candles = data.getCandles();
                data.setCandles((northSouth ? 1 : 2) + (candles == 3 || candles == 4 ? 2 : 0));
            }
            case LIME_CANDLE, PINK_CANDLE, GRAY_CANDLE, RED_CANDLE, PURPLE_CANDLE, LIGHT_GRAY_CANDLE -> {
                candleSessionData = session.getCandleData(placeMaterial, false);
                int candles = switch (clickedFace) {
                    case NORTH -> 2;
                    case WEST -> 3;
                    case SOUTH -> 4;
                    case UP, DOWN -> switch (player.getFacing()) {
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

        log(false, player, placementLocation.getBlock());
        placementLocation.getBlock().setType(placeMaterial, false);
        placementLocation.getBlock().setBlockData(data, false);
        log(true, player, placementLocation.getBlock());
        playSoundFor(true, player, placeMaterial);
        return true;
    }

    public static class CandleSession {

        private final Map<Material, Candle> dataMap;

        //Cyan candles in the resource pack are a bit of a special candle. It is an X/Z axis based directional candle- meaning, it only has 2 direction variants. Per variant, it has
        //2 different states- dirty or clean. The rest of the candles do not have this quality, so there needs to be a special case for cyan.
        //TRUE indicates North/South Orientation
        //FALSE indicates East/West Orientation
        private final Map<Boolean, Candle> cyanCandleData;

        public CandleSession() {
            dataMap = new HashMap<>();
            cyanCandleData = new HashMap<>();
        }

        public Candle getCandleData(Material material, boolean northSouth) {
//            if (material == Material.CYAN_CANDLE) return cyanCandleData.get(northSouth);
//            else return dataMap.get(material);
            return dataMap.get(material);
        }

        public void updateCandle(Material candleMaterial, Candle candleData) {
//            if (candleMaterial == Material.CYAN_CANDLE) cyanCandleData.put(candleData.getCandles() % 2 == 1, candleData);
//            else dataMap.put(candleMaterial, candleData);
            dataMap.put(candleMaterial, candleData);
        }

    }
}
