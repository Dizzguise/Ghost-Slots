package com.ghostslots.mixin;

import com.ghostslots.GhostSlotsClient;
import com.ghostslots.routing.GhostMatcher;
import com.ghostslots.routing.InventorySlotMap;
import net.minecraft.client.gui.Click;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.OptionalInt;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    private static final int BUTTON_W = 48;
    private static final int BUTTON_H = 14;
    private static final int BUTTON_GAP = 3;

    @Shadow
    @Final
    protected T handler;

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected int backgroundWidth;

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void ghostslots$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        renderLocks(context);
        renderButtons(context, mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void ghostslots$keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (!GhostSlotsClient.config().enabled || client == null || focusedSlot == null) {
            return;
        }

        OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(client, focusedSlot);
        if (inventoryIndex.isEmpty()) {
            return;
        }

        if (input.key() == GLFW.GLFW_KEY_G) {
            ItemStack source = focusedSlot.hasStack() ? focusedSlot.getStack() : handler.getCursorStack();
            if (!source.isEmpty()) {
                GhostSlotsClient.memory().setGhost(inventoryIndex.getAsInt(), source);
                cir.setReturnValue(true);
            }
        } else if (input.key() == GLFW.GLFW_KEY_X) {
            GhostSlotsClient.memory().clearGhost(inventoryIndex.getAsInt());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ghostslots$mouseClicked(Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (client == null || client.player == null || client.interactionManager == null) {
            return;
        }

        int button = click.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && handleButton(click.x(), click.y())) {
            cir.setReturnValue(true);
            return;
        }

        if (!GhostSlotsClient.config().enabled) {
            return;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && clearHeldDown() && clearHoveredGhost()) {
            cir.setReturnValue(true);
            return;
        }

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
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
    private void ghostslots$mouseDragged(Click click, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (GhostSlotsClient.config().enabled && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && clearHeldDown() && clearHoveredGhost()) {
            cir.setReturnValue(true);
        }
    }

    private void renderLocks(DrawContext context) {
        for (Slot slot : handler.slots) {
            OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(MinecraftClient.getInstance(), slot);
            if (inventoryIndex.isEmpty() || !GhostSlotsClient.memory().hasGhost(inventoryIndex.getAsInt())) {
                continue;
            }

            Optional<ItemStack> ghost = GhostSlotsClient.memory().getGhost(inventoryIndex.getAsInt());
            if (ghost.isEmpty()) {
                continue;
            }

            int slotX = x + slot.x;
            int slotY = y + slot.y;
            int border = GhostSlotsClient.config().enabled ? 0xCC48E58D : 0xCC7E8A91;
            context.fill(slotX - 1, slotY - 1, slotX + 17, slotY, border);
            context.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, border);
            context.fill(slotX - 1, slotY - 1, slotX, slotY + 17, border);
            context.fill(slotX + 16, slotY - 1, slotX + 17, slotY + 17, border);

            if (!slot.hasStack()) {
                context.drawItem(ghost.get(), slotX, slotY);
                context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x77000000);
            } else {
                context.fill(slotX + 11, slotY + 1, slotX + 15, slotY + 5, border);
            }
        }
    }

    private boolean routeCursorStack() {
        ItemStack cursor = handler.getCursorStack();
        if (!GhostSlotsClient.config().enabled || cursor.isEmpty()) {
            return false;
        }

        Slot target = findEmptyMatchingGhost(cursor);
        if (target == null) {
            return false;
        }

        client.interactionManager.clickSlot(handler.syncId, target.id, 0, SlotActionType.PICKUP, client.player);
        return true;
    }

    private boolean routeShiftClickedStack() {
        if (!GhostSlotsClient.config().enabled || focusedSlot == null || !focusedSlot.hasStack()) {
            return false;
        }
        if (InventorySlotMap.playerInventoryIndex(client, focusedSlot).isPresent()) {
            return false;
        }

        Slot target = findEmptyMatchingGhost(focusedSlot.getStack());
        if (target == null) {
            return false;
        }

        client.interactionManager.clickSlot(handler.syncId, focusedSlot.id, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(handler.syncId, target.id, 0, SlotActionType.PICKUP, client.player);
        if (!handler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(handler.syncId, focusedSlot.id, 0, SlotActionType.PICKUP, client.player);
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

            Slot target = InventorySlotMap.findPlayerSlot(handler, client, inventoryIndex);
            if (target != null && !target.hasStack() && target.canInsert(stack)) {
                return target;
            }
        }
        return null;
    }

    private boolean clearHoveredGhost() {
        OptionalInt inventoryIndex = InventorySlotMap.playerInventoryIndex(client, focusedSlot);
        if (inventoryIndex.isEmpty() || !GhostSlotsClient.memory().hasGhost(inventoryIndex.getAsInt())) {
            return false;
        }
        GhostSlotsClient.memory().clearGhost(inventoryIndex.getAsInt());
        return true;
    }

    private boolean clearHeldDown() {
        return client != null && InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_X);
    }

    private boolean shiftHeldDown() {
        return client != null
                && (InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    private void renderButtons(DrawContext context, int mouseX, int mouseY) {
        renderButton(context, mouseX, mouseY, 0, GhostSlotsClient.config().enabled ? Text.translatable("text.ghostslots.on") : Text.translatable("text.ghostslots.off"));
        renderButton(context, mouseX, mouseY, 1, Text.translatable("text.ghostslots.unlock"));
    }

    private void renderButton(DrawContext context, int mouseX, int mouseY, int index, Text label) {
        int left = buttonLeft(index);
        int top = buttonTop();
        boolean hovered = mouseX >= left && mouseX < left + BUTTON_W && mouseY >= top && mouseY < top + BUTTON_H;
        context.fill(left, top, left + BUTTON_W, top + BUTTON_H, hovered ? 0xCC53626D : 0xAA2B343B);
        int border = hovered ? 0xFFE8F1F2 : 0xFF7E8A91;
        context.fill(left, top, left + BUTTON_W, top + 1, border);
        context.fill(left, top + BUTTON_H - 1, left + BUTTON_W, top + BUTTON_H, border);
        context.fill(left, top, left + 1, top + BUTTON_H, border);
        context.fill(left + BUTTON_W - 1, top, left + BUTTON_W, top + BUTTON_H, border);
        int textWidth = textRenderer.getWidth(label);
        context.drawTextWithShadow(textRenderer, label, left + (BUTTON_W - textWidth) / 2, top + 3, 0xFFE8F1F2);
    }

    private boolean handleButton(double mouseX, double mouseY) {
        int index = hoveredButton(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        if (index == 0) {
            GhostSlotsClient.config().enabled = !GhostSlotsClient.config().enabled;
            GhostSlotsClient.config().save();
        } else {
            GhostSlotsClient.memory().clearHotbar();
        }
        return true;
    }

    private int hoveredButton(double mouseX, double mouseY) {
        for (int i = 0; i < 2; i++) {
            int left = buttonLeft(i);
            int top = buttonTop();
            if (mouseX >= left && mouseX < left + BUTTON_W && mouseY >= top && mouseY < top + BUTTON_H) {
                return i;
            }
        }
        return -1;
    }

    private int buttonLeft(int index) {
        return x + backgroundWidth - (BUTTON_W * 2 + BUTTON_GAP) + index * (BUTTON_W + BUTTON_GAP);
    }

    private int buttonTop() {
        return y - BUTTON_H - 3;
    }
}
