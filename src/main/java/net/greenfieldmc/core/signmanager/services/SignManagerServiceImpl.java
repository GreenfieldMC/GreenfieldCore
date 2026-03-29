package net.greenfieldmc.core.signmanager.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.signmanager.SavedSign;
import net.greenfieldmc.core.signmanager.SavedSignGroup;
import net.greenfieldmc.core.signmanager.SignManagerEntry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SignManagerServiceImpl extends ModuleService<ISignManagerService> implements ISignManagerService {

    private final ISignManagerStorageService storageService;

    public SignManagerServiceImpl(Plugin plugin, Module module, ISignManagerStorageService storageService) {
        super(plugin, module);
        this.storageService = storageService;
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
    }

    @Override
    public @Nullable SavedSign saveSignFromHand(Player player, String name) {
        var item = player.getInventory().getItemInMainHand();
        if (!SavedSign.isSignMaterial(item.getType())) return null;

        var sign = SavedSign.fromItemStack(item, name);
        if (sign == null) return null;

        storageService.saveSign(sign);
        storageService.saveDatabase();
        return sign;
    }

    @Override
    public @Nullable SavedSignGroup saveGroupFromHotbar(Player player, String groupName) {
        var mainHandItem = player.getInventory().getItemInMainHand();
        if (!SavedSign.isSignMaterial(mainHandItem.getType())) return null;

        var displaySign = SavedSign.fromItemStack(mainHandItem, groupName + "_display");
        if (displaySign == null) return null;

        var memberSigns = new ArrayList<SavedSign>();
        int signCount = 0;
        for (int slot = 0; slot < 9; slot++) {
            var item = player.getInventory().getItem(slot);
            if (item == null || !SavedSign.isSignMaterial(item.getType())) continue;

            signCount++;
            var memberSign = SavedSign.fromItemStack(item, groupName + "_" + signCount);
            if (memberSign != null) memberSigns.add(memberSign);
        }

        if (memberSigns.isEmpty()) return null;

        var group = new SavedSignGroup(groupName, displaySign, memberSigns);
        storageService.saveGroup(group);
        storageService.saveDatabase();
        return group;
    }

    @Override
    public boolean deleteSign(String name) {
        var sign = storageService.getSign(name);
        if (sign == null) return false;
        storageService.deleteSign(name);
        storageService.saveDatabase();
        return true;
    }

    @Override
    public boolean deleteGroup(String groupName) {
        var group = storageService.getGroup(groupName);
        if (group == null) return false;
        storageService.deleteGroup(groupName);
        storageService.saveDatabase();
        return true;
    }

    @Override
    public @Nullable SavedSign getSign(String name) {
        return storageService.getSign(name);
    }

    @Override
    public boolean nameExists(String name) {
        return storageService.getSign(name) != null || storageService.getGroup(name) != null;
    }

    @Override
    public List<SignManagerEntry> getAllEntries() {
        var entries = new ArrayList<SignManagerEntry>();
        storageService.getSigns().forEach(sign -> entries.add(SignManagerEntry.ofSign(sign)));
        storageService.getGroups().forEach(group -> entries.add(SignManagerEntry.ofGroup(group)));
        entries.sort(Comparator.comparing(entry -> entry.getName().toLowerCase()));
        return entries;
    }

    @Override
    public List<SignManagerEntry> searchEntries(String query) {
        if (query == null || query.isBlank()) return getAllEntries();
        var lowerQuery = query.toLowerCase();
        return getAllEntries().stream()
                .filter(entry -> entry.getPlainText().toLowerCase().contains(lowerQuery))
                .toList();
    }

    @Override
    public @Nullable SignManagerEntry giveEntry(Player player, String entryName) {
        // Check individual signs first
        var sign = storageService.getSign(entryName);
        if (sign != null) {
            var entry = SignManagerEntry.ofSign(sign);
            for (var item : entry.toGiveItemStacks()) {
                player.getInventory().addItem(item);
            }
            return entry;
        }

        // Check groups
        var group = storageService.getGroup(entryName);
        if (group != null) {
            var entry = SignManagerEntry.ofGroup(group);
            for (var item : entry.toGiveItemStacks()) {
                player.getInventory().addItem(item);
            }
            return entry;
        }

        return null;
    }

    @Override
    public List<String> getGroupNames() {
        return storageService.getGroupNames();
    }

    @Override
    public List<SavedSign> getAllSigns() {
        return storageService.getSigns();
    }

    @Override
    public List<String> getSignNames() {
        return storageService.getSigns().stream().map(SavedSign::getName).toList();
    }

    @Override
    public List<String> getAllEntryNames() {
        var names = new ArrayList<String>();
        storageService.getSigns().forEach(sign -> names.add(sign.getName()));
        storageService.getGroups().forEach(group -> names.add(group.getName()));
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }
}
