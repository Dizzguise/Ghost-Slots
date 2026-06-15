package com.ghostslots.routing;

import com.ghostslots.config.GhostSlotsConfig;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;

import java.util.Map;
import java.util.Objects;

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

    private GhostMatcher() {
    }

    public static boolean matches(ItemStack ghost, ItemStack candidate, GhostSlotsConfig config) {
        if (ghost.isEmpty() || candidate.isEmpty()) {
            return false;
        }
        if (exactMatch(ghost, candidate)) {
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
        return ItemStack.areItemsEqual(ghost, candidate) && Objects.equals(ghost.getNbt(), candidate.getNbt());
    }

    private static boolean isGearGhost(ItemStack stack, GhostSlotsConfig config) {
        Item item = stack.getItem();
        return item instanceof ArmorItem || item instanceof SwordItem || (config.axeWeaponFallback && item instanceof AxeItem);
    }

    private static boolean sameGearType(ItemStack ghost, ItemStack candidate, GhostSlotsConfig config) {
        Item ghostItem = ghost.getItem();
        Item candidateItem = candidate.getItem();

        if (ghostItem instanceof ArmorItem ghostArmor && candidateItem instanceof ArmorItem candidateArmor) {
            return ghostArmor.getSlotType() == candidateArmor.getSlotType();
        }
        if (ghostItem instanceof SwordItem && candidateItem instanceof SwordItem) {
            return true;
        }
        return config.axeWeaponFallback && ghostItem instanceof AxeItem && candidateItem instanceof AxeItem;
    }
}
