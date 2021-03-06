/*
 * Copyright 2018 Bradley Steele
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.bradleysteele.commons.inventory;

import me.bradleysteele.commons.worker.WorkerBInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * The {@link BInventory} is handled by the {@link WorkerBInventory} worker. You
 * must set your implementation of this interface as your {@link InventoryHolder}
 * for the inventory you wish to listen for. For example:
 *
 *  <pre> {@code
 * class InvExample implements BInventory {
 *
 *     final Inventory inventory;
 *
 *     InvExample() {
 *         inventory = Bukkit.createInventory(this, 9);
 *     }
 * }}</pre>
 *
 * @author Bradley Steele
 */
public interface BInventory extends InventoryHolder {

    /**
     * @param event   the event fired upon clicking.
     * @param clicker player who clicked the inventory.
     * @param stack   item stack that was clicked.
     */
    default void onClick(InventoryClickEvent event, Player clicker, ItemStack stack) {}

    /**
     * @param event   the event fired upon dragging.
     * @param clicker player who dragged in the inventory.
     * @param stack   item stack which was dragged.
     */
    default void onDrag(InventoryDragEvent event, Player clicker, ItemStack stack) {}

    /**
     * @param event  the event fired upon closing.
     * @param player the player who closed the inventory.
     */
    default void onClose(InventoryCloseEvent event, Player player) {}
}