package com.escapecrystalnotify;

// Inclusive directions will include the tile the object is on as past the player
// This is useful for entrances that the player steps in to (i.e. GodWars entrances)

public enum EscapeCrystalNotifyRegionEntranceDirection {
    NORTHWARD,
    NORTHWARD_INCLUSIVE,
    SOUTHWARD,
    SOUTHWARD_INCLUSIVE,
    EASTWARD,
    EASTWARD_INCLUSIVE,
    WESTWARD,
    WESTWARD_INCLUSIVE,
}
