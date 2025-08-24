package com.escapecrystalnotify;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;

@Getter
public class EscapeCrystalNotifyRegionEntranceObject {
    public GameObject gameObject;
    public NPC npc;
    public DecorativeObject decorativeObject;
    public WallObject wallObject;


    EscapeCrystalNotifyRegionEntranceObject(GameObject gameObject) {
        this.gameObject = gameObject;
        this.npc = null;
        this.decorativeObject = null;
        this.wallObject = null;
    }

    EscapeCrystalNotifyRegionEntranceObject(NPC npc) {
        this.gameObject = null;
        this.npc = npc;
        this.decorativeObject = null;
        this.wallObject = null;
    }

    EscapeCrystalNotifyRegionEntranceObject(DecorativeObject decorativeObject) {
        this.gameObject = null;
        this.npc = null;
        this.decorativeObject = decorativeObject;
        this.wallObject = null;
    }

    EscapeCrystalNotifyRegionEntranceObject(WallObject wallObject) {
        this.gameObject = null;
        this.npc = null;
        this.decorativeObject = null;
        this.wallObject = wallObject;
    }

    public WorldPoint getWorldLocation() {
        WorldPoint worldPoint = null;
        WorldView worldView = null;
        
        if (this.gameObject != null) {
            worldPoint = this.gameObject.getWorldLocation();
            worldView = this.gameObject.getWorldView();
        }

        if (this.npc != null) {
            worldPoint = this.npc.getWorldLocation();
            worldView = this.npc.getWorldView();
        }

        if (this.decorativeObject != null) {
            worldPoint = this.decorativeObject.getWorldLocation();
            worldView = this.decorativeObject.getWorldView();
        }

        if (this.wallObject != null) {
            worldPoint = this.wallObject.getWorldLocation();
            worldView = this.wallObject.getWorldView();
        }

        if (worldPoint == null) {
            return null;
        }

        if (worldView != null && worldView.isInstance()) {
            worldPoint = WorldPoint.fromLocalInstance(worldView.getScene(), this.getLocalLocation(), worldView.getPlane());
        }

        return worldPoint;
    }

    public LocalPoint getLocalLocation() {
        if (this.gameObject != null) {
            return this.gameObject.getLocalLocation();
        }

        if (this.npc != null) {
            return this.npc.getLocalLocation();
        }

        if (this.decorativeObject != null) {
            return this.decorativeObject.getLocalLocation();
        }

        if (this.wallObject != null) {
            return this.wallObject.getLocalLocation();
        }

        return null;
    }

    public int getId() {
        if (this.gameObject != null) {
            return this.gameObject.getId();
        }

        if (this.npc != null) {
            return this.npc.getId();
        }

        if (this.decorativeObject != null) {
            return this.decorativeObject.getId();
        }

        if (this.wallObject != null) {
            return this.wallObject.getId();
        }

        return -1;
    }

    public Shape getConvexHull() {
        try {
            if (gameObject != null) {
                return gameObject.getConvexHull();
            }
            if (npc != null) {
                return npc.getConvexHull();
            }
            if (decorativeObject != null) {
                return decorativeObject.getConvexHull();
            }
            if (wallObject != null) {
                return wallObject.getConvexHull();
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public net.runelite.api.Point getCanvasTextLocation(Graphics2D graphics, String s, int i) {
        if (this.gameObject != null) {
            return this.gameObject.getCanvasTextLocation(graphics, s, i);
        }

        if (this.npc != null) {
            return this.npc.getCanvasTextLocation(graphics, s, i);
        }

        if (this.decorativeObject != null) {
            return this.decorativeObject.getCanvasTextLocation(graphics, s, i);
        }

        if (this.wallObject != null) {
            return this.wallObject.getCanvasTextLocation(graphics, s, i);
        }

        return null;
    }
}
