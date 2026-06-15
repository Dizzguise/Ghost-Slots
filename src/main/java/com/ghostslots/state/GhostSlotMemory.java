package com.ghostslots.state;

import com.ghostslots.config.GhostSlotsConfig;
import com.mojang.serialization.DataResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public final class GhostSlotMemory {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ghostslots-memory.json");

    private final GhostSlotsConfig config;
    private final Map<Integer, NbtCompound> ghosts = new HashMap<>();

    public GhostSlotMemory(GhostSlotsConfig config) {
        this.config = config;
        load();
    }

    public boolean isGhostableInventoryIndex(int inventoryIndex) {
        if (inventoryIndex >= 0 && inventoryIndex < 9) {
            return true;
        }
        return config.fullInventoryGhosting && inventoryIndex >= 9 && inventoryIndex < 36;
    }

    public boolean hasGhost(int inventoryIndex) {
        return ghosts.containsKey(inventoryIndex);
    }

    public Optional<ItemStack> getGhost(int inventoryIndex) {
        NbtCompound nbt = ghosts.get(inventoryIndex);
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
        DataResult<NbtElement> encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, ghostStack);
        encoded.result()
                .filter(NbtCompound.class::isInstance)
                .map(NbtCompound.class::cast)
                .ifPresent(nbt -> {
                    ghosts.put(inventoryIndex, nbt);
                    save();
                });
    }

    public void clearGhost(int inventoryIndex) {
        ghosts.remove(inventoryIndex);
        save();
    }

    public void clearHotbar() {
        for (int i = 0; i < 9; i++) {
            ghosts.remove(i);
        }
        save();
    }

    public void clearMainInventory() {
        Iterator<Integer> iterator = ghosts.keySet().iterator();
        while (iterator.hasNext()) {
            int index = iterator.next();
            if (index >= 9 && index < 36) {
                iterator.remove();
            }
        }
        save();
    }

    public void clearAll() {
        ghosts.clear();
        save();
    }

    public Iterable<Map.Entry<Integer, NbtCompound>> entries() {
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
                NbtCompound nbt = StringNbtReader.readCompound(entry.getValue());
                if (index >= 0 && index < 36 && ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.copy()).result().filter(stack -> !stack.isEmpty()).isPresent()) {
                    ghosts.put(index, nbt);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void save() {
        GhostStore store = new GhostStore();
        for (Map.Entry<Integer, NbtCompound> entry : ghosts.entrySet()) {
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
