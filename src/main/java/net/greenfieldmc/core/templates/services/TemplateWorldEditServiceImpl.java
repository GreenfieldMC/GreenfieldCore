package net.greenfieldmc.core.templates.services;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.HandSide;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.shared.services.WorldEditServiceImpl;
import net.greenfieldmc.core.templates.WorldEditTemplateBrush;
import net.greenfieldmc.core.templates.models.Template;
import org.bukkit.Bukkit;
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

public class TemplateWorldEditServiceImpl extends WorldEditServiceImpl implements ITemplateWorldEditService {

    private final ITemplateService templateService;
    private final List<Path> schematicFiles = new ArrayList<>();
    private Thread watcherThread;

    public TemplateWorldEditServiceImpl(Plugin plugin, Module module, ITemplateService templateService) {
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
