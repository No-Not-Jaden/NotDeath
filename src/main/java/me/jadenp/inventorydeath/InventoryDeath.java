package me.jadenp.inventorydeath;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class InventoryDeath extends JavaPlugin{

    private boolean enabled;
    private boolean clearExp;
    private boolean keepInventory;
    private boolean worldFilterWhitelist;
    private List<String> worldFilterList;
    private ExcludedItems excludedItems;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // register event listener and command
        Bukkit.getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        Objects.requireNonNull(getCommand("InventoryDeath")).setExecutor(new ReloadCommand(this));
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
        reloadConfig();
        saveDefaultConfig();
        // load config options
        enabled = getConfig().getBoolean("enabled");
        worldFilterWhitelist = getConfig().getBoolean("world-filter.whitelist");
        worldFilterList = getConfig().getStringList("world-filter.worlds");
        clearExp = getConfig().getBoolean("clear-exp");
        keepInventory = getConfig().getBoolean("keep-inventory");
        // load excluded items
        excludedItems = new ExcludedItems(this);
    }

    /**
     * Check if the plugin features should be enabled.
     * @return True if normal operation should occur. False if player deaths should not be modified.
     */
    public boolean isInventoryDeathEnabled() {
        return enabled;
    }

    public boolean isClearExp() {
        return clearExp;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public ExcludedItems getExcludedItems() {
        return excludedItems;
    }

    public boolean shouldClearInventory(Player player) {
        return player.hasPermission("inventorydeath.player") && isWorldWhitelisted(player.getWorld());
    }

    private boolean isWorldWhitelisted(World world) {
        return world == null || (worldFilterList.contains(world.getName()) ^ !worldFilterWhitelist);
    }

}
