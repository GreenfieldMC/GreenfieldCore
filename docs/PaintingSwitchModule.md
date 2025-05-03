# Painting Switch Module Documentation

The Painting Switch module allows players to easily cycle through different painting variants in-game by interacting with paintings and using their hotbar scroll wheel.

## Permissions
- `greenfieldcore.paintingswitch.use` — Required to use the painting switch feature and its command.

## Commands

### `/pstoggle`, `/paintingswitch`
**Description:** Toggle the painting switch feature on or off for yourself.
- **Permission:** `greenfieldcore.paintingswitch.use`
- **Usage:** `/pstoggle`
- **Effect:**
  - When enabled, you can interact with paintings to change their art.
  - When disabled, the painting switch feature is turned off for you.

## How to Use the Painting Switch Feature
1. **Enable the Feature:**
   - Use `/pstoggle` to enable painting switching if it is not already enabled.
2. **Interact with a Painting:**
   - Right-click a painting (with main hand) to enter painting switch mode. You will receive a message: "Scroll to select painting."
3. **Scroll to Change Art:**
   - Use your hotbar scroll wheel to cycle through available painting variants. The painting will update in real time.
4. **Lock Your Selection:**
   - Right-click the painting again to lock in your selection. You will receive a message: "Painting locked."
   - Moving away from the painting or breaking it will also lock your selection.
5. **Future Painting Placements:**
   - Once you have placed a painting, you will not get random paintings like normal Minecraft does. It will *almost* always place the last painting you placed before, unless it physically cannot fit in the space provided. So, if you place a 1x1 painting, that 1x1 painting will be repeatedly placed until you manually change it with the scroll wheel, or if you disable the painting switch.

## Messages
- **Enabled/Disabled:** You will receive a message when you enable or disable the feature.
- **Painting Locked:** Informs you when your selection is locked.
- **Scroll to Select Painting:** Instructs you to use the scroll wheel to change the painting.
- **Painting Removed:** Informs you if the painting is removed while switching.