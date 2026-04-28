package net.greenfieldmc.core.templates.models;

import java.util.List;

public class TemplateInstance {

    private final String currentTemplateName;
    private final List<PasteOption> pasteOptions;

    public TemplateInstance(List<String> templates, List<PasteOption> pasteOptions) {
        this.currentTemplateName =  templates.get((int) (Math.random() * templates.size()));
        this.pasteOptions = pasteOptions;
    }

    public String getCurrentTemplateName() {
        return currentTemplateName;
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
