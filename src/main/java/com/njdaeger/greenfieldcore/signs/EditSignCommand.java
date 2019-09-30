package com.njdaeger.greenfieldcore.signs;

import com.njdaeger.bci.SenderType;
import com.njdaeger.bci.base.BCIException;
import com.njdaeger.bci.defaults.BCIBuilder;
import com.njdaeger.bci.defaults.CommandContext;
import com.njdaeger.bci.defaults.TabContext;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;

public class EditSignCommand {

    public EditSignCommand(GreenfieldCore plugin) {
        plugin.registerCommand(BCIBuilder.create("editsign")
                .executor(this::editSign)
                .completer(this::completer)
                .senders(SenderType.PLAYER)
                .permissions("greenfieldcore.signeditor.command")
                .usage("/editsign <line> [text]")
                .description("Edit a currently placed sign")
                .minArgs(1)
                .aliases("edit")
                .build());
    }

    private void editSign(CommandContext context) throws BCIException {
        Player player = context.asPlayer();
        RayTraceResult trace = player.rayTraceBlocks(5);
        if (trace == null) throw new BCIException(ChatColor.RED + "You are not looking at a block. (Are you too far away?)");
        Block block = trace.getHitBlock();
        if (block == null) throw new BCIException(ChatColor.RED + "You are not looking at a sign block. (Are you too far away?)");
        if (block.getState() instanceof Sign) {
            int line = context.integerAt(0);
            Sign sign = (Sign) block.getState();
            String replacement = context.joinArgs(1).replaceAll("(?<!\\\\)&(?=[a-fA-F0-9K-Ok-oRr])", ChatColor.COLOR_CHAR + "");
            sign.setLine(line - 1, replacement.replaceAll("\\\\&", "&"));
            sign.update(true, false);
        } else throw new BCIException(ChatColor.RED + "You are not looking at a sign block.");
    }

    private void completer(TabContext context) throws BCIException {

        //We check to see if the player has a sign in their line of sight up to 5 blocks away
        Player player = context.asPlayer();
        RayTraceResult trace = player.rayTraceBlocks(5);
        if (trace == null) return;
        Block block = trace.getHitBlock();

        //We check if its a sign and if the block exists.
        if (block != null && block.getState() instanceof Sign) {

            //Getting the sign state. this should be safe for 1.14 update
            Sign sign = (Sign) block.getState();
            //We complete all the lines at the first index.
            context.completionAt(0, "1", "2", "3", "4");

            //We need to make sure the length of the current argument list is greater than 1.
            if (context.getLength() > 1) {

                //Get the line provided in the first argument
                int line = context.getCommandContext().integerAt(0);

                //OK bucko, no trying to do weird line numbers
                if (line < 1 || line > 4) {
                    context.send(ChatColor.RED.toString() + line + " is not a valid line number. (1 - 4 only)");
                    return;
                }

                //We get the words on the line wanting to be edited.
                String[] words = sign.getLine(line - 1).split(" "); //Words on sign with color codes.
                List<String> lineWords = new ArrayList<>();

                //We need to make sure all escaped color chars are still escaped in the completions and that all color chars are changed to &'s
                for (String word : words) {
                    lineWords.add(word
                            .replaceAll("&(?=[a-fA-F0-9K-Ok-oRr])", "\\\\&")
                            .replaceAll(ChatColor.COLOR_CHAR + "", "&"));
                }

                //We remove all the overlapping words.
                List<String> used = new ArrayList<>(context.getArgs().subList(1, context.getLength())); // Currently used words
                for (String word : used) {
                    lineWords.remove(word);
                }
                context.completion(lineWords.toArray(new String[0]));
            }
        }
    }
}
