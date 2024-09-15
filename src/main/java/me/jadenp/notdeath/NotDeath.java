package me.jadenp.notdeath;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public final class NotDeath extends JavaPlugin{

    private boolean enabled;
    private boolean keepInventory;
    private boolean deleteIncludedItems;
    private boolean worldFilterWhitelist;
    private List<String> worldFilterList;
    private ExcludedItems excludedItems;
    private ExperienceCalculator experienceCalculator;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // register event listener and command
        Bukkit.getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        Objects.requireNonNull(getCommand("NotDeath")).setExecutor(new ReloadCommand(this));
        // read config for the first time
        readConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic (none)
    }

    /**
     * Reads the config.yml file and the excluded_items.txt file
     */
    public void readConfig() {
        saveDefaultConfig(); // saves config if it doesn't exist already
        reloadConfig(); // reloads config if changes were made

        // load any missing options
        boolean madeChanges = false;
        getConfig().setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(getResource("config.yml")))));
        for (String key : Objects.requireNonNull(getConfig().getDefaults()).getKeys(true)) {
            if (!getConfig().isSet(key)) {
                getConfig().set(key, getConfig().getDefaults().get(key));
                madeChanges = true;
            }
        }
        // save config if any options were added
        if (madeChanges)
            saveConfig();

        // load config options
        enabled = getConfig().getBoolean("enabled");
        worldFilterWhitelist = getConfig().getBoolean("world-filter.whitelist");
        worldFilterList = getConfig().getStringList("world-filter.worlds");
        keepInventory = getConfig().getBoolean("keep-inventory");
        deleteIncludedItems = getConfig().getBoolean("delete-included-items");
        experienceCalculator = new ExperienceCalculator(this);
        // load excluded items file
        excludedItems = new ExcludedItems(this);
    }

    /**
     * Check if the plugin features should be enabled.
     * @return True if normal operation should occur. False if player deaths should not be modified.
     */
    public boolean isNotDeathEnabled() {
        return enabled;
    }

    public ExperienceCalculator getExperienceCalculator() {
        return experienceCalculator;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public boolean isDeleteIncludedItems() {
        return deleteIncludedItems;
    }

    public ExcludedItems getExcludedItems() {
        return excludedItems;
    }

    public boolean shouldClearInventory(Player player) {
        return player.hasPermission("notdeath.player") && isWorldWhitelisted(player.getWorld());
    }

    private boolean isWorldWhitelisted(World world) {
        return world == null || (worldFilterList.contains(world.getName()) ^ !worldFilterWhitelist);
    }

}
