# Ghost Slots

Your inventory remembers where things belong.

Ghost Slots is a small Fabric client mod for Minecraft 1.21.11. It focuses on inventory slot memory rather than sorting: assigned slots remember the item that belongs there, show a lock/ghost visual, and try to receive matching stacks before vanilla inventory movement.

## Install

- `ghost-slots-0.2.5-pvp-safe.jar`: default recommended build. Supports hotbar, main inventory, and armor locks. Offhand locking is disabled, so totems are never auto-refilled.
- `ghost-slots-0.2.5-full-offhand.jar`: full build. Supports hotbar, main inventory, armor, and offhand locks. This can auto-refill totems if a totem is locked in offhand.

Install only one Ghost Slots jar at a time.

Checked-in install jars are under `release-jars/1.21.11/`.

### CurseForge / Fabric 1.21.11

1. Create or open a Minecraft Java `1.21.11` Fabric profile.
2. Make sure Fabric API for `1.21.11` is installed in the profile's `mods` folder.
3. Remove any older `ghost-slots-*.jar` files from the profile's `mods` folder.
4. Copy exactly one jar from `release-jars/1.21.11/` into the profile's `mods` folder.
5. For normal survival/PvP-adjacent use, choose `ghost-slots-0.2.5-pvp-safe.jar`.

## MVP Controls

- Hover a player inventory or armor slot and press `G` to lock that slot to its current stack.
- Carry a stack, hover an empty player inventory or armor slot, and press `G` to lock that carried stack to the slot.
- Hover a locked slot and press `X` to unlock one slot.
- Hold `X` and drag with left mouse across slots to unlock multiple slots.
- Use the inventory overlay `Unlock All` button to clear every lock.

The full-offhand build also allows the same controls on the offhand slot.

Middle-click is not used.

## Behavior

- Inventory and armor locking are enabled by default.
- Offhand locking is only available in the full-offhand build.
- Empty locked slots render a dim saved-item ghost image.
- Occupied locked slots render a small lock marker and border.
- Picked-up items that vanilla placed in a locked slot are moved out unless they match the saved item ID.
- Picked-up items that vanilla placed elsewhere in the player inventory are recovered into empty matching locked slots while no screen is open.
- Manual placement of the wrong item ID into a locked slot is blocked while the inventory screen is open.
- Left-clicking while carrying a matching stack routes it into an empty matching ghost slot first.
- Shift-left-clicking from a container routes a matching stack into an empty ghost slot before vanilla quick-move behavior.
- No full inventory sorting, wireless restock, or broad category matching is included.

## Configuration

The mod writes config files under `.minecraft/config/`:

- `ghostslots.json`
- `ghostslots-memory.json`

`ghostslots.json` options:

```json
{
  "gearFallback": false,
  "axeWeaponFallback": false
}
```

Gear fallback is intentionally narrow:

- Armor fallback stays armor-slot-specific.
- Sword fallback stays sword-specific.
- Axe fallback is disabled unless `axeWeaponFallback` is true.
- Blocks, food, redstone, tools, and general inventory items never use fallback matching.

## Build

This project uses Fabric Loom and the checked-in Gradle wrapper.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
```

The verified jar is produced at:

```text
build/libs/ghost-slots-0.2.5-pvp-safe.jar
```

Build the full offhand variant with:

```powershell
.\gradlew.bat clean build '-Pghostslots.allowOffhand=true' '-Pghostslots.variant=full-offhand'
```
