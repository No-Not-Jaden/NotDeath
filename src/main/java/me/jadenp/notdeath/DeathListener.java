package me.jadenp.notdeath;

import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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
            List<ItemStack> removedContents = removeItems(inventoryContents, true);
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


    /**
     * Removes included or excluded items from an array.
     * @param inventoryContents The items to be checked.
     * @param includedItems The returned item type. If this is true, included items that were removed will be returned.
     * @return The items removed from the array.
     */
    private List<ItemStack> removeItems(ItemStack[] inventoryContents, boolean includedItems) {
        List<ItemStack> removedItems = new ArrayList<>();
        for (int i = 0; i < inventoryContents.length; i++) {
            if (inventoryContents[i] != null) {
                // whether the item should be removed
                boolean included = plugin.getExcludedItems().isItemIncluded(inventoryContents[i]) == includedItems;
                if (plugin.isOpenContainers() && inventoryContents[i].getItemMeta() != null && inventoryContents[i].getItemMeta() instanceof BlockStateMeta) {
                    // look through container
                    BlockStateMeta blockStateMeta = (BlockStateMeta) inventoryContents[i].getItemMeta();
                    if (blockStateMeta != null && blockStateMeta.getBlockState() instanceof Container) {
                        Container container = (Container) blockStateMeta.getBlockState();
                        ItemStack[] containerContents = container.getInventory().getContents();
                        // remove items from the container's inventory
                        removedItems.addAll(removeItems(containerContents, !included));
                        // update the container's inventory
                        container.getInventory().setContents(containerContents);
                        blockStateMeta.setBlockState(container);
                        inventoryContents[i].setItemMeta(blockStateMeta);
                    }
                }

                // remove item from inventory
                if (included) {
                    removedItems.add(inventoryContents[i]);
                    inventoryContents[i] = null;
                }
            }
        }
        return removedItems;
    }
}
