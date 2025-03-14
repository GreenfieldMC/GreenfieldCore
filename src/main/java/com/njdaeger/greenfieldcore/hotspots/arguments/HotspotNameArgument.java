package com.njdaeger.greenfieldcore.hotspots.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
import com.njdaeger.greenfieldcore.hotspots.services.IHotspotService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractQuotedTypedArgument;

import java.util.List;
import java.util.function.Predicate;

public class HotspotNameArgument extends AbstractQuotedTypedArgument<Hotspot> {

    private static final DynamicCommandExceptionType HOTSPOT_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Hotspot " + o.toString() + " not found");
    private static final DynamicCommandExceptionType MULTIPLE_HOTSPOTS_FOUND = new DynamicCommandExceptionType(o -> () -> "Multiple hotspots found with the name " + o.toString());

    private final IHotspotService hotspotService;
    private final Predicate<Hotspot> filter;

    public HotspotNameArgument(IHotspotService hotspotService, Predicate<Hotspot> filter) {
        this.hotspotService = hotspotService;
        this.filter = filter;
    }

    @Override
    public Message getDefaultTooltipMessage() {
        return () -> "The name of the hotspot.";
    }

    @Override
    public List<Hotspot> listBasicSuggestions(ICommandContext commandContext) {
        return hotspotService.getHotspots(filter);
    }

    @Override
    public String convertToNative(Hotspot hotspot) {
        return hotspot.getName();
    }

    @Override
    public Hotspot convertToCustom(String nativeType, StringReader reader) throws CommandSyntaxException {
        var hotspot = hotspotService.getHotspots(hs -> hs.getName().equalsIgnoreCase(nativeType));
        if (hotspot.isEmpty()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw HOTSPOT_NOT_FOUND.createWithContext(reader, nativeType);
        }
        else if (hotspot.size() > 1) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw MULTIPLE_HOTSPOTS_FOUND.createWithContext(reader, nativeType);
        }
        else return hotspot.getFirst();
    }
}
