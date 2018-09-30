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

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Bradley Steele
 */
public final class Inventories {

    private Inventories() {}

    public static Inventory clone(Inventory inventory, InventoryHolder holder) {
        Inventory inv = Bukkit.createInventory(holder, inventory.getSize(), inventory.getTitle());
        inv.setContents(inventory.getContents().clone());

        return inv;
    }

    public static void setItem(Inventory inventory, ItemStack item, int... slots) {
        Arrays.stream(slots).forEach(slot -> inventory.setItem(slot, item.clone()));
    }

    public static void setRow(Inventory inventory, ItemStack item, int row) {
        int rowStart = row * 9;
        int rowEnd = rowStart + 8;

        for (int slot = rowStart; slot <= rowEnd; slot++) {
            inventory.setItem(slot, item.clone());
        }
    }
}