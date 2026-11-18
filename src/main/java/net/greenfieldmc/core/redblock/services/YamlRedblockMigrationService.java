package net.greenfieldmc.core.redblock.services;

import com.njdaeger.pdk.serviceprovider.Nullable;
import net.greenfieldmc.core.IGreenfieldCorePlugin;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfBulkImportRedblocksRequest;
import net.greenfieldmc.core.greenfieldapi.models.redblocks.apimodels.GfBulkImportStatus;
import net.greenfieldmc.core.greenfieldapi.services.IGreenfieldRedblockApiService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Migrates the legacy redblocks.yml in the plugin directory into the GreenfieldCoreApi. It is part of the redblock
 * module, so it only runs when that module is enabled.
 * <p>
 * When redblocks.yml exists, its redblocks are sent to the bulk import route in batches of {@value #BATCH_SIZE}. Each
 * batch becomes an import job that the api runs in the background. Once the batches are queued, redblocks.yml is renamed
 * to imported-redblocks#.yml, where # is the number of imported-redblocks files already in the plugin directory, so it
 * is not imported again on the next startup.
 * <p>
 * The import jobs are then followed by a sync task, which polls their status every {@value #POLL_SECONDS} seconds and
 * places each redblock in its world (cube, sign and display entity) as soon as it is imported. The jobs being followed
 * are saved in redblock-migration.yml, so the sync task picks them up again after a restart. When every job is finished,
 * the redblocks that could not be converted or imported are saved to redblocks_migration_failed#.yml, in the same format
 * as redblocks.yml, and redblock-migration.yml is deleted.
 * <p>
 * The world projects come from the world-project mappings in redblock-config.yml. Projects that already exist in the
 * api keep their current name; mapped projects that don't exist yet are created with a placeholder name.
 */
public class YamlRedblockMigrationService {

    private static final String LEGACY_FILE_NAME = "redblocks.yml";
    private static final String STATE_FILE_NAME = "redblock-migration.yml";
    private static final String IMPORTED_FILE_PREFIX = "imported-redblocks";
    private static final String FAILED_FILE_PREFIX = "redblocks_migration_failed";
    private static final String REDBLOCKS_SECTION = "redblocks";

    private static final int BATCH_SIZE = 250;
    private static final int POLL_SECONDS = 5;
    // placing a redblock waits on the main thread, which never runs while the server is stopping
    private static final int PLACE_TIMEOUT_SECONDS = 60;

    private final IGreenfieldCorePlugin plugin;
    private final Logger logger;
    private final IRedblockConfigService configService;
    private final IGreenfieldRedblockApiService redblockApiService;
    private final IRedblockServiceV2 redblockService;
    private final File stateFile;

    // the migration being followed. Only changed in the constructor, before the sync task starts, and by the sync task
    private @Nullable String importedFileName;
    private final Set<String> failedIds = new TreeSet<>();
    private final Map<UUID, PendingImport> pendingImports = new LinkedHashMap<>();

    private final AtomicBoolean syncing = new AtomicBoolean(false);
    private @Nullable BukkitTask syncTask;

    public YamlRedblockMigrationService(IGreenfieldCorePlugin plugin, Logger logger, IRedblockConfigService configService, IGreenfieldRedblockApiService redblockApiService, IRedblockServiceV2 redblockService) {
        this.plugin = plugin;
        this.logger = logger;
        this.configService = configService;
        this.redblockApiService = redblockApiService;
        this.redblockService = redblockService;
        this.stateFile = new File(plugin.getDataFolder(), STATE_FILE_NAME);

        var legacyFile = new File(plugin.getDataFolder(), LEGACY_FILE_NAME);

        if (stateFile.isFile()) {
            // a migration was still running when the server stopped, so keep following it
            if (!loadState()) return;
            logger.info("Resuming the RedBlock migration from " + STATE_FILE_NAME + " (" + pendingImports.size() + " imports left).");
            if (legacyFile.isFile())
                logger.warning("Found " + LEGACY_FILE_NAME + ", but the previous RedBlock migration is still running. It will be imported on a startup after that migration finishes.");
            startSync();
            return;
        }

        if (legacyFile.isFile()) startMigration(legacyFile);
    }

    // region Starting the migration

    /**
     * Converts redblocks.yml and queues its redblocks in batches. If no batch could be queued, the file is left in place
     * so the migration is tried again on the next startup.
     */
    private void startMigration(File legacyFile) {
        logger.info("Found " + LEGACY_FILE_NAME + ", converting it for the bulk import.");
        var yaml = new YamlConfiguration();
        try {
            yaml.load(legacyFile);
        } catch (Exception e) {
            logger.severe("Could not read " + LEGACY_FILE_NAME + ": " + e.getMessage());
            return;
        }

        var skippedIds = new TreeSet<String>();
        var request = convert(yaml, skippedIds);
        if (request == null) return;
        failedIds.addAll(skippedIds);

        var batches = batch(request);
        logger.info("Queueing " + request.redblocks().size() + " RedBlocks from " + LEGACY_FILE_NAME + " for import in " + batches.size() + " batches...");
        for (var batch : batches) {
            var queued = redblockApiService.bulkImportRedblocks(batch).join();
            if (queued.isFailure()) {
                logger.severe("Could not queue a batch of " + batch.redblocks().size() + " RedBlocks for import: " + queued.getErrorMessage());
                failedIds.addAll(batch.redblocks().keySet());
                continue;
            }
            pendingImports.put(queued.getData().importJobId(), new PendingImport(0, new TreeSet<>(batch.redblocks().keySet())));
        }

        if (!batches.isEmpty() && pendingImports.isEmpty()) {
            logger.severe("No RedBlocks could be queued for import. " + LEGACY_FILE_NAME + " was left in place, so the migration is tried again on the next startup.");
            failedIds.clear();
            return;
        }
        logger.info("Queued " + pendingImports.size() + " RedBlock imports: " + pendingImports.keySet().stream().map(UUID::toString).collect(Collectors.joining(", ")));

        // if the rename fails, the state file still stops redblocks.yml from being imported again
        var renamed = renameImportedFile(plugin.getDataFolder(), legacyFile);
        importedFileName = renamed == null ? LEGACY_FILE_NAME : renamed.getName();

        if (pendingImports.isEmpty()) {
            // nothing was sent, e.g. every redblock was skipped while converting
            finishMigration();
            return;
        }
        saveState();
        startSync();
    }

    /**
     * Splits a request into requests of at most {@value #BATCH_SIZE} redblocks, each with all the world projects.
     */
    private static List<GfBulkImportRedblocksRequest> batch(GfBulkImportRedblocksRequest request) {
        var batches = new ArrayList<GfBulkImportRedblocksRequest>();
        var current = new LinkedHashMap<String, GfBulkImportRedblocksRequest.Entry>();
        for (var redblock : request.redblocks().entrySet()) {
            current.put(redblock.getKey(), redblock.getValue());
            if (current.size() == BATCH_SIZE) {
                batches.add(new GfBulkImportRedblocksRequest(current, request.worldProjects()));
                current = new LinkedHashMap<>();
            }
        }
        if (!current.isEmpty()) batches.add(new GfBulkImportRedblocksRequest(current, request.worldProjects()));
        return batches;
    }

    // endregion

    // region Following the imports

    private void startSync() {
        syncTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::sync, 20L * POLL_SECONDS, 20L * POLL_SECONDS);
    }

    /**
     * Polls every pending import once, placing the redblocks they finished. Async repeating tasks can overlap when a run
     * takes longer than the period, so a run is skipped while the last one is still going.
     */
    private void sync() {
        if (!syncing.compareAndSet(false, true)) return;
        try {
            for (var importJobId : List.copyOf(pendingImports.keySet())) syncImport(importJobId);
            if (pendingImports.isEmpty()) finishMigration();
            else saveState();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "The RedBlock migration sync failed, it is retried in " + POLL_SECONDS + " seconds.", e);
        } finally {
            syncing.set(false);
        }
    }

    private void syncImport(UUID importJobId) {
        var pending = pendingImports.get(importJobId);
        while (true) {
            var result = redblockApiService.getBulkImportStatus(importJobId, pending.cursor).join();
            if (result.isFailure()) {
                if (result.getStatusCode().orElse(0) == 404) {
                    logger.severe("RedBlock import " + importJobId + " no longer exists. Its " + pending.remaining.size() + " unfinished RedBlocks are treated as failed.");
                    failedIds.addAll(pending.remaining);
                    pendingImports.remove(importJobId);
                } else {
                    logger.warning("Could not get the status of RedBlock import " + importJobId + ", retrying in " + POLL_SECONDS + " seconds: " + result.getErrorMessage());
                }
                return;
            }

            var status = result.getData();
            status.entries().forEach(entry -> handleEntry(importJobId, pending, entry));
            pending.cursor = status.cursor();

            // more finished entries are ready right away
            if (status.hasMoreEntries()) continue;

            if (status.isFinished()) {
                // anything the job never got to, or that failed, is still remaining
                failedIds.addAll(pending.remaining);
                pendingImports.remove(importJobId);
                var summary = "RedBlock import " + importJobId + " " + status.state().toLowerCase() + ": " + status.processed() + "/" + status.total() + " processed, " + pending.remaining.size() + " failed.";
                if (GfBulkImportStatus.STATE_FAILED.equals(status.state())) logger.severe(summary + " " + status.errorMessage());
                else logger.info(summary);
            } else if (!status.entries().isEmpty()) {
                logger.info("RedBlock import " + importJobId + ": " + status.processed() + "/" + status.total() + " processed.");
            }
            return;
        }
    }

    /**
     * Places an imported redblock in its world. Failed entries stay in the remaining keys, so they end up in the failed file.
     */
    private void handleEntry(UUID importJobId, PendingImport pending, GfBulkImportStatus.Entry entry) {
        var errors = entry.errors() == null ? List.<String>of() : entry.errors();
        switch (entry.state()) {
            case GfBulkImportStatus.ENTRY_IMPORTED -> {
                pending.remaining.remove(entry.legacyKey());
                errors.forEach(error -> logger.warning("RedBlock " + entry.legacyKey() + " was imported as " + entry.redblockKey() + " with an error: " + error));
                if (entry.redblockKey() != null) place(entry.legacyKey(), entry.redblockKey());
            }
            case GfBulkImportStatus.ENTRY_SKIPPED -> pending.remaining.remove(entry.legacyKey());
            default -> logger.warning("RedBlock " + entry.legacyKey() + " failed to import in " + importJobId + (errors.isEmpty() ? "." : ": " + String.join(" | ", errors)));
        }
    }

    private void place(String legacyKey, String redblockKey) {
        try {
            var placed = redblockService.refreshRedblockDisplay(redblockKey).orTimeout(PLACE_TIMEOUT_SECONDS, TimeUnit.SECONDS).join();
            if (placed.isFailure()) logger.warning("RedBlock " + legacyKey + " was imported as " + redblockKey + ", but could not be placed: " + placed.getErrorMessage());
        } catch (Exception e) {
            logger.warning("RedBlock " + legacyKey + " was imported as " + redblockKey + ", but could not be placed: " + e.getMessage());
        }
    }

    /**
     * Writes the failed file, deletes the state file and stops the sync task.
     */
    private void finishMigration() {
        if (!failedIds.isEmpty()) {
            var importedFile = new File(plugin.getDataFolder(), importedFileName == null ? LEGACY_FILE_NAME : importedFileName);
            var yaml = new YamlConfiguration();
            try {
                yaml.load(importedFile);
                writeFailedFile(plugin.getDataFolder(), yaml, failedIds);
            } catch (Exception e) {
                logger.severe("Could not read " + importedFile.getName() + " to save the " + failedIds.size() + " RedBlocks that failed to import: " + e.getMessage() + ". Their ids are: " + String.join(", ", failedIds));
            }
        }

        try {
            Files.deleteIfExists(stateFile.toPath());
        } catch (IOException e) {
            logger.severe("Could not delete " + STATE_FILE_NAME + ", delete it by hand or the finished migration is resumed on the next startup: " + e.getMessage());
        }

        if (syncTask != null) syncTask.cancel();
        logger.info("The RedBlock migration is finished" + (failedIds.isEmpty() ? "." : ", " + failedIds.size() + " RedBlocks failed."));
    }

    // endregion

    // region State file

    private static final class PendingImport {
        private long cursor;
        // the legacy keys of the batch that are not imported or skipped yet
        private final Set<String> remaining;

        private PendingImport(long cursor, Set<String> remaining) {
            this.cursor = cursor;
            this.remaining = remaining;
        }
    }

    private void saveState() {
        var yaml = new YamlConfiguration();
        yaml.set("importedFile", importedFileName);
        yaml.set("failed", new ArrayList<>(failedIds));
        pendingImports.forEach((importJobId, pending) -> {
            yaml.set("imports." + importJobId + ".cursor", pending.cursor);
            yaml.set("imports." + importJobId + ".remaining", new ArrayList<>(pending.remaining));
        });
        try {
            yaml.save(stateFile);
        } catch (IOException e) {
            logger.severe("Could not save " + STATE_FILE_NAME + ": " + e.getMessage());
        }
    }

    /**
     * @return Whether the state file could be read
     */
    private boolean loadState() {
        var yaml = new YamlConfiguration();
        try {
            yaml.load(stateFile);
        } catch (Exception e) {
            logger.severe("Could not read " + STATE_FILE_NAME + ", the RedBlock migration it holds is not resumed: " + e.getMessage());
            return false;
        }

        importedFileName = yaml.getString("importedFile");
        failedIds.addAll(yaml.getStringList("failed"));
        var imports = yaml.getConfigurationSection("imports");
        if (imports != null) {
            for (var importJobId : imports.getKeys(false)) {
                pendingImports.put(UUID.fromString(importJobId), new PendingImport(
                        imports.getLong(importJobId + ".cursor"),
                        new TreeSet<>(imports.getStringList(importJobId + ".remaining"))));
            }
        }
        return true;
    }

    // endregion

    // region Converting redblocks.yml

    /**
     * Converts the redblocks in redblocks.yml into a bulk import request.
     * @param yaml The loaded redblocks.yml
     * @param skippedIds Gets the ids of the redblocks that could not be converted
     * @return The request, or null if the projects could not be loaded from the api
     */
    private @Nullable GfBulkImportRedblocksRequest convert(YamlConfiguration yaml, Set<String> skippedIds) {
        var redblocks = new LinkedHashMap<String, GfBulkImportRedblocksRequest.Entry>();
        var section = yaml.getConfigurationSection(REDBLOCKS_SECTION);
        if (section != null) {
            for (var id : section.getKeys(false)) {
                try {
                    redblocks.put(id, toEntry(section.getConfigurationSection(id)));
                } catch (Exception e) {
                    skippedIds.add(id);
                    logger.warning("Skipping RedBlock " + id + " in " + LEGACY_FILE_NAME + ": " + e.getMessage());
                }
            }
        }

        var worldProjects = resolveWorldProjects();
        if (worldProjects == null) return null;

        var unmappedWorlds = redblocks.values().stream()
                .map(entry -> entry.location().world())
                .filter(world -> !worldProjects.containsKey(world))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!unmappedWorlds.isEmpty())
            logger.warning("These worlds in " + LEGACY_FILE_NAME + " are not mapped to a project in redblock-config.yml, so their RedBlocks have no project to import into: " + String.join(", ", unmappedWorlds));

        return new GfBulkImportRedblocksRequest(redblocks, worldProjects);
    }

    /**
     * Builds the world projects from the world-project mappings in redblock-config.yml, using each project's current
     * name from the api. Mapped projects that don't exist yet are created with a placeholder name.
     * @return The projects keyed by world name, or null if the projects could not be loaded from the api
     */
    private @Nullable Map<String, GfBulkImportRedblocksRequest.Project> resolveWorldProjects() {
        var projectsResult = redblockApiService.getProjects().join();
        if (projectsResult.isFailure()) {
            logger.severe("Could not load the RedBlock projects from the api: " + projectsResult.getErrorMessage());
            return null;
        }
        var existingProjects = projectsResult.getData().stream()
                .collect(Collectors.toMap(project -> project.getProjectKey().toUpperCase(), project -> project, (a, b) -> a));

        var worldProjects = new HashMap<String, GfBulkImportRedblocksRequest.Project>();
        for (var world : configService.getMappedWorlds()) {
            var projectKey = configService.getProjectKey(world);
            if (projectKey == null) continue;

            var project = existingProjects.get(projectKey.toUpperCase());
            if (project == null) {
                var placeholderName = projectKey + " (imported from " + world.getName() + ")";
                var created = redblockApiService.createProject(projectKey, placeholderName).join();
                if (created.isFailure()) {
                    logger.warning("Could not create project " + projectKey + " for world " + world.getName() + ": " + created.getErrorMessage());
                    continue;
                }
                project = created.getData();
                logger.info("Created project " + projectKey + " named \"" + placeholderName + "\" for world " + world.getName() + ".");
            }
            worldProjects.put(world.getName(), new GfBulkImportRedblocksRequest.Project(project.getProjectName(), project.getProjectKey()));
        }
        return worldProjects;
    }

    private static GfBulkImportRedblocksRequest.Entry toEntry(@Nullable ConfigurationSection redblock) {
        if (redblock == null) throw new IllegalArgumentException("it is not a section");

        var location = redblock.getConfigurationSection("location");
        if (location == null) throw new IllegalArgumentException("it has no location");

        return new GfBulkImportRedblocksRequest.Entry(
                required(redblock, "content"),
                required(redblock, "status"),
                UUID.fromString(required(redblock, "createdBy")),
                redblock.getLong("createdOn"),
                optionalUuid(redblock, "completedBy"),
                optionalTime(redblock, "completedOn"),
                optionalUuid(redblock, "approvedBy"),
                optionalTime(redblock, "approvedOn"),
                optionalUuid(redblock, "assignedTo"),
                optionalTime(redblock, "assignedOn"),
                new GfBulkImportRedblocksRequest.Location(location.getLong("x"), location.getLong("y"), location.getLong("z"), required(location, "world")),
                redblock.getString("minRank"),
                redblock.isList("armorstands") ? redblock.getStringList("armorstands").stream().map(UUID::fromString).toList() : null
        );
    }

    private static String required(ConfigurationSection section, String key) {
        var value = section.getString(key);
        if (value == null) throw new IllegalArgumentException("it has no " + key);
        return value;
    }

    private static @Nullable UUID optionalUuid(ConfigurationSection section, String key) {
        var value = section.getString(key);
        return value == null ? null : UUID.fromString(value);
    }

    // the legacy storage saved missing times as null, and treated 0 as missing too
    private static @Nullable Long optionalTime(ConfigurationSection section, String key) {
        var value = section.getLong(key);
        return value == 0 ? null : value;
    }

    // endregion

    // region Files

    /**
     * Writes the failed redblocks to redblocks_migration_failed#.yml, in the same format as redblocks.yml. It is a copy
     * of redblocks.yml with every other redblock removed, so renaming it to redblocks.yml imports just those again.
     */
    private void writeFailedFile(File directory, YamlConfiguration yaml, Set<String> failedIds) {
        var section = yaml.getConfigurationSection(REDBLOCKS_SECTION);
        if (section != null) {
            for (var id : section.getKeys(false)) {
                if (!failedIds.contains(id)) section.set(id, null);
            }
        }

        var target = nextNumberedFile(directory, FAILED_FILE_PREFIX);
        try {
            yaml.save(target);
            logger.warning(failedIds.size() + " RedBlocks could not be imported. They were saved to " + target.getName() + ": " + String.join(", ", failedIds));
        } catch (IOException e) {
            logger.severe("Could not save the " + failedIds.size() + " RedBlocks that failed to import to " + target.getName() + ": " + e.getMessage() + ". Their ids are: " + String.join(", ", failedIds));
        }
    }

    /**
     * Gets prefix#.yml, where # is the number of prefix#.yml files already in the directory, so files are not overwritten.
     */
    private static File nextNumberedFile(File directory, String prefix) {
        var pattern = Pattern.compile(Pattern.quote(prefix) + "\\d+\\.yml");
        var existing = directory.listFiles((dir, name) -> pattern.matcher(name).matches());
        var number = existing == null ? 0 : existing.length;
        // skip numbers that are already taken, e.g. if an older file was deleted
        var target = new File(directory, prefix + number + ".yml");
        while (target.exists()) target = new File(directory, prefix + (++number) + ".yml");
        return target;
    }

    /**
     * @return The renamed file, or null if it could not be renamed
     */
    private @Nullable File renameImportedFile(File directory, File legacyFile) {
        var target = nextNumberedFile(directory, IMPORTED_FILE_PREFIX);
        try {
            Files.move(legacyFile.toPath(), target.toPath());
            logger.info("Renamed " + LEGACY_FILE_NAME + " to " + target.getName() + ".");
            return target;
        } catch (IOException e) {
            logger.severe("Could not rename " + LEGACY_FILE_NAME + " to " + target.getName() + ": " + e.getMessage());
            return null;
        }
    }

    // endregion
}
