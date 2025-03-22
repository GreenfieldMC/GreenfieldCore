package com.njdaeger.greenfieldcore.hotspots.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.services.IHotspotService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractStringTypedArgument;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CategoryIdArgument extends AbstractStringTypedArgument<Category> {

    private static final DynamicCommandExceptionType CATEGORY_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Category " + o.toString() + " not found");

    private final IHotspotService hotspotService;
    private final Predicate<Category> filter;

    public CategoryIdArgument(IHotspotService hotspotService, Predicate<Category> filter) {
        this.hotspotService = hotspotService;
        this.filter = filter;
    }

    @Override
    public Map<Category, Message> listSuggestions(ICommandContext commandContext) {
        return hotspotService.getCategories(filter).stream().collect(Collectors.toMap(cat -> cat, cat -> cat::getName));
    }

    @Override
    public Message getDefaultTooltipMessage() {
        return () -> "The id of the category.";
    }

    @Override
    public String convertToNative(Category category) {
        return category.getId();
    }

    @Override
    public Category convertToCustom(String nativeType, StringReader reader) throws CommandSyntaxException {
        var category = hotspotService.getCategory(nativeType);
        if (category == null) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw CATEGORY_NOT_FOUND.createWithContext(reader, nativeType);
        }
        return category;
    }
}
