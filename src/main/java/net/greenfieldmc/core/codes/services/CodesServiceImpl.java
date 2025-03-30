package net.greenfieldmc.core.codes.services;

import net.greenfieldmc.core.Module;
import net.greenfieldmc.core.ModuleService;
import net.greenfieldmc.core.codes.Code;
import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.IConfig;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodesServiceImpl extends ModuleService<ICodesService> implements ICodesService {

    private IConfig config;
    private final Map<Integer, Code> codeMap = new HashMap<>();

    public CodesServiceImpl(Plugin plugin, Module module) {
        super(plugin, module);
    }

    @Override
    public void tryEnable(Plugin plugin, Module module) throws Exception {
        try {
            this.config = ConfigType.YML.createNew(plugin, "codes");

            config.addEntry("codes", Arrays.asList(
                    "Floors in buildings should be 3 blocks in height and have one block between multiple floors. (See https://i.imgur.com/sMBuzaO.png)",
                    "Flat roofs should be made out of either stone, gravel, or dark gray wool. The flat roof texture must also not be visible from street level and must differ from the exterial material of the building. For example; you should not build a stone roof when the rest of the building is stone.",
                    "Buildings should not be fully lit inside, you need to take a realistic approach and have some parts lit and some parts dark. (most of the time, houses are not 100% lit)",
                    "Do not hide glowstone under carpet on the exterior of a building or on the insides of large buildings/warehouses. (Road lamps are an exception)",
                    "Keep the usage of black materials such as black wool or black glass to a minimum, it contrasts too much and it looks bad in most cases.",
                    "The narrow gravely/stone pathways near buildings are alleys, and when you're building a house with a yard, you must place 1.5 - 2 block tall walls on the perimeter of your plot facing the alleys. With a building that covers the whole plot (or when a building wall is directly beside an alley), simply just block out the ground so there are no windows facing the alleys. (https://i.imgur.com/vfpPxoi.png)",
                    "Do not use the same texture on the outside of a building as the interior flooring. For example, dont build a house with a primarily granite exterior and then put a granite floor inside."));

            config.getStringList("codes").forEach(code -> {
                var currentId = codeMap.size() + 1;
                codeMap.put(currentId, new Code(code, currentId));
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the CodesService", e);
        }
    }

    @Override
    public void tryDisable(Plugin plugin, Module module) throws Exception {
        save();
    }

    @Override
    public void addCode(String code) {
        var currentId = codeMap.size();
        codeMap.put(currentId, new Code(code, currentId));
    }

    @Override
    public void removeCode(int id) {
        codeMap.remove(id);
    }

    @Override
    public Code getCode(int id) {
        return codeMap.get(id);
    }

    @Override
    public List<Code> getCodes() {
        return codeMap.values().stream().toList();
    }

    @Override
    public void reload() {
        config.reload();
        codeMap.clear();
        config.getStringList("codes").forEach(code -> {
            var currentId = codeMap.size();
            codeMap.put(currentId, new Code(code, currentId));
        });
    }

    @Override
    public void save() {
        config.setEntry("codes", codeMap.values().stream().map(Code::getCode).toList());
        config.save();
    }
}
