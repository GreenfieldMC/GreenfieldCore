# Power Shovel Module Documentation

The Power Shovel module provides a special tool for utility and infrastructure work, allowing players to place transformers, meters, and power lines with ease.

## Permissions
- `greenfieldcore.powershovel` — Required to use the Power Shovel and its command.

## Commands

### `/powershovel`, `/ps`
**Description:** Gives the player a Power Shovel item with selected mode.
- **Permission:** `greenfieldcore.powershovel`
- **Usage:** 
  - `/ps powerconnector` - Set to powerconnector mode
  - `/ps powerline` - Set to powerline mode
  - `/ps powerline <droopStrength>` - Set to powerline mode with custom droop strength (0.1-10.0)
- **Effect:**
  - The player receives a Power Shovel in their inventory.
  - A message confirms receipt and indicates the current mode.

## Power Shovel Modes

### Power Connector Mode
- **Usage:**
  1. **Right-click a fence** with the Power Shovel to place a transformer (custom head) on top of a mossy cobblestone wall next to the fence.
  2. **Right-click a wall** to place a meter (mossy cobblestone wall) with correct orientation.
  3. **Block Logging:** All block changes are logged with CoreProtect for rollback and auditing.
- **Cooldown:** There is a short cooldown (500ms) between uses to prevent accidental double clicks.
- **Block Face Restrictions:** Only clicks on the EAST, NORTH, SOUTH, or WEST faces are valid.

### Power Line Mode
- **Usage:**
  1. **Left-click a block** to set the starting point of the power line.
  2. **Right-click a block** to set the ending point and create a catenary power line between the two points.
  3. **WorldEdit Integration:** Power lines are created as WorldEdit operations, allowing them to be undone with `/undo`.
  4. The power line follows a catenary curve (similar to a real hanging cable) with customizable droop.
  5. **Air clicks:** Left or right-clicking in the air will clear your current selection.
- **Droop Strength:** Controls how much the power line sags:
  - Default: 2.4
  - Custom: Can be set with `/ps powerline <value>` (range: 0.1-10.0)
  - Lower values create more pronounced droop, higher values create flatter lines

