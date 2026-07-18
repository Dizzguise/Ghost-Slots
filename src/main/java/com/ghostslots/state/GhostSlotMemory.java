package com.ghostslots.state;

import com.ghostslots.config.GhostSlotsConfig;
import com.mojang.serialization.DataResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GhostSlotMemory {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ghostslots-memory.json");

    private final GhostSlotsConfig config;
    private final Map<Integer, CompoundTag> ghosts = new HashMap<>();

    public GhostSlotMemory(GhostSlotsConfig config) {
        this.config = config;
        load();
    }

    public boolean isGhostableInventoryIndex(int inventoryIndex) {
        return inventoryIndex >= 0 && inventoryIndex < 40;
    }

    public boolean hasGhost(int inventoryIndex) {
        return ghosts.containsKey(inventoryIndex);
    }

    public Optional<ItemStack> getGhost(int inventoryIndex) {
        CompoundTag nbt = ghosts.get(inventoryIndex);
        if (nbt == null) {
            return Optional.empty();
        }
        return ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.copy()).result().filter(stack -> !stack.isEmpty());
    }

    public void setGhost(int inventoryIndex, ItemStack stack) {
        if (!isGhostableInventoryIndex(inventoryIndex) || stack.isEmpty()) {
            return;
        }

        ItemStack ghostStack = stack.copyWithCount(1);
        DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, ghostStack);
        encoded.result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .ifPresent(nbt -> {
                    ghosts.put(inventoryIndex, nbt);
                    save();
                });
    }

    public void clearGhost(int inventoryIndex) {
        ghosts.remove(inventoryIndex);
        save();
    }

    public void clearAll() {
        ghosts.clear();
        save();
    }

    public Iterable<Map.Entry<Integer, CompoundTag>> entries() {
        return ghosts.entrySet();
    }

    private void load() {
        if (!Files.exists(PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            GhostStore store = GSON.fromJson(reader, GhostStore.class);
            if (store == null || store.ghosts == null) {
                return;
            }
            for (Map.Entry<String, String> entry : store.ghosts.entrySet()) {
                int index = Integer.parseInt(entry.getKey());
                CompoundTag nbt = TagParser.parseCompoundFully(entry.getValue());
                if (isGhostableInventoryIndex(index) && ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.copy()).result().filter(stack -> !stack.isEmpty()).isPresent()) {
                    ghosts.put(index, nbt);
                }
            }
        } catch (Exception ignored) {
        }
        ghosts.keySet().removeIf(index -> !isGhostableInventoryIndex(index));
        save();
    }

    private void save() {
        GhostStore store = new GhostStore();
        for (Map.Entry<Integer, CompoundTag> entry : ghosts.entrySet()) {
            store.ghosts.put(Integer.toString(entry.getKey()), entry.getValue().toString());
        }

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(store, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private static final class GhostStore {
        Map<String, String> ghosts = new HashMap<>();
    }
}
