package com.escapecrystalnotify;

import lombok.Getter;

import java.util.*;
import java.util.function.IntPredicate;

@Getter
public class EscapeCrystalNotifyRegionEntrance {
    public EscapeCrystalNotifyRegionEntranceOverlayType overlayType;
    public List<Integer> chunkIds;
    public EscapeCrystalNotifyRegionEntranceDirection entranceDirection;
    public int[] entranceIds;
    public EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel;
    public boolean escapeCrystalDisabled;
    public boolean logoutBugPossible;
    public EscapeCrystalNotifyRegionEntranceObjectType objectType;
    public boolean isDebug;
    public boolean bossInstanced;
    public boolean closest;
    public Map<Integer, IntPredicate> varbitConstraints;

    public EscapeCrystalNotifyRegionEntrance withClosest() {
        this.closest = true;
        return this;
    }

    public EscapeCrystalNotifyRegionEntrance withInstancedBoss() {
        this.bossInstanced = true;
        return this;
    }

    public EscapeCrystalNotifyRegionEntrance withVarbitConstraints(List<Map<Integer, IntPredicate>> constraints) {
        Map<Integer, IntPredicate> combined = new HashMap<>();

        for (Map<Integer, IntPredicate> map : constraints) {
            combined.putAll(map);
        }

        this.varbitConstraints = combined;

        return this;
    }

    EscapeCrystalNotifyRegionEntrance(int entranceId, boolean isDebug) {
        this(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, null,
            EscapeCrystalNotifyRegionEntranceObjectType.ANY, entranceId);
        this.isDebug = isDebug;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = false;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = false;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = false;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = false;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, EscapeCrystalNotifyRegionEntranceDirection entranceDirection, List<Integer> chunkIds, EscapeCrystalNotifyRegionEntrancePlaneLevel planeLevel, boolean escapeCrystalDisabled, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = entranceDirection;
        this.entranceIds = entranceIds;
        this.planeLevel = planeLevel;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.logoutBugPossible = false;
        this.objectType = objectType;
    }

    EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType overlayType, List<Integer> chunkIds, boolean escapeCrystalDisabled, boolean logoutBugPossible, EscapeCrystalNotifyRegionEntranceObjectType objectType, int...entranceIds) {
        this.overlayType = overlayType;
        this.chunkIds = chunkIds;
        this.entranceDirection = null;
        this.entranceIds = entranceIds;
        this.planeLevel = EscapeCrystalNotifyRegionEntrancePlaneLevel.ANY;
        this.escapeCrystalDisabled = escapeCrystalDisabled;
        this.logoutBugPossible = logoutBugPossible;
        this.objectType = objectType;
    }

    public Set<Integer> getConstraintVarbitIds() {
        if (this.varbitConstraints == null) return new HashSet<>();
        return this.varbitConstraints.keySet();
    }
}
