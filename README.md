# Ghost Slots

Your inventory remembers where things belong.

Ghost Slots is a small Fabric client mod for Minecraft 1.21.11. It focuses on hotbar slot memory rather than sorting: assigned hotbar slots remember the item that belongs there, show a lock/ghost visual, and try to receive matching stacks before vanilla inventory movement.

## MVP Controls

- Hover a hotbar slot and press `G` to lock that slot to its current stack.
- Carry a stack, hover an empty hotbar slot, and press `G` to lock that carried stack to the slot.
- Hover a locked hotbar slot and press `X` to unlock one slot.
- Hold `X` and drag with left mouse across hotbar slots to unlock multiple slots.
- Use the inventory overlay `Unlock` button to unlock the entire hotbar.
- Use the inventory overlay `On`/`Off` button to enable or disable the mod.

Middle-click is not used.

## Behavior

- Hotbar locking is enabled by default.
- Main inventory slots are never remembered.
- Empty locked slots render a dim saved-item ghost image.
- Occupied locked slots render a small lock marker and border.
- Picked-up items that vanilla placed in a locked hotbar slot are moved out unless they match the saved item ID.
- Picked-up items that vanilla placed elsewhere in the player inventory are recovered into empty matching hotbar slots while no screen is open.
- Manual placement of the wrong item ID into a locked hotbar slot is blocked while the inventory screen is open.
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
  "enabled": true,
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
build/libs/ghost-slots-0.2.2.jar
```
