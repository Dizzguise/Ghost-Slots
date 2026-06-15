package com.ghostslots.routing;

import com.ghostslots.GhostSlotsClient;
import com.ghostslots.GhostSlotsBuildOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

public final class PickupRouter {
    private static int cooldownTicks;

    private PickupRouter() {
    }

    public static void routePlayerInventory(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null || client.currentScreen != null) {
            return;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        PlayerInventory inventory = client.player.getInventory();
        if (evictMismatchedLockedSlots(client, inventory)) {
            cooldownTicks = 2;
            return;
        }

        for (var entry : GhostSlotsClient.memory().entries()) {
            int targetIndex = entry.getKey();
            if (!GhostSlotsClient.memory().isGhostableInventoryIndex(targetIndex) || !inventory.getStack(targetIndex).isEmpty()) {
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

    private static boolean evictMismatchedLockedSlots(MinecraftClient client, PlayerInventory inventory) {
        for (var entry : GhostSlotsClient.memory().entries()) {
            int sourceIndex = entry.getKey();
            if (!GhostSlotsClient.memory().isGhostableInventoryIndex(sourceIndex)) {
                continue;
            }

            ItemStack stack = inventory.getStack(sourceIndex);
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

    private static int findSourceIndex(PlayerInventory inventory, int targetIndex, ItemStack ghost) {
        int lastSourceIndex = GhostSlotsBuildOptions.ALLOW_OFFHAND_LOCKING ? 40 : 39;
        for (int sourceIndex = 0; sourceIndex <= lastSourceIndex; sourceIndex++) {
            if (sourceIndex == targetIndex) {
                continue;
            }

            ItemStack candidate = inventory.getStack(sourceIndex);
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

    private static int findEmptyUnlockedInventorySlot(PlayerInventory inventory) {
        for (int index = 0; index < 36; index++) {
            if (inventory.getStack(index).isEmpty() && !GhostSlotsClient.memory().hasGhost(index)) {
                return index;
            }
        }
        return -1;
    }

    private static void moveStack(MinecraftClient client, int sourceIndex, int targetIndex) {
        int syncId = client.player.playerScreenHandler.syncId;
        int sourceSlotId = playerHandlerSlotId(sourceIndex);
        int targetSlotId = playerHandlerSlotId(targetIndex);

        client.interactionManager.clickSlot(syncId, sourceSlotId, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(syncId, targetSlotId, 0, SlotActionType.PICKUP, client.player);
        if (!client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(syncId, sourceSlotId, 0, SlotActionType.PICKUP, client.player);
        }
    }

    private static int playerHandlerSlotId(int inventoryIndex) {
        if (inventoryIndex < 9) {
            return 36 + inventoryIndex;
        }
        if (inventoryIndex < 36) {
            return inventoryIndex;
        }
        if (inventoryIndex < 40) {
            return 44 - inventoryIndex;
        }
        if (inventoryIndex == 40) {
            return 45;
        }
        return inventoryIndex;
    }
}
