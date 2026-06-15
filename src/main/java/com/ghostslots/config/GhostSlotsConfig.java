package com.ghostslots.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GhostSlotsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ghostslots.json");

    public boolean fullInventoryGhosting = false;
    public boolean gearFallback = false;
    public boolean axeWeaponFallback = false;

    public static GhostSlotsConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                GhostSlotsConfig loaded = GSON.fromJson(reader, GhostSlotsConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException ignored) {
            }
        }

        GhostSlotsConfig config = new GhostSlotsConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
