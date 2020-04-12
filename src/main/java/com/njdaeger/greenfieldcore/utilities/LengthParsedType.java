package com.njdaeger.greenfieldcore.utilities;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.types.ParsedType;
import org.bukkit.ChatColor;

public class LengthParsedType extends ParsedType<LengthType> {

    @Override
    public LengthType parse(String input) throws BCIException {
        try {
            return LengthType.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw new BCIException(ChatColor.RED + "Unknown conversion type: " + input);
        }
    }

    @Override
    public Class<LengthType> getType() {
        return LengthType.class;
    }
}
