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


public class EscapeCrystalNotifyTextOverlayPanel extends OverlayPanel {
    private static final Font OVERLAY_PANEL_FONT = FontManager.getRunescapeSmallFont();
    private static final int OVERLAY_PANEL_WIDTH = 235;
    private static final int OVERLAY_PANEL_HEIGHT = 100;
    private static final String LEVIATHAN_BUG_INFO_HEADER_TEXT = "WARNING: LEVIATHAN BUG";
    private static final String LEVIATHAN_BUG_INFO_TEXT = "Attempting to logout inside the arena will DISABLE ALL PLAYER ACTIONS (teleports, prayers, food, etc...) and cause an UNAVOIDABLE DEATH";
    private static final String LEVIATHAN_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT = "Right-Click Required";
    private static final String LEVIATHAN_LOGOUT_STATUS_HEADER_TEXT = "Current Logout Setting:";
    private static final String LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT = "Left-Click";
    private static final String LEVIATHAN_SIX_HOUR_WARNING_TEXT = "WARNING: You are approaching the 6-hour forced logout timer! You should re-log to avoid a potential forced death";
    private static final String LEVIATHAN_INSTRUCTION_TEXT = "Navigate to the 'Leviathan Safeguards' section in the 'Escape Crystal Notify' settings to enable left-click logout prevention and/or filter these messages";

    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    EscapeCrystalNotifyTextOverlayPanel(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);

        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(OverlayPriority.HIGH);

        this.plugin = plugin;
        this.config = config;
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isLeviathanSafeguardPanelEnabled()) return null;
        if (plugin.getLeviathanRowboat() == null) return null;

        final FontMetrics metrics = graphics.getFontMetrics(OVERLAY_PANEL_FONT);

        LineComponent newLineComponent = LineComponent.builder().left("\n").build();

        panelComponent.setPreferredSize(new Dimension(OVERLAY_PANEL_WIDTH, OVERLAY_PANEL_HEIGHT));
        panelComponent.getChildren().clear();

        if (!config.hideLeviathanBugInfoText()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(getCenteredText(LEVIATHAN_BUG_INFO_HEADER_TEXT, metrics))
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(LEVIATHAN_BUG_INFO_TEXT)
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());
        }

        if (!config.hideLeviathanLogoutSettingText()) {
            if (!panelComponent.getChildren().isEmpty()) {
                panelComponent.getChildren().add(newLineComponent);
            }

            LineComponent.LineComponentBuilder logoutStatusComponent = LineComponent.builder()
                    .left(LEVIATHAN_LOGOUT_STATUS_HEADER_TEXT)
                    .leftColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT)
                    .leftFont(OVERLAY_PANEL_FONT);

            if (config.deprioritizeLeviathanLogout()) {
                logoutStatusComponent
                        .right(LEVIATHAN_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT)
                        .rightColor(Color.GREEN)
                        .rightFont(OVERLAY_PANEL_FONT);
            } else {
                logoutStatusComponent
                        .right(LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT)
                        .rightColor(JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND)
                        .rightFont(OVERLAY_PANEL_FONT);
            }

            panelComponent.getChildren().add(logoutStatusComponent.build());
        }

        if (config.warnLeviathanLogoutTimer() && plugin.isCloseToSixHourLogout()) {
            if (!panelComponent.getChildren().isEmpty()) {
                panelComponent.getChildren().add(newLineComponent);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(LEVIATHAN_SIX_HOUR_WARNING_TEXT)
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND)
                    .build());
        }

        if (!config.hideLeviathanSettingsInstructionText()) {
            if (!panelComponent.getChildren().isEmpty()) {
                panelComponent.getChildren().add(newLineComponent);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(LEVIATHAN_INSTRUCTION_TEXT)
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());
        }

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
