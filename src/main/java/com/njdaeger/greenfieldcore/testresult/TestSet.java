package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.greenfieldcore.Util;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public class TestSet implements PageItem<ICommandContext> {

    private final List<TestAttempt> attempts;
    private final UUID uuid;

    public TestSet(UUID userBeingTested, List<TestAttempt> attempts) {
        this.attempts = attempts;
        this.uuid = userBeingTested;
    }

    public List<TestAttempt> getAttempts() {
        return attempts;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var playerName = Util.resolvePlayerName(uuid);
        var attemptCount = attempts.size();
        return "* " + playerName + " - Attempts: " + attemptCount;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var playerName = Util.resolvePlayerName(uuid);
        var attemptCount = attempts.size();
        var hasSuccessfulTest = attempts.stream().anyMatch(att -> att.isSuccessful() && att.isComplete());
        var whitelistedPlayer = Bukkit.getWhitelistedPlayers().stream()
                .filter(player -> player.getUniqueId().equals(uuid))
                .findFirst().orElse(null);


        var isWhitelisted = whitelistedPlayer != null;
        var isBanned = whitelistedPlayer != null && Bukkit.getBanList(BanListType.PROFILE).isBanned(whitelistedPlayer.getPlayerProfile());

        String status;
        if (isBanned) status = "Banned";
        else if (isWhitelisted) {
            if (hasSuccessfulTest) status = "Member";
            else if (attemptCount > 0) status = "Pending";
            else status = "Non-Member";
        } else {
            if (hasSuccessfulTest) status = "Ex-Member";
            else if (attemptCount > 0) status = "Failed Test";
            else status = "Non-Member";
        }

        TextComponent viewButton;
        if (!playerName.equalsIgnoreCase("!!Unknown!!")) {
            viewButton = Component.text("«", NamedTextColor.BLUE, TextDecoration.BOLD).toBuilder()
                    .hoverEvent(HoverEvent.showText(Component.text("View attempts", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/attempts user " + playerName)).build();
        } else {
            viewButton = Component.text("«", paginator.getGrayedOutColor(), TextDecoration.BOLD).toBuilder()
                    .hoverEvent(HoverEvent.showText(Component.text("User not found, no attempts can be viewed.", NamedTextColor.GRAY))).build();
        }

        var text = Component.text();

        text.append(viewButton);
        text.appendSpace();
        if (playerName.equalsIgnoreCase("!!Unknown!!"))
            text.append(Component.text(playerName, paginator.getGrayedOutColor()).hoverEvent(HoverEvent.showText(Component.text(uuid.toString(), paginator.getGrayColor()))));
        else text.append(Component.text(playerName, paginator.getHighlightColor()));

        text.append(Component.text(": " + attemptCount + " Attempts - Current Status: ", paginator.getGrayColor()))
                .append(Component.text(status, paginator.getHighlightColor()));

        return text.build();
    }
}
