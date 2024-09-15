package me.jadenp.inventorydeath;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ExcludedItems {
    private final List<Material> excludedMaterials = new ArrayList<>();
    private final Map<Material, List<Integer>> excludedCustomModelData = new EnumMap<>(Material.class);

    /**
     * Reads the excluded items from the excluded_items.txt file.
     * @param plugin Plugin that holds the file.
     */
    public ExcludedItems(InventoryDeath plugin) {
        File excludedItems = new File(plugin.getDataFolder() + File.separator + "excluded_items.txt");
        // create file if it doesn't exist already
        if (!excludedItems.exists())
            plugin.saveResource("excluded_items.txt", false);

        try(BufferedReader reader = new BufferedReader(new FileReader(excludedItems))) {
            String line = reader.readLine();

            while (line != null) {
                if (line.startsWith("--") || line.isEmpty()) {
                    // lines that start with -- are used for comments
                    line = reader.readLine();
                    continue;
                }
                // split line in case it has extra data
                // the first string should be the material
                // the second (optional) string may have custom model data
                String[] lineData = line.split(":");
                // get the material from the line data
                Material material = getMaterial(lineData);
                if (material != null) {
                    saveExcludedMaterial(material, lineData);
                }

                line = reader.readLine();
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("[InventoryDeath] Error reading excluded_items.txt");
            Bukkit.getLogger().throwing(ExcludedItems.class.getName(), "Constructor", e);
        }
    }

    /**
     * Save a material to be excluded from deletion.
     * @param material A material to be excluded from deletion.
     * @param lineData A line in the excluded_items.txt file split by colons.
     */
    private void saveExcludedMaterial(Material material, String[] lineData) {
        if (lineData.length > 1) {
            int customModelData = getCustomModelData(lineData);
            if (customModelData != -1) {
                if (excludedCustomModelData.containsKey(material)) {
                    excludedCustomModelData.get(material).add(customModelData);
                } else {
                    excludedCustomModelData.put(material, new ArrayList<>(Collections.singletonList(customModelData)));
                }
            }
        } else {
            excludedMaterials.add(material);
        }
    }

    /**
     * Gets the material from the line data of excluded_items.txt.
     * @param lineData A line in the file split by colons.
     * @return The material for the line, or null if the name was invalid.
     */
    private @Nullable Material getMaterial(String[] lineData) {
        try {
            return Material.valueOf(lineData[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[InventoryDeath] Unknown material \"" + lineData[0] + "\" in excluded_items.txt");
            return null;
        }
    }

    /**
     * Gets the custom model data for the line data of excluded_items.txt.
     * lineData should be at least 2 elements long.
     * @param lineData A line in the file split by colons.
     * @return The customModelData for the line, or -1 if the value wasn't an integer.
     */
    private int getCustomModelData(String[] lineData) {
        try {
            return Integer.parseInt(lineData[1]);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[InventoryDeath] Invalid custom model of \"" + lineData[1] + "\" for material \"" + lineData[0] + "\"");
            return -1;
        }
    }

    /**
     * Checks if the {@link ItemStack} should be deleted.
     * @param itemStack Item to be deleted.
     * @return True if the item should be deleted.
     */
    public boolean isItemIncluded(@Nonnull ItemStack itemStack) {
        return !excludedMaterials.contains(itemStack.getType()) &&
                (itemStack.getItemMeta() == null || !itemStack.getItemMeta().hasCustomModelData() || !excludedCustomModelData.containsKey(itemStack.getType()) || !excludedCustomModelData.get(itemStack.getType()).contains(itemStack.getItemMeta().getCustomModelData()));
    }
}
