package com.escapecrystalnotify;

import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;

public class EscapeCrystalNotifyRegionEntranceOverlay extends Overlay {
    private static final EntranceOverlayImage previouslyGeneratedImage = new EntranceOverlayImage();
    private static BufferedImage entranceOverlayImage;
    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    EscapeCrystalNotifyRegionEntranceOverlay(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);

        this.plugin = plugin;
        this.config = config;

        initializePreviouslyGeneratedImage();
    }

    private void initializePreviouslyGeneratedImage() {
        previouslyGeneratedImage.scale = 1;
        previouslyGeneratedImage.generatedImage = entranceOverlayImage;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        boolean enabled = config.displayEntranceOverlay();
        boolean active = plugin.isEscapeCrystalInactivityTeleportActive();
        boolean notHardcore = config.requireHardcoreAccountType() && !plugin.isHardcoreAccountType();
        boolean atNotifyRegion = plugin.isAtNotifyRegionId();

        if (!enabled || notHardcore || !atNotifyRegion || active) {
            return null;
        }

        List<EscapeCrystalNotifyLocatedEntrance> validEntrances = plugin.getValidEntrances();
        if (validEntrances.isEmpty()) return null;

        if (entranceOverlayImage == null) {
            entranceOverlayImage = plugin.getEntranceOverlayImage();
            initializePreviouslyGeneratedImage();
        }

        for (EscapeCrystalNotifyLocatedEntrance entrance : validEntrances) {
            if (!entrance.canHighlight() || entrance.isPlayerPastEntrance(plugin.getCurrentWorldPoint())) {
                continue;
            }

            Shape entranceClickbox = entrance.getTarget().getConvexHull();

            if (entranceClickbox == null) {
                continue;
            }

            Color fillColor = entrance.isPrioritized()
                ? config.prioritizedEntranceOverlayFillColor() 
                : config.entranceOverlayFillColor();

            graphics.setColor(fillColor);
            graphics.fill(entranceClickbox);

            graphics.setColor(fillColor.darker());
            graphics.draw(entranceClickbox);

            Point baseImageLocation = entrance.getTarget().getCanvasTextLocation(graphics, "", 125);

            if (baseImageLocation == null) {
                continue;
            }

            BufferedImage generatedEntranceOverlayImage = generateEntranceOverlayImage();
            int xOffset = generatedEntranceOverlayImage.getWidth() / 2;
            int yOffset = generatedEntranceOverlayImage.getHeight() / 2;
            Point imageLocation = new Point(baseImageLocation.getX() - xOffset, baseImageLocation.getY() - yOffset);
            OverlayUtil.renderImageLocation(graphics, imageLocation, generatedEntranceOverlayImage);
        }

        return null;
    }

    private BufferedImage generateEntranceOverlayImage() {
        double targetScale = Math.max(config.entranceOverlayImageScale(), 0.1);
        boolean entranceOverlayImageScaleChanged = previouslyGeneratedImage.scale != targetScale;

        if (!entranceOverlayImageScaleChanged) {
            return previouslyGeneratedImage.generatedImage;
        }

        BufferedImage generatedEntranceOverlayImage = scaleImage(entranceOverlayImage, targetScale);

        previouslyGeneratedImage.generatedImage = generatedEntranceOverlayImage;
        previouslyGeneratedImage.scale = targetScale;

        return generatedEntranceOverlayImage;
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

    private static class EntranceOverlayImage {
        private double scale;
        private BufferedImage generatedImage;

    }
}
