package com.escapecrystalnotify;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum EscapeCrystalNotifyRegionPlaneRequirements {

    PERILOUS_MOONS_DUNGEON(0, 5525, 5526, 5527, 5528, 5783, 6037, 6038, 6039);

    private int plane;
    @Getter
    private int[] regionIds;

    EscapeCrystalNotifyRegionPlaneRequirements(int plane, int... regionIds) {
        this.plane = plane;
        this.regionIds = regionIds;
    }

    public static Map<Integer, Integer> getRegionPlaneMap() {
        Map<Integer, Integer> regionPlaneMap = new HashMap<>();
        for (EscapeCrystalNotifyRegionPlaneRequirements e : values()) {
            for (int regionId : e.regionIds) {
                regionPlaneMap.put(regionId, e.plane);
            }
        }
        return regionPlaneMap;
    }

}
