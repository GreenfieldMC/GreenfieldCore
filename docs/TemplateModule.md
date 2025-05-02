# Template Module

Manages templates and template brushes. Pretty much better schematic categorization and usage for common structures.

---

## Required Dependencies
- `WorldEdit`: This module requires WorldEdit to be installed on the server. It is used for the template brushes and schematic management.

---

## Commands
* `/tcreate`: Create a template.
  - Permission: `greenfieldcore.template.create`
  - Command Help:
    - `/tcreate <schematicFile> <templateName> [attributes...]`
      - `<schematicFile>`: The schematic file to use. This must be a valid existing WorldEdit schematic file.
      - `<templateName>`: The name of the template to create. This will be used to identify the template in the future. This must be unique.
      - `[attributes...]`: Optional. A list of attributes that can be used to categorize or tag the template for easier searching.
* `/tedit`: Edit an existing template.
  - Permission: `greenfieldcore.template.edit`
  - Command Help:
    - `/tedit <templateName> name <newTemplateName>`: Changes the name of the template.
    - `/tedit <templateName> schematic <newSchematicFile>`: Changes the schematic file used by the template.
    - `/tedit <templateName> attributes add <addedAttribute>`: Adds an attribute to the template. Attribute must not already exist on the template.
    - `/tedit <templateName> attributes remove <removedAttribute>`: Removes an attribute from the template. Attribute must already exist on the template.
      - `<templateName>`: The name of the template to edit. This must be an existing template.
      - `<newTemplateName>`: The new name for the template. Must be unique and not already in use.
      - `<newSchematicFile>`: The new schematic file to use. Must be a valid existing WorldEdit schematic file.
      - `<addedAttribute>`: The attribute to add.
      - `<removedAttribute>`: The attribute to remove.
* `/tdelete`: Delete a template.
  - Permission: `greenfieldcore.template.delete`
  - Command Help:
    - `/tdelete <templateName>`
      - `<templateName>`: The name of the template to delete. This must be an existing template.
* `/tlist`: List all templates.
  - Permission: `greenfieldcore.template.list`
  - Command Help:
    - `/tlist [filter...]`
      - `[filter...]`: Optional. A way to filter templates by attribute and name.
  - Command Flags:
    - `-page <pageNumber>`: The page number to display.
    - `-brush`: Hidden flag. This will show the template list in brush editing mode.
* `/tview`: View a template.
  - Permission: `greenfieldcore.template.view`
  - Command Help:
    - `/tview <templateName> [scale]`
      - `<templateName>`: The name of the template to view. This must be an existing template.
      - `[scale]`: Optional. Defaults to mini. Select whether the template should be displayed to you as a mini or a full scale render when viewed.
  - Command Flags:
    - `-force`: Force the template to render into view, even if the template has too many blocks to be rendered properly. 
      - Note: This requires the `greenfieldcore.template.view.force` permission.
* `/tcopy`: Copy a saved template to a WorldEdit clipboard.
  - Permission: `greenfieldcore.template.copy`
  - Command Help:
    - `/tcopy <templateName>`
      - `<templateName>`: The name of the template to copy. This must be an existing template.
* `/tbrush`: Create a new or edit an existing template brush.
  - Permission: `greenfieldcore.template.brush`
  - Command Help:
    - `/tbrush`: Opens the brush editor for your template brush.
    - `/tbrush next`: Randomizes the next template in your brush.
    - `/tbrush add template <templateName>`: Adds a template to your brush.
    - `/tbrush add option (flip <flipOption>|rotate <rotateOption>|paste <pasteOption>)`: Adds an option to your brush. Options:
      - `flip <flipOption>`: Adds a flip option (e.g., `NONE`, `X`, `Y`, `Z`).
      - `rotate <rotateOption>`: Adds a rotation option (e.g., `NONE`, `CLOCKWISE_90`, `CLOCKWISE_180`, `CLOCKWISE_270`).
      - `paste <pasteOption>`: Adds a paste option (e.g., `NONE`, `NO_AIR`, `NO_ENTITIES`).
    - `/tbrush remove template <templateName>`: Removes a template from your brush.
    - `/tbrush remove option (flip <flipOption>|rotate <rotateOption>|paste <pasteOption>)`: Removes an option from your brush.
  - Command Flags:
    - `-page <pageNumber>`: The page number to display in the brush editor.
    - `-brush`: Hidden flag. Used internally for brush editing mode.

---

## Example Usage
1. Creating a template
   * Create a schematic using WorldEdit and save it as `my_template` (eg `/schematic save my_template`)
     > Notes:
     > 
     > When copying the structure before saving as a schematic, check WHERE you are copying the structure from relative to the structure itself.
     > 
     > This copy location is critical to being able to use this template effectively with a template brush. For example, if you have a tree you want to save as a template, you should copy the tree from **one block BELOW** the lowest block on the primary tree trunk. This way, when the tree template is used as a template in a template brush, and you click on the ground to paste a tree, it will not place the tree one block underground.
     > 
     > The pasting location of the template brush is literal, meaning whatever block you right click on will be exactly where the brush pastes the template. Hence, in the case of the tree, we explicitly copy a block BELOW the base of the tree to account for this offset introduced by the brush.
   * Create a template pointing towards the newly created schematic. `/tcreate my_template my_template_name some descriptive things and words`
     - Breaking down the command:
       - `my_template` is the schematic file name this template references.
       - `my_template_name` is the name of the template to be uased throughout the plugin and server. The name must be unique.
       - `some descriptive things and words` are the attributes that can be used to categorize or tag the template for easier searching. Each word is considered a separate attribute; in the example above, `some`, `descriptive`, `things`, and `words` are all separate attributes. Normally, you would put things like `tree` or `pink` or `flowery` etc.
   * The template is now created!
2. Editing a template
   * Edit the template using `/tedit my_template_name name my_new_template_name`
     - This will change the name of the template to `my_new_template_name`.
     - Any brushes currently using this template will not be affected. 
   * Edit the template using `/tedit my_template_name schematic my_new_schematic_file`
     - This will change the schematic file used by the template to `my_new_schematic_file`.
     - Any brushes currently using this template will use the new schematic file.
   * Edit the template using `/tedit my_template_name attributes add new_attribute`
     - This will add `new_attribute` to the list of attributes for the template.
   * Edit the template using `/tedit my_template_name attributes remove old_attribute`
     - This will remove `old_attribute` from the list of attributes for the template. 
3. Viewing a template
   * `/tview my_template_name`
     - This will show you a mini render of the template. The limit for this command is 100k blocks. If the template is larger than that by volume, it will refuse to render.
   * `/tview my_template_name -force`
     - This will bypass the 100k block limit and force the template to render. This is not recommended, as it can cause performance issues with the client.
     - This requires the `greenfieldcore.template.view.force` permission.
   * `/tview my_template_name full`
     - This will force the template to render in full scale. No performance impact occurs by doing this - the mini display is just easier to digest, hence it's automatically defaulting to the mini. 
   > Note:
   > 
   > For all viewed templates, the origin of the copy is shown as a red wool block. 
4. Creating a template brush
   * Run the `/tbrush` command with no arguments. A chat prompt will show up displaying all the available templates for use.
     - The options for each template are: `[V C S]` (if the template hasn't been selected for the current brush yet and is capable of being added.) or `[V C X]` (if the template HAS been selected for the current brush and is capable of being removed.)
     - `V` = To view the template.
     - `C` = To copy the template to your worldedit clipboard.
     - `S` = To select the template for your brush.
     - `X` = To remove the template from your brush.
   * Select any templates you wish to add to your brush.
   * Select any options you want associated with your brush.
     - Available rotation options: `[S 0 90 180 270]`
       - `S` = Rotate according to the player's facing direction. This option is referred to as the "Self" option.
       - `0` = May paste the templates with the 0 rotation.
       - `90` = May paste the templates with a 90-degree clockwise rotation.
       - `180` = May paste the templates with a 180-degree clockwise rotation.
       - `270` = May paste the templates with a 270-degree clockwise rotation.
       > Notes:
       > 
       > The S rotation is mutually exclusive with the other rotation options.
       >
       > Any mix of the other rotations may be selected. For example, when 0, 90, and 270 are selected, the brush will randomly select one of those three rotation options when pasting in the templates.
       > 
       > If no rotation options are selected, the brush will just paste the schematic as it was saved with no variation to rotation.
     - Available flip options: `[-x x -z z]`
       - `-x` = Flips the template along the negative X axis.
       - `x` = Flips the template along the X axis.
       - `-z` = Flips the template along the negative Z axis.
       - `z` = Flips the template along the Z axis.
     - Available paste options: `[-e -b -a]`
       - `-e` = Pastes the template with entities saved in the schematic.
       - `-b` = Pastes the template with the biomes saved in the schematic.
       - `-a` = Pastes the template and skips all air blocks.