# Advanced Build Mode Module Documentation

The Advanced Build Mode (ABM/AVB) module provides powerful context-aware building tools. It enables a suite of custom block interaction handlers, streamlining complex building tasks.

## Permissions
- `greenfieldcore.advbuild` — Required to use advanced build mode and its commands.

## Commands

### `/advbuild`, `/avb`, `/abm`
**Description:** Main command for advanced build mode.
- **Permission:** `greenfieldcore.advbuild`
- **Usage:**
  - `/avb` — Toggle advanced build mode on/off for yourself.
  - `/avb help [page]` — List all available interaction handlers and their descriptions (paginated).
  - `/avb handler <handler>` — View detailed info and usage for a specific handler.
- **Notes:**
  - When enabled, your block interactions are processed by ABM handlers.
  - When disabled, you interact with blocks normally.

## Settings
- **Per-User Enablement:**
  - AVB is toggled per-player. Your enabled/disabled state is remembered between sessions.
- **Config File:**
  - The module stores enabled users and settings in `advancedbuildmode.yml`.

## General Usage
1. **Enable AVB:** Use `/avb` to toggle advanced build mode on.
2. **Handler Info:** Use `/avb help` to browse handlers, or `/avb handler <handler>` for specific instructions.
3. **Disable AVB:** Use `/avb` again to turn off advanced build mode.

## Integration
- **CoreProtect:** All block changes made by ABM are logged for rollback and auditing.

## Example Usage
- `/avb` — Enable or disable advanced build mode for yourself.
- `/avb help 2` — View the second page of available handlers.
- `/avb handler CommandBlockInteraction` — View details and usage for the command block handler.
