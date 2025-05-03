# Hotspot Module Documentation

The Hotspot module provides commands for managing, teleporting to, and organizing hotspots. Hotspots are locations of interest, and categories help organize them.

## Permissions
- `greenfieldcore.hotspot.goto` — Allows teleporting to hotspots.
- `greenfieldcore.hotspot.create` — Allows creating hotspots and categories.
- `greenfieldcore.hotspot.delete` — Allows deleting hotspots and categories.
- `greenfieldcore.hotspot.edit` — Allows editing hotspots and categories.
- `greenfieldcore.hotspot.list` — Allows listing hotspots and categories.

## Commands

### `/hsgoto`, `/hstp`, `/hotspot`, `/hs`
**Description:** Teleport to a hotspot by name or ID.
- **Permission:** `greenfieldcore.hotspot.goto`
- **Usage:**
  - `/hsgoto <hotspotName>`
  - `/hsgoto byId <hotspotId>`
- **Notes:**
  - If multiple hotspots match by name, a paginated list is shown for selection.

### `/hscreate`, `/hsc`
**Description:** Create a new hotspot or hotspot category.
- **Permission:** `greenfieldcore.hotspot.create`
- **Usage:**
  - `/hscreate hotspot <hotspotName> <categoryId> [customIcon]`
  - `/hscreate category <categoryName> <categoryId> [categoryIcon]`
- **Arguments:**
  - `hotspotName`: Name of the new hotspot (quoted if it contains spaces).
  - `categoryId`: ID of the category to assign or create.
  - `customIcon`/`categoryIcon`: (Optional) Marker icon name.

### `/hsdelete`, `/hsd`, `/hsdel`
**Description:** Delete a hotspot or hotspot category.
- **Permission:** `greenfieldcore.hotspot.delete`
- **Usage:**
  - `/hsdelete hotspot <hotspotName>`
  - `/hsdelete hotspot byId <hotspotId>`
  - `/hsdelete category <categoryId> [replacementCategoryId]`
- **Notes:**
  - Deleting a category with assigned hotspots requires a replacement category.
  - If multiple hotspots match, a paginated list is shown for selection.

### `/hsedit`, `/hse`
**Description:** Edit a hotspot or hotspot category.
- **Permission:** `greenfieldcore.hotspot.edit`
- **Usage:**
  - `/hsedit hotspot <hotspotName> category <categoryId>`
  - `/hsedit hotspot <hotspotName> icon [iconName]`
  - `/hsedit hotspot <hotspotName> name <newName>`
  - `/hsedit hotspot byId <hotspotId> ...`
  - `/hsedit category <categoryId> icon [iconName]`
  - `/hsedit category <categoryId> name <newName>`
- **Notes:**
  - If multiple hotspots match, a paginated list is shown for selection.

### `/hslist`, `/hsl`, `/hotspots`
**Description:** List hotspots or hotspot categories, with pagination and modes for editing/deleting.
- **Permission:** `greenfieldcore.hotspot.list`
- **Usage:**
  - `/hslist [hotspots] [categoryId]`
  - `/hslist categories`
- **Flags:**
  - `-page <n>`: Show page number n.
  - `-deleteMode`: Show deletable entries (used internally).
  - `-editMode`: Show editable entries (used internally).

## Hotspot and Category Arguments
- **Hotspot Name:** Quoted string, case-insensitive, tab-completes existing hotspots.
- **Hotspot ID:** Integer, tab-completes existing IDs.
- **Category ID:** String, tab-completes existing categories.
- **Icon Name:** String, tab-completes available marker icons.

## Example Usage
- `/hscreate hotspot "Central Park" parks`
- `/hsedit hotspot "Central Park" icon tree`
- `/hsdelete category parks replacementCategoryId recreation`
- `/hslist hotspots parks flags: -page 2`
