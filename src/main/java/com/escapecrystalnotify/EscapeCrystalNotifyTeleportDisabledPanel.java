package com.escapecrystalnotify;

import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.awt.*;

public class EscapeCrystalNotifyTeleportDisabledPanel extends OverlayPanel {
    private static final Font OVERLAY_PANEL_FONT = FontManager.getRunescapeFont();
    private static final int OVERLAY_PANEL_WIDTH = 185;
    private static final int OVERLAY_PANEL_HEIGHT = 40;
    private static final String TELEPORT_DISABLED_WARNING_TEXT = "WARNING: THE ESCAPE CRYSTAL DOES NOT WORK HERE";

    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    EscapeCrystalNotifyTeleportDisabledPanel(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);

        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(OverlayPriority.HIGH);

        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isTeleportDisabledPanelEnabled()) return null;

        final FontMetrics metrics = graphics.getFontMetrics(OVERLAY_PANEL_FONT);

        panelComponent.setPreferredSize(new Dimension(OVERLAY_PANEL_WIDTH, OVERLAY_PANEL_HEIGHT));
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(LineComponent.builder()
                .left(getCenteredText(TELEPORT_DISABLED_WARNING_TEXT, metrics))
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND)
                .build());

        return panelComponent.render(graphics);
    }

    private static String getCenteredText(String text, FontMetrics metrics) {
        int spaceWidth = metrics.stringWidth(" ");
        int remainingWidth = OVERLAY_PANEL_WIDTH - metrics.stringWidth(text);
        int requiredSpaces = remainingWidth / spaceWidth;
        int characterCount = text.length() + requiredSpaces;
        return StringUtils.center(text, characterCount);
    }
}
