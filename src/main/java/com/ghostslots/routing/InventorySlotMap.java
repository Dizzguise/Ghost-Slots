package com.ghostslots.routing;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.OptionalInt;

public final class InventorySlotMap {
    private InventorySlotMap() {
    }

    public static OptionalInt playerInventoryIndex(Minecraft client, Slot slot) {
        if (client.player == null || slot == null) {
            return OptionalInt.empty();
        }
        Inventory inventory = client.player.getInventory();
        if (slot.container != inventory) {
            return OptionalInt.empty();
        }
        int index = slot.getContainerSlot();
        if (index >= 0 && index < 40) {
            return OptionalInt.of(index);
        }
        return OptionalInt.empty();
    }

    public static Slot findPlayerSlot(AbstractContainerMenu handler, Minecraft client, int inventoryIndex) {
        if (client.player == null) {
            return null;
        }
        Inventory inventory = client.player.getInventory();
        for (Slot slot : handler.slots) {
            if (slot.container == inventory && slot.getContainerSlot() == inventoryIndex) {
                return slot;
            }
        }
        return null;
    }
}
