package com.njdaeger.greenfieldcore.codes;

import com.njdaeger.bcm.types.YmlConfig;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodesConfig extends YmlConfig {

    private List<String> codes;

    public CodesConfig(Plugin plugin) {
        super(plugin, "codes");

        addEntry("codes", Arrays.asList(
                "Floors in buildings should be 3 blocks in height and have one block between multiple floors. (See https://i.imgur.com/sMBuzaO.png)",
                "Flat roofs should be made out of either stone, gravel, or dark gray wool. The flat roof texture must also not be visible from street level and must differ from the exterial material of the building. For example; you should not build a stone roof when the rest of the building is stone.",
                "Buildings should not be fully lit inside, you need to take a realistic approach and have some parts lit and some parts dark. (most of the time, houses are not 100% lit)",
                "Do not hide glowstone under carpet on the exterior of a building or on the insides of large buildings/warehouses. (Road lamps are an exception)",
                "Keep the usage of black materials such as black wool or black glass to a minimum, it contrasts too much and it looks bad in most cases.",
                "The narrow gravely/stone pathways near buildings are alleys, and when you're building"));

        this.codes = new ArrayList<>();
    }

    public List<String> getCodes() {
        return null;
    }

}
