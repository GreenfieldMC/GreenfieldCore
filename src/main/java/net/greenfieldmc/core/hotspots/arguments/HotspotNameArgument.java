package net.greenfieldmc.core.hotspots.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.greenfieldmc.core.hotspots.Hotspot;
import net.greenfieldmc.core.hotspots.services.IHotspotService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractQuotedTypedArgument;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Predicate;

public class HotspotNameArgument extends AbstractQuotedTypedArgument<List<Hotspot>> {

    private static final DynamicCommandExceptionType HOTSPOT_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Hotspot " + o.toString() + " not found");

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
    public List<List<Hotspot>> listBasicSuggestions(ICommandContext commandContext) {
        return hotspotService.getHotspots(filter).stream().map(List::of).toList();
    }

    @Override
    public String convertToNative(List<Hotspot> hotspot) {
        return hotspot.getFirst().getName();
    }

    @Override
    public List<Hotspot> convertToCustom(CommandSender sender, String nativeType, StringReader reader) throws CommandSyntaxException {
        var hotspots = hotspotService.getHotspots(hs -> hs.getName().equalsIgnoreCase(nativeType));
        if (hotspots.isEmpty()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw HOTSPOT_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return hotspots;
    }
}
