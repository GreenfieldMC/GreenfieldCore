package com.njdaeger.greenfieldcore.hotspots.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.services.IHotspotService;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.arguments.AbstractQuotedTypedArgument;

import java.util.List;
import java.util.function.Predicate;

public class CategoryNameArgument extends AbstractQuotedTypedArgument<Category> {

    private static final DynamicCommandExceptionType CATEGORY_NOT_FOUND = new DynamicCommandExceptionType(o -> () -> "Category " + o.toString() + " not found");
    private static final DynamicCommandExceptionType MULTIPLE_CATEGORIES_FOUND = new DynamicCommandExceptionType(o -> () -> "Multiple categories found with the name " + o.toString());

    private final IHotspotService hotspotService;
    private final Predicate<Category> filter;

    public CategoryNameArgument(IHotspotService hotspotService, Predicate<Category> filter) {
        this.hotspotService = hotspotService;
        this.filter = filter;
    }

    @Override
    public Message getDefaultTooltipMessage() {
        return () -> "The name of the category.";
    }

    @Override
    public List<Category> listBasicSuggestions(ICommandContext commandContext) {
        return hotspotService.getCategories(filter);
    }

    @Override
    public String convertToNative(Category category) {
        return category.getName();
    }

    @Override
    public Category convertToCustom(String nativeType, StringReader reader) throws CommandSyntaxException {
        var categories = hotspotService.getCategories(cat -> cat.getName().equalsIgnoreCase(nativeType));
        if (categories.isEmpty()) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw CATEGORY_NOT_FOUND.createWithContext(reader, nativeType);
        }
        if (categories.size() > 1) {
            reader.setCursor(reader.getCursor() - nativeType.length());
            throw MULTIPLE_CATEGORIES_FOUND.createWithContext(reader, nativeType);
        }
        return categories.getFirst();
    }
}
