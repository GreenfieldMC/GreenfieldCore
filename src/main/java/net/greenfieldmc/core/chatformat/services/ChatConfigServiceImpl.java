package net.greenfieldmc.core.chatformat.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.chatformat.ChatUser;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatConfigServiceImpl extends ModuleService<IChatConfigService> implements IChatConfigService {

    private IConfig config;
    private final Map<UUID, ChatUser> users = new HashMap<>();

    public ChatConfigServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "chatconfig");

            config.addEntry("users", new ArrayList<>());
            if (config.hasSection("users")) config.getSection("users").getKeys(false).stream().map(UUID::fromString).forEach(uuid -> {
                var user = new ChatUser(uuid);
                user.setAllowMentions(config.getBoolean("users." + uuid + ".allow-mentions"));
                user.setVolume(config.getFloat("users." + uuid + ".volume"));
                user.setSound(Sound.valueOf(config.getString("users." + uuid + ".sound")));
                users.put(uuid, user);
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to enable ChatConfigService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        saveDatabase();
    }

    @Override
    public void saveDatabase() {
        users.values().stream().filter(ChatUser::hasChanged).forEach(user -> {
            config.setEntry("users." + user.getUuid() + ".allow-mentions", user.allowsMentions());
            config.setEntry("users." + user.getUuid() + ".volume", user.getVolume());
            config.setEntry("users." + user.getUuid() + ".sound", user.getSound().name());
        });
        config.save();
    }

    @Override
    public void save(Player player) {
        config.setEntry("users." + player.getUniqueId() + ".allow-mentions", allowsMentions(player));
        config.setEntry("users." + player.getUniqueId() + ".volume", getVolume(player));
        config.setEntry("users." + player.getUniqueId() + ".sound", getSound(player).name());
        config.save();
    }

    @Override
    public ChatUser getUser(Player player) {
        if (!users.containsKey(player.getUniqueId())) {
            var user = new ChatUser(player.getUniqueId());
            users.put(player.getUniqueId(), user);
            return user;
        } else {
            return users.get(player.getUniqueId());
        }
    }
}
