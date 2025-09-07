package com.escapecrystalnotify;

import lombok.Getter;

import java.util.List;

@Getter
public class EscapeCrystalNotifyRegionEntrance {
    public EscapeCrystalNotifyRegionEntranceOverlayType overlayType;
    public List<Integer> chunkIds;
    public EscapeCrystalNotifyRegionEntranceDirection entranceDirection;
    public int[] entranceIds;
    public EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel;
    public boolean escapeCrystalDisabled;
    public EscapeCrystalNotifyRegionEntranceObjectType objectType;

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.objectType = objectType;
    }
}
