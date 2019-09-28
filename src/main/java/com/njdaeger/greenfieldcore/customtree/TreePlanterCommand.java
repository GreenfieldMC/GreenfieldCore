package com.njdaeger.greenfieldcore.customtree;

import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.customtree.tools.ClassicPalmTool;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.item.ItemType;
import org.bukkit.ChatColor;

public class TreePlanterCommand {

    public TreePlanterCommand(GreenfieldCore plugin) {
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
        context.send(ChatColor.LIGHT_PURPLE + "Tree tool bound to " + type.getName());
    }

    private void gTreeCompletion(TabContext context) {
        context.completionAt(0, "palm");
    }

}
