package net.greenfieldmc.core.hotspots;

import net.greenfieldmc.core.hotspots.paginators.CategoryPaginator;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Category implements PageItem<CategoryPaginator.CategoryPaginatorMode> {

    private boolean hasChanged = false;

    private String name;
    private String marker;
    private final String id;

    public Category(String name, String marker, String id) {
        this.name = name;
        this.marker = marker;
        this.id = id;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setChanged(boolean changed) {
        this.hasChanged = changed;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
        hasChanged = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        hasChanged = true;
    }

    public String getId() {
        return id;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, CategoryPaginator.CategoryPaginatorMode> paginator, CategoryPaginator.CategoryPaginatorMode mode) {

        var questionMark = getQuestionMark();

        var line = Component.text();
        line.append(questionMark);
        line.resetStyle().appendSpace();

        switch (mode) {
            case EDIT -> line.append(Component.text("[E]", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.suggestCommand("/hsedit category " + id + " "))
                    .hoverEvent(Component.text("Click to begin editing this Hotspot Category", NamedTextColor.GRAY)));
            case DELETE -> line.append(Component.text("[X]", NamedTextColor.RED, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/hsdelete category " + id))
                    .hoverEvent(Component.text("Click to delete this Hotspot Category", NamedTextColor.GRAY)));
        }

        line.resetStyle().appendSpace();
        line.append(Component.text("- " + getName(), NamedTextColor.GRAY));
        return line.build();
    }

    private TextComponent getQuestionMark() {
        var questionMark = Component.text("?", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD).toBuilder();
        var hoverText = Component.text();
        hoverText.append(Component.text("DisplayName: ", NamedTextColor.GRAY).append(Component.text(getName(), NamedTextColor.BLUE)));
        hoverText.appendNewline();
        hoverText.append(Component.text("Marker: ", NamedTextColor.GRAY).append(Component.text(getMarker() == null ? "None" : getMarker(), NamedTextColor.BLUE)));
        hoverText.appendNewline();
        hoverText.append(Component.text("ID: ", NamedTextColor.GRAY).append(Component.text(getId(), NamedTextColor.BLUE)));
        questionMark.hoverEvent(HoverEvent.showText(hoverText));
        return questionMark.build();
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, CategoryPaginator.CategoryPaginatorMode> paginator, CategoryPaginator.CategoryPaginatorMode generatorInfo) {
        return getName();
    }
}
