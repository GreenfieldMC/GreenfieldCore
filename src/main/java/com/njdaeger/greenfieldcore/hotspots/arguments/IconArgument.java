package com.njdaeger.greenfieldcore.hotspots.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.services.IDynmapService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;
import org.bukkit.command.CommandSender;
import org.dynmap.markers.MarkerIcon;

import java.util.List;

public class IconArgument extends AbstractStringTypedArgument<String> {

    private static final DynamicCommandExceptionType ICON_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Icon " + o.toString() + " not found");
    private final IDynmapService dynmapService;

    public IconArgument(IDynmapService dynmapService) {
        super();
        this.dynmapService = dynmapService;
    }

    @Override
    public List<String> listBasicSuggestions(ICommandContext commandContext) {
        if (!dynmapService.isEnabled()) {
            return List.of();
        }
        return dynmapService.getMarkerAPI().getMarkerIcons()
                .stream()
                .map(MarkerIcon::getMarkerIconID)
                .toList();
    }

    @Override
    public String convertToNative(String s) {
        return s;
    }

    @Override
    public String convertToCustom(CommandSender sender, String nativeType, StringReader reader) throws CommandSyntaxException {
        if (!dynmapService.isEnabled()) {
            return nativeType;
        }
        var marker = dynmapService.getMarkerIcon(nativeType);
        if (marker == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw ICON_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return marker.getMarkerIconID();
    }
}
