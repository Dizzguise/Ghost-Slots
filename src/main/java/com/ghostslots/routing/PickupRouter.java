package com.ghostslots.routing;

import com.ghostslots.GhostSlotsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class PickupRouter {
    private static int cooldownTicks;

    private PickupRouter() {
    }

    public static void routePlayerInventory(Minecraft client) {
        if (client.player == null || client.gameMode == null || client.gui.screen() != null || client.player.containerMenu != client.player.inventoryMenu) {
            return;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        Inventory inventory = client.player.getInventory();
        if (evictMismatchedLockedSlots(client, inventory)) {
            cooldownTicks = 2;
            return;
        }

        for (var entry : GhostSlotsClient.memory().entries()) {
            int targetIndex = entry.getKey();
            if (!GhostSlotsClient.memory().isGhostableInventoryIndex(targetIndex) || !inventory.getItem(targetIndex).isEmpty()) {
                continue;
            }

            Optional<ItemStack> ghost = GhostSlotsClient.memory().getGhost(targetIndex);
            if (ghost.isEmpty()) {
                continue;
            }

            int sourceIndex = findSourceIndex(inventory, targetIndex, ghost.get());
            if (sourceIndex < 0) {
                continue;
            }

            moveStack(client, sourceIndex, targetIndex);
            cooldownTicks = 4;
            return;
        }
    }

    private static boolean evictMismatchedLockedSlots(Minecraft client, Inventory inventory) {
        for (var entry : GhostSlotsClient.memory().entries()) {
            int sourceIndex = entry.getKey();
            if (!GhostSlotsClient.memory().isGhostableInventoryIndex(sourceIndex)) {
                continue;
            }

            ItemStack stack = inventory.getItem(sourceIndex);
            if (stack.isEmpty()) {
                continue;
            }

            Optional<ItemStack> ghost = GhostSlotsClient.memory().getGhost(sourceIndex);
            if (ghost.isEmpty() || GhostMatcher.matches(ghost.get(), stack, GhostSlotsClient.config())) {
                continue;
            }

            int targetIndex = findEmptyUnlockedInventorySlot(inventory);
            if (targetIndex < 0) {
                return false;
            }

            moveStack(client, sourceIndex, targetIndex);
            return true;
        }
        return false;
    }

    private static int findSourceIndex(Inventory inventory, int targetIndex, ItemStack ghost) {
        for (int sourceIndex = 0; sourceIndex < 40; sourceIndex++) {
            if (sourceIndex == targetIndex) {
                continue;
            }

            ItemStack candidate = inventory.getItem(sourceIndex);
            if (candidate.isEmpty() || !GhostMatcher.matches(ghost, candidate, GhostSlotsClient.config())) {
                continue;
            }

            Optional<ItemStack> sourceGhost = GhostSlotsClient.memory().getGhost(sourceIndex);
            if (sourceGhost.isPresent() && GhostMatcher.matches(sourceGhost.get(), candidate, GhostSlotsClient.config())) {
                continue;
            }
            return sourceIndex;
        }
        return -1;
    }

    private static int findEmptyUnlockedInventorySlot(Inventory inventory) {
        for (int index = 0; index < 36; index++) {
            if (inventory.getItem(index).isEmpty() && !GhostSlotsClient.memory().hasGhost(index)) {
                return index;
            }
        }
        return -1;
    }

    private static void moveStack(Minecraft client, int sourceIndex, int targetIndex) {
        int syncId = client.player.inventoryMenu.containerId;
        int sourceSlotId = playerHandlerSlotId(client, sourceIndex);
        int targetSlotId = playerHandlerSlotId(client, targetIndex);
        if (sourceSlotId < 0 || targetSlotId < 0) {
            return;
        }

        client.gameMode.handleContainerInput(syncId, sourceSlotId, 0, ContainerInput.PICKUP, client.player);
        client.gameMode.handleContainerInput(syncId, targetSlotId, 0, ContainerInput.PICKUP, client.player);
        if (!client.player.inventoryMenu.getCarried().isEmpty()) {
            client.gameMode.handleContainerInput(syncId, sourceSlotId, 0, ContainerInput.PICKUP, client.player);
        }
    }

    private static int playerHandlerSlotId(Minecraft client, int inventoryIndex) {
        Inventory inventory = client.player.getInventory();
        for (int slotId = 0; slotId < client.player.inventoryMenu.slots.size(); slotId++) {
            Slot slot = client.player.inventoryMenu.slots.get(slotId);
            if (slot.container == inventory && slot.getContainerSlot() == inventoryIndex) {
                return slotId;
            }
        }
        return -1;
    }
}
