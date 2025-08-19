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
    private static final String SIX_HOUR_WARNING_TEXT = "WARNING: You are approaching the 6-hour forced logout timer! You should re-log to avoid a potential forced death";
    private static final String LEVIATHAN_BUG_INFO_HEADER_TEXT = "WARNING: LEVIATHAN BUG";
    private static final String LEVIATHAN_BUG_INFO_TEXT = "Attempting to logout during the encounter can/will DISABLE ALL PLAYER ACTIONS (teleports, prayers, food, etc...) and cause an UNAVOIDABLE DEATH";
    private static final String LEVIATHAN_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT = "Right-Click Required";
    private static final String LEVIATHAN_LOGOUT_STATUS_HEADER_TEXT = "Current Logout Setting:";
    private static final String LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT = "Left-Click";
    private static final String LEVIATHAN_INSTRUCTION_TEXT = "Navigate to the 'Leviathan Safeguards' section in the 'Escape Crystal Notify' settings to enable left-click logout prevention and/or filter these messages";
    private static final String DOOM_BUG_INFO_HEADER_TEXT = "WARNING: DOOM BUG";
    private static final String DOOM_BUG_INFO_TEXT = "Attempting to logout during the encounter can/will DISABLE ALL PLAYER ACTIONS (teleports, prayers, food, etc...) and cause an UNAVOIDABLE DEATH";
    private static final String DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT = "Right-Click Required";
    private static final String DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_ADDITIONAL_INFO = "Left-Click enabled between floors";
    private static final String DOOM_LOGOUT_STATUS_HEADER_TEXT = "Current Logout Setting:";
    private static final String DOOM_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT = "Left-Click";
    private static final String DOOM_INSTRUCTION_TEXT = "Navigate to the 'Doom Safeguards' section in the 'Escape Crystal Notify' settings to enable left-click logout prevention and/or filter these messages";

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
        if (!plugin.isLeviathanSafeguardPanelEnabled() && !plugin.isDoomSafeguardPanelEnabled()) return null;

        String bugInfoHeaderText = null;
        String bugInfoText = null;
        String logoutStatusHeaderText = null;
        String logoutStatusText = null;
        Color logoutStatusTextColor = null;
        String sixHourWarningText = null;
        String instructionText = null;

        if (plugin.isLeviathanSafeguardPanelEnabled()) {
            if (!config.hideLeviathanBugInfoText()) {
                bugInfoHeaderText = LEVIATHAN_BUG_INFO_HEADER_TEXT;
                bugInfoText = LEVIATHAN_BUG_INFO_TEXT;
            }

            if (!config.hideLeviathanLogoutSettingText()) {
                logoutStatusHeaderText = LEVIATHAN_LOGOUT_STATUS_HEADER_TEXT;

                if (config.deprioritizeLeviathanLogout()) {
                    logoutStatusText = LEVIATHAN_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT;
                    logoutStatusTextColor = Color.GREEN;
                } else {
                    logoutStatusText = LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT;
                    logoutStatusTextColor = JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND;
                }
            };

            if (config.warnLeviathanLogoutTimer()) sixHourWarningText = SIX_HOUR_WARNING_TEXT;
            if (!config.hideLeviathanSettingsInstructionText()) instructionText = LEVIATHAN_INSTRUCTION_TEXT;
        } else if (plugin.isDoomSafeguardPanelEnabled()) {
            if (!config.hideDoomBugInfoText()) {
                bugInfoHeaderText = DOOM_BUG_INFO_HEADER_TEXT;
                bugInfoText = DOOM_BUG_INFO_TEXT;
            }

            if (!config.hideDoomLogoutSettingText()) {
                logoutStatusHeaderText = DOOM_LOGOUT_STATUS_HEADER_TEXT;

                if (config.deprioritizeDoomLogout()) {
                    logoutStatusText = DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT;
                    logoutStatusTextColor = Color.GREEN;
                } else {
                    logoutStatusText = DOOM_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT;
                    logoutStatusTextColor = JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND;
                }
            };

            if (config.warnDoomLogoutTimer()) sixHourWarningText = SIX_HOUR_WARNING_TEXT;
            if (!config.hideDoomSettingsInstructionText()) instructionText = DOOM_INSTRUCTION_TEXT;
        } else {
            return null;
        }

        final FontMetrics metrics = graphics.getFontMetrics(OVERLAY_PANEL_FONT);

        LineComponent newLineComponent = LineComponent.builder().left("\n").build();

        panelComponent.setPreferredSize(new Dimension(OVERLAY_PANEL_WIDTH, OVERLAY_PANEL_HEIGHT));
        panelComponent.getChildren().clear();

        if (bugInfoText != null) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(getCenteredText(bugInfoHeaderText, metrics))
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(bugInfoText)
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());
        }

        if (logoutStatusHeaderText != null) {
            if (!panelComponent.getChildren().isEmpty()) {
                panelComponent.getChildren().add(newLineComponent);
            }

            LineComponent.LineComponentBuilder logoutStatusComponent = LineComponent.builder()
                    .left(logoutStatusHeaderText)
                    .leftColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT)
                    .leftFont(OVERLAY_PANEL_FONT);

            logoutStatusComponent
                    .right(logoutStatusText)
                    .rightColor(logoutStatusTextColor)
                    .rightFont(OVERLAY_PANEL_FONT);

            panelComponent.getChildren().add(logoutStatusComponent.build());

            if (plugin.isDoomSafeguardPanelEnabled() && config.deprioritizeDoomLogout()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_ADDITIONAL_INFO)
                        .leftFont(OVERLAY_PANEL_FONT)
                        .leftColor(Color.GREEN)
                        .build());
            }
        }

        if (sixHourWarningText != null && plugin.isCloseToSixHourLogout()) {
            if (!panelComponent.getChildren().isEmpty()) {
                panelComponent.getChildren().add(newLineComponent);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(SIX_HOUR_WARNING_TEXT)
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND)
                    .build());
        }

        if (instructionText != null) {
            if (!panelComponent.getChildren().isEmpty()) {
                panelComponent.getChildren().add(newLineComponent);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(instructionText)
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
