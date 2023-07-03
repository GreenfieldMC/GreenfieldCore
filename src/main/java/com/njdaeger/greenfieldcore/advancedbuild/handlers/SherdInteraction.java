package com.njdaeger.greenfieldcore.advancedbuild.handlers;

import com.njdaeger.greenfieldcore.advancedbuild.InteractionHandler;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.type.DecoratedPot;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SherdInteraction extends InteractionHandler {

    private final Map<UUID, Long> lastPlaced = new HashMap<>();

    public SherdInteraction() {
        super(
                Material.ANGLER_POTTERY_SHERD,
                Material.ARCHER_POTTERY_SHERD,
                Material.ARMS_UP_POTTERY_SHERD,
                Material.BLADE_POTTERY_SHERD,
                Material.BREWER_POTTERY_SHERD,
                Material.BURN_POTTERY_SHERD,
                Material.DANGER_POTTERY_SHERD,
                Material.EXPLORER_POTTERY_SHERD,
                Material.FRIEND_POTTERY_SHERD,
                Material.HEART_POTTERY_SHERD,
                Material.HEARTBREAK_POTTERY_SHERD,
                Material.HOWL_POTTERY_SHERD,
                Material.MINER_POTTERY_SHERD,
                Material.MOURNER_POTTERY_SHERD,
                Material.PLENTY_POTTERY_SHERD,
                Material.PRIZE_POTTERY_SHERD,
                Material.SHEAF_POTTERY_SHERD,
                Material.SHELTER_POTTERY_SHERD,
                Material.SKULL_POTTERY_SHERD,
                Material.SNORT_POTTERY_SHERD
        );
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) {
            var last = lastPlaced.get(event.getPlayer().getUniqueId());
            if (last != null && System.currentTimeMillis() - last < 50) return;
            var placementLocation = getPlaceableLocation(event);
            if (placementLocation == null) return;
            var sherdMat = Material.DECORATED_POT;
            var data = (DecoratedPot) sherdMat.createBlockData();
            data.setWaterlogged(false);
            data.setFacing(event.getPlayer().getFacing());
            placeBlockAt(event.getPlayer(), placementLocation, sherdMat, data, Sound.BLOCK_DECORATED_POT_PLACE);
            try {
                setShard(getHandMat(event), placementLocation);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            lastPlaced.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    private static void setShard(Material material, Location location) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        var list = new NBTTagList();
        list.add(NBTTagString.a(material.getKey().getKey()));
        list.add(NBTTagString.a(material.getKey().getKey()));
        list.add(NBTTagString.a(material.getKey().getKey()));
        list.add(NBTTagString.a(material.getKey().getKey()));
        NBTEditor.set(location.getBlock(), list, "sherds");
    }

}
