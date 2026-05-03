package net.greenfieldmc.core.templates.services.impl;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import net.greenfieldmc.core.templates.services.ITemplateService;
import net.greenfieldmc.core.templates.services.ITemplateWorldEditService;
import org.jetbrains.annotations.Nullable;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import net.greenfieldmc.core.templates.models.Template;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TemplateWorldEditService extends WorldEditServiceImpl implements ITemplateWorldEditService {

    private final ITemplateService templateService;
    private final List<Path> schematicFiles = new ArrayList<>();
    private Thread watcherThread;

    public TemplateWorldEditService(Plugin plugin, Module module, ITemplateService templateService) {
        super(plugin, module);
        this.templateService = templateService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        super.tryEnable(plugin, module);
        var schemPath = impl.getWorldEdit().getWorkingDirectoryPath(impl.getWorldEdit().getConfiguration().saveDir);
        schematicFiles.addAll(getAllFiles(schemPath));

        module.getLogger().info("Starting schematic watcher for path: " + schemPath);
        var ws = FileSystems.getDefault().newWatchService();
        schemPath.register(ws, java.nio.file.StandardWatchEventKinds.ENTRY_CREATE, java.nio.file.StandardWatchEventKinds.ENTRY_DELETE);
        this.watcherThread = new Thread(() -> {
            module.getLogger().info("[SchematicWatcher] Watching for schematic file changes in: " + schemPath);
            while (isEnabled()) {
                try {
                    WatchKey key = ws.poll(5, TimeUnit.SECONDS);
                    if (key != null) {
                        for (var event : key.pollEvents()) {
                            var path = schemPath.resolve(event.context().toString());
                            if (event.kind() == java.nio.file.StandardWatchEventKinds.ENTRY_CREATE) {
                                if ((path.toString().endsWith(".schematic") || path.toString().endsWith(".schem")) && !schematicFiles.contains(path)) {
                                    schematicFiles.add(path);
                                    module.getLogger().info("[SchematicWatcher] Schematic file added: " + path);
                                }

                            } else if (event.kind() == java.nio.file.StandardWatchEventKinds.ENTRY_DELETE) {
                                module.getLogger().info("[SchematicWatcher] Schematic file removed: " + path);
                                schematicFiles.remove(path);
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    module.getLogger().severe("Watcher thread interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            try {
                ws.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            module.getLogger().info("[SchematicWatcher] Stopped watching for schematic file changes.");
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> watcherThread.start(), 120L);

    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        super.tryDisable(plugin, module);
    }

    public List<Path> getSchematicFiles() {
        if (!isEnabled()) return new ArrayList<>();
        return schematicFiles;
    }

    @Override
    public void loadToClipboard(Template template, Player player) throws Exception {
        if (!isEnabled()) throw new Exception("WorldEdit is not enabled.");
        var bukkitPlayer = BukkitAdapter.adapt(player);
        var localSession = impl.getWorldEdit().getSessionManager().getIfPresent(bukkitPlayer);
        if (localSession == null) throw new Exception("The player is not in a WorldEdit session.");
        if (!template.isLoaded()) template.loadClipboard();
        var holder = new ClipboardHolder(template.getClipboard());
        localSession.setClipboard(holder);
    }

    @Override
    public WorldEditTemplateBrush getBrush(Player player) throws Exception {
        if (!isEnabled()) throw new Exception("WorldEdit is not enabled.");
        var bukkitPlayer = BukkitAdapter.adapt(player);
        var localSession = impl.getWorldEdit().getSessionManager().getIfPresent(bukkitPlayer);
        if (localSession == null) throw new Exception("The player is not in a WorldEdit session.");
        var brush = localSession.getBrush(bukkitPlayer.getItemInHand(HandSide.MAIN_HAND).getType());
        if (brush == null) throw new Exception("There is no brush equipped in this player's hand.");
        if (brush.getBrush() instanceof WorldEditTemplateBrush templateBrush) {
            return templateBrush;
        } else {
            throw new Exception("The brush in this player's hand is not a WorldEditTemplateBrush.");
        }
    }

    @Override
    public void addBrush(Player player, int brushId) throws Exception {
        if (!isEnabled()) return;
        var bukkitPlayer = BukkitAdapter.adapt(player);
        var localSession = impl.getWorldEdit().getSessionManager().getIfPresent(bukkitPlayer);
        if (localSession == null) throw new Exception("The player is not in a WorldEdit session.");
        try {
            localSession.forceBrush(
                    bukkitPlayer.getItemInHand(HandSide.MAIN_HAND).getType(),
                    new WorldEditTemplateBrush(templateService, player.getUniqueId(), brushId),
                    "greenfieldcore.template.brush"
            );
        } catch (InvalidToolBindException e) {
            throw new Exception("Failed to bind brush to item in hand.", e);
        }
    }

    @Override
    public void pasteTemplate(Template template, Location anchor, Player player, boolean ignoreAir, int rotationDegrees) throws Exception {
        if (!isEnabled()) throw new Exception("WorldEdit is not enabled.");
        if (!template.isLoaded()) throw new Exception("Template '" + template.getTemplateName() + "' is not loaded. Load the template before pasting.");

        var weWorld = BukkitAdapter.adapt(anchor.getWorld());
        var wePosition = BlockVector3.at(anchor.getBlockX(), anchor.getBlockY(), anchor.getBlockZ());
        var actor = BukkitAdapter.adapt(player);
        var localSession = impl.getWorldEdit().getSessionManager().get(actor);

        try (var editSession = impl.getWorldEdit().newEditSessionBuilder()
                .world(weWorld)
                .actor(actor)
                .build()) {
            // Suppress neighbour/physics block-updates so schematics with redstone,
            // observers, pistons, etc. are placed exactly as saved.
            editSession.setSideEffectApplier(
                    SideEffectSet.defaults().with(SideEffect.NEIGHBORS, SideEffect.State.OFF)
            );
            var holder = new ClipboardHolder(template.getClipboard());
            // Apply the same Y-rotation that the player confirmed during placement mode.
            // This mirrors exactly what DisplayLifecycleManager uses for the hologram.
            if (rotationDegrees != 0) {
                holder.setTransform(new AffineTransform().rotateY(rotationDegrees));
            }
            var operation = holder.createPaste(editSession)
                    .to(wePosition)
                    .ignoreAirBlocks(ignoreAir)
                    .build();
            Operations.completeLegacy(operation);
            localSession.remember(editSession);
        }
    }

    @Override
    public Path copySelectionToSchematic(Player player, String schematicName, @Nullable String mask) throws Exception {
        if (!isEnabled()) throw new Exception("WorldEdit is not enabled.");

        var actor = BukkitAdapter.adapt(player);
        var localSession = impl.getWorldEdit().getSessionManager().getIfPresent(actor);
        if (localSession == null) throw new Exception("You have no active WorldEdit session. Make a WorldEdit selection first.");

        // Verify the player has an active, complete selection
        try {
            localSession.getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            throw new Exception("Your WorldEdit selection is incomplete. Select a region first.");
        }

        // Dispatch //copy, optionally with a mask
        String copyCmd = (mask != null && !mask.isBlank())
                ? "worldedit:/copy -m " + "!" + mask
                : "worldedit:/copy";
        Bukkit.dispatchCommand(player, copyCmd);

        // Dispatch //schematic save (use -f to overwrite existing files)
        Bukkit.dispatchCommand(player, "worldedit:/schematic save -f " + schematicName);

        // Return the expected schematic path (WE writes asynchronously, but path is deterministic)
        var schemsDir = impl.getWorldEdit().getWorkingDirectoryPath(impl.getWorldEdit().getConfiguration().saveDir);
        return schemsDir.resolve(schematicName + ".schem");
    }

    private static List<Path> getAllFiles(Path fromPath) throws IOException {
        var pathList = new ArrayList<Path>();
        try (var stream = Files.newDirectoryStream(fromPath)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) pathList.addAll(getAllFiles(p));
                else if (p.toString().endsWith(".schematic") || p.toString().endsWith(".schem")) pathList.add(p);
            }
        }
        return pathList;
    }

}
