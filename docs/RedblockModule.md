# Redblock Module Documentation

The Redblock module provides a workflow to allow staff members to track incomplete work.

## Permissions
- `greenfieldcore.redblock.create` — Create new RedBlocks.
- `greenfieldcore.redblock.edit` — Edit incomplete RedBlocks.
- `greenfieldcore.redblock.delete` — Delete incomplete or pending RedBlocks.
- `greenfieldcore.redblock.approve` — Approve pending RedBlocks.
- `greenfieldcore.redblock.deny` — Deny pending RedBlocks.
- `greenfieldcore.redblock.complete` — Complete incomplete RedBlocks.
- `greenfieldcore.redblock.info` — View RedBlock information.
- `greenfieldcore.redblock.goto` — Teleport to RedBlocks.
- `greenfieldcore.redblock.list` — List and filter RedBlocks.

## Commands

### `/rbcreate`, `/rbc`
**Description:** Create a new RedBlock at your location.
- **Permission:** `greenfieldcore.redblock.create`
- **Usage:** `/rbcreate <description>`
- **Flags:**
  - `-assign <player>`: Assign to a player.
  - `-rank <rank>`: Set minimum rank required.

### `/rbedit`, `/rbe`
**Description:** Edit an incomplete RedBlock.
- **Permission:** `greenfieldcore.redblock.edit`
- **Usage:** `/rbedit <description>`
- **Flags:**
  - `-id <id>`: Target RedBlock by ID (otherwise nearest).
  - `-assign <player>`: Assign to a player.
  - `-unassign`: Unassign the RedBlock.
  - `-rank <rank>`: Set minimum rank.
  - `-unrank`: Remove minimum rank.

### `/rbdelete`, `/rbr`, `/rbremove`, `/rbrem`, `/rbdel`
**Description:** Delete an incomplete or pending RedBlock.
- **Permission:** `greenfieldcore.redblock.delete`
- **Flags:**
  - `-id <id>`: Target RedBlock by ID (otherwise nearest).

### `/rbapprove`, `/rba`
**Description:** Approve a pending RedBlock.
- **Permission:** `greenfieldcore.redblock.approve`
- **Flags:**
  - `-id <id>`: Target RedBlock by ID (otherwise nearest).

### `/rbdeny`, `/rbd`
**Description:** Deny a pending RedBlock (returns to incomplete).
- **Permission:** `greenfieldcore.redblock.deny`
- **Flags:**
  - `-id <id>`: Target RedBlock by ID (otherwise nearest).

### `/rbcomplete`, `/rbdone`
**Description:** Mark an incomplete RedBlock as completed (moves to pending).
- **Permission:** `greenfieldcore.redblock.complete`
- **Flags:**
  - `-id <id>`: Target RedBlock by ID (otherwise nearest).

### `/rbinfo`, `/rbi`
**Description:** View detailed information about a RedBlock.
- **Permission:** `greenfieldcore.redblock.info`
- **Flags:**
  - `-id <id>`: Target RedBlock by ID (otherwise nearest).

### `/rbtp`, `/rbgoto`
**Description:** Teleport to an incomplete or pending RedBlock.
- **Permission:** `greenfieldcore.redblock.goto`
- **Flags:**
  - `-id <id>`: Teleport to RedBlock by ID.
  - `-mine`: Teleport to the nearest incomplete RedBlock assigned to you.
  - `-radius <n>`: Restrict search radius.

### `/rblist`, `/rbl`, `/redblocks`
**Description:** List and filter RedBlocks with pagination and advanced filters.
- **Permission:** `greenfieldcore.redblock.list`
- **Flags:**
  - `-deleted`: Show deleted RedBlocks.
  - `-incomplete`: Show incomplete RedBlocks.
  - `-pending`: Show pending RedBlocks.
  - `-approved`: Show approved RedBlocks.
  - `-mine`: Show RedBlocks assigned to you.
  - `-assignedTo <player>`: Filter by assignee.
  - `-createdBy <player>`: Filter by creator.
  - `-completedBy <player>`: Filter by completer.
  - `-approvedBy <player>`: Filter by approver.
  - `-radius <n>`: Restrict search radius.
  - `-page <n>`: Page number.

## RedBlock Workflow
1. **Create:** Use `/rbcreate` to make a new RedBlock. Optionally assign or set a rank.
2. **Edit:** Use `/rbedit` to update description, assignment, or rank.
3. **Complete:** Use `/rbcomplete` when the task is done (moves to pending).
4. **Approve/Deny:** Use `/rbapprove` to finalize, or `/rbdeny` to return to incomplete.
5. **Delete:** Use `/rbdelete` to remove incomplete or pending RedBlocks.
6. **Info/Teleport/List:** Use `/rbinfo`, `/rbtp`, and `/rblist` to view, navigate, and filter RedBlocks.

## Example Usage
- `/rbcreate "Add streetlights to Main St" flags: -assign Alex`
- `/rbedit "Update description for RedBlock 42" flags: -id 42 -rank builder`
- `/rbdelete flags: -id 42`
- `/rbcomplete flags: -id 42`
- `/rbapprove flags: -id 42`
- `/rbdeny flags: -id 42`
- `/rbinfo flags: -id 42`
- `/rblist flags: -mine -incomplete -page 2`
- `/rblist flags: -assignedTo Alex -pending`
- `/rblist flags: -createdBy Jamie -approved -radius 1000`

## Notes
- All commands support tab completion and context-aware targeting (nearest RedBlock if no ID is given).
- RedBlocks are visualized in-game and on Dynmap, with status-based icons.
- RedBlock status: INCOMPLETE → PENDING → APPROVED/INCOMPLETE (if denied) → DELETED (if removed).

