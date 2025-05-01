# Template Module

Manages templates and template brushes. Pretty much better schematic categorization and usage for common structures.

## Required Dependencies
- `WorldEdit`: This module requires WorldEdit to be installed on the server. It is used for the template brushes and schematic management.

## Commands
- `/tcreate`: Create a template.
  - Permission: `greenfieldcore.template.create`
  - Command Help:
    - `/tcreate <schematicFile> <templateName> [attributes...]`
      - `<schematicFile>`: The schematic file to use. This must be a valid existing WorldEdit schematic file.
      - `<templateName>`: The name of the template to create. This will be used to identify the template in the future. This must be unique.
      - `[attributes...]`: Optional. A list of attributes that can be used to categorize or tag the template for easier searching.
- `/tedit`: Edit an existing template.
  - Permission: `greenfieldcore.template.edit`
  - Command Help:
    - `/tedit <templateName> (schematic <newSchematicFile>)|(name <newTemplateName>)|(attributes (add <addedAttribute>)|(remove <removedAttribute>))`
      - `<templateName>`: The name of the template to edit. This must be an existing template.
      - `schematic <newSchematicFile>`: The new schematic file to use for the template. This must be a valid existing WorldEdit schematic file.
      - `name <newTemplateName>`: The new name of the template. This must be unique and not already in use.
      - `attributes`
        - `add <addedAttribute>`: The attribute to add to the template. Attribute must not exist on the current template.
        - `remove <removedAttribute>`: The attribute to remove from the template. Attribute must exist on the template.
- `/tdelete`: Delete a template.
  - Permission: `greenfieldcore.template.delete`
  - Command Help:
    - `/tdelete <templateName>`
      - `<templateName>`: The name of the template to delete. This must be an existing template.
- `/tlist`: List all templates.
  - Permission: `greenfieldcore.template.list`
  - Command Help:
    - `/tlist [filter...]`
      - `[filter...]`: Optional. A way to filter templates by attribute and name.
  - Command Flags:
    - `-page <pageNumber>`: The page number to display.
    - `-brush`: Hidden flag. This will show the template list in brush editing mode.
- `/tview`: View a template.
  - Permission: `greenfieldcore.template.view`
  - Command Help:
    - `/tview <templateName> [scale]`
      - `<templateName>`: The name of the template to view. This must be an existing template.
      - `[scale]`: Optional. Defaults to mini. Select whether the template should be displayed to you as a mini or a full scale render when viewed.
  - Command Flags:
    - `-force`: Force the template to render into view, even if the template has too many blocks to be rendered properly. 
      - Note: This requires the `greenfieldcore.template.view.force` permission.
- `/tcopy`: Copy a saved template to a WorldEdit clipboard.
  - Permission: `greenfieldcore.template.copy`
  - Command Help:
    - `/tcopy <templateName>`
      - `<templateName>`: The name of the template to copy. This must be an existing template.
- `/tbrush`: Create a new or edit an existing template brush.
  - Permission: `greenfieldcore.template.brush`
  - Command Help:
  - `/tbrush next|(add (template <templateName)|(option (flip <flipOption>)|(rotate <rotateOptions>)|(paste <pasteOptions))`