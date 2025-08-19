package com.escapecrystalnotify;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
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
import java.time.Instant;
import java.util.*;
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
	private static final List<Integer> LEVIATHAN_LOBBY_CHUNK_IDS = List.of(525092, 525093, 527139, 527140, 527141, 529188);
	private static final List<Integer> DOOM_LOBBY_CHUNK_IDS = List.of(335016, 335017, 335018, 337064, 337065, 337066);
	private static final List<Integer> DOOM_BURROW_HOLE_IDS = List.of(ObjectID.BURROW_HOLE, ObjectID.BURROW_HOLE_57285);
	private static final List<Integer> DOOM_NPC_IDS = List.of(NpcID.DOOM_OF_MOKHAIOTL, NpcID.DOOM_OF_MOKHAIOTL_SHIELDED, NpcID.DOOM_OF_MOKHAIOTL_BURROWED);
	private static final int SIX_HOUR_LOG_WARNING_THRESHOLD_TICKS = 34000;

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
	private EscapeCrystalNotifyTextOverlayPanel escapeCrystalNotifyTextOverlayPanel;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Getter
	private Instant loginTime;

	@Getter
	private int ticksSinceLogin;

	private boolean ready;

	private boolean notifyMissing = false;
	private boolean notifyInactive = false;
	private boolean notifyTimeRemainingThreshold = false;
	private boolean notifyNonLeftClickTeleport = false;
	private String notifyTimeRemainingThresholdMessage;
	private EscapeCrystalNotifyAccountType accountType = EscapeCrystalNotifyAccountType.NON_HARDCORE;
	private boolean hardcoreAccountType = false;
	private boolean completedWesternEliteDiary = false;
	private boolean escapeCrystalWithPlayer = true;
	private boolean escapeCrystalActive = true;
	private boolean escapeCrystalRingOfLifeActive = true;
	private boolean escapeCrystalLeftClickTeleportEnabled = true;
	private int escapeCrystalInactivityTicks;
	private int clientInactivityTicks;
	private int expectedServerInactivityTicks = 0;
	private int expectedTicksUntilTeleport;
	private WorldPoint currentWorldPoint;
	private int currentRegionId;
	private int currentPlaneId;
	private int currentChunkId;
	private int previousRegionId;
	private boolean enteredNotifyRegionId = false;
	private boolean atNotifyRegionId = false;
	private boolean previouslyAtNotifyRegionId = false;
	private boolean atLeviathanRegionId = false;
	private boolean atLeviathanLobby = false;
	private boolean atDoomRegionId = false;
	private boolean atDoomLobby = false;
	private boolean doomFloorCleared = false;
	private int timeRemainingThresholdTicks;
	private boolean inTimeRemainingThreshold = false;
	private boolean previouslyInTimeRemainingThreshold = false;
	private boolean enteredTimeRemainingThreshold = false;
	private List<Integer> targetRegionIds;
	private final List<Integer> excludedRegionIds = EscapeCrystalNotifyRegionChunkExclusions.getAllExcludedRegionIds();
	private final List<Integer> excludedChunkIds = EscapeCrystalNotifyRegionChunkExclusions.getAllExcludedChunkIds();
	private final Map<Integer, Integer> planeRequirements = EscapeCrystalNotifyRegionPlaneRequirements.getRegionPlaneMap();
	private final List<Integer> leviathanRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_THE_LEVIATHAN.getRegionIds()).boxed().collect(Collectors.toList());
	private final List<Integer> doomRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_DOOM_OF_MOKHAIOTL.getRegionIds()).boxed().collect(Collectors.toList());
	private List<Integer> logoutBugRegionIds = new ArrayList<>();
	private final List<Integer> zulrahRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_ZULRAH.getRegionIds()).boxed().collect(Collectors.toList());
	private EscapeCrystalNotifyInfoBox activeInfoBox;

	@Override
	protected void startUp() throws Exception
	{
		this.targetRegionIds = getTargetRegionIdsFromConfig(this.accountType);
		this.notifyTimeRemainingThresholdMessage = generateTimeRemainingThresholdMessage();
		this.timeRemainingThresholdTicks = normalizeTimeRemainingThresholdValue();

		this.logoutBugRegionIds.addAll(leviathanRegionIds);
		this.logoutBugRegionIds.addAll(doomRegionIds);

		overlayManager.add(escapeCrystalNotifyOverlayActive);
		overlayManager.add(escapeCrystalNotifyOverlayInactive);
		overlayManager.add(escapeCrystalNotifyInventoryOverlay);
		overlayManager.add(escapeCrystalNotifyTextOverlayPanel);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(escapeCrystalNotifyOverlayActive);
		overlayManager.remove(escapeCrystalNotifyOverlayInactive);
		overlayManager.remove(escapeCrystalNotifyInventoryOverlay);
		overlayManager.remove(escapeCrystalNotifyTextOverlayPanel);
		removeInfoBoxes();
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		ticksSinceLogin++;
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

	@Subscribe
	public void onPostMenuSort(PostMenuSort e) {
		if (config.deprioritizeLeviathanLogout() && this.atLeviathanRegionId && !this.atLeviathanLobby) {
			deprioritizeLogoutButton();
		}
		if (config.deprioritizeDoomLogout() && this.atDoomRegionId && !this.atDoomLobby && !this.doomFloorCleared) {
			deprioritizeLogoutButton();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!this.doomRegionIds.contains(this.currentRegionId)) return;

		GameObject spawnedObject = event.getGameObject();
		int spawnedObjectId = spawnedObject.getId();

        if (DOOM_BURROW_HOLE_IDS.contains(spawnedObjectId)) this.doomFloorCleared = true;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npc)
	{
		if (!this.doomRegionIds.contains(this.currentRegionId)) {
			this.doomFloorCleared = true;
			return;
		}

		NPC spawnedNpc = npc.getNpc();
		int spawnedNpcId = spawnedNpc.getId();
		if (DOOM_NPC_IDS.contains(spawnedNpcId)) this.doomFloorCleared = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		GameState state = event.getGameState();

		switch (state)
		{
			case LOGGING_IN:
			case HOPPING:
				ready = true;
				break;
			case LOGGED_IN:
				if (ready)
				{
					loginTime = Instant.now();
					ticksSinceLogin = 0;
					ready = false;
				}
				break;
		}
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
		this.completedWesternEliteDiary = client.getVarbitValue(Varbits.DIARY_WESTERN_ELITE) == 1;

		if (this.accountType != previousAccountType) {
			this.targetRegionIds = getTargetRegionIdsFromConfig(this.accountType);
		}
	}

	private void computeLocationMetrics() {
		this.previousRegionId = this.currentRegionId;
		this.previouslyAtNotifyRegionId = this.atNotifyRegionId;

		computeWorldPointMetrics();

		this.atNotifyRegionId = checkAtNotifyLocation();
		this.enteredNotifyRegionId = !this.previouslyAtNotifyRegionId && this.atNotifyRegionId;
		this.atLeviathanRegionId = this.leviathanRegionIds.contains(this.currentRegionId);
		this.atLeviathanLobby = LEVIATHAN_LOBBY_CHUNK_IDS.contains(this.currentChunkId);
		this.atDoomRegionId = this.doomRegionIds.contains(this.currentRegionId);
		this.atDoomLobby = DOOM_LOBBY_CHUNK_IDS.contains(this.currentChunkId);
	}

	private void computeWorldPointMetrics() {
		this.currentWorldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		this.currentRegionId = this.currentWorldPoint.getRegionID();
		this.currentPlaneId = this.currentWorldPoint.getPlane();

		int currentTileX = this.currentWorldPoint.getX();
		int currentTileY = this.currentWorldPoint.getY();
		final int currentChunkX = currentTileX >> 3;
		final int currentChunkY = currentTileY >> 3;

		this.currentChunkId = (currentChunkX << 11) | currentChunkY;
	}

	private boolean checkAtNotifyLocation() {
		if (config.displayEverywhere()) return true;

		if (!targetRegionIds.contains(this.currentRegionId)) return false;

		if (excludedChunkIds.contains(this.currentChunkId)) return false;

		int planeRequirement = this.planeRequirements.getOrDefault(this.currentRegionId, this.currentPlaneId);
		return this.currentPlaneId == planeRequirement;
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

		if (config.excludeZulrahWithEliteDiary() && this.completedWesternEliteDiary) {
			regionIds.removeAll(this.zulrahRegionIds);
		}

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

	private void deprioritizeLogoutButton() {
		MenuEntry[] menuEntries = client.getMenuEntries();

		if (menuEntries.length == 0) return;

		int topEntryIndex = menuEntries.length - 1;
		MenuEntry topEntry = menuEntries[topEntryIndex];
		boolean isLogoutOption = topEntry.getOption().equals("Logout");
		boolean isWorldSwitchOption = topEntry.getOption().equals("Switch") && WidgetUtil.componentToInterface(topEntry.getWidget().getId()) == InterfaceID.WORLD_SWITCHER;

		if (!isLogoutOption && !isWorldSwitchOption) return;

		if (menuEntries.length == 1) {
			client.getMenu().createMenuEntry(0).setType(MenuAction.CANCEL);
		}
		else {
			MenuEntry cancelEntry = menuEntries[0];
			menuEntries[topEntryIndex] = cancelEntry;
			menuEntries[0] = topEntry;
			client.setMenuEntries(menuEntries);
		}
	}

	public String getItemModelDisplayText(EscapeCrystalNotifyConfig.OverlayDisplayType displayFormat, EscapeCrystalNotifyConfig.InactivityTimeFormat timeFormat, String timeExpiredText) {
		String displayText;

		if (!escapeCrystalWithPlayer) {
			displayText = this.config.infoBoxMissingCrystalText();
		}
		else if (displayFormat == EscapeCrystalNotifyConfig.OverlayDisplayType.REMAINING_TIME) {
			if (!isEscapeCrystalActive()) {
				displayText = getItemModelCurrentSettingDisplayText(timeFormat);
			}
			else if (isTimeExpired()) {
				displayText = timeExpiredText;
			}
			else if (timeFormat == EscapeCrystalNotifyConfig.InactivityTimeFormat.SECONDS) {
				displayText = this.getExpectedSecondsUntilTeleport() + "s";
			}
			else if (timeFormat == EscapeCrystalNotifyConfig.InactivityTimeFormat.SECONDS_MMSS) {
				int minutes = (this.getEscapeCrystalInactivitySeconds() % 3600) / 60;
				int seconds = this.getEscapeCrystalInactivitySeconds() % 60;

				displayText = String.format("%02d:%02d", minutes, seconds);
			}
			else {
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
			case SECONDS_MMSS: {
				if (this.getEscapeCrystalInactivitySeconds() < 0) return "00:00";

				int minutes = (this.getEscapeCrystalInactivitySeconds() % 3600) / 60;
				int seconds = this.getEscapeCrystalInactivitySeconds() % 60;

				return String.format("%02d:%02d", minutes, seconds);
			}
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

	public boolean isLeviathanSafeguardPanelEnabled() {
		return this.atLeviathanLobby && !config.disableLeviathanSafeguardPanelPopup();
	}

	public boolean isDoomSafeguardPanelEnabled() {
		return this.atDoomLobby && !config.disableDoomSafeguardPanelPopup();
	}

	public boolean isCloseToSixHourLogout() {
		return ticksSinceLogin >= SIX_HOUR_LOG_WARNING_THRESHOLD_TICKS;
	}

	private int convertTicksToSeconds(int ticks) {
		return (int) Math.round(ticks * 0.6);
	}

	private int convertSecondsToTicks(int seconds) {
		return (int) Math.round(seconds * 1.6);
	}
}
