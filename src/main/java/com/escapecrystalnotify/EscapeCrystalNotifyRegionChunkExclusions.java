package com.escapecrystalnotify;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum EscapeCrystalNotifyRegionChunkExclusions {

    LUMBRIDGE_BASEMENT(12950, 822449, 822450, 822451, 824497, 824498),
    EVIL_DAVE_BASEMENT(12442, 787666, 787667, 787668, 787669, 789714, 789715, 789716, 789717);

    @Getter
    private int regionId;
    @Getter
    private int[] chunkIds;

    EscapeCrystalNotifyRegionChunkExclusions(int regionId, int... chunkIds) {
        this.regionId = regionId;
        this.chunkIds = chunkIds;
    }

    public static HashSet<Integer> getAllExcludedRegionIds() {
        return Arrays.stream(EscapeCrystalNotifyRegionChunkExclusions.values())
                .map(EscapeCrystalNotifyRegionChunkExclusions::getRegionId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static HashSet<Integer> getAllExcludedChunkIds() {
        return Arrays.stream(EscapeCrystalNotifyRegionChunkExclusions.values())
                .flatMap(region -> Arrays.stream(region.getChunkIds()).boxed())
                .collect(Collectors.toCollection(HashSet::new));
    }

}
