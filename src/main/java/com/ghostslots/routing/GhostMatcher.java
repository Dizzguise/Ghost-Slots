package com.ghostslots.routing;

import com.ghostslots.config.GhostSlotsConfig;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Map;
import java.util.Set;

public final class GhostMatcher {
    private static final Map<Item, Item> LOWER_TIER = Map.ofEntries(
            Map.entry(Items.NETHERITE_SWORD, Items.DIAMOND_SWORD),
            Map.entry(Items.DIAMOND_SWORD, Items.IRON_SWORD),
            Map.entry(Items.IRON_SWORD, Items.STONE_SWORD),
            Map.entry(Items.STONE_SWORD, Items.WOODEN_SWORD),
            Map.entry(Items.NETHERITE_AXE, Items.DIAMOND_AXE),
            Map.entry(Items.DIAMOND_AXE, Items.IRON_AXE),
            Map.entry(Items.IRON_AXE, Items.STONE_AXE),
            Map.entry(Items.STONE_AXE, Items.WOODEN_AXE),
            Map.entry(Items.NETHERITE_HELMET, Items.DIAMOND_HELMET),
            Map.entry(Items.DIAMOND_HELMET, Items.IRON_HELMET),
            Map.entry(Items.IRON_HELMET, Items.CHAINMAIL_HELMET),
            Map.entry(Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE),
            Map.entry(Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE),
            Map.entry(Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE),
            Map.entry(Items.NETHERITE_LEGGINGS, Items.DIAMOND_LEGGINGS),
            Map.entry(Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS),
            Map.entry(Items.IRON_LEGGINGS, Items.CHAINMAIL_LEGGINGS),
            Map.entry(Items.NETHERITE_BOOTS, Items.DIAMOND_BOOTS),
            Map.entry(Items.DIAMOND_BOOTS, Items.IRON_BOOTS),
            Map.entry(Items.IRON_BOOTS, Items.CHAINMAIL_BOOTS)
    );
    private static final Set<Item> HELMETS = Set.of(Items.NETHERITE_HELMET, Items.DIAMOND_HELMET, Items.IRON_HELMET, Items.CHAINMAIL_HELMET, Items.GOLDEN_HELMET, Items.LEATHER_HELMET);
    private static final Set<Item> CHESTPLATES = Set.of(Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE);
    private static final Set<Item> LEGGINGS = Set.of(Items.NETHERITE_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.LEATHER_LEGGINGS);
    private static final Set<Item> BOOTS = Set.of(Items.NETHERITE_BOOTS, Items.DIAMOND_BOOTS, Items.IRON_BOOTS, Items.CHAINMAIL_BOOTS, Items.GOLDEN_BOOTS, Items.LEATHER_BOOTS);
    private static final Set<Item> SWORDS = Set.of(Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.WOODEN_SWORD);

    private GhostMatcher() {
    }

    public static boolean matches(ItemStack ghost, ItemStack candidate, GhostSlotsConfig config) {
        if (ghost.isEmpty() || candidate.isEmpty()) {
            return false;
        }
        if (ItemStack.areItemsEqual(ghost, candidate)) {
            return true;
        }
        if (!config.gearFallback || !isGearGhost(ghost, config)) {
            return false;
        }
        if (sameGearType(ghost, candidate, config) && ghost.isOf(candidate.getItem())) {
            return true;
        }
        return sameGearType(ghost, candidate, config) && LOWER_TIER.get(ghost.getItem()) == candidate.getItem();
    }

    public static boolean exactMatch(ItemStack ghost, ItemStack candidate) {
        return ItemStack.areItemsAndComponentsEqual(ghost, candidate);
    }

    private static boolean isGearGhost(ItemStack stack, GhostSlotsConfig config) {
        Item item = stack.getItem();
        return isArmor(item) || SWORDS.contains(item) || (config.axeWeaponFallback && item instanceof AxeItem);
    }

    private static boolean sameGearType(ItemStack ghost, ItemStack candidate, GhostSlotsConfig config) {
        Item ghostItem = ghost.getItem();
        Item candidateItem = candidate.getItem();

        if (sameSet(HELMETS, ghostItem, candidateItem)
                || sameSet(CHESTPLATES, ghostItem, candidateItem)
                || sameSet(LEGGINGS, ghostItem, candidateItem)
                || sameSet(BOOTS, ghostItem, candidateItem)) {
            return true;
        }
        if (sameSet(SWORDS, ghostItem, candidateItem)) {
            return true;
        }
        return config.axeWeaponFallback && ghostItem instanceof AxeItem && candidateItem instanceof AxeItem;
    }

    private static boolean isArmor(Item item) {
        return HELMETS.contains(item) || CHESTPLATES.contains(item) || LEGGINGS.contains(item) || BOOTS.contains(item);
    }

    private static boolean sameSet(Set<Item> items, Item ghostItem, Item candidateItem) {
        return items.contains(ghostItem) && items.contains(candidateItem);
    }
}
