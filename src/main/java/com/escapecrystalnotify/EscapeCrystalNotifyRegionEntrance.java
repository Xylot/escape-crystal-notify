package com.escapecrystalnotify;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class EscapeCrystalNotifyRegionEntrance {
    public EscapeCrystalNotifyRegionEntranceOverlayType overlayType;
    public List<Integer> chunkIds;
    public EscapeCrystalNotifyRegionEntranceDirection entranceDirection;
    public int[] entranceIds;


    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
    }
}
