package com.escapecrystalnotify;

import lombok.Getter;

@Getter
public enum EscapeCrystalNotifyRegionEntrancePlaneLevel {
    ANY(null),
    GROUND(0),
    FIRST_FLOOR(1),
    SECOND_FLOOR(2),
    THIRD_FLOOR(3);

    private final Integer planeLevel;

    EscapeCrystalNotifyRegionEntrancePlaneLevel(Integer planeLevel) {
        this.planeLevel = planeLevel;
    }

    public boolean matchesPlane(int playerPlane) {
        return this.planeLevel == null || this.planeLevel == playerPlane;
    }
}
