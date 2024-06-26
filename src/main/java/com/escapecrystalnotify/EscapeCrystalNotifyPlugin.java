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
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.awt.*;
import java.awt.image.BufferedImage;
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
	private static final int ITEMS_STORED_VARBIT = 14283;
	private static final int STANDARD_HARDCORE_ACCOUNT_TYPE_VARBIT_VALUE = 3;
	private static final int GROUP_HARDCORE_ACCOUNT_TYPE_VARBIT_VALUE = 5;

	@Inject
	private Notifier notifier;
	@Inject
	private Client client;
	@Inject
	private ConfigManager configManager;

	@Inject
	private EscapeCrystalNotifyConfig config;

	@Inject
	private EscapeCrystalNotifyOverlayActive escapeCrystalNotifyOverlayActive;

	@Inject
	private EscapeCrystalNotifyOverlayInactive escapeCrystalNotifyOverlayInactive;

	@Inject
	private EscapeCrystalNotifyInventoryOverlay escapeCrystalNotifyInventoryOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	private boolean notifyMissing = false;
	private boolean notifyInactive = false;
	private boolean notifyTimeRemainingThreshold = false;
	private boolean notifyNonLeftClickTeleport = false;
	private String notifyTimeRemainingThresholdMessage;
	private EscapeCrystalNotifyAccountType accountType = EscapeCrystalNotifyAccountType.NON_HARDCORE;
	private boolean hardcoreAccountType = false;
	private boolean escapeCrystalWithPlayer = true;
	private boolean escapeCrystalActive = true;
	private boolean escapeCrystalRingOfLifeActive = true;
	private boolean escapeCrystalLeftClickTeleportEnabled = true;
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
	private EscapeCrystalNotifyInfoBox activeInfoBox;

	@Override
	protected void startUp() throws Exception
	{
		this.targetRegionIds = getTargetRegionIdsFromConfig(this.accountType);
		this.notifyTimeRemainingThresholdMessage = generateTimeRemainingThresholdMessage();
		this.timeRemainingThresholdTicks = normalizeTimeRemainingThresholdValue();

		overlayManager.add(escapeCrystalNotifyOverlayActive);
		overlayManager.add(escapeCrystalNotifyOverlayInactive);
		overlayManager.add(escapeCrystalNotifyInventoryOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(escapeCrystalNotifyOverlayActive);
		overlayManager.remove(escapeCrystalNotifyOverlayInactive);
		overlayManager.remove(escapeCrystalNotifyInventoryOverlay);
		removeInfoBoxes();
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		computeAccountTypeMetrics();
		computeLocationMetrics();
		computeEscapeCrystalMetrics();
		computeNotificationMetrics();

		if (this.config.enableInfoBox() && isAccountTypeEnabled()) {
			createInfoBox();
		} else {
			removeInfoBoxes();
		}

		sendRequestedNotifications();
	}

	private EscapeCrystalNotifyAccountType determineAccountType() {
		switch (client.getVarbitValue(Varbits.ACCOUNT_TYPE)) {
			case STANDARD_HARDCORE_ACCOUNT_TYPE_VARBIT_VALUE: return EscapeCrystalNotifyAccountType.STANDARD_HARDCORE;
			case GROUP_HARDCORE_ACCOUNT_TYPE_VARBIT_VALUE: return EscapeCrystalNotifyAccountType.GROUP_HARDCORE;
			default: return EscapeCrystalNotifyAccountType.NON_HARDCORE;
		}
	}

	private void computeAccountTypeMetrics() {
		EscapeCrystalNotifyAccountType previousAccountType = this.accountType;
		this.accountType = determineAccountType();
		this.hardcoreAccountType = this.accountType != EscapeCrystalNotifyAccountType.NON_HARDCORE;

		if (this.accountType != previousAccountType) {
			this.targetRegionIds = getTargetRegionIdsFromConfig(this.accountType);
		}
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
		if (client.getVarbitValue(ITEMS_STORED_VARBIT) == 0) {
			this.escapeCrystalWithPlayer = checkEscapeCrystalWithPlayer();
		}

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

		this.escapeCrystalLeftClickTeleportEnabled = configManager.getConfiguration("menuentryswapper", "item_" + ItemID.ESCAPE_CRYSTAL) == null;
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

		if (this.inTimeRemainingThreshold && this.enteredTimeRemainingThreshold && this.escapeCrystalActive && this.escapeCrystalWithPlayer && this.atNotifyRegionId) {
			this.notifyTimeRemainingThreshold = true;
		}

		if (!this.escapeCrystalLeftClickTeleportEnabled && this.enteredNotifyRegionId && this.escapeCrystalWithPlayer) {
			this.notifyNonLeftClickTeleport = true;
		}
	}

	private void resetNotificationFlags() {
		this.notifyMissing = false;
		this.notifyInactive = false;
		this.notifyTimeRemainingThreshold = false;
		this.notifyNonLeftClickTeleport = false;
	}

	private String generateTimeRemainingThresholdMessage() {
		return String.format("Your escape crystal will teleport you in %s %s!", config.notifyTimeUntilTeleportThreshold(), config.notificationInactivityTimeFormat().toString().toLowerCase());
	}

	private int normalizeTimeRemainingThresholdValue() {
		switch (config.onScreenWidgetInactivityTimeFormat()) {
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
		this.targetRegionIds = getTargetRegionIdsFromConfig(this.accountType);
		this.notifyTimeRemainingThresholdMessage = generateTimeRemainingThresholdMessage();
		this.timeRemainingThresholdTicks = normalizeTimeRemainingThresholdValue();
	}

	private List<Integer> getTargetRegionIdsFromConfig(EscapeCrystalNotifyAccountType accountType) {
		ArrayList<EscapeCrystalNotifyRegionType> targetRegions = new ArrayList<>();

		if (config.displayBosses()) targetRegions.add(EscapeCrystalNotifyRegionType.BOSSES);
		if (config.displayRaids()) targetRegions.add(EscapeCrystalNotifyRegionType.RAIDS);
		if (config.displayDungeons()) targetRegions.add(EscapeCrystalNotifyRegionType.DUNGEONS);
		if (config.displayMinigames()) targetRegions.add(EscapeCrystalNotifyRegionType.MINIGAMES);

		List<Integer> regionIds = EscapeCrystalNotifyRegion.getRegionIdsFromTypes(targetRegions, getTargetDeathTypes(accountType));
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

	private List<EscapeCrystalNotifyRegionDeathType> getTargetDeathTypes(EscapeCrystalNotifyAccountType accountType) {
		switch (accountType) {
            case GROUP_HARDCORE: return Arrays.asList(EscapeCrystalNotifyRegionDeathType.UNSAFE, EscapeCrystalNotifyRegionDeathType.UNSAFE_HCGIM);
			default: return List.of(EscapeCrystalNotifyRegionDeathType.UNSAFE);
        }
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

		if (config.notifyNonLeftClickTeleport() && this.notifyNonLeftClickTeleport) {
			notifier.notify("Your escape crystal is not set to left click teleport!");
			this.notifyNonLeftClickTeleport = false;
		}
	}

	private void createInfoBox() {
		List<InfoBox> currentInfoBoxes = infoBoxManager.getInfoBoxes();

		if (this.isEscapeCrystalActive() && this.escapeCrystalWithPlayer){
			if (currentInfoBoxes.contains(this.activeInfoBox)) {
				if (this.activeInfoBox.imageId != ItemID.ESCAPE_CRYSTAL) {
					this.activeInfoBox.setImage(itemManager.getImage(ItemID.ESCAPE_CRYSTAL));
					this.activeInfoBox.imageId = ItemID.ESCAPE_CRYSTAL;
					this.activeInfoBox.setTooltip(getInfoBoxTooltip());
					infoBoxManager.updateInfoBoxImage(this.activeInfoBox);
				}
				return;
			}

			BufferedImage activeImage = itemManager.getImage(ItemID.ESCAPE_CRYSTAL);
			this.activeInfoBox = new EscapeCrystalNotifyInfoBox(ItemID.ESCAPE_CRYSTAL, activeImage, this, this.config);
			this.activeInfoBox.setTooltip(getInfoBoxTooltip());
			infoBoxManager.addInfoBox(this.activeInfoBox);

		} else {
			if (currentInfoBoxes.contains(this.activeInfoBox)) {
				if (this.activeInfoBox.imageId != ItemID.CORRUPTED_ESCAPE_CRYSTAL) {
					this.activeInfoBox.setImage(itemManager.getImage(ItemID.CORRUPTED_ESCAPE_CRYSTAL));
					this.activeInfoBox.imageId = ItemID.CORRUPTED_ESCAPE_CRYSTAL;
					this.activeInfoBox.setTooltip(getInfoBoxTooltip());
					infoBoxManager.updateInfoBoxImage(this.activeInfoBox);
				}
				return;
			}

			BufferedImage inactiveImage = itemManager.getImage(ItemID.CORRUPTED_ESCAPE_CRYSTAL);
			this.activeInfoBox = new EscapeCrystalNotifyInfoBox(ItemID.CORRUPTED_ESCAPE_CRYSTAL, inactiveImage, this, this.config);
			this.activeInfoBox.setTooltip(getInfoBoxTooltip());
			infoBoxManager.addInfoBox(this.activeInfoBox);
		}
	}

	private void removeInfoBoxes() {
		infoBoxManager.removeIf(b -> b instanceof EscapeCrystalNotifyInfoBox);
	}

	public String getItemModelDisplayText(EscapeCrystalNotifyConfig.OverlayDisplayType displayFormat, EscapeCrystalNotifyConfig.InactivityTimeFormat timeFormat) {
		String displayText;

		if (!escapeCrystalWithPlayer) {
			displayText = "Missing";
		}
		else if (displayFormat == EscapeCrystalNotifyConfig.OverlayDisplayType.REMAINING_TIME) {
			if (!isEscapeCrystalActive()) {
				displayText = getItemModelCurrentSettingDisplayText(timeFormat);
			}
			else if (isTimeExpired()) {
				displayText = "Tele";
			}
			else if (timeFormat == EscapeCrystalNotifyConfig.InactivityTimeFormat.SECONDS) {
				displayText = this.getExpectedSecondsUntilTeleport() + "s";
			} else {
				displayText = Integer.toString(this.getExpectedTicksUntilTeleport());
			}
		} else if (displayFormat == EscapeCrystalNotifyConfig.OverlayDisplayType.CURRENT_SETTING) {
			displayText = getItemModelCurrentSettingDisplayText(timeFormat);
		} else {
			displayText = "";
		}

		return displayText;
	}

	public String getItemModelCurrentSettingDisplayText(EscapeCrystalNotifyConfig.InactivityTimeFormat timeFormat) {
		switch (timeFormat) {
			case GAME_TICKS: return this.getEscapeCrystalInactivityTicks() + "t";
			case SECONDS: return this.getEscapeCrystalInactivitySeconds() + "s";
		}

		return "";
	}

	public String getInfoBoxTooltip() {
		if (!this.escapeCrystalWithPlayer) {
			return "Status: MISSING";
		}
		else if (this.isEscapeCrystalActive()) {
			return "Status: ACTIVE (set to " + this.getEscapeCrystalInactivitySeconds() + " seconds / " + this.getEscapeCrystalInactivityTicks() + " ticks)";
		}
		else {
			return "Status: DISABLED (set to " + this.getEscapeCrystalInactivitySeconds() + " seconds / " + this.getEscapeCrystalInactivityTicks() + " ticks)";
		}
	}

	public Color getItemModelDisplayTextColor(EscapeCrystalNotifyConfig.OverlayDisplayType displayFormat) {
		if (!escapeCrystalWithPlayer) {
			return Color.RED;
		}
		else if (displayFormat == EscapeCrystalNotifyConfig.OverlayDisplayType.REMAINING_TIME) {
			if (isTimeExpired() && isEscapeCrystalActive()) {
				return Color.RED;
			}
		}

		return Color.WHITE;
	}

	public boolean isHardcoreAccountType() {
		return hardcoreAccountType;
	}

	public boolean isAccountTypeEnabled() {
		if (config.requireHardcoreAccountType()) {
			return isHardcoreAccountType();
		}
		return true;
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

	public boolean isEscapeCrystalLeftClickTeleportEnabled() {
		return escapeCrystalLeftClickTeleportEnabled;
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

	public boolean isTimeExpired() {
		return expectedTicksUntilTeleport == 0;
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
