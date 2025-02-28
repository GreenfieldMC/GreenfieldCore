package com.njdaeger.greenfieldcore.redblock;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.njdaeger.greenfieldcore.GreenfieldCore;
import com.njdaeger.greenfieldcore.redblock.arguments.RedblockArgument;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.command.brigadier.builder.CommandBuilder;
import com.njdaeger.pdk.command.brigadier.builder.PdkArgumentTypes;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import static com.njdaeger.greenfieldcore.ComponentUtils.moduleMessage;

public class RedblockBrigadierCommands {

    private final RedblockStorage storage;

    public RedblockBrigadierCommands(RedblockStorage storage, GreenfieldCore plugin) {
        this.storage = storage;

        CommandBuilder.of("2rbcreate", "2rbc")
                .description("Create a new redblock")
                .permission("greenfieldcore.redblock.create")
                .then("description", PdkArgumentTypes.quotedString(false, () -> "Enter a description for the redblock"))
                .executes(this::createRedblock)
                .flag("assign", "Assign this redblock to a specific player", PdkArgumentTypes.player())
                .flag("rank", "Assign this redblock to a specific rank",  StringArgumentType.word())
                .register(plugin);

        CommandBuilder.of("2rbapprove", "rba")
                .description("Approve a pending redblock")
                .permission("greenfieldcore.redblock.approve")
                .then("id", new RedblockArgument(storage, rb -> rb.getStatus() == Redblock.Status.PENDING))
                .executes(this::approveRedblock)
                .register(plugin);

    }

    private void createRedblock(ICommandContext ctx) throws PDKCommandException {
        var assginTo = ctx.<Player>getFlag("assign");
        var rank = ctx.<String>getFlag("rank");
        String description = ctx.getTyped("description", String.class);
        var rb = storage.createRedblock(description, ctx.asPlayer(), ctx.getLocation().getBlock().getLocation(), assginTo.getUniqueId(), rank);
        ctx.send(moduleMessage("Redblock").append(Component.text("Redblock created with id " + rb.getId(), NamedTextColor.GRAY)));
    }

    private void approveRedblock(ICommandContext ctx) throws PDKCommandException {
        var rb = storage.getRedblock(ctx.getTyped("id", Integer.class));
        if (rb == null) {
            ctx.error("No redblock with that id was found.");
            return;
        }
        rb.setStatus(Redblock.Status.APPROVED);
        ctx.send(moduleMessage("Redblock").append(Component.text("Redblock " + rb.getId() + " approved.", NamedTextColor.GRAY)));
    }

}
