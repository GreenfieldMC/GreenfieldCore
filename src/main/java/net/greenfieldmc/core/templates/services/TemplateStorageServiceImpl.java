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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TemplateStorageServiceImpl extends ModuleService<ITemplateStorageService> implements ITemplateStorageService {

    private IConfig templatesConfig;
    private IConfig tagsConfig;
    private final Map<String, Template> templates = new HashMap<>();
    private final Map<String, Tag> tags = new HashMap<>();

    public TemplateStorageServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            // ---- Templates config ----
            this.templatesConfig = ConfigType.YML.createNew(plugin, "templates");
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
            this.tagsConfig = ConfigType.YML.createNew(plugin, "tags");
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
