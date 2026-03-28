package net.greenfieldmc.core.signmanager;

public enum SignFlag {
    CONSTRUCTION("Construction"),
    ROAD("Road"),
    RAIL("Rail"),
    TRANSIT("Transit"),
    MUNICIPAL("Municipal");

    private final String displayName;

    SignFlag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SignFlag fromString(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return SignFlag.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

