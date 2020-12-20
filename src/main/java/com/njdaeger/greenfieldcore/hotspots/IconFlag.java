package com.njdaeger.greenfieldcore.hotspots;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import org.dynmap.markers.MarkerIcon;

public class IconFlag extends Flag<String> {

    private final HotspotModule module;

    public IconFlag(HotspotModule module) {
        super(String.class, "Custom icon to use when creating a hotspot", "-icon <iconId>", "icon");
        this.module = module;
    }

    @Override
    public String parse(CommandContext context, String argument) throws PDKCommandException {
        return module.getMarkerApi().getMarkerIcon(argument) == null ? null : argument;
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(module.getMarkerApi().getMarkerIcons().stream().map(MarkerIcon::getMarkerIconID).toArray(String[]::new));
    }
}
