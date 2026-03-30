package net.greenfieldmc.core.signmanager.services;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.SavedSignGroup;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SignManagerStorageServiceImpl extends ModuleService<ISignManagerStorageService> implements ISignManagerStorageService {

    private final Map<String, SavedSign> signs = new HashMap<>();
    private final Map<String, SavedSignGroup> groups = new HashMap<>();
    private IConfig config;

    public SignManagerStorageServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "signmanager");

            // Load individual signs
            if (config.hasSection("signs")) {
                for (var signName : config.getSection("signs").getKeys(false)) {
                    var sign = loadSign("signs." + signName, signName);
                    if (sign != null) signs.put(signName.toLowerCase(), sign);
                }
            }

            // Load groups
            if (config.hasSection("groups")) {
                for (var groupName : config.getSection("groups").getKeys(false)) {
                    // Load display sign
                    var displaySign = loadSign("groups." + groupName + ".displaySign", groupName + "_display");
                    if (displaySign == null) {
                        plugin.getLogger().warning("SignManager: Group '" + groupName + "' has invalid display sign. Skipping.");
                        continue;
                    }

                    // Load member signs
                    var memberSigns = new ArrayList<SavedSign>();
                    var section = config.getSection("groups." + groupName);
                    if (section.contains("members")) {
                        for (var memberKey : section.getSection("members").getKeys(false)) {
                            var memberSign = loadSign("groups." + groupName + ".members." + memberKey, memberKey);
                            if (memberSign != null) memberSigns.add(memberSign);
                        }
                    }

                    groups.put(groupName.toLowerCase(), new SavedSignGroup(groupName, displaySign, memberSigns));
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to enable SignManagerStorageService", e);
        }
    }

    private @Nullable SavedSign loadSign(String path, String name) {
        var section = config.getSection(path);
        if (section == null) return null;
        var material = Material.matchMaterial(section.getString("material"));
        if (material == null) return null;
        var frontLines = section.getStringList("frontLines");
        var backLines = section.getStringList("backLines");

        while (frontLines.size() < 4) frontLines.add("\"\"");
        while (backLines.size() < 4) backLines.add("\"\"");

        return new SavedSign(name, material, frontLines, backLines);
    }

    private void persistSign(String path, SavedSign sign) {
        config.setEntry(path + ".material", sign.getSignMaterial().name());
        config.setEntry(path + ".frontLines", sign.getFrontLines());
        config.setEntry(path + ".backLines", sign.getBackLines());
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        saveDatabase();
    }

    @Override
    public @Nullable SavedSign getSign(String name) {
        return signs.get(name.toLowerCase());
    }

    @Override
    public List<SavedSign> getSigns(Predicate<SavedSign> filter) {
        return new ArrayList<>(signs.values()).stream().filter(filter).toList();
    }

    @Override
    public void saveSign(SavedSign sign) {
        signs.put(sign.getName().toLowerCase(), sign);
        persistSign("signs." + sign.getName().toLowerCase(), sign);
    }


    @Override
    public void deleteSign(String name) {
        var removed = signs.remove(name.toLowerCase());
        if (removed != null) {
            config.setEntry("signs." + name.toLowerCase(), null);
        }
    }

    @Override
    public @Nullable SavedSignGroup getGroup(String name) {
        return groups.get(name.toLowerCase());
    }

    @Override
    public List<SavedSignGroup> getGroups(Predicate<SavedSignGroup> filter) {
        return new ArrayList<>(groups.values()).stream().filter(filter).toList();
    }

    @Override
    public void saveGroup(SavedSignGroup group) {
        groups.put(group.getName().toLowerCase(), group);
        var basePath = "groups." + group.getName().toLowerCase();
        persistSign(basePath + ".displaySign", group.getDisplaySign());
        // Clear old members and rewrite
        config.setEntry(basePath + ".members", null);
        var memberSigns = group.getMemberSigns();
        for (int i = 0; i < memberSigns.size(); i++) {
            persistSign(basePath + ".members.sign_" + i, memberSigns.get(i));
        }
    }

    @Override
    public void deleteGroup(String name) {
        var removed = groups.remove(name.toLowerCase());
        if (removed != null) {
            config.setEntry("groups." + name.toLowerCase(), null);
        }
    }

    @Override
    public List<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }

    @Override
    public void saveDatabase() {
        signs.values().forEach(this::saveSign);
        groups.values().forEach(this::saveGroup);
        config.save();
    }
}
