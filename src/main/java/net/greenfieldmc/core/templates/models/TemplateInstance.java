package net.greenfieldmc.core.templates.models;

import java.util.List;

public class TemplateInstance {

    private final String currentTemplateName;
    private final FlipOption currentFlipOption;
    private final RotationOption currentRotationOption;
    private final List<PasteOption> pasteOptions;

    public TemplateInstance(List<String> templates, List<RotationOption> rotationOptions, List<FlipOption> flipOptions, List<PasteOption> pasteOptions) {
        this.currentTemplateName =  templates.get((int) (Math.random() * templates.size()));
        this.currentRotationOption = rotationOptions.isEmpty() ? null : rotationOptions.get((int) (Math.random() * rotationOptions.size()));
        this.currentFlipOption = flipOptions.isEmpty() ? null : flipOptions.get((int) (Math.random() * flipOptions.size()));
        this.pasteOptions = pasteOptions;
    }

    public String getCurrentTemplateName() {
        return currentTemplateName;
    }

    public FlipOption getCurrentFlipOption() {
        return currentFlipOption;
    }

    public boolean hasFlipOption() {
        return currentFlipOption != null;
    }

    public RotationOption getCurrentRotationOption() {
        return currentRotationOption;
    }

    public boolean hasRotationOption() {
        return currentRotationOption != null;
    }

    public boolean ignoreAirBlocks() {
        return pasteOptions.contains(PasteOption.SKIP_AIR);
    }

    public boolean pasteEntities() {
        return pasteOptions.contains(PasteOption.PASTE_ENTITIES);
    }

    public boolean pasteBiomes() {
        return pasteOptions.contains(PasteOption.PASTE_BIOMES);
    }

}
