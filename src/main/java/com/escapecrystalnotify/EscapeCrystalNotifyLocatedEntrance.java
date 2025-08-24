package com.escapecrystalnotify;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

@Getter
public class EscapeCrystalNotifyLocatedEntrance {
    public EscapeCrystalNotifyRegionEntranceObject target;
    public EscapeCrystalNotifyRegionEntrance definition;
    public WorldPoint initialWorldPoint;


    EscapeCrystalNotifyLocatedEntrance(EscapeCrystalNotifyRegionEntranceObject target, EscapeCrystalNotifyRegionEntrance definition, WorldPoint initialWorldPoint) {
        this.target = target;
        this.definition = definition;
        this.initialWorldPoint = initialWorldPoint;
    }

    public boolean hasMoved() {
        return computeChunkIdFromWorldPoint(this.target.getWorldLocation()) != computeChunkIdFromWorldPoint(initialWorldPoint);
    }

    public boolean isChunkless() {
        return this.definition.getChunkIds() == null;
    }

    public boolean isEntranceInValidChunk() {
        if (this.isChunkless()) return true;
        return this.definition.getChunkIds().contains(computeChunkIdFromWorldPoint(this.target.getWorldLocation()));
    }

    public boolean canHighlight() {
        return this.definition.getOverlayType().canHighlight();
    }

    public boolean canDeprioritize() {
        return this.definition.getOverlayType().canDeprioritize();
    }

    public boolean isPlayerPastEntrance(WorldPoint playerWorldPoint) {
        if (this.definition.getEntranceDirection() == null) return false;

        int entranceTileX = this.target.getWorldLocation().getX();
        int entranceTileY = this.target.getWorldLocation().getY();
        int playerTileX = playerWorldPoint.getX();
        int playerTileY = playerWorldPoint.getY();

        switch (this.definition.getEntranceDirection()) {
            case NORTHWARD:
                return playerTileY > entranceTileY;
            case SOUTHWARD:
                return playerTileY < entranceTileY;
            case EASTWARD:
                return playerTileX > entranceTileX;
            case WESTWARD:
                return playerTileX < entranceTileX;
            default:
                return false;
        }
    }

    public static int computeChunkIdFromWorldPoint(WorldPoint worldPoint) {
        int currentTileX = worldPoint.getX();
        int currentTileY = worldPoint.getY();
        final int currentChunkX = currentTileX >> 3;
        final int currentChunkY = currentTileY >> 3;

        return (currentChunkX << 11) | currentChunkY;
    }
}
