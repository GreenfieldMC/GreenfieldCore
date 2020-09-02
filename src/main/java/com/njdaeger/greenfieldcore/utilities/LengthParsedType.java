package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.types.ParsedType;
import org.bukkit.ChatColor;

public class LengthParsedType extends ParsedType<LengthType> {

    @Override
    public LengthType parse(String input) throws PDKCommandException {
        try {
            return LengthType.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw new PDKCommandException(ChatColor.RED + "Unknown conversion type: " + input);
        }
    }

    @Override
    public Class<LengthType> getType() {
        return LengthType.class;
    }
}
