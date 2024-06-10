package com.escapecrystalnotify;

import net.runelite.api.ItemID;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EscapeCrystalNotifyInfoBox extends InfoBox {
    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;
    public int imageId;

    EscapeCrystalNotifyInfoBox(int imageId, BufferedImage image, EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) {
        super(image, plugin);
        this.plugin = plugin;
        this.config = config;
        this.imageId = imageId;
    }

    @Override
    public String getText() {
        return this.plugin.getItemModelDisplayText(this.config.infoBoxDisplayFormat(), this.config.infoBoxInactivityTimeFormat());
    }

    @Override
    public Color getTextColor() {
        return this.plugin.getItemModelDisplayTextColor(this.config.infoBoxDisplayFormat());
    }

    @Override
    public boolean render() {
        boolean atNotifyRegion;

        if (this.plugin.isAtNotifyRegionId()) {
            atNotifyRegion = true;
        } else {
            atNotifyRegion = this.config.alwaysDisplayInfoBox();
        }

        return this.config.enableInfoBox() && atNotifyRegion;
    }

}
