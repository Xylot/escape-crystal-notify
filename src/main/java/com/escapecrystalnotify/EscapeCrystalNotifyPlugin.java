package com.escapecrystalnotify;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Escape Crystal Notify"
)
public class EscapeCrystalNotifyPlugin extends Plugin
{
	private static final int ESCAPE_CRYSTAL_ACTIVE_VARBIT = 14838;
	private static final int ESCAPE_CRYSTAL_INACTIVITY_TICKS_VARBIT = 14849;
	private static final int ESCAPE_CRYSTAL_RING_OF_LIFE_ACTIVE_VARBIT = 14857;
	private static final List<Integer> HARDCORE_ACCOUNT_TYPE_VARBIT_VALUES = Arrays.asList(3, 5);

	@Inject
	private Notifier notifier;
	@Inject
	private Client client;

	@Inject
	private EscapeCrystalNotifyConfig config;

	@Inject private EscapeCrystalNotifyOverlayActive escapeCrystalNotifyOverlayActive;
	@Inject private EscapeCrystalNotifyOverlayInactive escapeCrystalNotifyOverlayInactive;

	@Inject private OverlayManager overlayManager;

	private boolean notifyMissing = false;
	private boolean notifyInactive = false;
	private boolean notifyTimeRemainingThreshold = false;
	private String notifyTimeRemainingThresholdMessage;
	private boolean hardcoreAccountType;
	private boolean escapeCrystalWithPlayer = true;
	private boolean escapeCrystalActive = true;
	private boolean escapeCrystalRingOfLifeActive = true;
	private int escapeCrystalInactivityTicks;
	private int clientInactivityTicks;
	private int expectedServerInactivityTicks = 0;
	private int expectedTicksUntilTeleport;
	private int currentRegionId;
	private int previousRegionId;
	private boolean enteredNotifyRegionId = false;
	private boolean atNotifyRegionId = false;
	private boolean previouslyAtNotifyRegionId = false;
	private int timeRemainingThresholdTicks;
	private boolean inTimeRemainingThreshold = false;
	private boolean previouslyInTimeRemainingThreshold = false;
	private boolean enteredTimeRemainingThreshold = false;
	private List<Integer> targetRegionIds;

	@Override
	protected void startUp() throws Exception
	{
		this.targetRegionIds = getTargetRegionIdsFromConfig();
		this.notifyTimeRemainingThresholdMessage = generateTimeRemainingThresholdMessage();
		this.timeRemainingThresholdTicks = normalizeTimeRemainingThresholdValue();

		overlayManager.add(escapeCrystalNotifyOverlayActive);
		overlayManager.add(escapeCrystalNotifyOverlayInactive);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(escapeCrystalNotifyOverlayActive);
		overlayManager.remove(escapeCrystalNotifyOverlayInactive);
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		this.hardcoreAccountType = HARDCORE_ACCOUNT_TYPE_VARBIT_VALUES.contains(client.getVarbitValue(Varbits.ACCOUNT_TYPE));

		computeLocationMetrics();
		computeEscapeCrystalMetrics();
		computeNotificationMetrics();

		sendRequestedNotifications();
	}

	private void computeLocationMetrics() {
		this.previousRegionId = this.currentRegionId;
		this.previouslyAtNotifyRegionId = this.atNotifyRegionId;
		this.currentRegionId = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		this.atNotifyRegionId = config.displayEverywhere() || this.targetRegionIds.contains(this.currentRegionId);
		this.enteredNotifyRegionId = !this.previouslyAtNotifyRegionId && this.atNotifyRegionId;
	}

	private boolean checkEscapeCrystalWithPlayer() {
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (equipmentContainer == null && inventoryContainer == null) {
			return false;
		}

		boolean escapeCrystalEquipped = equipmentContainer != null && equipmentContainer.contains(ItemID.ESCAPE_CRYSTAL);
		boolean escapeCrystalInInventory = inventoryContainer != null && inventoryContainer.contains(ItemID.ESCAPE_CRYSTAL);

        return escapeCrystalEquipped || escapeCrystalInInventory;
    }

	private void computeEscapeCrystalMetrics() {
		this.escapeCrystalWithPlayer = checkEscapeCrystalWithPlayer();
		this.escapeCrystalActive = client.getVarbitValue(ESCAPE_CRYSTAL_ACTIVE_VARBIT) == 1;
		this.escapeCrystalInactivityTicks = client.getVarbitValue(ESCAPE_CRYSTAL_INACTIVITY_TICKS_VARBIT);
		this.escapeCrystalRingOfLifeActive = client.getVarbitValue(ESCAPE_CRYSTAL_RING_OF_LIFE_ACTIVE_VARBIT) == 1;

		int currentClientInactivityTicks = Math.min(client.getKeyboardIdleTicks(), client.getMouseIdleTicks());

		if (currentClientInactivityTicks > this.clientInactivityTicks) {
			this.expectedServerInactivityTicks += 1;
		}
		else {
			this.expectedServerInactivityTicks = 0;
			this.notifyTimeRemainingThreshold = false;
		}

		this.clientInactivityTicks = currentClientInactivityTicks;
		this.expectedTicksUntilTeleport = this.escapeCrystalInactivityTicks - this.expectedServerInactivityTicks;

		if (this.expectedTicksUntilTeleport < 0) {
			this.expectedTicksUntilTeleport = 0;
		}
	}

	private void computeNotificationMetrics() {
		if (!this.atNotifyRegionId || (config.requireHardcoreAccountType() && !this.hardcoreAccountType)) {
			resetNotificationFlags();
			return;
		}

		if (!this.escapeCrystalWithPlayer && this.enteredNotifyRegionId) {
			this.notifyMissing = true;
		}

		if (!this.escapeCrystalActive && this.enteredNotifyRegionId) {
			this.notifyInactive = true;
		}

		this.previouslyInTimeRemainingThreshold = this.inTimeRemainingThreshold;
		this.inTimeRemainingThreshold = this.expectedTicksUntilTeleport <= this.timeRemainingThresholdTicks;
		this.enteredTimeRemainingThreshold = !this.previouslyInTimeRemainingThreshold && this.inTimeRemainingThreshold;

		if (this.inTimeRemainingThreshold && this.enteredTimeRemainingThreshold) {
			this.notifyTimeRemainingThreshold = true;
		}
	}

	private void resetNotificationFlags() {
		this.notifyMissing = false;
		this.notifyInactive = false;
		this.notifyTimeRemainingThreshold = false;
	}

	private String generateTimeRemainingThresholdMessage() {
		return String.format("Your escape crystal will teleport you in %s %s!", config.notifyTimeUntilTeleportThreshold(), config.inactivityTimeFormat().toString());
	}

	private int normalizeTimeRemainingThresholdValue() {
		switch (config.inactivityTimeFormat()) {
			case SECONDS:
				return convertSecondsToTicks(config.notifyTimeUntilTeleportThreshold());
            default: return config.notifyTimeUntilTeleportThreshold();
		}
    }

	@Provides
	EscapeCrystalNotifyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EscapeCrystalNotifyConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		this.targetRegionIds = getTargetRegionIdsFromConfig();
		this.notifyTimeRemainingThresholdMessage = generateTimeRemainingThresholdMessage();
		this.timeRemainingThresholdTicks = normalizeTimeRemainingThresholdValue();
	}

	private List<Integer> getTargetRegionIdsFromConfig() {
		ArrayList<EscapeCrystalNotifyRegionType> targetRegions = new ArrayList<>();

		if (config.displayBosses()) targetRegions.add(EscapeCrystalNotifyRegionType.BOSSES);
		if (config.displayRaids()) targetRegions.add(EscapeCrystalNotifyRegionType.RAIDS);
		if (config.displayDungeons()) targetRegions.add(EscapeCrystalNotifyRegionType.DUNGEONS);
		if (config.displayMinigames()) targetRegions.add(EscapeCrystalNotifyRegionType.MINIGAMES);

		List<Integer> regionIds = EscapeCrystalNotifyRegion.getRegionIdsFromTypes(targetRegions);
		List<Integer> includeRegionIds = parseAdditionalConfigRegionIds(config.includeRegionIds());
		List<Integer> excludeRegionIds = parseAdditionalConfigRegionIds(config.excludeRegionIds());

		regionIds.addAll(includeRegionIds);
		regionIds.removeAll(excludeRegionIds);

		return regionIds;
	}

	private List<Integer> parseAdditionalConfigRegionIds(String regionIds) {
		if (regionIds.isEmpty()) return List.of();

		return Arrays.stream(regionIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
	}

	private void sendRequestedNotifications() {
		if (config.notifyMissing() && this.notifyMissing) {
			notifier.notify("You are missing an escape crystal!");
			this.notifyMissing = false;
		} else if (config.notifyInactive() && this.notifyInactive) {
			notifier.notify("Your escape crystal is inactive!");
			this.notifyInactive = false;
		} else if (config.notifyTimeUntilTeleportThreshold() != 0 && this.notifyTimeRemainingThreshold) {
			notifier.notify(this.notifyTimeRemainingThresholdMessage);
			this.notifyTimeRemainingThreshold = false;
		}
	}

	public boolean isHardcoreAccountType() {
		return hardcoreAccountType;
	}

	public boolean isEscapeCrystalActive() {
		return escapeCrystalActive;
	}

	public boolean isEscapeCrystalWithPlayer() {
		return escapeCrystalWithPlayer;
	}

	public boolean isEscapeCrystalInactivityTeleportActive() {
		return escapeCrystalWithPlayer && escapeCrystalActive;
	}

	public boolean isEscapeCrystalRingOfLifeActive() {
		return escapeCrystalRingOfLifeActive;
	}

	public int getEscapeCrystalInactivityTicks() {
		return escapeCrystalInactivityTicks;
	}

	public int getEscapeCrystalInactivitySeconds() {
		return convertTicksToSeconds(escapeCrystalInactivityTicks);
	}

	public int getClientInactivityTicks() {
		return clientInactivityTicks;
	}

	public int getExpectedServerInactivityTicks() {
		return expectedServerInactivityTicks;
	}

	public int getExpectedServerInactivitySeconds() {
		return convertTicksToSeconds(expectedServerInactivityTicks);
	}

	public int getExpectedTicksUntilTeleport() {
		return expectedTicksUntilTeleport;
	}

	public int getExpectedSecondsUntilTeleport() {
		return convertTicksToSeconds(expectedTicksUntilTeleport);
	}

	public int getCurrentRegionId() {
		return currentRegionId;
	}

	public boolean isAtNotifyRegionId() {
		return atNotifyRegionId;
	}

	private int convertTicksToSeconds(int ticks) {
		return (int) Math.round(ticks * 0.6);
	}

	private int convertSecondsToTicks(int seconds) {
		return (int) Math.round(seconds * 1.6);
	}
}
