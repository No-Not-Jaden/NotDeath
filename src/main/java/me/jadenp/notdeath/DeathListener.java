package me.jadenp.notdeath;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {

    private final NotDeath plugin;
    public DeathListener(NotDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.isNotDeathEnabled() && plugin.shouldClearInventory(event.getEntity())) {
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
            event.getDrops().clear(); // drops must be cleared to avoid duplicates

            // kept items are modified in the current player's inventory
            ItemStack[] inventoryContents = event.getEntity().getInventory().getContents();
            List<ItemStack> removedContents = removeIncludedItems(inventoryContents);
            if (!plugin.isDeleteIncludedItems()) {
                // add the removed contents to the drops
                event.getDrops().addAll(removedContents);
            }
            // update inventory contents
            event.getEntity().getInventory().setContents(inventoryContents);
        } else {
            // modify drops
            event.getDrops().removeIf(itemStack -> itemStack != null && plugin.getExcludedItems().isItemIncluded(itemStack));
        }

        // send the event over to the experience calculator to set the correct exp drops
        plugin.getExperienceCalculator().handleDeath(event);
    }


    private List<ItemStack> removeIncludedItems(ItemStack[] inventoryContents) {
        List<ItemStack> removedItems = new ArrayList<>();
        for (int i = 0; i < inventoryContents.length; i++) {
            if (inventoryContents[i] != null && plugin.getExcludedItems().isItemIncluded(inventoryContents[i])) {
                // item should be removed
                removedItems.add(inventoryContents[i]);
                inventoryContents[i] = null;
            }
        }
        return removedItems;
    }
}
