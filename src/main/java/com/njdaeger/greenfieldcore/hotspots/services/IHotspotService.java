package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;
import org.bukkit.World;

import java.util.List;
import java.util.function.Predicate;

public interface IHotspotService extends IModuleService<IHotspotService> {

    /**
     * Creates a new hotspot with the given parameters
     * @param name The name of the hotspot
     * @param category The category of the hotspot
     * @param x The x coordinate of the hotspot
     * @param y The y coordinate of the hotspot
     * @param z The z coordinate of the hotspot
     * @param yaw The yaw of the hotspot
     * @param pitch The pitch of the hotspot
     * @param world The world of the hotspot
     * @param customMarker The custom marker of the hotspot, can be null
     * @return The created hotspot
     */
    Hotspot createHotspot(String name, Category category, int x, int y, int z, float yaw, float pitch, World world, String customMarker);

    /**
     * Edits the given hotspot
     *
     * @param hotspot
     * @param name
     * @param category
     * @param customMarker
     */
    void editHotspot(Hotspot hotspot, String name, Category category, String customMarker);

    /**
     * Deletes the given hotspot
     * @param hotspot The hotspot to delete
     */
    void deleteHotspot(Hotspot hotspot);

    /**
     * Gets a hotspot by its id
     * @param id The id of the hotspot
     * @return The hotspot
     */
    Hotspot getHotspot(int id);

    /**
     * Gets a list of all hotspots
     * @return The list of hotspots
     */
    default List<Hotspot> getHotspots() {
        return getHotspots(hotspot -> true);
    }

    /**
     * Gets a list of hotspots that match the given filter
     * @param filter The filter to apply to the hotspots before returning them
     * @return The list of hotspots that match the filter
     */
    List<Hotspot> getHotspots(Predicate<Hotspot> filter);

    /**
     * Creates a new category with the given parameters
     * @param name The name of the category
     * @param marker The marker of the category
     * @param id The id of the category
     * @return The created category
     */
    Category createCategory(String name, String marker, String id);

    /**
     * Edits the given category
     * @param editedCategory The category to edit
     */
    void editCategory(Category editedCategory, String name, String marker);

    /**
     * Deletes the given category
     * @param category The category to delete
     * @param replacement The category to replace the deleted category with on any hotspots that have the deleted category
     */
    void deleteCategory(Category category, Category replacement);

    /**
     * Gets a category by its id
     *
     * @param id The id of the category
     * @return The category
     */
    Category getCategory(String id);

    /**
     * Gets a list of all categories
     * @return The list of categories
     */
    default List<Category> getCategories() {
        return getCategories(category -> true);
    }

    /**
     * Gets a list of categories that match the given filter
     * @param filter The filter to apply to the categories before returning them
     * @return The list of categories that match the filter
     */
    List<Category> getCategories(Predicate<Category> filter);

}
