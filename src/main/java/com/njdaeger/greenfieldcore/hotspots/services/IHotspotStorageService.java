package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;

import java.util.List;
import java.util.function.Predicate;

public interface IHotspotStorageService extends IModuleService<IHotspotStorageService> {

    /**
     * Creates a new category with the given parameters
     * @param filter The filter to apply to the categories before returning them
     * @return The list of categories that match the filter
     */
    List<Category> getCategories(Predicate<Category> filter);

    /**
     * Gets all categories
     * @return A map of all categories
     */
    default List<Category> getCategories() {
        return getCategories(category -> true);
    }

    /**
     * Gets a category by its name
     *
     * @param id The id of the category
     * @return The category, or null if it does not exist
     */
    Category getCategory(String id);

    /**
     * Saves a category
     * @param category The category to save
     */
    void saveCategory(Category category);

    /**
     * Deletes a category by its id
     *
     * @param id The id of the category to delete
     */
    void deleteCategory(String id);

    /**
     * Gets the next available hotspot id
     * @return The next available hotspot id
     */
    int getNextHotspotId();

    /**
     * Gets all hotspots
     * @param filter A predicate to filter the hotspots, only hotspots that match the predicate will be returned
     * @return A map of all hotspots
     */
    List<Hotspot> getHotspots(Predicate<Hotspot> filter);

    /**
     * Gets all hotspots without any filter
     * @return A map of all hotspots
     */
    default List<Hotspot> getHotspots() {
        return getHotspots(hotspot -> true);
    }

    /**
     * Gets a hotspot by its id
     * @param id The id of the hotspot
     * @return The hotspot, or null if it does not exist
     */
    Hotspot getHotspot(int id);

    /**
     * Saves a hotspot
     * @param hotspot The hotspot to save
     */
    void saveHotspot(Hotspot hotspot);

    /**
     * Deletes a hotspot by its id
     * @param id The id of the hotspot to delete
     */
    void deleteHotspot(int id);

    /**
     * Saves the database
     */
    void saveDatabase();
}
