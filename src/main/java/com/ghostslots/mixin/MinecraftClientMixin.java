package com.ghostslots.mixin;

import com.ghostslots.GhostSlotsClient;
import com.ghostslots.routing.PickupRouter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void ghostslots$tick(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        GhostSlotsClient.syncMemoryContext(client);
        PickupRouter.routePlayerInventory(client);
    }
}
