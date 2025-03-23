package com.njdaeger.greenfieldcore.hotspots;

import com.njdaeger.greenfieldcore.hotspots.paginators.HotspotPaginator;
import com.njdaeger.pdk.command.brigadier.ICommandContext;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.PageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.World;

public class Hotspot implements PageItem<Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext>> {

    private boolean hasChanged = false;

    private String name;
    private String category;
    private final int id;
    private int x;
    private int y;
    private int z;
    private float yaw;
    private float pitch;
    private World world;
    private String customMarker;

    public Hotspot(String name, String category, int id, int x, int y, int z, float yaw, float pitch, World world, String customMarker) {
        this.name = name;
        this.category = category;
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.customMarker = customMarker;
    }

    public Hotspot(String name, String category, int id, int x, int y, int z, float yaw, float pitch, World world) {
        this(name, category, id, x, y, z, yaw, pitch, world, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        hasChanged = true;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        hasChanged = true;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.world = location.getWorld();
        hasChanged = true;
    }

    public String getCustomMarker() {
        return customMarker;
    }

    public void setCustomMarker(String customMarker) {
        this.customMarker = customMarker;
        hasChanged = true;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public TextComponent getItemText(ChatPaginator<?, Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext>> paginator, Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext> generatorInfo) {
        var mode = generatorInfo.getFirst();
        var ctx = generatorInfo.getSecond();
        var location = ctx.getLocationOrNull();

        var questionMark = getQuestionMark(paginator, location);

        var line = Component.text();
        line.append(questionMark);
        line.resetStyle().appendSpace();

        switch (mode) {
            case LIST -> line.append(Component.text("[T]", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/hstp byId " + id))
                    .hoverEvent(Component.text("Click to teleport to this Hotspot", NamedTextColor.GRAY)));
            case EDIT -> line.append(Component.text("[E]", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.suggestCommand("/hsedit hotspot byId " + id + " "))
                    .hoverEvent(Component.text("Click to begin editing this Hotspot", NamedTextColor.GRAY)));
            case DELETE -> line.append(Component.text("[X]", NamedTextColor.RED, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/hsdelete hotspot byId " + id))
                    .hoverEvent(Component.text("Click to delete this Hotspot", NamedTextColor.GRAY)));
        }

        line.resetStyle().appendSpace();
        line.append(Component.text("- " + getName(), paginator.getGrayColor()));
        return line.build();
    }

    private TextComponent getQuestionMark(ChatPaginator<?,?> paginator, Location distanceFrom) {
        var questionMark = Component.text("?", paginator.getHighlightColor(), TextDecoration.BOLD).toBuilder();
        var hoverText = Component.text();
        hoverText.append(Component.text("Location: ", NamedTextColor.GRAY).append(Component.text(getLocation().getWorld().getName() + ", " + getLocation().getBlockX() + ", " + getLocation().getBlockY() + ", " + getLocation().getBlockZ(), NamedTextColor.BLUE)));
        hoverText.appendNewline();
        hoverText.append(Component.text("Category: ", NamedTextColor.GRAY).append(Component.text(getCategory(), NamedTextColor.BLUE)));
        if (getCustomMarker() != null )
            hoverText.appendNewline().append(Component.text("Custom Marker: ", NamedTextColor.GRAY).append(Component.text(getCustomMarker(), NamedTextColor.BLUE)));
        if (distanceFrom != null)
            hoverText.appendNewline().append(Component.text("Distance: ", NamedTextColor.GRAY).append(Component.text(String.format("%.2f", distanceFrom.distance(getLocation())), NamedTextColor.BLUE)));

        hoverText.appendNewline();
        hoverText.append(Component.text("ID: ", NamedTextColor.GRAY).append(Component.text(getId(), NamedTextColor.BLUE)));
        questionMark.hoverEvent(HoverEvent.showText(hoverText));
        return questionMark.build();
    }

    @Override
    public String getPlainItemText(ChatPaginator<?, Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext>> paginator, Pair<HotspotPaginator.HotspotPaginatorMode, ICommandContext> generatorInfo) {
        return getName();
    }

    public void setChanged(boolean changedStatus) {
        this.hasChanged = changedStatus;
    }
}

