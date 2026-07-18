package com.ghostslots;

import com.ghostslots.config.GhostSlotsConfig;
import com.ghostslots.mixin.MinecraftServerAccessor;
import com.ghostslots.state.GhostSlotMemory;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;

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

    public static void syncMemoryContext(Minecraft client) {
        memory.activate(memoryContext(client));
    }

    private static String memoryContext(Minecraft client) {
        if (client.player == null) {
            return null;
        }

        IntegratedServer localServer = client.getSingleplayerServer();
        if (localServer != null) {
            String levelId = ((MinecraftServerAccessor) localServer).ghostslots$getStorageSource().getLevelId();
            return "singleplayer:" + levelId;
        }

        ServerData server = client.getCurrentServer();
        if (server != null && server.ip != null && !server.ip.isBlank()) {
            return "multiplayer:" + server.ip.toLowerCase(java.util.Locale.ROOT);
        }

        ClientPacketListener connection = client.getConnection();
        if (connection != null && connection.getConnection().getRemoteAddress() != null) {
            return "connection:" + connection.getConnection().getRemoteAddress();
        }
        return null;
    }
}
