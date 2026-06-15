# Ghost Slots

Your inventory remembers where things belong.

Ghost Slots is a small Fabric client mod for Minecraft 1.20.1. It focuses on slot memory rather than sorting: assigned slots remember the item that belongs there, show a faint icon while empty, and try to receive matching stacks before vanilla inventory movement.

## MVP Controls

- Hover a player inventory slot and press `G` to assign a ghost from the slot's current stack.
- Carry a stack, hover an empty player inventory slot, and press `G` to assign that carried stack as the ghost.
- Hover a ghosted slot and press `X` to clear one ghost.
- Hold `X` and drag with left mouse across player inventory slots to clear multiple ghosts.
- Use the inventory overlay buttons to clear `Hotbar`, `Main`, or `All` ghosts.

Middle-click is not used.

## Behavior

- Hotbar ghosting is enabled by default.
- Main inventory ghosting is optional and disabled by default.
- Empty ghosted slots render a dim item icon.
- Picked-up items that vanilla placed elsewhere in the player inventory are recovered into empty matching ghost slots while no screen is open.
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
  "fullInventoryGhosting": false,
  "gearFallback": false,
  "axeWeaponFallback": false
}
```

Builder mode is represented by enabling `fullInventoryGhosting`. Full inventory ghosts use exact item and NBT matching only unless `gearFallback` is also enabled and the ghost item is supported gear.

Gear fallback is intentionally narrow:

- Armor fallback stays armor-slot-specific.
- Sword fallback stays sword-specific.
- Axe fallback is disabled unless `axeWeaponFallback` is true.
- Blocks, food, redstone, tools, and general inventory items never use fallback matching.

## Build

This project uses Fabric Loom and Gradle.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
gradle build
```

The verified jar is produced at:

```text
build/libs/ghost-slots-0.1.0.jar
```
