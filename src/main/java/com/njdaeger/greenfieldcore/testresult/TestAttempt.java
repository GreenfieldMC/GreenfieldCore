package com.njdaeger.greenfieldcore.testresult;

import com.njdaeger.greenfieldcore.Util;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.UUID;

public class TestAttempt implements PageItem<ICommandContext> {

    private boolean hasChanged = false;

    private final int attemptNumber;
    private final long attemptStart;
    private final UUID startedBy;
    private long attemptEnd;
    private UUID finishedBy;
    private String attemptNotes;
    private boolean successful;

    public TestAttempt(int attemptNumber, long attemptStart, UUID startedBy, long attemptEnd, UUID finishedBy, String attemptNotes, boolean successful) {
        this.attemptNumber = attemptNumber;
        this.attemptStart = attemptStart;
        this.startedBy = startedBy;
        this.attemptEnd = attemptEnd;
        this.finishedBy = finishedBy;
        this.attemptNotes = attemptNotes;
        this.successful = successful;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public long getAttemptStart() {
        return attemptStart;
    }

    public UUID getStartedBy() {
        return startedBy;
    }

    public long getAttemptEnd() {
        return attemptEnd;
    }

    public void setAttemptEnd(long attemptEnd) {
        this.attemptEnd = attemptEnd;
        hasChanged = true;
    }

    public UUID getFinishedBy() {
        return finishedBy;
    }

    public void setFinishedBy(UUID finishedBy) {
        this.finishedBy = finishedBy;
        hasChanged = true;
    }

    public String getAttemptNotes() {
        return attemptNotes;
    }

    public void setAttemptNotes(String attemptNotes) {
        this.attemptNotes = attemptNotes;
        hasChanged = true;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
        hasChanged = true;
    }

    public boolean isComplete() {
        return attemptEnd != 0;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        return "Attempt #" + attemptNumber + " - Successful: " + successful;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, ICommandContext> paginator, ICommandContext generatorInfo) {
        var hover = Component.text();
        hover.append(Component.text("Started by: ", paginator.getGrayColor())).append(Component.text(Util.resolvePlayerName(startedBy), NamedTextColor.BLUE));
        hover.appendNewline();
        hover.append(Component.text("Started at: ", paginator.getGrayColor())).append(Component.text(Util.formatDate(attemptStart), NamedTextColor.BLUE));
        if (isComplete()) {
            hover.appendNewline();
            hover.append(Component.text("Finished by: ", paginator.getGrayColor())).append(Component.text(Util.resolvePlayerName(finishedBy), NamedTextColor.BLUE));
            hover.appendNewline();
            hover.append(Component.text("Finished at: ", paginator.getGrayColor())).append(Component.text(Util.formatDate(attemptEnd), NamedTextColor.BLUE));
        }

        var text = Component.text();
        text.append(Component.text("?", paginator.getHighlightColor(), TextDecoration.BOLD).hoverEvent(HoverEvent.showText(hover.build())));
        text.appendSpace();
        text.append(Component.text("Attempt #" + attemptNumber, paginator.getGrayColor()));
        text.appendSpace();
        text.append(Component.text(successful ? "Passed" : "Failed", successful ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD));
        text.appendSpace();
        text.append(Component.text(" - " + attemptNotes, paginator.getGrayColor()));
        return text.build();
    }
}
