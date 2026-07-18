package com.ghostslots.mixin;

import com.ghostslots.ScreenState;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "added", at = @At("HEAD"))
    private void ghostslots$added(CallbackInfo ci) {
        ScreenState.markScreenOpen();
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void ghostslots$removed(CallbackInfo ci) {
        ScreenState.markScreenClosed();
    }
}
