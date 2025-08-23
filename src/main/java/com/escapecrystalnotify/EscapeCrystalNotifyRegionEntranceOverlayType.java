package com.escapecrystalnotify;

public enum EscapeCrystalNotifyRegionEntranceOverlayType {
    NONE(false, false),
    PRIORITIZED_WITH_HIGHLIGHT(true, false),
    DEPRIORITIZED_WITH_HIGHLIGHT(true, true),
    DEPRIORITIZED_NO_HIGHLIGHT(false, true);

    private final boolean highlight;
    private final boolean deprioritize;

    EscapeCrystalNotifyRegionEntranceOverlayType(boolean highlight, boolean deprioritize) {
        this.highlight = highlight;
        this.deprioritize = deprioritize;
    }

    public boolean canHighlight() {
        return this.highlight;
    }

    public boolean canDeprioritize() {
        return this.deprioritize;
    }
}
