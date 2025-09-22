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
    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    EscapeCrystalNotifyOverlayActive(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);
        setPriority(OverlayPriority.MED);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);

        this.plugin = plugin;
        this.config = config;

        escapeCrystalImage = loadEscapeCrystalImage();

        initializePreviouslyGeneratedImage();
    }

    private static BufferedImage loadEscapeCrystalImage() {
        return ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-active.png");
    }

    private void initializePreviouslyGeneratedImage() {
        previouslyGeneratedImage.scale = -1;
        previouslyGeneratedImage.generatedImage = escapeCrystalImage;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        boolean enabled = config.enableOnScreenWidget();
        boolean active = plugin.isEscapeCrystalInactivityTeleportActive();
        boolean notHardcore = config.requireHardcoreAccountType() && !plugin.isHardcoreAccountType();
        boolean inactiveOnly = config.onlyDisplayInactiveOnScreenWidget();
        boolean atSafeRegion = plugin.isAtSafeRegionId();

        boolean atNotifyRegion;

        if (this.plugin.isAtNotifyRegionId()) {
            atNotifyRegion = true;
        } else {
            atNotifyRegion = this.config.alwaysDisplayOnScreenWidget();
        }

        if (!enabled || notHardcore || !atNotifyRegion || !active || inactiveOnly || atSafeRegion) {
            return null;
        }

        BufferedImage generatedEscapeCrystalImage = generateEscapeCrystalImage();
        ImageComponent imagePanelComponent = new ImageComponent(generatedEscapeCrystalImage);
        return imagePanelComponent.render(graphics);
    }

    private BufferedImage generateEscapeCrystalImage() {
        double targetScale = Math.max(config.activeCrystalWidgetScale(), 1);

        boolean escapeCrystalImageScaleChanged = previouslyGeneratedImage.scale != targetScale;
        boolean escapeCrystalLeftClickTeleportEnabledChanged = previouslyGeneratedImage.escapeCrystalLeftClickTeleportEnabled != plugin.isEscapeCrystalLeftClickTeleportEnabled();
        boolean escapeCrystalInactivityTicksChanged = previouslyGeneratedImage.escapeCrystalInactivityTicks != plugin.getEscapeCrystalInactivityTicks();
        boolean expectedServerInactivityTicksChanged = previouslyGeneratedImage.expectedServerInactivityTicks != plugin.getExpectedServerInactivityTicks();

        if (!escapeCrystalInactivityTicksChanged && !expectedServerInactivityTicksChanged && !escapeCrystalImageScaleChanged && !escapeCrystalLeftClickTeleportEnabledChanged) {
            return previouslyGeneratedImage.generatedImage;
        }

        BufferedImage scaledEscapeCrystalImage;
        if (!escapeCrystalImageScaleChanged) {
            scaledEscapeCrystalImage = previouslyGeneratedImage.scaledBaseImage;
        } else {
            scaledEscapeCrystalImage = scaleImage(escapeCrystalImage, targetScale / 5);
        }

        String overlayText = determineActiveEscapeCrystalOverlayText();
        boolean displayLeftClickWarning = !plugin.isEscapeCrystalLeftClickTeleportEnabled() && config.displayDisabledLeftClickTeleportText();
        BufferedImage generatedEscapeCrystalImage = drawInfoTextOnImage(scaledEscapeCrystalImage, targetScale, overlayText, displayLeftClickWarning);

        previouslyGeneratedImage.scaledBaseImage = scaledEscapeCrystalImage;
        previouslyGeneratedImage.generatedImage = generatedEscapeCrystalImage;
        previouslyGeneratedImage.scale = targetScale;
        previouslyGeneratedImage.escapeCrystalLeftClickTeleportEnabled = plugin.isEscapeCrystalLeftClickTeleportEnabled();
        previouslyGeneratedImage.escapeCrystalInactivityTicks = plugin.getEscapeCrystalInactivityTicks();
        previouslyGeneratedImage.expectedServerInactivityTicks = plugin.getExpectedServerInactivityTicks();

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

    private String determineActiveEscapeCrystalOverlayText() {
        return plugin.getItemModelDisplayText(config.onScreenWidgetDisplayFormat(), config.onScreenWidgetInactivityTimeFormat(), config.onScreenWidgetTimeExpiredText());
    }

    private BufferedImage drawInfoTextOnImage(BufferedImage image, double scale, String overlayText, boolean addLeftClickWarning) {
        BufferedImage imageWithInfoText = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        Graphics g = imageWithInfoText.getGraphics();
        g.drawImage(image, 0, 0, null);

        int fontSize;
        if (overlayText.length() >= 4) {
            fontSize = 5;
        } else {
            fontSize = 6;
        }

        Font font = new Font("Arial", Font.BOLD, fontSize * (int) Math.ceil(scale));

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawTextLowerFourth(g, font, overlayText, imageWithInfoText.getWidth() + 1, imageWithInfoText.getHeight() + 1);

        g.setColor(plugin.getItemModelDisplayTextColor(config.onScreenWidgetDisplayFormat()));

        drawTextLowerFourth(g, font, overlayText, imageWithInfoText.getWidth() - 1, imageWithInfoText.getHeight() - 1);

        if (addLeftClickWarning){
            Font warningMessageFont = new Font("Arial", Font.BOLD, (int) (2.5 * Math.ceil(scale)));
            g.setFont(warningMessageFont);
            g.setColor(Color.RED);
            FontMetrics metrics = g.getFontMetrics(warningMessageFont);
            drawTextBottomMiddle(g, warningMessageFont, "NO LEFT", imageWithInfoText.getWidth(), imageWithInfoText.getHeight() - metrics.getHeight());
            drawTextBottomMiddle(g, warningMessageFont, "CLICK", imageWithInfoText.getWidth(), imageWithInfoText.getHeight());
        }

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

    private void drawTextBottomMiddle(Graphics g, Font font, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int xDrawLocation = (x - textWidth) / 2;
        g.drawString(text, xDrawLocation, y);
    }

    private static class EscapeCrystalImage {
        private double scale;
        private BufferedImage scaledBaseImage;
        private BufferedImage generatedImage;
        private boolean escapeCrystalLeftClickTeleportEnabled;
        private int escapeCrystalInactivityTicks;
        private int expectedServerInactivityTicks;


    }
}
