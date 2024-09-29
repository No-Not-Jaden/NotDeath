package me.jadenp.notdeath;

import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            List<ItemStack> removedContents = removeItems(inventoryContents);
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
     *
     * @param inventoryContents The items to be checked.
     * @return The items removed from the array.
     */
    private List<ItemStack> removeItems(ItemStack[] inventoryContents) {
        List<ItemStack> removedItems = new ArrayList<>(); // items to be removed from the inventory
        List<ItemStack> includedContainers = new ArrayList<>();
        for (int i = 0; i < inventoryContents.length; i++) {
            if (inventoryContents[i] != null) {
                // whether the item should be removed
                boolean included = plugin.getExcludedItems().isItemIncluded(inventoryContents[i]);
                if (plugin.isOpenContainers() && inventoryContents[i].getItemMeta() != null && inventoryContents[i].getItemMeta() instanceof BlockStateMeta &&
                        ((BlockStateMeta) Objects.requireNonNull(inventoryContents[i].getItemMeta())).getBlockState() instanceof Container) {
                    // look through container
                    BlockStateMeta blockStateMeta = (BlockStateMeta) inventoryContents[i].getItemMeta();
                    assert blockStateMeta != null;
                    Container container = (Container) blockStateMeta.getBlockState();
                    ItemStack[] containerContents = container.getInventory().getContents();

                    if (!included)  {
                        // container is excluded
                        // removed container items are not excluded
                        // remove items from the container's inventory
                        List<ItemStack> removedContainerItems = removeItems(containerContents);
                        removedItems.addAll(removedContainerItems);
                        // update the container's inventory
                        container.getInventory().setContents(containerContents);
                        blockStateMeta.setBlockState(container);
                        inventoryContents[i].setItemMeta(blockStateMeta);
                    } else {
                        // container is not excluded
                        // add to list to search through after the player has more empty slots
                        includedContainers.add(inventoryContents[i]);
                    }
                }

                // remove item from inventory
                if (included) {
                    removedItems.add(inventoryContents[i]);
                    inventoryContents[i] = null;
                }
            }
        }

        transferContainers(inventoryContents, includedContainers);

        return removedItems;
    }

    /**
     * Transfer the included container contents into the inventory.
     * If the inventory is full, contents will not be transferred.
     * @param inventoryContents Inventory to transfer contents to.
     * @param includedContainers Containers with items.
     */
    private void transferContainers(ItemStack[] inventoryContents, List<ItemStack> includedContainers) {
        for (ItemStack itemStack : includedContainers) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
            assert blockStateMeta != null;
            Container container = (Container) blockStateMeta.getBlockState();
            ItemStack[] containerContents = container.getInventory().getContents();

            for (int i = 0; i < containerContents.length; i++) {
                if (containerContents[i] != null && !plugin.getExcludedItems().isItemIncluded(containerContents[i])) {
                    // item is excluded
                    ItemStack currentItem = containerContents[i];
                    if (searchForSpot(inventoryContents, currentItem)) {
                        // item was successfully transferred
                        containerContents[i] = null;
                    }
                }
            }

            // update the container's inventory
            container.getInventory().setContents(containerContents);
            blockStateMeta.setBlockState(container);
            itemStack.setItemMeta(blockStateMeta);
        }
    }

    /**
     * Search for a spot in an inventory to place an item.
     * @param inventoryContents Inventory to search for a spot in.
     * @param currentItem Item to place in the inventory.
     * @return True if the item was successfully placed in the inventory.
     */
    private boolean searchForSpot(ItemStack[] inventoryContents, ItemStack currentItem) {
        // look for a place in the inventory to add the item that isn't in an armor slot or offhand
        int leftOver = currentItem.getAmount();
        for (int j = 0; j < Math.min(inventoryContents.length, 36); j++) {
            if (inventoryContents[j] == null) {
                inventoryContents[j] = currentItem;
                leftOver = 0;
            } else if (inventoryContents[j].isSimilar(currentItem)) {
                int newAmount = Math.min(currentItem.getMaxStackSize(), inventoryContents[j].getAmount() + currentItem.getAmount());
                leftOver = Math.max(0, currentItem.getAmount() - (currentItem.getMaxStackSize() - inventoryContents[j].getAmount()));
                inventoryContents[j].setAmount(newAmount);
                currentItem.setAmount(leftOver);
            }
            if (leftOver == 0) {
                // item was successfully transferred
                return true;
            }
        }
        return false;
    }
}
