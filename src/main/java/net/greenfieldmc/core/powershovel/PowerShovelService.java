package net.greenfieldmc.core.powershovel;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.shared.services.ICoreProtectService;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import net.greenfieldmc.core.shared.services.IWorldEditService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.greenfieldmc.core.ComponentUtils.moduleMessage;

public class PowerShovelService extends ModuleService<PowerShovelService> implements IModuleService<PowerShovelService>, Listener {

    private PlayerProfile PROFILE;
    private static final PowerShovel SHOVEL = new PowerShovel();
    private static final Map<UUID, Long> TIMEOUTS = new HashMap<>();
    private static final Map<UUID, Mode> PLAYER_MODES = new HashMap<>();
    private static final Map<UUID, Location> LINE_STARTS = new HashMap<>();

    private final ICoreProtectService coreProtectService;
    private final IWorldEditService worldEditService;

    private enum Mode { POWERCONNECTOR, POWERLINE }

    public PowerShovelService(Plugin plugin, Module module, ICoreProtectService coreProtectService, IWorldEditService worldEditService) {
        super(plugin, module);
        this.coreProtectService = coreProtectService;
        this.worldEditService = worldEditService;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && PROFILE != null;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        module.getLogger().info("Loading custom head...");
        PROFILE = Bukkit.createProfile(UUID.fromString("e4ac149f-2815-4e06-8ce1-770a628a2cfc"));
        var texture = PROFILE.getTextures();
        texture.setSkin(URI.create("http://textures.minecraft.net/texture/633c0bb37ebe1193ee4618103460a7f129277a8c7fd081b6aedb34a92bd5").toURL());
        PROFILE.setTextures(texture);
        PROFILE.complete();
        module.getLogger().info("Custom head loaded.");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        CommandBuilder.of("powershovel", "ps")
                .permission("greenfieldcore.powershovel")
                .description("Select power shovel mode and receive shovel")
            .then("powerconnector")
                .canExecute(ctx -> {
                    UUID id = ctx.asPlayer().getUniqueId();
                    PLAYER_MODES.put(id, Mode.POWERCONNECTOR);
                    ctx.send(moduleMessage("PowerShovel", "Mode set to powerconnector. You now have the power shovel."));
                    ctx.asPlayer().getInventory().addItem(SHOVEL);
                })
            .end()
            .then("powerline")
                .canExecute(ctx -> {
                    UUID id = ctx.asPlayer().getUniqueId();
                    PLAYER_MODES.put(id, Mode.POWERLINE);
                    LINE_STARTS.remove(id);
                    ctx.send(moduleMessage("PowerShovel", "Mode set to powerline. You now have the power shovel. Left-click to set start, right-click to place line."));
                    ctx.asPlayer().getInventory().addItem(SHOVEL);
                })
            .end()
            .register(plugin);
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {

    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {
        if (!isEnabled()) return;
        var player = e.getPlayer();
        UUID id = player.getUniqueId();
        if (!player.hasPermission("greenfieldcore.powershovel") || !SHOVEL.equals(player.getInventory().getItemInMainHand()) || e.getHand() == EquipmentSlot.OFF_HAND) return;
        Mode mode = PLAYER_MODES.get(id);
        if (mode == null) return;
        e.setCancelled(true);
        if (mode == Mode.POWERLINE) {
            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR) {
                handlePowerlineAirClick(id, player);
                return;
            }
            handlePowerlineMode(e, player, id);
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        handleConnectorMode(e, player);
    }

    private void handlePowerlineAirClick(UUID id, org.bukkit.entity.Player player) {
        LINE_STARTS.remove(id);
        player.sendMessage(moduleMessage("PowerShovel", "Selection cleared."));
    }

    private void handlePowerlineMode(PlayerInteractEvent e, org.bukkit.entity.Player player, UUID id) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            LINE_STARTS.put(id, e.getClickedBlock().getLocation());
            player.sendMessage(moduleMessage("PowerShovel", "Start location set."));
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            createPowerline(e, player, id);
        }
    }

    private void createPowerline(PlayerInteractEvent e, org.bukkit.entity.Player player, UUID id) {
        Location start = LINE_STARTS.get(id);
        if (start == null || start.getWorld() != e.getClickedBlock().getWorld()) {
            LINE_STARTS.remove(id);
            player.sendMessage(moduleMessage("PowerShovel", "No start location set."));
            return;
        }
        Location end = e.getClickedBlock().getLocation();
        var dist = start.distance(end);
        var dz = end.getBlockY() - start.getBlockY();
        var C = dist * 2;//to make the dip more pronounced, you can change 2 to a smaller number
        var sinhL2C = Math.sinh(dist / (2.0 * C));
        var asinhArg = (dz) / (2.0 * C * sinhL2C);
        var Lb = dist / 2.0 - C * Math.log(asinhArg + Math.sqrt(asinhArg * asinhArg + 1));
        var yStart = start.getBlockY();
        var yCatenaryStart = C * (Math.cosh((-Lb) / C) - Math.cosh(Lb / C));
        var yCatenaryEnd = C * (Math.cosh((dist - Lb) / C) - Math.cosh(Lb / C));        // Calculate step count based on maximum coordinate difference to prevent thick lines
        int xDiff = Math.abs(end.getBlockX() - start.getBlockX());
        int zDiff = Math.abs(end.getBlockZ() - start.getBlockZ());
        int minSteps = Math.max(xDiff, zDiff);
        
        // Also consider arc length for steep curves, but cap it to prevent excessive density
        double arcLength = C * (Math.sinh((dist - Lb) / C) + Math.sinh(Lb / C));
        int arcSteps = (int) Math.ceil(arcLength);
        
        // Use the maximum of coordinate-based steps and arc-based steps, but with reasonable limits
        int steps = Math.max(minSteps, Math.min(arcSteps, (int)(dist * 3))); // Cap at 3x distance to prevent excessive steps
        
        var localSession = worldEditService.getLocalSession(player);
        try (var session = localSession.createEditSession(BukkitAdapter.adapt(player))) {

            var block = BukkitAdapter.adapt(Material.COBWEB.createBlockData()).toBaseBlock();
            
            System.out.println("Distance: " + dist + ", XDiff: " + xDiff + ", ZDiff: " + zDiff + ", MinSteps: " + minSteps + ", ArcSteps: " + arcSteps + ", FinalSteps: " + steps);

            var placements = createContiguousPath(start, end, steps, yStart, yCatenaryStart, yCatenaryEnd, C, Lb, dist);
            
            for (Location location : placements) {
                session.setBlock(new BlockVector3(location.getBlockX(), location.getBlockY(), location.getBlockZ()), block);
            }

            localSession.remember(session);
        } catch (WorldEditException ex) {
            player.sendMessage(Component.text("An error occurred while making the powerline. " + ex.getMessage(), NamedTextColor.RED));
            return;
        }

        LINE_STARTS.remove(id);
        player.sendMessage(moduleMessage("PowerShovel", "Powerline created."));
    }

    private double calculateCatenaryY(Location start, Location end, double droopStrength, double t) {
        var distance = start.distance(end);
        var dz = end.getBlockY() - start.getBlockY();
        var c = distance * droopStrength;
        var sinhL2C = Math.sinh(distance / (2.0 * c));
        var asinhArg = (dz) / (2.0 * c * sinhL2C);
        var lb = distance / 2.0 - c * Math.log(asinhArg + Math.sqrt(asinhArg * asinhArg + 1));
        var yStart = start.getBlockY();
        var yCatenaryStart = c * (Math.cosh((-lb) / c) - Math.cosh(lb / c));
        var yCatenaryEnd = c * (Math.cosh((distance - lb) / c) - Math.cosh(lb / c));
        var s = distance * t;
        return yStart + (yCatenaryEnd - yCatenaryStart) * t + c * (Math.cosh((s - lb) / c) - Math.cosh(lb / c)) - yCatenaryStart * (1 - t) - yCatenaryEnd * t;
    }

    private Location getLocationAtStep(Location startLocation, Location endLocation, int currentStep, int stepCount) {
        double t = (double) currentStep / stepCount;
        int x = (int) (startLocation.getBlockX() + (endLocation.getBlockX() - startLocation.getBlockX()) * t);
        int z = (int) (startLocation.getBlockZ() + (endLocation.getBlockZ() - startLocation.getBlockZ()) * t);
        double y = calculateCatenaryY(startLocation, endLocation, 2.0, t);
        return new Location(startLocation.getWorld(), x, (int)y, z);
    }

    private boolean isBlockInNearbySpace(Location sourceBlock, Location targetBlock) {
        int xDiff = Math.abs(sourceBlock.getBlockX() - targetBlock.getBlockX());
        int yDiff = Math.abs(sourceBlock.getBlockY() - targetBlock.getBlockY());
        int zDiff = Math.abs(sourceBlock.getBlockZ() - targetBlock.getBlockZ());
        return (xDiff <= 1 && yDiff <= 1 && zDiff <= 1);
    }

    private int blocksInNearbySpace(Location startLocation, Location endLocation, Location sourceBlock, int currentStep, int stepCount) {
        var lastLastLocation = getLocationAtStep(startLocation, endLocation, currentStep - 2, stepCount);
        var lastLocation = getLocationAtStep(startLocation, endLocation, currentStep - 1, stepCount);
//        var nextLocation = getLocationAtStep(startLocation, endLocation, currentStep + 1, stepCount);
//        var nextNextLocation = getLocationAtStep(startLocation, endLocation, currentStep + 2, stepCount);

        if (lastLocation.equals(sourceBlock) || lastLocation.equals(lastLastLocation)) {
            lastLocation = getLocationAtStep(startLocation, endLocation, currentStep - 2, stepCount);
            lastLastLocation = getLocationAtStep(startLocation, endLocation, currentStep - 3, stepCount);
        }

//        if (nextLocation.equals(sourceBlock) || nextLocation.equals(nextNextLocation)) {
//            nextLocation = getLocationAtStep(startLocation, endLocation, currentStep + 2, stepCount);
//            nextNextLocation = getLocationAtStep(startLocation, endLocation, currentStep + 3, stepCount);
//        }

        int count = 0;

        if (isBlockInNearbySpace(sourceBlock, lastLastLocation)) count++;
        if (isBlockInNearbySpace(sourceBlock, lastLocation)) count++;
//        if (!sourceBlock.equals(nextLocation) && isBlockInNearbySpace(sourceBlock, nextLocation)) count++;
//        if (isBlockInNearbySpace(sourceBlock, nextNextLocation)) count++;

        return count;
    }

    private void handleConnectorMode(PlayerInteractEvent e, org.bukkit.entity.Player player) {
        BlockFace clickedFace = e.getBlockFace();
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) return;
        Long tmt = TIMEOUTS.get(e.getPlayer().getUniqueId());
        if (tmt == null) TIMEOUTS.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        else if (System.currentTimeMillis() - tmt > 500) TIMEOUTS.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        else return;
        if (clickedFace != BlockFace.EAST && clickedFace != BlockFace.NORTH && clickedFace != BlockFace.SOUTH && clickedFace != BlockFace.WEST) {
            e.getPlayer().sendMessage(ChatColor.RED + "You cannot click on that block face. You must click on any of the following: East, North, South, or West.");
            return;
        }
        if (clickedBlock.getBlockData() instanceof Fence fence) {
            handleTransformerPlacement(e, player, clickedBlock, clickedFace, fence);
        } else {
            handleMeterPlacement(e, player, clickedBlock, clickedFace);
        }
    }

    private void handleTransformerPlacement(PlayerInteractEvent e, org.bukkit.entity.Player player, Block clickedBlock, BlockFace clickedFace, Fence fence) {
        coreProtectService.logRemoval(player.getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
        fence.setFace(clickedFace, true);
        clickedBlock.setBlockData(fence, false);
        coreProtectService.logPlacement(player.getName(), clickedBlock.getLocation(), clickedBlock.getType(), clickedBlock.getBlockData());
        Location transformerLocation = new Location(clickedBlock.getWorld(), clickedBlock.getX() + clickedFace.getDirection().getBlockX(), clickedBlock.getY(), clickedBlock.getZ()  + clickedFace.getDirection().getBlockZ());
        coreProtectService.logRemoval(player.getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
        transformerLocation.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL, false);
        Wall wall = (Wall) transformerLocation.getBlock().getBlockData();
        wall.setUp(true);
        wall.setHeight(clickedFace.getOppositeFace(), Wall.Height.LOW);
        transformerLocation.getBlock().setBlockData(wall, false);
        coreProtectService.logPlacement(player.getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
        transformerLocation = new Location(transformerLocation.getWorld(), transformerLocation.getX(), transformerLocation.getY() + 1, transformerLocation.getZ());
        coreProtectService.logRemoval(player.getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
        transformerLocation.getBlock().setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) transformerLocation.getBlock().getState();
        skull.setPlayerProfile(PROFILE);
        skull.update();
        coreProtectService.logPlacement(player.getName(), transformerLocation, transformerLocation.getBlock().getType(), transformerLocation.getBlock().getBlockData());
    }

    private void handleMeterPlacement(PlayerInteractEvent e, org.bukkit.entity.Player player, Block clickedBlock, BlockFace clickedFace) {
        Location meterLocation = new Location(clickedBlock.getWorld(), clickedBlock.getX() + clickedFace.getDirection().getBlockX(), clickedBlock.getY(), clickedBlock.getZ()  + clickedFace.getDirection().getBlockZ());
        coreProtectService.logRemoval(player.getName(), meterLocation, meterLocation.getBlock().getType(), meterLocation.getBlock().getBlockData());
        meterLocation.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL, false);
        Wall wall = (Wall) meterLocation.getBlock().getBlockData();
        wall.setUp(true);
        if (clickedFace != BlockFace.NORTH) wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
        if (clickedFace != BlockFace.SOUTH) wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
        if (clickedFace != BlockFace.EAST) wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
        if (clickedFace != BlockFace.WEST) wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
        meterLocation.getBlock().setBlockData(wall, false);
        coreProtectService.logPlacement(player.getName(), meterLocation, meterLocation.getBlock().getType(), meterLocation.getBlock().getBlockData());
    }

    private ArrayList<Location> createContiguousPath(Location start, Location end, int steps, int yStart, 
                                                   double yCatenaryStart, double yCatenaryEnd, double C, double Lb, double dist) {
        var placements = new ArrayList<Location>();
        Location lastPlaced = null;
        
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) (start.getBlockX() + (end.getBlockX() - start.getBlockX()) * t);
            int z = (int) (start.getBlockZ() + (end.getBlockZ() - start.getBlockZ()) * t);
            double s = dist * t;
            double yCatenary = yStart + (yCatenaryEnd - yCatenaryStart) * t + C * (Math.cosh((s - Lb) / C) - Math.cosh(Lb / C)) - yCatenaryStart * (1 - t) - yCatenaryEnd * t;
            int yBlock = (int) Math.round(yCatenary);
            var currentLocation = new Location(start.getWorld(), x, yBlock, z);
            
            // Always place the first block
            if (lastPlaced == null) {
                placements.add(currentLocation);
                lastPlaced = currentLocation;
                continue;
            }
            
            // Check if current location is the same as the last placed (duplicate)
            if (currentLocation.equals(lastPlaced)) {
                continue;
            }
            
            // Check if current location is adjacent to the last placed
            if (isAdjacent(currentLocation, lastPlaced)) {
                placements.add(currentLocation);
                lastPlaced = currentLocation;
            } else {
                // If not adjacent, we need to fill the gap with intermediate blocks
                var intermediateBlocks = fillGap(lastPlaced, currentLocation);
                for (Location intermediate : intermediateBlocks) {
                    if (!intermediate.equals(lastPlaced) && !placements.contains(intermediate)) {
                        placements.add(intermediate);
                        lastPlaced = intermediate;
                    }
                }
                // Add the target block if it's not already added
                if (!currentLocation.equals(lastPlaced)) {
                    placements.add(currentLocation);
                    lastPlaced = currentLocation;
                }
            }
        }
        
        return placements;
    }
    
    private boolean isAdjacent(Location loc1, Location loc2) {
        int xDiff = Math.abs(loc1.getBlockX() - loc2.getBlockX());
        int yDiff = Math.abs(loc1.getBlockY() - loc2.getBlockY());
        int zDiff = Math.abs(loc1.getBlockZ() - loc2.getBlockZ());
        return xDiff <= 1 && yDiff <= 1 && zDiff <= 1 && (xDiff + yDiff + zDiff) > 0;
    }
    
    private ArrayList<Location> fillGap(Location from, Location to) {
        var gapBlocks = new ArrayList<Location>();
        
        int xDiff = to.getBlockX() - from.getBlockX();
        int yDiff = to.getBlockY() - from.getBlockY();
        int zDiff = to.getBlockZ() - from.getBlockZ();
        
        // Determine the number of steps needed to bridge the gap
        int steps = Math.max(Math.max(Math.abs(xDiff), Math.abs(yDiff)), Math.abs(zDiff));
        
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int x = from.getBlockX() + (int) Math.round(xDiff * t);
            int y = from.getBlockY() + (int) Math.round(yDiff * t);
            int z = from.getBlockZ() + (int) Math.round(zDiff * t);
            gapBlocks.add(new Location(from.getWorld(), x, y, z));
        }
        
        return gapBlocks;
    }

}
