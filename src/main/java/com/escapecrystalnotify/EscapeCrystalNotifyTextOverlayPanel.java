package com.escapecrystalnotify;

import net.runelite.api.MenuAction;
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
    static final String CONFIRM_FIX_OPTION = "Confirm fix";
    static final String LEVIATHAN_FIX_TARGET = "Leviathan logout bug";
    static final String DOOM_FIX_TARGET = "Doom logout bug";
    private static final String FIX_INSTRUCTION_TEXT = "Right-click this panel and select 'Confirm fix' to hide this notice. Enable 'Display Fix Info' in the safeguard settings to show it again.";
    private static final String LEVIATHAN_FIX_INFO_TEXT = "The Leviathan logout bug has been fixed. Safeguards are now disabled by default and remain available as an optional setting.";
    private static final String DOOM_FIX_INFO_TEXT = "The Doom of Mokhaiotl logout bug has been fixed. Safeguards are now disabled by default and remain available as an optional setting.";
    private static final Font OVERLAY_PANEL_FONT = FontManager.getRunescapeSmallFont();
    private static final int OVERLAY_PANEL_WIDTH = 235;
    private static final int OVERLAY_PANEL_HEIGHT = 100;
    private static final String SIX_HOUR_WARNING_TEXT = "WARNING: You are approaching the 6-hour forced logout timer! You should re-log to avoid a potential forced death";
    private static final String LEVIATHAN_BUG_INFO_HEADER_TEXT = "WARNING: LEVIATHAN BUG";
    private static final String LEVIATHAN_BUG_INFO_TEXT = "Attempting to logout during the encounter can/will DISABLE ALL PLAYER ACTIONS (teleports, prayers, food, etc...) and cause an UNAVOIDABLE DEATH";
    private static final String LEVIATHAN_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT = "Right-Click Required";
    private static final String LEVIATHAN_LOGOUT_STATUS_HEADER_TEXT = "Current Logout Setting:";
    private static final String LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT = "Left-Click";
    private static final String LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT_ADDITIONAL_INFO = "NOT PROTECTED";
    private static final String LEVIATHAN_INSTRUCTION_TEXT = "Navigate to the 'Leviathan Safeguards' section in the 'Escape Crystal Notify' settings to enable left-click logout prevention and/or hide this panel";
    private static final String DOOM_BUG_INFO_HEADER_TEXT = "WARNING: DOOM BUG";
    private static final String DOOM_BUG_INFO_TEXT = "Attempting to logout during the encounter can/will DISABLE ALL PLAYER ACTIONS (teleports, prayers, food, etc...) and cause an UNAVOIDABLE DEATH";
    private static final String DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT = "Right-Click Required";
    private static final String DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_ADDITIONAL_INFO = "Left-Click enabled between floors";
    private static final String DOOM_LOGOUT_STATUS_HEADER_TEXT = "Current Logout Setting:";
    private static final String DOOM_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT = "Left-Click";
    private static final String DOOM_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT_ADDITIONAL_INFO = "NOT PROTECTED";
    private static final String DOOM_INSTRUCTION_TEXT = "Navigate to the 'Doom Safeguards' section in the 'Escape Crystal Notify' settings to enable left-click logout prevention and/or hide this panel";

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
        getMenuEntries().clear();
        panelComponent.getChildren().clear();
        if (!plugin.isLeviathanSafeguardPanelEnabled() && !plugin.isDoomSafeguardPanelEnabled()) return null;

        String bugInfoHeaderText = null;
        String bugInfoText = null;
        String logoutStatusHeaderText = null;
        String logoutStatusText = null;
        String logoutStatusAdditionalText = null;
        Color logoutStatusTextColor = null;
        String sixHourWarningText = null;
        String instructionText = null;
        String fixInfoText = null;

        if (plugin.isLeviathanSafeguardPanelEnabled()) {
            boolean modeEnabled = config.leviathanLogoutSafeguardMode() != EscapeCrystalNotifyConfig.SafeguardAccountType.DISABLED;
            if (modeEnabled && config.displayLeviathanBugInfo()) {
                bugInfoHeaderText = LEVIATHAN_BUG_INFO_HEADER_TEXT;
                bugInfoText = LEVIATHAN_BUG_INFO_TEXT;
                instructionText = LEVIATHAN_INSTRUCTION_TEXT;
            }

            if (config.displayLeviathanFixInfo()) {
                fixInfoText = LEVIATHAN_FIX_INFO_TEXT;
                if (bugInfoHeaderText == null) bugInfoHeaderText = "LEVIATHAN BUG FIXED";
                instructionText = FIX_INSTRUCTION_TEXT;
                addMenuEntry(MenuAction.RUNELITE_OVERLAY, CONFIRM_FIX_OPTION, LEVIATHAN_FIX_TARGET);
            }

            if (modeEnabled && config.displayLeviathanLogoutSetting()) {
                logoutStatusHeaderText = LEVIATHAN_LOGOUT_STATUS_HEADER_TEXT;

                if (plugin.isLeviathanSafeguardEnabled()) {
                    logoutStatusText = LEVIATHAN_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT;
                    logoutStatusTextColor = Color.GREEN;
                } else {
                    logoutStatusText = LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT;
                    logoutStatusAdditionalText = LEVIATHAN_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT_ADDITIONAL_INFO;
                    logoutStatusTextColor = JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND;
                }
            };

            if (plugin.isLeviathanSafeguardEnabled() && config.displayLeviathanLogoutSetting()) sixHourWarningText = SIX_HOUR_WARNING_TEXT;
        } else if (plugin.isDoomSafeguardPanelEnabled()) {
            boolean modeEnabled = config.doomLogoutSafeguardMode() != EscapeCrystalNotifyConfig.SafeguardAccountType.DISABLED;
            if (modeEnabled && config.displayDoomBugInfo()) {
                bugInfoHeaderText = DOOM_BUG_INFO_HEADER_TEXT;
                bugInfoText = DOOM_BUG_INFO_TEXT;
                instructionText = DOOM_INSTRUCTION_TEXT;
            }

            if (config.displayDoomFixInfo()) {
                fixInfoText = DOOM_FIX_INFO_TEXT;
                if (bugInfoHeaderText == null) bugInfoHeaderText = "DOOM BUG FIXED";
                instructionText = FIX_INSTRUCTION_TEXT;
                addMenuEntry(MenuAction.RUNELITE_OVERLAY, CONFIRM_FIX_OPTION, DOOM_FIX_TARGET);
            }

            if (modeEnabled && config.displayDoomLogoutSetting()) {
                logoutStatusHeaderText = DOOM_LOGOUT_STATUS_HEADER_TEXT;

                if (plugin.isDoomSafeguardEnabled()) {
                    logoutStatusText = DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_SHORT;
                    logoutStatusAdditionalText = DOOM_LOGOUT_STATUS_DEPRIORITIZED_TEXT_ADDITIONAL_INFO;
                    logoutStatusTextColor = Color.GREEN;
                } else {
                    logoutStatusText = DOOM_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT;
                    logoutStatusAdditionalText = DOOM_LOGOUT_STATUS_PRIORITIZED_TEXT_SHORT_ADDITIONAL_INFO;
                    logoutStatusTextColor = JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND;
                }
            }

            if (plugin.isDoomSafeguardEnabled() && config.displayDoomLogoutSetting()) sixHourWarningText = SIX_HOUR_WARNING_TEXT;
        } else {
            return null;
        }

        final FontMetrics metrics = graphics.getFontMetrics(OVERLAY_PANEL_FONT);

        LineComponent newLineComponent = LineComponent.builder().left("\n").build();

        panelComponent.setPreferredSize(new Dimension(OVERLAY_PANEL_WIDTH, OVERLAY_PANEL_HEIGHT));
        panelComponent.getChildren().clear();

        if (bugInfoHeaderText != null) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(getCenteredText(bugInfoHeaderText, metrics))
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());
        }

        if (bugInfoText != null) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(bugInfoText)
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                    .build());
        }

        if (fixInfoText != null) {
            if (bugInfoText != null) panelComponent.getChildren().add(newLineComponent);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(fixInfoText)
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

            if (logoutStatusAdditionalText != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .right(logoutStatusAdditionalText)
                        .rightFont(OVERLAY_PANEL_FONT)
                        .rightColor(logoutStatusTextColor)
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
