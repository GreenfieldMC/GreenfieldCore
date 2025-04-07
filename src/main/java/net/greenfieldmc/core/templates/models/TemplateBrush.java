package net.greenfieldmc.core.templates.models;

import java.util.ArrayList;
import java.util.List;

public class TemplateBrush {

    private int brushId;
    private final List<String> templates;
    private final List<RotationOption> rotationOptions;
    private final List<FlipOption> flipOptions;
    private final List<PasteOption> pasteOptions;
    private TemplateInstance nextTemplate;

    public TemplateBrush() {
        this.templates = new ArrayList<>();
        this.rotationOptions = new ArrayList<>();
        this.flipOptions = new ArrayList<>();
        this.pasteOptions = new ArrayList<>();
    }

    public boolean hasNextTemplate() {
        return nextTemplate != null && !templates.isEmpty();
    }

    public TemplateInstance getNextTemplate() {
        if (nextTemplate == null || templates.isEmpty()) throw new IllegalStateException("No next template set or template list is empty.");
        return nextTemplate;
    }

    public void randomizeNextTemplate() {
        if (templates.isEmpty()) nextTemplate = null;
        else nextTemplate = new TemplateInstance(templates, rotationOptions, flipOptions, pasteOptions);
    }

    public void setBrushId(int brushId) {
        this.brushId = brushId;
    }

    public int getBrushId() {
        return brushId;
    }

    public void addTemplate(String template) {
        this.templates.add(template);
    }

    public void removeTemplate(String template) {
        this.templates.remove(template);
    }

    public void addRotationOption(RotationOption rotationOption) {
        this.rotationOptions.add(rotationOption);
    }

    public void removeRotationOption(RotationOption rotationOption) {
        this.rotationOptions.remove(rotationOption);
    }

    public void addFlipOption(FlipOption flipOption) {
        this.flipOptions.add(flipOption);
    }

    public void removeFlipOption(FlipOption flipOption) {
        this.flipOptions.remove(flipOption);
    }

    public void addPasteOption(PasteOption pasteOption) {
        this.pasteOptions.add(pasteOption);
    }

    public void removePasteOption(PasteOption pasteOption) {
        this.pasteOptions.remove(pasteOption);
    }

    public List<String> getTemplates() {
        return templates;
    }

    public List<RotationOption> getRotationOptions() {
        return rotationOptions;
    }

    public List<FlipOption> getFlipOptions() {
        return flipOptions;
    }

    public List<PasteOption> getPasteOptions() {
        return pasteOptions;
    }
}
