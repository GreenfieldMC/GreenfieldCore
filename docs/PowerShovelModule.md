# Power Shovel Module Documentation

The Power Shovel module provides a special tool for utility and infrastructure work, allowing players to place transformers and meters with ease.

## Permissions
- `greenfieldcore.powershovel` — Required to use the Power Shovel and its command.

## Commands

### `/powershovel`, `/ps`
**Description:** Gives the player a Power Shovel item.
- **Permission:** `greenfieldcore.powershovel`
- **Usage:** `/powershovel`
- **Effect:**
  - The player receives a Power Shovel in their inventory.
  - A message confirms receipt: "You now have the power shovel."

## Power Shovel Item Behavior
- **Type:** Iron Shovel with custom name, unbreakable, and hidden enchantments.
- **Usage:**
  1. **Right-click a fence** with the Power Shovel to place a transformer (custom head) on top of a mossy cobblestone wall next to the fence.
  2. **Right-click a wall** to place a meter (mossy cobblestone wall) with correct orientation.
  3. **Block Logging:** All block changes are logged with CoreProtect for rollback and auditing.
- **Cooldown:** There is a short cooldown (500ms) between uses to prevent accidental double clicks.
- **Block Face Restrictions:** Only clicks on the EAST, NORTH, SOUTH, or WEST faces are valid.