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
        previouslyScaledImage.inactivityTeleportTime = escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTime();
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
        boolean inactivityTeleportTimeChanged = previouslyScaledImage.inactivityTeleportTime != escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTime();
        boolean escapeCrystalImageScaleChanged = previouslyScaledImage.scale != escapeCrystalNotifyConfig.scale();
        boolean escapeCrystalImageScaleBelowMinimum = escapeCrystalNotifyConfig.scale() <= 0;

        if (!inactivityTeleportTimeChanged && !escapeCrystalImageScaleChanged) {
            return previouslyScaledImage.scaledBufferedImage;
        }

        int scale;
        if (escapeCrystalImageScaleBelowMinimum) {
            scale = 1;
        }
        else {
            scale = escapeCrystalNotifyConfig.scale();
        }

        int width = escapeCrystalImage.getWidth();
        int height = escapeCrystalImage.getHeight();

        BufferedImage scaledEscapeCrystalImage = new BufferedImage(
                scale * width,
                scale * height,
                BufferedImage.TYPE_INT_ARGB
        );

        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        scaledEscapeCrystalImage = scaleOp.filter(escapeCrystalImage, scaledEscapeCrystalImage);

        Graphics g = scaledEscapeCrystalImage.getGraphics();
        Font font = new Font("Arial", Font.BOLD, 12 * scale);
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.setColor(Color.BLACK);
        drawText(g, font, escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTime() + "s", scaledEscapeCrystalImage.getWidth() - 1, scaledEscapeCrystalImage.getHeight() - 1);
        g.setColor(Color.WHITE);
        drawText(g, font, escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTime() + "s", scaledEscapeCrystalImage.getWidth() - 3, scaledEscapeCrystalImage.getHeight() - 3);

        previouslyScaledImage.scaledBufferedImage = scaledEscapeCrystalImage;
        previouslyScaledImage.scale = scale;
        previouslyScaledImage.inactivityTeleportTime = escapeCrystalNotifyPlugin.getEscapeCrystalInactivityTime();

        g.dispose();

        return scaledEscapeCrystalImage;
    }

    private void drawText(Graphics g, Font font, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        g.drawString(text, x - textWidth, y - textHeight/4); // Adjust position to bottom right
    }

    private static class ScaledImage {
        private int scale;
        private int inactivityTeleportTime;
        private BufferedImage scaledBufferedImage;
    }
}
