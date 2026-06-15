package com.ghostslots.routing;

import com.ghostslots.GhostSlotsClient;
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

    private static int findSourceIndex(PlayerInventory inventory, int targetIndex, ItemStack ghost) {
        for (int sourceIndex = 0; sourceIndex < 36; sourceIndex++) {
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
        return inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
    }
}
