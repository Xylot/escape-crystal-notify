package com.escapecrystalnotify;

import lombok.Getter;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.List;

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
        if (this.gameObject != null) {
            return this.gameObject.getWorldLocation();
        }

        if (this.npc != null) {
            return this.npc.getWorldLocation();
        }

        if (this.decorativeObject != null) {
            return this.decorativeObject.getWorldLocation();
        }

        if (this.wallObject != null) {
            return this.wallObject.getWorldLocation();
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
