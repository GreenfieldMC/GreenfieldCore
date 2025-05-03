# CommandStorageModule Documentation

## Overview
The CommandStorageModule allows players to save commands. Commands can be stored per-user or globally (server).

## Usage
- By default, commands are stored for the user. Use the `server` argument to store or manage server-wide commands (requires extra permissions).
- All destructive actions require confirmation, either by repeating the command with `-confirm` or by clicking the provided chat button.

## Commands
- `/scmd <description> <command>` — Save a command. Requires confirmation. Aliases: `/savecmd`, `/addcmd`, `/acmd`, `/sc`.
  - `/scmd server <description> <command>` — Save a server-wide command.
- `/rcmd <commandId>` — Remove a command. Requires confirmation. Aliases: `/remcmd`, `/delcmd`, `/dcmd`, `/rc`.
  - `/rcmd server <commandId>` — Remove a server-wide command.
- `/ecmd <commandId> description <newDescription>` — Edit a command's description. Requires confirmation. Aliases: `/editcmd`, `/modcmd`, `/mcmd`.
  - `/ecmd <commandId> command <newCommand>` — Edit a command's command string.
  - `/ecmd server <commandId> ...` — Edit a server-wide command.
- `/lcmd` — List your stored commands. Aliases: `/listcmd`, `/getcmd`, `/gcmd`, `/cmds`, `/lc`.
  - `/lcmd server` — List server-wide commands.
  - `-frequency` — Sort by most used.
  - `-page <n>` — View a specific page.
- `/fcmd <query>` — Search your commands by description. Aliases: `/findcmd`, `/searchcmd`, `/fc`.
  - `/fcmd -command <query>` — Search by command string.
  - `/fcmd server <query>` — Search server-wide commands.
  - `-page <n>` — View a specific page.
- `/wcmd <commandId>` — Run a stored command. (Internal/advanced usage)
  - `/wcmd server <commandId>` — Run a server-wide command.

## Permissions
- `greenfieldcore.commandstorage.add` — Add commands.
- `greenfieldcore.commandstorage.add.server` — Add server-wide commands.
- `greenfieldcore.commandstorage.remove` — Remove commands.
- `greenfieldcore.commandstorage.remove.server` — Remove server-wide commands.
- `greenfieldcore.commandstorage.edit` — Edit commands.
- `greenfieldcore.commandstorage.edit.server` — Edit server-wide commands.
- `greenfieldcore.commandstorage.list` — List commands.
- `greenfieldcore.commandstorage.list.server` — List server-wide commands.
- `greenfieldcore.commandstorage.search` — Search commands.
- `greenfieldcore.commandstorage.search.server` — Search server-wide commands.
- `greenfieldcore.commandstorage.run` — Run stored commands.
- `greenfieldcore.commandstorage.run.server` — Run server-wide commands.

## Examples
- Save a command: `/scmd "Say hello" "/say Hello, world!"`
- Remove a command: `/rcmd 2` (then confirm)
- Edit a command: `/ecmd 2 command "/say Goodbye!"` (then confirm)
- List commands: `/lcmd -frequency -page 2`
- Search commands: `/fcmd "hello" -page 1`
- Save a server command: `/scmd server "Restart server" "/restart"`