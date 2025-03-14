package com.njdaeger.greenfieldcore.hotspots.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.hotspots.Category;
import com.njdaeger.greenfieldcore.hotspots.Hotspot;

import java.util.Map;

public interface IHotspotStorageService extends IModuleService<IHotspotStorageService> {

    /**
     * Gets all categories
     * @return A map of all categories
     */
    Map<String, Category> getCategories();

    /**
     * Gets a category by its name
     * @param name The name of the category
     * @return The category, or null if it does not exist
     */
    Category getCategory(String name);

    /**
     * Saves a category
     * @param category The category to save
     */
    void saveCategory(Category category);

    /**
     * Deletes a category by its name
     * @param name The name of the category to delete
     */
    void deleteCategory(String name);

    /**
     * Gets all hotspots
     * @return A map of all hotspots
     */
    Map<Integer, Hotspot> getHotspots();

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
