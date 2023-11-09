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
    private static final EscapeCrystalImage previouslyGeneratedImage = new EscapeCrystalImage();
    private static BufferedImage activeEscapeCrystalImage;
    private static BufferedImage inactiveEscapeCrystalImage;
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

        loadEscapeCrystalImages();
        initializePreviouslyGeneratedImage();
    }

    private static void loadEscapeCrystalImages() {
        activeEscapeCrystalImage = ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-active.png");
        inactiveEscapeCrystalImage = ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-inactive.png");

    }

    private void initializePreviouslyGeneratedImage() {
        previouslyGeneratedImage.scale = 1;
        previouslyGeneratedImage.scaledBaseImage = inactiveEscapeCrystalImage;
        previouslyGeneratedImage.generatedImage = inactiveEscapeCrystalImage;
        previouslyGeneratedImage.escapeCrystalInactivityTicks = escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTicks();
        previouslyGeneratedImage.escapeCrystalInactivitySeconds = escapeCrystalNotifyPlugin.getEscapeCrystalInactivitySeconds();
        previouslyGeneratedImage.expectedServerInactivityTicks = escapeCrystalNotifyPlugin.getExpectedServerInactivityTicks();
        previouslyGeneratedImage.expectedServerInactivitySeconds = escapeCrystalNotifyPlugin.getExpectedServerInactivitySeconds();
        previouslyGeneratedImage.expectedTicksUntilTeleport = escapeCrystalNotifyPlugin.getExpectedTicksUntilTeleport();
        previouslyGeneratedImage.expectedSecondsUntilTeleport = escapeCrystalNotifyPlugin.getExpectedSecondsUntilTeleport();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        boolean active = escapeCrystalNotifyPlugin.isEscapeCrystalInactivityTeleportActive();

        if (active && !escapeCrystalNotifyConfig.displayTimeBeforeTeleport()) {
            return null;
        }

        BufferedImage generatedEscapeCrystalImage = generateEscapeCrystalImage(active);
        ImageComponent imagePanelComponent = new ImageComponent(generatedEscapeCrystalImage);
        return imagePanelComponent.render(graphics);
    }

    private BufferedImage generateEscapeCrystalImage(boolean active) {
        BufferedImage targetEscapeCrystalImage = determineBaseEscapeCrystalImage(active);

        double targetScale = determineEscapeCrystalImageScale(active);

        boolean escapeCrystalActivityChanged = previouslyGeneratedImage.active != active;
        boolean escapeCrystalInactivityTicksChanged = previouslyGeneratedImage.escapeCrystalInactivityTicks != escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTicks();
        boolean expectedServerInactivityTicksChanged = previouslyGeneratedImage.expectedServerInactivityTicks != escapeCrystalNotifyPlugin.getExpectedServerInactivityTicks();
        boolean escapeCrystalImageScaleChanged = previouslyGeneratedImage.scale != targetScale;

        if (!escapeCrystalActivityChanged && !escapeCrystalInactivityTicksChanged && !expectedServerInactivityTicksChanged && !escapeCrystalImageScaleChanged) {
            return previouslyGeneratedImage.generatedImage;
        }

        BufferedImage scaledEscapeCrystalImage;
        if (!escapeCrystalImageScaleChanged && !escapeCrystalActivityChanged) {
            scaledEscapeCrystalImage = previouslyGeneratedImage.scaledBaseImage;
        } else {
            scaledEscapeCrystalImage = scaleImage(targetEscapeCrystalImage, targetScale / 15);
        }

        BufferedImage generatedEscapeCrystalImage;
        if (active) {
            String overlayText = determineActiveEscapeCrystalOverlayText(escapeCrystalNotifyConfig.inactivityTimeFormat());
            generatedEscapeCrystalImage = drawInfoTextOnImage(scaledEscapeCrystalImage, targetScale, overlayText);
        } else {
            generatedEscapeCrystalImage = scaledEscapeCrystalImage;
        }

        previouslyGeneratedImage.active = active;
        previouslyGeneratedImage.scaledBaseImage = scaledEscapeCrystalImage;
        previouslyGeneratedImage.generatedImage = generatedEscapeCrystalImage;
        previouslyGeneratedImage.scale = targetScale;
        previouslyGeneratedImage.escapeCrystalInactivityTicks = escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTicks();
        previouslyGeneratedImage.escapeCrystalInactivitySeconds = escapeCrystalNotifyPlugin.getEscapeCrystalInactivitySeconds();
        previouslyGeneratedImage.expectedServerInactivityTicks = escapeCrystalNotifyPlugin.getExpectedServerInactivityTicks();
        previouslyGeneratedImage.expectedServerInactivitySeconds = escapeCrystalNotifyPlugin.getExpectedServerInactivitySeconds();
        previouslyGeneratedImage.expectedTicksUntilTeleport = escapeCrystalNotifyPlugin.getExpectedTicksUntilTeleport();
        previouslyGeneratedImage.expectedSecondsUntilTeleport = escapeCrystalNotifyPlugin.getExpectedSecondsUntilTeleport();

        return generatedEscapeCrystalImage;
    }

    private BufferedImage determineBaseEscapeCrystalImage(boolean active) {
        if (active) {
            return activeEscapeCrystalImage;
        }

        return inactiveEscapeCrystalImage;
    }

    private int determineEscapeCrystalImageScale(boolean active) {
        if (active) {
            return Math.max(escapeCrystalNotifyConfig.activeCrystalScale(), 1);
        }

        return Math.max(escapeCrystalNotifyConfig.inactiveCrystalScale(), 1);
    }

    private String determineActiveEscapeCrystalOverlayText(EscapeCrystalNotifyConfig.InactivityTimeFormat timeFormat) {
        switch (timeFormat) {
            case SECONDS:
                return escapeCrystalNotifyPlugin.getExpectedSecondsUntilTeleport() + "s";
            case GAME_TICKS:
                return String.valueOf(escapeCrystalNotifyPlugin.getExpectedTicksUntilTeleport());
            default:
                return "";

        }
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

    private BufferedImage drawInfoTextOnImage(BufferedImage image, double scale, String overlayText) {
        BufferedImage imageWithInfoText = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        Graphics g = imageWithInfoText.getGraphics();
        g.drawImage(image, 0, 0, null);
        Font font = new Font("Arial", Font.BOLD, 16 * (int) Math.ceil(scale));

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawTextLowerFourth(g, font, overlayText, imageWithInfoText.getWidth() + 3, imageWithInfoText.getHeight() + 1);
        g.setColor(Color.WHITE);
        drawTextLowerFourth(g, font, overlayText, imageWithInfoText.getWidth() + 1, imageWithInfoText.getHeight() - 1);

        g.dispose();

        return imageWithInfoText;
    }

    private void drawTextBottomRight(Graphics g, Font font, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        g.drawString(text, x - textWidth, y - textHeight / 4);
    }

    private void drawTextLowerFourth(Graphics g, Font font, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(font);

        int textWidth = metrics.stringWidth(text);
        int textAscent = metrics.getAscent();
        int textDescent = metrics.getDescent();

        int xDrawLocation = (x - textWidth) / 2;
        int yDrawLocation = y - (textAscent + (y - (textAscent + textDescent)) / 4);

        g.drawString(text, xDrawLocation, yDrawLocation);
    }

    private static class EscapeCrystalImage {
        private boolean active;
        private double scale;
        private BufferedImage scaledBaseImage;
        private BufferedImage generatedImage;
        private int escapeCrystalInactivityTicks;
        private int escapeCrystalInactivitySeconds;
        private int expectedServerInactivityTicks;
        private int expectedServerInactivitySeconds;
        private int expectedTicksUntilTeleport;
        private int expectedSecondsUntilTeleport;

    }
}
