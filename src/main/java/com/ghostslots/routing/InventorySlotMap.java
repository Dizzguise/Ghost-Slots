package com.ghostslots.routing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.OptionalInt;

public final class InventorySlotMap {
    private InventorySlotMap() {
    }

    public static OptionalInt playerInventoryIndex(MinecraftClient client, Slot slot) {
        if (client.player == null || slot == null) {
            return OptionalInt.empty();
        }
        PlayerInventory inventory = client.player.getInventory();
        if (slot.inventory != inventory) {
            return OptionalInt.empty();
        }
        int index = slot.getIndex();
        return index >= 0 && index < 36 ? OptionalInt.of(index) : OptionalInt.empty();
    }

    public static Slot findPlayerSlot(ScreenHandler handler, MinecraftClient client, int inventoryIndex) {
        if (client.player == null) {
            return null;
        }
        PlayerInventory inventory = client.player.getInventory();
        for (Slot slot : handler.slots) {
            if (slot.inventory == inventory && slot.getIndex() == inventoryIndex) {
                return slot;
            }
        }
        return null;
    }
}
