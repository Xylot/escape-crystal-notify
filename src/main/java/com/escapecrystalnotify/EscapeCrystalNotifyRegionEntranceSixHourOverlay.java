package com.escapecrystalnotify;

import net.runelite.api.Point;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;

public class EscapeCrystalNotifyRegionEntranceSixHourOverlay extends Overlay {
    private static final SixHourOverlayImage previouslyGeneratedImage = new SixHourOverlayImage();
    private static BufferedImage entranceSixHourOverlayImage;
    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    EscapeCrystalNotifyRegionEntranceSixHourOverlay(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);

        this.plugin = plugin;
        this.config = config;

        initializePreviouslyGeneratedImage();
    }

    private void initializePreviouslyGeneratedImage() {
        previouslyGeneratedImage.scale = 1;
        previouslyGeneratedImage.generatedImage = entranceSixHourOverlayImage;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        boolean enabledLeviathan = plugin.isAtLeviathanLobby() && plugin.isLeviathanSafeguardEnabled();
        boolean enabledDoom = plugin.isAtDoomLobby() && plugin.isDoomSafeguardEnabled();

        if (!enabledLeviathan && !enabledDoom) {
            return null;
        }

        List<EscapeCrystalNotifyLocatedEntrance> validEntrances = plugin.getValidEntrances();
        if (validEntrances.isEmpty()) return null;

        if (entranceSixHourOverlayImage == null) {
            entranceSixHourOverlayImage = plugin.getEntranceSixHourOverlayImage();
            initializePreviouslyGeneratedImage();
        }

        for (EscapeCrystalNotifyLocatedEntrance entrance : validEntrances) {
            if (!entrance.canHighlight() || entrance.isPlayerPastEntrance(plugin.getCurrentWorldPoint())) {
                continue;
            }

            boolean isLogoutBugWarning = entrance.getDefinition().isLogoutBugPossible() && plugin.isCloseToSixHourLogout();
            if (!isLogoutBugWarning) {
                continue;
            }

            Shape entranceClickbox = entrance.getTarget().getConvexHull();
            if (entranceClickbox == null) {
                continue;
            }

            Color fillColor;
            if (plugin.isAtLeviathanLobby()) {
                fillColor = config.leviathanLogoutBugHighlightColor();
            } else if (plugin.isAtDoomLobby()) {
                fillColor = config.doomLogoutBugHighlightColor();
            } else {
                fillColor = entrance.isPrioritized()
                    ? config.prioritizedEntranceOverlayFillColor() 
                    : config.entranceOverlayFillColor();
            }

            graphics.setColor(fillColor);
            graphics.fill(entranceClickbox);

            graphics.setColor(fillColor.darker());
            graphics.draw(entranceClickbox);

            Point baseImageLocation = entrance.getTarget().getCanvasTextLocation(graphics, "", 125);

            if (baseImageLocation == null) {
                continue;
            }

            BufferedImage generatedSixHourOverlayImage = generateSixHourOverlayImage();
            int xOffset = generatedSixHourOverlayImage.getWidth() / 2;
            int yOffset = generatedSixHourOverlayImage.getHeight() / 2;
            Point imageLocation = new Point(baseImageLocation.getX() - xOffset, baseImageLocation.getY() - yOffset);
            OverlayUtil.renderImageLocation(graphics, imageLocation, generatedSixHourOverlayImage);

            String logoutBugMessage;
            if (plugin.isAtLeviathanLobby()) {
                logoutBugMessage = config.leviathanLogoutBugMessage();
            } else if (plugin.isAtDoomLobby()) {
                logoutBugMessage = config.doomLogoutBugMessage();
            } else {
                logoutBugMessage = "Relog - Close to 6 hour logout";
            }
            
            FontMetrics fontMetrics = graphics.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(logoutBugMessage);
            int centeredX = baseImageLocation.getX() - (textWidth / 2);
            int textY = baseImageLocation.getY() + 30;
            
            Point textLocation = new Point(centeredX, textY);
            OverlayUtil.renderTextLocation(graphics, textLocation, logoutBugMessage, fillColor.brighter());
        }

        return null;
    }

    private BufferedImage generateSixHourOverlayImage() {
        double targetScale = Math.max(config.entranceOverlayImageScale(), 0.1);
        boolean sixHourOverlayImageScaleChanged = previouslyGeneratedImage.scale != targetScale;

        if (!sixHourOverlayImageScaleChanged) {
            return previouslyGeneratedImage.generatedImage;
        }

        BufferedImage generatedSixHourOverlayImage = scaleImage(entranceSixHourOverlayImage, targetScale);

        previouslyGeneratedImage.generatedImage = generatedSixHourOverlayImage;
        previouslyGeneratedImage.scale = targetScale;

        return generatedSixHourOverlayImage;
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

    private static class SixHourOverlayImage {
        private double scale;
        private BufferedImage generatedImage;
    }
}
