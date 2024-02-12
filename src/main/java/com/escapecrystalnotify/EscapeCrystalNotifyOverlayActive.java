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

public class EscapeCrystalNotifyOverlayActive extends Overlay {
    private static final EscapeCrystalImage previouslyGeneratedImage = new EscapeCrystalImage();
    private static BufferedImage escapeCrystalImage;
    private final EscapeCrystalNotifyPlugin escapeCrystalNotifyPlugin;
    private final EscapeCrystalNotifyConfig escapeCrystalNotifyConfig;

    @Inject
    EscapeCrystalNotifyOverlayActive(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);
        setPriority(OverlayPriority.MED);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);

        this.escapeCrystalNotifyPlugin = plugin;
        this.escapeCrystalNotifyConfig = config;

        escapeCrystalImage = loadEscapeCrystalImage();

        initializePreviouslyGeneratedImage();
    }

    private static BufferedImage loadEscapeCrystalImage() {
        return ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-active.png");
    }

    private void initializePreviouslyGeneratedImage() {
        previouslyGeneratedImage.scale = 1;
        previouslyGeneratedImage.generatedImage = escapeCrystalImage;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        boolean active = escapeCrystalNotifyPlugin.isEscapeCrystalInactivityTeleportActive();
        boolean notHardcore = escapeCrystalNotifyConfig.requireHardcoreAccountType() && !escapeCrystalNotifyPlugin.isHardcoreAccountType();
        boolean notAtNotifyRegion = !escapeCrystalNotifyPlugin.isAtNotifyRegionId();
        boolean disabledActivityTimer = active && !escapeCrystalNotifyConfig.displayTimeBeforeTeleport();

        if (notHardcore || notAtNotifyRegion || !active || disabledActivityTimer) {
            return null;
        }

        BufferedImage generatedEscapeCrystalImage = generateEscapeCrystalImage();
        ImageComponent imagePanelComponent = new ImageComponent(generatedEscapeCrystalImage);
        return imagePanelComponent.render(graphics);
    }

    private BufferedImage generateEscapeCrystalImage() {
        double targetScale = Math.max(escapeCrystalNotifyConfig.activeCrystalScale(), 1);

        boolean escapeCrystalImageScaleChanged = previouslyGeneratedImage.scale != targetScale;
        boolean escapeCrystalInactivityTicksChanged = previouslyGeneratedImage.escapeCrystalInactivityTicks != escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTicks();
        boolean expectedServerInactivityTicksChanged = previouslyGeneratedImage.expectedServerInactivityTicks != escapeCrystalNotifyPlugin.getExpectedServerInactivityTicks();

        if (!escapeCrystalInactivityTicksChanged && !expectedServerInactivityTicksChanged && !escapeCrystalImageScaleChanged) {
            return previouslyGeneratedImage.generatedImage;
        }

        BufferedImage scaledEscapeCrystalImage;
        if (!escapeCrystalImageScaleChanged) {
            scaledEscapeCrystalImage = previouslyGeneratedImage.scaledBaseImage;
        } else {
            scaledEscapeCrystalImage = scaleImage(escapeCrystalImage, targetScale / 5);
        }

        String overlayText = determineActiveEscapeCrystalOverlayText(escapeCrystalNotifyConfig.inactivityTimeFormat());
        BufferedImage generatedEscapeCrystalImage = drawInfoTextOnImage(scaledEscapeCrystalImage, targetScale, overlayText);

        previouslyGeneratedImage.scaledBaseImage = scaledEscapeCrystalImage;
        previouslyGeneratedImage.generatedImage = generatedEscapeCrystalImage;
        previouslyGeneratedImage.scale = targetScale;
        previouslyGeneratedImage.escapeCrystalInactivityTicks = escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTicks();
        previouslyGeneratedImage.expectedServerInactivityTicks = escapeCrystalNotifyPlugin.getExpectedServerInactivityTicks();

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

    private BufferedImage drawInfoTextOnImage(BufferedImage image, double scale, String overlayText) {
        BufferedImage imageWithInfoText = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        Graphics g = imageWithInfoText.getGraphics();
        g.drawImage(image, 0, 0, null);
        Font font = new Font("Arial", Font.BOLD, 6 * (int) Math.ceil(scale));

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawTextLowerFourth(g, font, overlayText, imageWithInfoText.getWidth() + 1, imageWithInfoText.getHeight() + 1);
        g.setColor(Color.WHITE);
        drawTextLowerFourth(g, font, overlayText, imageWithInfoText.getWidth() - 1, imageWithInfoText.getHeight() - 1);

        g.dispose();

        return imageWithInfoText;
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
        private double scale;
        private BufferedImage scaledBaseImage;
        private BufferedImage generatedImage;
        private int escapeCrystalInactivityTicks;
        private int expectedServerInactivityTicks;


    }
}
