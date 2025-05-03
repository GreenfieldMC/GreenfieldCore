# Codes Module

Manages the server's build codes—rules and guidelines for building in Greenfield. Allows players to view, add, remove, and reload codes via commands.

---

## Commands
* `/codes`: List or manage build codes.
  - Permission: `greenfieldcore.codes`
  - Command Help:
    - `/codes`: Lists all build codes. Shows the first page of codes.
    - `/codes page <pageNumber>`: Shows a specific page of build codes.
    - `/codes add <newCode>`: Adds a new build code. Requires `greenfieldcore.codes.modify` permission.
      - `<newCode>`: The text of the new build code to add.
    - `/codes remove <codeNumber>`: Removes a build code by its number. Requires `greenfieldcore.codes.modify` permission.
      - `<codeNumber>`: The number of the code to remove (as shown in the list).
    - `/codes reload`: Reloads the codes from the configuration file. Requires `greenfieldcore.codes.modify` permission.

---

## Usage Examples
- `/codes` — View the first page of build codes.
- `/codes page 2` — View the second page of build codes.
- `/codes add Do not use glowstone under carpets on exteriors.` — Add a new code (with permission).
- `/codes remove 3` — Remove the third code (with permission).
- `/codes reload` — Reload the codes from disk (with permission).

---

## Notes
- Codes are displayed in a paginated list, 8 per page.
- Only users with `greenfieldcore.codes.modify` can add, remove, or reload codes.
- Codes are stored in a configuration file and persist across server restarts.
