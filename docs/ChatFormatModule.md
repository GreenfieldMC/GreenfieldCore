# ChatFormatModule Documentation

## Overview
The ChatFormatModule provides advanced chat formatting and user experience features. It adds color codes, clickable links, user pings, unit conversions, and customizable notification sounds for mentions.

## Features
- **Color Codes:** Supports Minecraft color codes (e.g., `&a`, `&b`, etc.) and hex color codes (e.g., `&#FF00FF`) for chat messages.
- **Clickable Links:** Automatically detects URLs and Markdown-style links, making them clickable in chat.
- **User Mentions (Pings):** Mention a player using `@username` to highlight their name and optionally play a notification sound for them.
- **Unit Conversions:** Recognizes and formats unit conversions in chat (e.g., `10km`), displaying hoverable conversion info.
- **Customizable Mention Settings:** Players can enable/disable mention notifications, set their ping sound, and adjust the volume.
- **EssentialsChat Integration:** If EssentialsChat is present, the module overrides its chat handler for seamless integration.

## Usage
- **Color Codes:** Use `&` followed by a color/formatting code (e.g., `&aHello`, `&lBold`). Hex codes are supported as `&#RRGGBB`.
- **Links:** Paste a URL (e.g., `https://example.com`) or use Markdown `[text](url)` to create clickable links.
- **Mentions:** Type `@username` in chat to mention a player. If they have mentions enabled, they will receive a sound notification.
- **Unit Conversions:** Type values with units (e.g., `5km`) to show conversions on hover.

## Commands
- `/togglepings` or `/togglementions` — Toggle mention notifications for yourself.
- `/togglepings <player>` — Toggle mention notifications for another player (requires permission, see Permissions below).
- `/pings volume <value>` — Set your ping sound volume (0.1 to 2.0).
- `/pings sound <sound>` — Set your ping sound (choose a valid Minecraft sound).
- `/pings sound reset` — Reset your ping sound to default.

## Permissions
- `greenfieldcore.chat.mention` — Allows a player to mention others and receive mention notifications.
- `greenfieldcore.chat.toggle-mentions` — Allows toggling your own mention notifications.
- `greenfieldcore.chat.toggle-mentions.others` — Allows toggling mention notifications for other players.

## Integration
- If EssentialsChat is installed, ChatFormatModule will override its chat handler. Since essentials does its own chat formatting, we want to completely overwrite it
- If EssentialsChat is not present, the module uses its own default chat handler.
- User mention settings are stored and loaded automatically.