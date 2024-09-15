package me.jadenp.inventorydeath;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathListener implements Listener {

    private final InventoryDeath plugin;
    public DeathListener(InventoryDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.isInventoryDeathEnabled() && plugin.shouldClearInventory(event.getEntity())) {
            editDeath(event);
        }
    }

    /**
     * Edit the death of a player to delete items.
     * @param event Event that handles the player's death.
     */
    private void editDeath(PlayerDeathEvent event) {
        if (plugin.isKeepInventory()) {
            // set keep inventory settings for the event
            event.setKeepInventory(true);
            event.setDroppedExp(0);
            event.getDrops().clear(); // drops must be cleared to avoid duplicates

            if (plugin.isClearExp()) {
                // must set the players exp this way if keep inventory is enabled
                event.setKeepLevel(false);
                event.setNewTotalExp(0);
            } else {
                event.setKeepLevel(true);
            }

            // kept items are modified in the current player's inventory
            ItemStack[] inventoryContents = event.getEntity().getInventory().getContents();
            for (int i = 0; i < inventoryContents.length; i++) {
                if (inventoryContents[i] != null && plugin.getExcludedItems().isItemIncluded(inventoryContents[i])) {
                    // item should be removed
                    inventoryContents[i] = null;
                }
            }
            // update inventory contents
            event.getEntity().getInventory().setContents(inventoryContents);
        } else {
            if (plugin.isClearExp())
                // clear exp
                event.setDroppedExp(0);
            // modify drops
            event.getDrops().removeIf(itemStack -> itemStack != null && plugin.getExcludedItems().isItemIncluded(itemStack));
        }
    }
}
