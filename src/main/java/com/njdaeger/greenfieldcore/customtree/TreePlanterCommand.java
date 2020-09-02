package com.njdaeger.greenfieldcore.customtree;

public class TreePlanterCommand {

    /*public TreePlanterCommand(GreenfieldCore plugin) {
        plugin.registerCommand(BCIBuilder.create("/tree")
                .executor(this::gTree)
                .completer(this::gTreeCompletion)
                .minArgs(1)
                .maxArgs(1)
                .permissions("worldedit.tool.tree")
                .description("Creates a custom tree")
                .usage("//tree <type>")
                .build());
    }

    private void gTree(CommandContext context) throws BCIException {
        LocalSession session = TreePlanterModule.getWorldEdit().getSession(context.asPlayer());
        ItemType type = BukkitAdapter.adapt(context.asPlayer()).getItemInHand(HandSide.MAIN_HAND).getType();
        try {
            if (context.argAt(0).equalsIgnoreCase("palm")) session.setTool(type, new ClassicPalmTool());
            else throw new BCIException(ChatColor.RED + "Invalid tree type");
        } catch (InvalidToolBindException e) {
            throw new BCIException(ChatColor.RED + "Blocks can't be used.");
        }
        context.send(ChatColor.LIGHT_PURPLE + "Tree tool bound to " + type.getId());
    }

    private void gTreeCompletion(TabContext context) {
        context.completionAt(0, "palm");
    }*/

}
