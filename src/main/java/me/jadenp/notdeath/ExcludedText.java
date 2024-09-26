package me.jadenp.notdeath;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcludedText {
    private final String name;
    private final List<String> lore;
    private final Material material;

    public ExcludedText(ConfigurationSection configuration) {
        // read the name from the configuration
        name = configuration.isSet("name") ? hex(configuration.getString("name")) : "";
        // read the lore from the configuration
        if (configuration.isString("lore")) {
            // single line
            lore = Collections.singletonList(hex(configuration.getString("lore")));
        } else if (configuration.isList("lore")) {
            // multiple lines
            List<String> tempLore = new ArrayList<>();
            for (String text : configuration.getStringList("lore")) {
                tempLore.add(hex(text));
            }
            lore = Collections.unmodifiableList(tempLore);
        } else {
            // nothing provided
            lore = Collections.emptyList();
        }
        // read material from the configuration
        String tempMaterialString = configuration.getString("material");
        if (tempMaterialString == null || tempMaterialString.isEmpty()) {
            // no material set
            material = null;
        } else {
            // try to parse material
            Material tempMaterial = null;
            try {
                tempMaterial = Material.valueOf(tempMaterialString.toUpperCase());
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[NotDeath] Invalid material \"" + tempMaterialString + "\" for excluded item: " + configuration.getName());
            }
            material = tempMaterial;
        }
    }

    /**
     * Check if an ItemStack contains the excluded text
     * @param itemStack ItemStack to check.
     * @return True if the item should be excluded.
     */
    public boolean isItemExcluded(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return false;
        // check if all the valid fields match
        return (material == null || itemStack.getType() == material)
                && (name.isEmpty() || meta.getDisplayName().contains(name))
                && (lore.isEmpty() || (meta.getLore() != null && new HashSet<>(meta.getLore()).containsAll(lore)));
    }

    /**
     * <a href="https://www.spigotmc.org/threads/hex-color-code-translate.449748/#post-3867804">...</a>
     * @author zwrumpy
     * @param message Message to be parsed for color.
     * @return A parsed message.
     */
    private static String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
