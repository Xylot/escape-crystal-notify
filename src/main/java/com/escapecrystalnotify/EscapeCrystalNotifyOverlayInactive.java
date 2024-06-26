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

public class EscapeCrystalNotifyOverlayInactive extends Overlay {
    private static final EscapeCrystalImage previouslyGeneratedImage = new EscapeCrystalImage();
    private static BufferedImage escapeCrystalImage;
    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    EscapeCrystalNotifyOverlayInactive(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);
        setPriority(OverlayPriority.LOW);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);

        this.plugin = plugin;
        this.config = config;

        escapeCrystalImage = loadEscapeCrystalImage();

        initializePreviouslyGeneratedImage();
    }

    private static BufferedImage loadEscapeCrystalImage() {
        return ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-inactive.png");
    }

    private void initializePreviouslyGeneratedImage() {
        previouslyGeneratedImage.scale = 1;
        previouslyGeneratedImage.generatedImage = escapeCrystalImage;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        boolean enabled = config.enableOnScreenWidget();
        boolean active = plugin.isEscapeCrystalInactivityTeleportActive();
        boolean notHardcore = config.requireHardcoreAccountType() && !plugin.isHardcoreAccountType();

        boolean atNotifyRegion;
        if (this.plugin.isAtNotifyRegionId()) {
            atNotifyRegion = true;
        } else {
            atNotifyRegion = this.config.alwaysDisplayOnScreenWidget();
        }

        if (!enabled || notHardcore || !atNotifyRegion || active) {
            return null;
        }

        BufferedImage generatedEscapeCrystalImage = generateEscapeCrystalImage();
        ImageComponent imagePanelComponent = new ImageComponent(generatedEscapeCrystalImage);
        return imagePanelComponent.render(graphics);
    }

    private BufferedImage generateEscapeCrystalImage() {
        double targetScale = Math.max(config.inactiveCrystalWidgetScale(), 1);
        boolean escapeCrystalImageScaleChanged = previouslyGeneratedImage.scale != targetScale;

        if (!escapeCrystalImageScaleChanged) {
            return previouslyGeneratedImage.generatedImage;
        }

        BufferedImage generatedEscapeCrystalImage = scaleImage(escapeCrystalImage, targetScale / 5);

        previouslyGeneratedImage.generatedImage = generatedEscapeCrystalImage;
        previouslyGeneratedImage.scale = targetScale;

        return generatedEscapeCrystalImage;
    }

    private BufferedImage scaleImage(BufferedImage image, double scale) {
        BufferedImage scaledImage = new BufferedImage(
                (int) (scale * image.getWidth()),
                (int) (scale * image.getHeight()),
                BufferedImage.TYPE_INT_ARGB
        );

        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        scaledImage = scaleOp.filter(image, scaledImage);

        return scaledImage;
    }

    private static class EscapeCrystalImage {
        private double scale;
        private BufferedImage generatedImage;

    }
}
