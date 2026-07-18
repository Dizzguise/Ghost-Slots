package com.ghostslots.mixin;

import com.ghostslots.GhostSlotsClient;
import com.ghostslots.routing.GhostMatcher;
import com.ghostslots.routing.InventorySlotMap;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.OptionalInt;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen {
    private static final int BUTTON_W = 64;
    private static final int BUTTON_H = 14;
    private static final int BUTTON_GAP = 3;

    @Shadow
    @Final
    protected T menu;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    @Final
    protected int imageWidth;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void ghostslots$extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.nextStratum();
        renderLocks(context);
        renderButtons(context, mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void ghostslots$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft == null || hoveredSlot == null) {
            return;
        }

        OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(minecraft, hoveredSlot);
        if (inventoryIndex.isEmpty()) {
            return;
        }

        if (input.key() == InputConstants.KEY_G) {
            ItemStack source = hoveredSlot.hasItem() ? hoveredSlot.getItem() : menu.getCarried();
            if (!source.isEmpty()) {
                GhostSlotsClient.memory().setGhost(inventoryIndex.getAsInt(), source);
                cir.setReturnValue(true);
            }
        } else if (input.key() == InputConstants.KEY_X) {
            GhostSlotsClient.memory().clearGhost(inventoryIndex.getAsInt());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ghostslots$mouseClicked(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) {
            return;
        }

        int button = click.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT && handleButton(click.x(), click.y())) {
            cir.setReturnValue(true);
            return;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && clearHeldDown() && clearHoveredGhost()) {
            cir.setReturnValue(true);
            return;
        }

        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }

        if (blocksLockedSlotPlacement()) {
            cir.setReturnValue(true);
            return;
        }

        if (shiftHeldDown() && routeShiftClickedStack()) {
            cir.setReturnValue(true);
            return;
        }

        if (routeCursorStack()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void ghostslots$mouseDragged(MouseButtonEvent click, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT && clearHeldDown() && clearHoveredGhost()) {
            cir.setReturnValue(true);
        }
    }

    private boolean blocksLockedSlotPlacement() {
        if (hoveredSlot == null || menu.getCarried().isEmpty()) {
            return false;
        }

        OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(minecraft, hoveredSlot);
        if (inventoryIndex.isEmpty()) {
            return false;
        }

        Optional<ItemStack> ghost = GhostSlotsClient.memory().getGhost(inventoryIndex.getAsInt());
        return ghost.isPresent() && !GhostMatcher.matches(ghost.get(), menu.getCarried(), GhostSlotsClient.config());
    }

    private void renderLocks(GuiGraphicsExtractor context) {
        for (Slot slot : menu.slots) {
            OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(Minecraft.getInstance(), slot);
            if (inventoryIndex.isEmpty() || !GhostSlotsClient.memory().hasGhost(inventoryIndex.getAsInt())) {
                continue;
            }

            Optional<ItemStack> ghost = GhostSlotsClient.memory().getGhost(inventoryIndex.getAsInt());
            if (ghost.isEmpty()) {
                continue;
            }

            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;
            int border = 0xCC48E58D;
            context.fill(slotX - 1, slotY - 1, slotX + 17, slotY, border);
            context.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, border);
            context.fill(slotX - 1, slotY - 1, slotX, slotY + 17, border);
            context.fill(slotX + 16, slotY - 1, slotX + 17, slotY + 17, border);

            if (!slot.hasItem()) {
                context.item(ghost.get(), slotX, slotY);
                context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x77000000);
            } else {
                context.fill(slotX + 11, slotY + 1, slotX + 15, slotY + 5, border);
            }
        }
    }

    private boolean routeCursorStack() {
        ItemStack cursor = menu.getCarried();
        if (cursor.isEmpty()) {
            return false;
        }

        Slot target = findEmptyMatchingGhost(cursor);
        if (target == null) {
            return false;
        }

        minecraft.gameMode.handleContainerInput(menu.containerId, menu.slots.indexOf(target), 0, ContainerInput.PICKUP, minecraft.player);
        return true;
    }

    private boolean routeShiftClickedStack() {
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return false;
        }
        if (InventorySlotMap.playerInventoryIndex(minecraft, hoveredSlot).isPresent()) {
            return false;
        }

        Slot target = findEmptyMatchingGhost(hoveredSlot.getItem());
        if (target == null) {
            return false;
        }

        int focusedSlotId = menu.slots.indexOf(hoveredSlot);
        int targetSlotId = menu.slots.indexOf(target);
        minecraft.gameMode.handleContainerInput(menu.containerId, focusedSlotId, 0, ContainerInput.PICKUP, minecraft.player);
        minecraft.gameMode.handleContainerInput(menu.containerId, targetSlotId, 0, ContainerInput.PICKUP, minecraft.player);
        if (!menu.getCarried().isEmpty()) {
            minecraft.gameMode.handleContainerInput(menu.containerId, focusedSlotId, 0, ContainerInput.PICKUP, minecraft.player);
        }
        return true;
    }

    @Nullable
    private Slot findEmptyMatchingGhost(ItemStack stack) {
        for (var entry : GhostSlotsClient.memory().entries()) {
            int inventoryIndex = entry.getKey();
            if (!GhostSlotsClient.memory().isGhostableInventoryIndex(inventoryIndex)) {
                continue;
            }

            Optional<ItemStack> ghost = GhostSlotsClient.memory().getGhost(inventoryIndex);
            if (ghost.isEmpty() || !GhostMatcher.matches(ghost.get(), stack, GhostSlotsClient.config())) {
                continue;
            }

            Slot target = InventorySlotMap.findPlayerSlot(menu, minecraft, inventoryIndex);
            if (target != null && !target.hasItem() && target.mayPlace(stack)) {
                return target;
            }
        }
        return null;
    }

    private boolean clearHoveredGhost() {
        OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(minecraft, hoveredSlot);
        if (inventoryIndex.isEmpty() || !GhostSlotsClient.memory().hasGhost(inventoryIndex.getAsInt())) {
            return false;
        }
        GhostSlotsClient.memory().clearGhost(inventoryIndex.getAsInt());
        return true;
    }

    private boolean clearHeldDown() {
        return minecraft != null && InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_X);
    }

    private boolean shiftHeldDown() {
        return minecraft != null
                && (InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_RSHIFT));
    }

    private void renderButtons(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        renderButton(context, mouseX, mouseY, 0, Component.translatable("text.ghostslots.unlock_all"));
    }

    private void renderButton(GuiGraphicsExtractor context, int mouseX, int mouseY, int index, Component label) {
        int left = buttonLeft(index);
        int top = buttonTop();
        boolean hovered = mouseX >= left && mouseX < left + BUTTON_W && mouseY >= top && mouseY < top + BUTTON_H;
        context.fill(left, top, left + BUTTON_W, top + BUTTON_H, hovered ? 0xCC53626D : 0xAA2B343B);
        int border = hovered ? 0xFFE8F1F2 : 0xFF7E8A91;
        context.fill(left, top, left + BUTTON_W, top + 1, border);
        context.fill(left, top + BUTTON_H - 1, left + BUTTON_W, top + BUTTON_H, border);
        context.fill(left, top, left + 1, top + BUTTON_H, border);
        context.fill(left + BUTTON_W - 1, top, left + BUTTON_W, top + BUTTON_H, border);
        int textWidth = font.width(label);
        context.text(font, label, left + (BUTTON_W - textWidth) / 2, top + 3, 0xFFE8F1F2, true);
    }

    private boolean handleButton(double mouseX, double mouseY) {
        int index = hoveredButton(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        GhostSlotsClient.memory().clearAll();
        return true;
    }

    private int hoveredButton(double mouseX, double mouseY) {
        for (int i = 0; i < 1; i++) {
            int left = buttonLeft(i);
            int top = buttonTop();
            if (mouseX >= left && mouseX < left + BUTTON_W && mouseY >= top && mouseY < top + BUTTON_H) {
                return i;
            }
        }
        return -1;
    }

    private int buttonLeft(int index) {
        return leftPos + imageWidth - BUTTON_W + index * (BUTTON_W + BUTTON_GAP);
    }

    private int buttonTop() {
        return topPos - BUTTON_H - 3;
    }
}
