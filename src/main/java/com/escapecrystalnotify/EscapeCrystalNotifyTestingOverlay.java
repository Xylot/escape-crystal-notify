package com.escapecrystalnotify;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;


@Slf4j
public class EscapeCrystalNotifyTestingOverlay extends OverlayPanel {
    private static final Font OVERLAY_PANEL_FONT = FontManager.getRunescapeSmallFont();
    private static final Font OVERLAY_PANEL_HEADER_FONT = FontManager.getRunescapeFont();
    private static final int OVERLAY_PANEL_WIDTH = 400;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;

    @Inject
    public EscapeCrystalNotifyTestingOverlay(EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) throws PluginInstantiationException {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enableDebugMode()) {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(OVERLAY_PANEL_WIDTH, 50));

        int totalHeight = 0;

        if (config.showPlayerLocationSection()) {
            totalHeight += renderPlayerLocationSection();
        }

        if (config.showEscapeCrystalSection()) {
            totalHeight += renderEscapeCrystalSection();
        }

        if (config.showAccountInfoSection()) {
            totalHeight += renderAccountInfoSection();
        }

        if (config.showPossibleEntrancesSection()) {
            totalHeight += renderPossibleEntrancesSection();
        }

        if (config.showValidEntrancesSection()) {
            totalHeight += renderValidEntrancesSection();
        }

        if (config.showSpecialRegionSection()) {
            totalHeight += renderSpecialRegionSection();
        }

        if (config.showNotificationSection()) {
            totalHeight += renderNotificationSection();
        }

        panelComponent.setPreferredSize(new Dimension(OVERLAY_PANEL_WIDTH, totalHeight + 20));

        return panelComponent.render(graphics);
    }

    private int renderPlayerLocationSection() {
        int height = 0;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("PLAYER LOCATION")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        WorldPoint worldPoint = plugin.getCurrentWorldPoint();
        String worldPointText = worldPoint != null ? 
            String.format("(%d, %d, %d)", worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane()) : "null";
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("World Point:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(worldPointText)
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Region ID:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getCurrentRegionId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Plane:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getCurrentPlaneId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Chunk ID:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getCurrentChunkId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Notify Region:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtNotifyRegionId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtNotifyRegionId() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Notify Entrance:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtNotifyRegionEntrance()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtNotifyRegionEntrance() ? Color.GREEN : Color.RED)
                .build());
        height += 20; 

        return height;
    }

    private int renderEscapeCrystalSection() {
        int height = 0;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("ESCAPE CRYSTAL STATUS")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("With Player:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isEscapeCrystalWithPlayer()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isEscapeCrystalWithPlayer() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Active:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isEscapeCrystalActive()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isEscapeCrystalActive() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Ring of Life:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isEscapeCrystalRingOfLifeActive()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isEscapeCrystalRingOfLifeActive() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Left Click Teleport:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isEscapeCrystalLeftClickTeleportEnabled()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isEscapeCrystalLeftClickTeleportEnabled() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Inactivity Ticks:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getEscapeCrystalInactivityTicks()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Expected Ticks Until Teleport:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getExpectedTicksUntilTeleport()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.getExpectedTicksUntilTeleport() <= 0 ? Color.RED : Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Client Inactivity Ticks:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getClientInactivityTicks()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Expected Server Inactivity:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getExpectedServerInactivityTicks()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 20; 

        return height;
    }

    private int renderAccountInfoSection() {
        int height = 0;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("ACCOUNT INFORMATION")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        String accountTypeText = plugin.getAccountType().toString();
        boolean isOverridden = config.testingAccountTypeOverride() != EscapeCrystalNotifyConfig.TestingAccountType.DEFAULT;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Account Type:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(accountTypeText + (isOverridden ? " (OVERRIDDEN)" : ""))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(isOverridden ? Color.ORANGE : Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Hardcore:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isHardcoreAccountType()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isHardcoreAccountType() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Ticks Since Login:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getTicksSinceLogin()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 15;

        String loginTimeText = plugin.getLoginTime() != null ? 
            LocalDateTime.ofInstant(plugin.getLoginTime(), ZoneId.systemDefault()).format(TIME_FORMATTER) : "null";
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Login Time:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(loginTimeText)
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 20; 

        return height;
    }

    private int renderPossibleEntrancesSection() {
        int height = 0;
        
        Map<Integer, List<EscapeCrystalNotifyLocatedEntrance>> possibleEntrances = plugin.getPossibleEntrances();
        int totalCount = possibleEntrances.values().stream().mapToInt(List::size).sum();
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("POSSIBLE ENTRANCES (" + totalCount + ")")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        if (possibleEntrances.isEmpty()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("None")
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(Color.WHITE)
                    .build());
            height += 15;
        } else {
            for (Map.Entry<Integer, List<EscapeCrystalNotifyLocatedEntrance>> entry : possibleEntrances.entrySet()) {
                int regionId = entry.getKey();
                List<EscapeCrystalNotifyLocatedEntrance> entrances = entry.getValue();
                
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Region " + regionId + " (" + entrances.size() + " entrances):")
                        .leftFont(OVERLAY_PANEL_FONT)
                        .leftColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT)
                        .build());
                height += 15;
                
                for (EscapeCrystalNotifyLocatedEntrance entrance : entrances) {
                    try {
                        String entranceInfo = entrance.getTarget().getId() + 
                                            " at " + formatWorldPoint(entrance.getTarget().getWorldLocation()) +
                                            " (Valid: " + entrance.isEntranceInValidChunk() +
                                            ", Moved: " + entrance.hasMoved() +
                                            ", Past: " + entrance.isPlayerPastEntrance(plugin.getCurrentWorldPoint()) +
                                            ", Plane Match: " + entrance.matchesPlayerPlane(plugin.getCurrentPlaneId()) + ")";
                        
                        panelComponent.getChildren().add(LineComponent.builder()
                            .left("  - " + entranceInfo)
                            .leftFont(OVERLAY_PANEL_FONT)
                            .leftColor(Color.WHITE)
                            .build());
                        height += 15;
                    } catch (Exception ignored) {
                        log.debug("Target={}, Definition={}, Initial World Point={}, Initial Target Id={}", entrance.getTarget(), entrance.getDefinition(), entrance.getInitialWorldPoint(), entrance.getInitialTargetId());
                    }
                }
            }
        }
        height += 20;

        return height;
    }

    private int renderValidEntrancesSection() {
        int height = 0;
        
        List<EscapeCrystalNotifyLocatedEntrance> validEntrances = plugin.getValidEntrances();
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("VALID ENTRANCES (" + validEntrances.size() + ")")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        if (validEntrances.isEmpty()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("None")
                    .leftFont(OVERLAY_PANEL_FONT)
                    .leftColor(Color.WHITE)
                    .build());
            height += 15;
        } else {
            for (EscapeCrystalNotifyLocatedEntrance entrance : validEntrances) {
                try {
                    String validInfo = entrance.getTarget().getId() + 
                                    " at " + formatWorldPoint(entrance.getTarget().getWorldLocation()) +
                                    " (Can Highlight: " + entrance.canHighlight() +
                                    ", Can Deprioritize: " + entrance.canDeprioritize() +
                                    ", Prioritized: " + entrance.isPrioritized() + ")";
                    
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("- " + validInfo)
                            .leftFont(OVERLAY_PANEL_FONT)
                            .leftColor(Color.WHITE)
                            .build());
                    height += 15;
                } catch (Exception ignored) {
                    log.debug("Target={}, Definition={}, Initial World Point={}, Initial Target Id={}", entrance.getTarget(), entrance.getDefinition(), entrance.getInitialWorldPoint(), entrance.getInitialTargetId());
                }
            }
        }
        height += 20; 

        return height;
    }

    private int renderSpecialRegionSection() {
        int height = 0;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("SPECIAL REGION STATUS")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Leviathan Region:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtLeviathanRegionId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtLeviathanRegionId() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Leviathan Lobby:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtLeviathanLobby()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtLeviathanLobby() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Doom Region:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtDoomRegionId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtDoomRegionId() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Doom Lobby:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtDoomLobby()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtDoomLobby() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Doom Floor Cleared:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isDoomFloorCleared()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isDoomFloorCleared() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("At Safe Region:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isAtSafeRegionId()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isAtSafeRegionId() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Western Elite Diary Done:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isCompletedWesternEliteDiary()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isCompletedWesternEliteDiary() ? Color.GREEN : Color.RED)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Zulrah Revive Active:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(!plugin.hasDiedAtZulrah()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.hasDiedAtZulrah() ? Color.RED : Color.GREEN)
                .build());
        height += 20;

        return height;
    }

    private int renderNotificationSection() {
        int height = 0;
        
        panelComponent.getChildren().add(LineComponent.builder()
                .left("NOTIFICATION STATUS")
                .leftFont(OVERLAY_PANEL_HEADER_FONT)
                .leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
                .build());
        height += 20;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("In Time Remaining Threshold:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.isInTimeRemainingThreshold()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(plugin.isInTimeRemainingThreshold() ? Color.RED : Color.WHITE)
                .build());
        height += 15;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time Remaining Threshold Ticks:")
                .leftFont(OVERLAY_PANEL_FONT)
                .leftColor(Color.WHITE)
                .right(String.valueOf(plugin.getTimeRemainingThresholdTicks()))
                .rightFont(OVERLAY_PANEL_FONT)
                .rightColor(Color.WHITE)
                .build());
        height += 20; 

        return height;
    }

    private String formatWorldPoint(WorldPoint worldPoint) {
        if (worldPoint == null) return "null";
        return String.format("(region=%d, x=%d, y=%d, plane=%d)", worldPoint.getRegionID(), worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }
}
