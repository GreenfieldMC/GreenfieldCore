package net.greenfieldmc.core.templates.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.templates.models.Tag;
import net.greenfieldmc.core.templates.models.Template;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class TemplateStorageServiceImpl extends ModuleService<ITemplateStorageService> implements ITemplateStorageService {

    private IConfig templatesConfig;
    private IConfig tagsConfig;
    private IConfig mainConfig;
    private final Map<String, Template> templates = new HashMap<>();
    private final Map<String, Tag> tags = new HashMap<>();
    
    // Player configs: UUID -> config file (lazy-loaded from templates/user/<UUID>.yml)
    private final Map<UUID, IConfig> playerConfigs = new HashMap<>();

    public TemplateStorageServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            // ---- Create folder structure ----
            File storageFolder = new File(plugin.getDataFolder(), "templates/storage");
            File userFolder = new File(plugin.getDataFolder(), "templates/user");
            
            if (!storageFolder.exists() && !storageFolder.mkdirs()) {
                throw new Exception("Failed to create storage folder: " + storageFolder.getAbsolutePath());
            }
            if (!userFolder.exists() && !userFolder.mkdirs()) {
                throw new Exception("Failed to create user data folder: " + userFolder.getAbsolutePath());
            }

            // ---- Main config (entity rendering limits by rank) ----
            this.mainConfig = ConfigType.YML.createNew(plugin, "templates/config");
            // Initialize default entity rendering limits if not present
            if (!mainConfig.hasSection("entityRenderingLimits")) {
                mainConfig.setEntry("entityRenderingLimits.default", 100);
                mainConfig.setEntry("entityRenderingLimits.vip", 200);
                mainConfig.setEntry("entityRenderingLimits.premium", 500);
                mainConfig.setEntry("entityRenderingLimits.admin", 1000);
                mainConfig.save();
            }

            // ---- Templates config ----
            this.templatesConfig = ConfigType.YML.createNew(plugin, "templates/storage/templates");
            if (templatesConfig.hasSection("templates")) {
                for (var templateName : templatesConfig.getSection("templates").getKeys(false)) {
                    var section = templatesConfig.getSection("templates." + templateName);
                    var schematicFile = section.getString("schematicFile");
                    var attributes = section.getStringList("attributes");
                    var displayItemBase64 = section.getString("displayItem");
                    ItemStack displayItem = deserializeItem(displayItemBase64);
                    templates.put(templateName, new Template(templateName, schematicFile, attributes, displayItem));
                }
            }

            // ---- Tags config ----
            this.tagsConfig = ConfigType.YML.createNew(plugin, "templates/storage/tags");
            if (tagsConfig.hasSection("tags")) {
                for (var tagName : tagsConfig.getSection("tags").getKeys(false)) {
                    var section = tagsConfig.getSection("tags." + tagName);
                    var displayItemBase64 = section.getString("displayItem");
                    ItemStack displayItem = deserializeItem(displayItemBase64);
                    tags.put(tagName, new Tag(tagName, displayItem));
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to enable TemplateStorageService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        saveDatabase();
        // Save all loaded player configs before shutdown
        for (var entry : playerConfigs.entrySet()) {
            entry.getValue().save();
        }
        playerConfigs.clear();
    }

    // ---- Template methods ----

    @Override
    public Template getTemplate(String name) {
        return templates.get(name);
    }

    @Override
    public List<Template> getTemplates(Predicate<Template> filter) {
        return new ArrayList<>(templates.values()).stream().filter(filter).toList();
    }

    @Override
    public void saveTemplate(Template template) {
        templates.put(template.getTemplateName(), template);
        var path = "templates." + template.getTemplateName();
        templatesConfig.setEntry(path + ".schematicFile", template.getSchematicFile());
        templatesConfig.setEntry(path + ".attributes", template.getAttributes());
        templatesConfig.setEntry(path + ".displayItem",
                template.hasDisplayItem() ? serializeItem(template.getDisplayItem()) : null);
    }

    @Override
    public void deleteTemplate(String name) {
        templates.remove(name);
        templatesConfig.setEntry("templates." + name, null);
    }

    // ---- Tag methods ----

    @Override
    public Tag getTag(String name) {
        return tags.get(name);
    }

    @Override
    public List<Tag> getTags() {
        return new ArrayList<>(tags.values());
    }

    @Override
    public void saveTag(Tag tag) {
        tags.put(tag.getName(), tag);
        var path = "tags." + tag.getName();
        tagsConfig.setEntry(path + ".displayItem",
                tag.hasDisplayItem() ? serializeItem(tag.getDisplayItem()) : null);
    }

    @Override
    public void deleteTag(String name) {
        tags.remove(name);
        tagsConfig.setEntry("tags." + name, null);
    }

    // ---- Persistence ----

    @Override
    public void saveDatabase() {
        templates.values().forEach(this::saveTemplate);
        templatesConfig.save();
        tags.values().forEach(this::saveTag);
        tagsConfig.save();
        mainConfig.save();
    }

    // ---- Main config methods ----

    /**
     * Get the entity rendering limit for a given rank/permission group.
     * @param rank The rank name (e.g., "default", "vip", "premium", "admin")
     * @return The rendering limit, or 100 if not configured
     */
    public int getEntityRenderingLimit(String rank) {
        if (mainConfig == null) return 100;
        try {
            Integer limit = mainConfig.getInt("entityRenderingLimits." + rank.toLowerCase());
            return limit != null ? limit : 100;
        } catch (NullPointerException e) {
            // Key doesn't exist in config, return default
            return 100;
        }
    }

    /**
     * Set the entity rendering limit for a given rank/permission group.
     * @param rank The rank name
     * @param limit The max number of entities to render
     */
    public void setEntityRenderingLimit(String rank, int limit) {
        if (mainConfig == null) return;
        mainConfig.setEntry("entityRenderingLimits." + rank.toLowerCase(), limit);
        mainConfig.save();
    }

    // ---- Player data methods ----

    /**
     * Lazy-load a player's config file from templates/user/<UUID>.yml
     * Creates the file if it doesn't exist.
     */
    private IConfig getPlayerConfig(UUID playerUuid) {
        return playerConfigs.computeIfAbsent(playerUuid, uuid -> {
            try {
                // Create config using relative path from plugin data folder
                String relativePath = "templates/user/" + uuid.toString();
                return ConfigType.YML.createNew(getPlugin(), relativePath);
            } catch (Exception e) {
                getModule().getLogger().severe("Failed to load player config for " + uuid + ": " + e.getMessage());
                return null;
            }
        });
    }

    @Override
    public boolean getPlayerBoolean(UUID playerUuid, String key, boolean defaultValue) {
        var config = getPlayerConfig(playerUuid);
        if (config == null) return defaultValue;
        try {
            Boolean value = config.getBoolean(key);
            return value != null ? value : defaultValue;
        } catch (NullPointerException e) {
            // Key doesn't exist in config, return default
            return defaultValue;
        }
    }

    @Override
    public void setPlayerBoolean(UUID playerUuid, String key, boolean value) {
        var config = getPlayerConfig(playerUuid);
        if (config == null) return;
        config.setEntry(key, value);
    }

    @Override
    public String getPlayerString(UUID playerUuid, String key, String defaultValue) {
        var config = getPlayerConfig(playerUuid);
        if (config == null) return defaultValue;
        String value = config.getString(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public void setPlayerString(UUID playerUuid, String key, String value) {
        var config = getPlayerConfig(playerUuid);
        if (config == null) return;
        config.setEntry(key, value);
    }

    @Override
    public int getPlayerInt(UUID playerUuid, String key, int defaultValue) {
        var config = getPlayerConfig(playerUuid);
        if (config == null) return defaultValue;
        try {
            Integer value = config.getInt(key);
            return value != null ? value : defaultValue;
        } catch (NullPointerException e) {
            // Key doesn't exist in config, return default
            return defaultValue;
        }
    }

    @Override
    public void setPlayerInt(UUID playerUuid, String key, int value) {
        var config = getPlayerConfig(playerUuid);
        if (config == null) return;
        config.setEntry(key, value);
    }

    @Override
    public void savePlayerConfig(UUID playerUuid) {
        var config = playerConfigs.get(playerUuid);
        if (config != null) {
            config.save();
        }
    }

    // ---- Serialization helpers ----

    private static String serializeItem(ItemStack item) {
        if (item == null) return null;
        try (var baos = new ByteArrayOutputStream();
             var oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(item);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    private static ItemStack deserializeItem(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try (var bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             var ois = new BukkitObjectInputStream(bais)) {
            return (ItemStack) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
