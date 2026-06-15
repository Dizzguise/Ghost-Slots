package com.ghostslots;

import com.ghostslots.config.GhostSlotsConfig;
import com.ghostslots.state.GhostSlotMemory;
import net.fabricmc.api.ClientModInitializer;

public final class GhostSlotsClient implements ClientModInitializer {
    public static final String MOD_ID = "ghostslots";

    private static GhostSlotsConfig config;
    private static GhostSlotMemory memory;

    @Override
    public void onInitializeClient() {
        config = GhostSlotsConfig.load();
        memory = new GhostSlotMemory(config);
    }

    public static GhostSlotsConfig config() {
        return config;
    }

    public static GhostSlotMemory memory() {
        return memory;
    }
}
