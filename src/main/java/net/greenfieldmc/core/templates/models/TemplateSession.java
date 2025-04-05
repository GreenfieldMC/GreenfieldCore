package net.greenfieldmc.core.templates.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemplateSession {

    private final UUID uuid;
    private final List<TemplateBrush> brushes;
    private int lastBrushId = 0;

    public TemplateSession(UUID uuid) {
        this.uuid = uuid;
        this.brushes = new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<TemplateBrush> getBrushes() {
        return brushes;
    }

    public TemplateBrush getBrush(int brushId) {
        for (TemplateBrush brush : brushes) {
            if (brush.getBrushId() == brushId) {
                return brush;
            }
        }
        return null;
    }

    public TemplateBrush addBrush(TemplateBrush brush) {
        brush.setBrushId(lastBrushId++);
        this.brushes.add(brush);
        return brush;
    }

    public TemplateBrush removeBrush(TemplateBrush brush) {
        this.brushes.remove(brush);
        return brush;
    }
}
