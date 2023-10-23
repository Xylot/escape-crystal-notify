package com.escapecrystalnotify;

import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class EscapeCrystalNotifyOverlay extends Overlay {
    private static final ScaledImage previouslyScaledImage = new ScaledImage();
    private static BufferedImage escapeCrystalImage;
    private final EscapeCrystalNotifyPlugin escapeCrystalNotifyPlugin;
    private final EscapeCrystalNotifyConfig escapeCrystalNotifyConfig;

    @Inject
    EscapeCrystalNotifyOverlay(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);
        setPriority(OverlayPriority.MED);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.escapeCrystalNotifyPlugin = plugin;
        this.escapeCrystalNotifyConfig = config;
        loadEscapeCrystalImage();
        previouslyScaledImage.scale = 1;
        previouslyScaledImage.scaledBufferedImage = escapeCrystalImage;
    }

    private static void loadEscapeCrystalImage() {
        escapeCrystalImage = ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal.png");
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        if (escapeCrystalNotifyPlugin.isEscapeCrystalWithPlayer() && escapeCrystalNotifyPlugin.isEscapeCrystalActive()) {
            return null;
        }

        BufferedImage scaledEscapeCrystalImage = scaleImage(escapeCrystalImage);
        ImageComponent imagePanelComponent = new ImageComponent(scaledEscapeCrystalImage);
        return imagePanelComponent.render(graphics);
    }

    private BufferedImage scaleImage(BufferedImage escapeCrystalImage) {
        if (previouslyScaledImage.scale == escapeCrystalNotifyConfig.scale() || escapeCrystalNotifyConfig.scale() <= 0) {
            return previouslyScaledImage.scaledBufferedImage;
        }

        int width = escapeCrystalImage.getWidth();
        int height = escapeCrystalImage.getHeight();

        BufferedImage scaledEscapeCrystalImage = new BufferedImage(
                escapeCrystalNotifyConfig.scale() * width,
                escapeCrystalNotifyConfig.scale() * height,
                BufferedImage.TYPE_INT_ARGB
        );

        AffineTransform at = new AffineTransform();
        at.scale(escapeCrystalNotifyConfig.scale(), escapeCrystalNotifyConfig.scale());
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        scaledEscapeCrystalImage = scaleOp.filter(escapeCrystalImage, scaledEscapeCrystalImage);

        previouslyScaledImage.scaledBufferedImage = scaledEscapeCrystalImage;
        previouslyScaledImage.scale = escapeCrystalNotifyConfig.scale();

        return scaledEscapeCrystalImage;
    }

    private static class ScaledImage {
        private int scale;
        private BufferedImage scaledBufferedImage;
    }
}
