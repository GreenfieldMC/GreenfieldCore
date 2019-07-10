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
                "The narrow gravely/stone pathways near buildings are alleys, and when you're building a house with a yard, you must place 1.5 - 2 block tall walls on the perimeter of your plot facing the alleys. With a building that covers the whole plot (or when a building wall is directly beside an alley), simply just block out the ground so there are no windows facing the alleys. (https://i.imgur.com/vfpPxoi.png)",
                "Do not use the same texture on the outside of a building as the interior flooring. For example, dont build a house with a primarily granite exterior and then put a granite floor inside."));

        this.codes = new ArrayList<>(getStringList("codes"));
    }

    public List<String> getCodes() {
        return codes;
    }

}
