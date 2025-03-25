package com.njdaeger.greenfieldcore.hotspots.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
import com.njdaeger.greenfieldcore.hotspots.services.IHotspotService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractIntegerTypedArgument;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Predicate;

public class HotspotIdArgument extends AbstractIntegerTypedArgument<Hotspot> {

    private static final DynamicCommandExceptionType HOTSPOT_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Hotspot " + o.toString() + " not found");

    private final IHotspotService hotspotService;
    private final Predicate<Hotspot> filter;

    public HotspotIdArgument(IHotspotService hotspotService, Predicate<Hotspot> filter) {
        super(0, Integer.MAX_VALUE);
        this.hotspotService = hotspotService;
        this.filter = filter;
    }

    @Override
    public Map<Hotspot, Message> listSuggestions(ICommandContext commandContext) {
        return hotspotService.getHotspots(filter).stream()
                .collect(java.util.stream.Collectors.toMap(hotspot -> hotspot, (hs) -> hs::getName));
    }

    @Override
    public Integer convertToNative(Hotspot hotspot) {
        return hotspot.getId();
    }

    @Override
    public Hotspot convertToCustom(CommandSender sender, Integer nativeType, StringReader reader) throws CommandSyntaxException {
        var hotspot = hotspotService.getHotspot(nativeType);
        if (hotspot == null) {
            reader.setCursor(reader.getCursor() - String.valueOf(nativeType).length());
            throw HOTSPOT_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return hotspot;
    }
}
