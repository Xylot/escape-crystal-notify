package com.escapecrystalnotify;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
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
import net.runelite.client.util.ColorUtil;

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
	private static final List<Integer> HYDRA_ENTRANCE_IDS = List.of(34553, 34554);
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
	private EscapeCrystalNotifyRegionEntranceOverlay escapeCrystalNotifyRegionEntranceOverlay;

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
	@Getter
	private boolean hardcoreAccountType = false;
	private boolean completedWesternEliteDiary = false;
	@Getter
	private boolean escapeCrystalWithPlayer = true;
	@Getter
	private boolean escapeCrystalActive = true;
	@Getter
	private boolean escapeCrystalRingOfLifeActive = true;
	@Getter
	private boolean escapeCrystalLeftClickTeleportEnabled = true;
	@Getter
	private int escapeCrystalInactivityTicks;
	@Getter
	private int clientInactivityTicks;
	@Getter
	private int expectedServerInactivityTicks = 0;
	@Getter
	private int expectedTicksUntilTeleport;
	@Getter
	private WorldPoint currentWorldPoint;
	@Getter
	private int currentRegionId;
	private int currentPlaneId;
	private int currentChunkId;
	private int previousRegionId;
	private boolean enteredNotifyRegionId = false;
	@Getter
	private boolean atNotifyRegionId = false;
	private boolean previouslyAtNotifyRegionId = false;
	@Getter
	private boolean atNotifyRegionEntrance = false;
	@Getter
	private List<EscapeCrystalNotifyLocatedEntrance> validEntrances = new ArrayList<>();
	private EscapeCrystalNotifyRegionEntranceObject regionEntrance;
	private EscapeCrystalNotifyRegionEntranceDirection regionEntranceDirection;
	private EscapeCrystalNotifyRegionEntranceOverlayType regionEntranceOverlayType;
	private WorldPoint regionEntranceOriginalLocation;
	private boolean playerPastRegionEntrance;
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
	private final Map<Integer, List<Integer>> chunkRequirements = EscapeCrystalNotifyRegion.getRegionChunkRequirementsMap();
	private final Map<Integer, EscapeCrystalNotifyRegionEntrance> chunkEntranceMap = EscapeCrystalNotifyRegion.getChunkEntranceMap();
	private final Map<Integer, EscapeCrystalNotifyRegionEntrance> regionEntranceMap = EscapeCrystalNotifyRegion.getRegionEntranceMap();
	private final Map<Integer, List<EscapeCrystalNotifyLocatedEntrance>> possibleEntrances = new HashMap<>();
	private final List<Integer> allEntranceIds = EscapeCrystalNotifyRegion.getAllEntranceIds();
	private final List<Integer> leviathanRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_THE_LEVIATHAN.getRegionIds()).boxed().collect(Collectors.toList());
	private final List<Integer> doomRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_DOOM_OF_MOKHAIOTL.getRegionIds()).boxed().collect(Collectors.toList());
	private final List<Integer> infernoEntranceRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_INFERNO_ENTRANCE.getRegionIds()).boxed().collect(Collectors.toList());
	private final List<Integer> fightCavesEntranceRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_TZHAAR_FIGHT_CAVES_ENTRANCE.getRegionIds()).boxed().collect(Collectors.toList());
	private final List<Integer> whispererEntranceRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_THE_WHISPERER.getRegionIds()).boxed().collect(Collectors.toList());
	private final List<Integer> hydraEntranceRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_HYDRA.getRegionIds()).boxed().collect(Collectors.toList());
	private List<Integer> logoutBugRegionIds = new ArrayList<>();
	private final List<Integer> zulrahRegionIds = Arrays.stream(EscapeCrystalNotifyRegion.BOSS_ZULRAH.getRegionIds()).boxed().collect(Collectors.toList());
	private BufferedImage inactiveEscapeCrystalImage;
	private BufferedImage activeEscapeCrystalImage;
	private BufferedImage bankFillerImage;
	private BufferedImage entranceOverlayImage;
	private EscapeCrystalNotifyInfoBox activeInfoBox;

	@Override
	protected void startUp() throws Exception
	{
		this.targetRegionIds = getTargetRegionIdsFromConfig(this.accountType);
		this.notifyTimeRemainingThresholdMessage = generateTimeRemainingThresholdMessage();
		this.timeRemainingThresholdTicks = normalizeTimeRemainingThresholdValue();

		this.logoutBugRegionIds.addAll(leviathanRegionIds);
		this.logoutBugRegionIds.addAll(doomRegionIds);

		this.possibleEntrances.clear();

		overlayManager.add(escapeCrystalNotifyOverlayActive);
		overlayManager.add(escapeCrystalNotifyOverlayInactive);
		overlayManager.add(escapeCrystalNotifyInventoryOverlay);
		overlayManager.add(escapeCrystalNotifyTextOverlayPanel);
		overlayManager.add(escapeCrystalNotifyRegionEntranceOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.possibleEntrances.clear();
		overlayManager.remove(escapeCrystalNotifyOverlayActive);
		overlayManager.remove(escapeCrystalNotifyOverlayInactive);
		overlayManager.remove(escapeCrystalNotifyInventoryOverlay);
		overlayManager.remove(escapeCrystalNotifyTextOverlayPanel);
		overlayManager.remove(escapeCrystalNotifyRegionEntranceOverlay);
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
		boolean inLeviathanEncounter = config.deprioritizeLeviathanLogout() && this.atLeviathanRegionId && !this.atLeviathanLobby;
		boolean inDoomEncounter = config.deprioritizeDoomLogout() && this.atDoomRegionId && !this.atDoomLobby && !this.doomFloorCleared;

		if (inLeviathanEncounter || inDoomEncounter) {
			deprioritizeLogoutButton();
		}

		if (this.shouldDeprioritizeEntranceEnterOption()) {
			deprioritizeEnterOption();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject spawnedObject = event.getGameObject();
		int spawnedObjectId = spawnedObject.getId();

		if (this.allEntranceIds.contains(spawnedObjectId)) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(spawnedObject.getWorldLocation(), spawnedObject.getLocalLocation());
			int locatedChunkId = computeChunkIdFromWorldPoint(locatedWorldPoint);

			if (this.infernoEntranceRegionIds.contains(locatedWorldPoint.getRegionID())) {
				for (int regionId : this.infernoEntranceRegionIds) {
					possibleEntrances.computeIfAbsent(regionId, k -> new ArrayList<>()).add(
							new EscapeCrystalNotifyLocatedEntrance(
									new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
									EscapeCrystalNotifyRegion.BOSS_INFERNO_ENTRANCE.getRegionEntrance(),
									locatedWorldPoint
							)
					);
				}

				for (int regionId : this.fightCavesEntranceRegionIds) {
					possibleEntrances.computeIfAbsent(regionId, k -> new ArrayList<>()).add(
							new EscapeCrystalNotifyLocatedEntrance(
									new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
									EscapeCrystalNotifyRegion.BOSS_TZHAAR_FIGHT_CAVES_ENTRANCE.getRegionEntrance(),
									locatedWorldPoint
							)
					);
				}
			} else {
				possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
						new EscapeCrystalNotifyLocatedEntrance(
								new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
								this.chunkEntranceMap.get(locatedChunkId),
								locatedWorldPoint
						)
				);
			}
		}

		if (!this.doomRegionIds.contains(this.currentRegionId)) return;

        if (DOOM_BURROW_HOLE_IDS.contains(spawnedObjectId)) this.doomFloorCleared = true;
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject despawnedObject = event.getGameObject();

		if (this.allEntranceIds.contains(despawnedObject.getId())) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(despawnedObject.getWorldLocation(), despawnedObject.getLocalLocation());
			int regionId = locatedWorldPoint.getRegionID();
			List<EscapeCrystalNotifyLocatedEntrance> entrances = possibleEntrances.get(regionId);

			if (entrances != null) {
				entrances.removeIf(entrance ->
					entrance.getTarget().getId() == despawnedObject.getId());

				if (entrances.isEmpty()) {
					possibleEntrances.remove(regionId);
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npc)
	{
		NPC spawnedNpc = npc.getNpc();
		int spawnedNpcId = spawnedNpc.getId();

		if (this.allEntranceIds.contains(spawnedNpcId)) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(spawnedNpc.getWorldLocation(), spawnedNpc.getLocalLocation());
			int locatedChunkId = computeChunkIdFromWorldPoint(locatedWorldPoint);

			if (this.infernoEntranceRegionIds.contains(locatedWorldPoint.getRegionID())) {
				for (int regionId : this.infernoEntranceRegionIds) {
					possibleEntrances.computeIfAbsent(regionId, k -> new ArrayList<>()).add(
							new EscapeCrystalNotifyLocatedEntrance(
									new EscapeCrystalNotifyRegionEntranceObject(spawnedNpc),
									this.chunkEntranceMap.get(locatedChunkId),
									locatedWorldPoint
							)
					);
				}
			} else if (spawnedNpcId == NpcID.ODD_FIGURE) {
				for (int regionId : this.whispererEntranceRegionIds) {
					possibleEntrances.computeIfAbsent(regionId, k -> new ArrayList<>()).add(
							new EscapeCrystalNotifyLocatedEntrance(
									new EscapeCrystalNotifyRegionEntranceObject(spawnedNpc),
									EscapeCrystalNotifyRegion.BOSS_THE_WHISPERER.getRegionEntrance(),
									locatedWorldPoint
							)
					);
				}
			} else {
				possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
						new EscapeCrystalNotifyLocatedEntrance(
								new EscapeCrystalNotifyRegionEntranceObject(spawnedNpc),
								this.chunkEntranceMap.get(locatedChunkId),
								locatedWorldPoint
						)
				);
			}
		}

		if (!this.doomRegionIds.contains(this.currentRegionId)) {
			this.doomFloorCleared = true;
			return;
		}
		if (DOOM_NPC_IDS.contains(spawnedNpcId)) this.doomFloorCleared = false;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npc)
	{
		NPC despawnedNpc = npc.getNpc();

		if (this.allEntranceIds.contains(despawnedNpc.getId())) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(despawnedNpc.getWorldLocation(), despawnedNpc.getLocalLocation());
			int regionId = locatedWorldPoint.getRegionID();
			List<EscapeCrystalNotifyLocatedEntrance> entrances = possibleEntrances.get(regionId);
			
			if (entrances != null) {
				entrances.removeIf(entrance -> 
					entrance.getTarget().getId() == despawnedNpc.getId());
				
				if (entrances.isEmpty()) {
					possibleEntrances.remove(regionId);
				}
			}
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged npc)
	{
		if (npc.getNpc().getId() == NpcID.THE_WHISPERER) {
			for (int regionId : EscapeCrystalNotifyRegion.BOSS_THE_WHISPERER.getRegionIds()) {
				possibleEntrances.remove(regionId);
			}
		}
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		DecorativeObject spawnedObject = event.getDecorativeObject();

		if (this.allEntranceIds.contains(spawnedObject.getId())) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(spawnedObject.getWorldLocation(), spawnedObject.getLocalLocation());
			int locatedChunkId = computeChunkIdFromWorldPoint(locatedWorldPoint);

			possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
					new EscapeCrystalNotifyLocatedEntrance(
							new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
							this.chunkEntranceMap.get(locatedChunkId),
							locatedWorldPoint
					)
			);
		}
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		DecorativeObject despawnedObject = event.getDecorativeObject();

		if (this.allEntranceIds.contains(despawnedObject.getId())) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(despawnedObject.getWorldLocation(), despawnedObject.getLocalLocation());
			int regionId = locatedWorldPoint.getRegionID();
			List<EscapeCrystalNotifyLocatedEntrance> entrances = possibleEntrances.get(regionId);
			
			if (entrances != null) {
				entrances.removeIf(entrance -> 
					entrance.getTarget().getId() == despawnedObject.getId());
				
				if (entrances.isEmpty()) {
					possibleEntrances.remove(regionId);
				}
			}
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		WallObject spawnedObject = event.getWallObject();

		if (this.allEntranceIds.contains(spawnedObject.getId())) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(spawnedObject.getWorldLocation(), spawnedObject.getLocalLocation());
			int locatedChunkId = computeChunkIdFromWorldPoint(locatedWorldPoint);

			if (HYDRA_ENTRANCE_IDS.contains(spawnedObject.getId())) {
				for (int regionId : this.hydraEntranceRegionIds) {
					possibleEntrances.computeIfAbsent(regionId, k -> new ArrayList<>()).add(
							new EscapeCrystalNotifyLocatedEntrance(
									new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
									EscapeCrystalNotifyRegion.BOSS_HYDRA.getRegionEntrance(),
									locatedWorldPoint
							)
					);
				}
			} else {
				possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
						new EscapeCrystalNotifyLocatedEntrance(
								new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
								this.chunkEntranceMap.get(locatedChunkId),
								locatedWorldPoint
						)
				);
			}
		}
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		WallObject despawnedObject = event.getWallObject();

		if (this.allEntranceIds.contains(despawnedObject.getId())) {
			WorldPoint locatedWorldPoint = resolvePossiblyInstancedWorldPoint(despawnedObject.getWorldLocation(), despawnedObject.getLocalLocation());
			int regionId = locatedWorldPoint.getRegionID();
			List<EscapeCrystalNotifyLocatedEntrance> entrances = possibleEntrances.get(regionId);
			
			if (entrances != null) {
				entrances.removeIf(entrance -> 
					entrance.getTarget().getId() == despawnedObject.getId());
				
				if (entrances.isEmpty()) {
					possibleEntrances.remove(regionId);
				}
			}
		}
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

		this.computeWorldPointMetrics();

		this.atNotifyRegionId = this.checkAtNotifyLocation();
		this.enteredNotifyRegionId = !this.previouslyAtNotifyRegionId && this.atNotifyRegionId;

		this.atLeviathanRegionId = this.leviathanRegionIds.contains(this.currentRegionId);
		this.atLeviathanLobby = LEVIATHAN_LOBBY_CHUNK_IDS.contains(this.currentChunkId);
		this.atDoomRegionId = this.doomRegionIds.contains(this.currentRegionId);
		this.atDoomLobby = DOOM_LOBBY_CHUNK_IDS.contains(this.currentChunkId);

		this.computeEntranceObjectMetrics();
	}

	private void computeWorldPointMetrics() {
		this.currentWorldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		this.currentRegionId = this.currentWorldPoint.getRegionID();
		this.currentPlaneId = this.currentWorldPoint.getPlane();
		this.currentChunkId = this.computeChunkIdFromWorldPoint(this.currentWorldPoint);
	}

	private void computeEntranceObjectMetrics() {
		if (!this.atNotifyRegionId) {
			this.validEntrances.clear();
			return;
		}

		this.validEntrances.clear();
		for (List<EscapeCrystalNotifyLocatedEntrance> entrances : this.possibleEntrances.values()) {
			for (EscapeCrystalNotifyLocatedEntrance entrance : entrances) {
				if (entrance.isEntranceInValidChunk() && 
					!entrance.hasMoved() && 
					!entrance.isPlayerPastEntrance(this.currentWorldPoint) &&
					entrance.matchesPlayerPlane(this.currentPlaneId)) {
					this.validEntrances.add(entrance);
				}
			}
		}
	}

	private boolean checkAtNotifyLocation() {
		if (config.displayEverywhere()) return true;

		if (!targetRegionIds.contains(this.currentRegionId)) return false;

		if (excludedChunkIds.contains(this.currentChunkId)) return false;

		if (this.chunkRequirements.containsKey(this.currentRegionId)) {
			if (!this.chunkRequirements.get(this.currentRegionId).contains(this.currentChunkId)) return false;
		}

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
					this.activeInfoBox.setImage(this.getActiveEscapeCrystalImage());
					this.activeInfoBox.imageId = ItemID.ESCAPE_CRYSTAL;
					this.activeInfoBox.setTooltip(getInfoBoxTooltip());
					infoBoxManager.updateInfoBoxImage(this.activeInfoBox);
				}
				return;
			}

			BufferedImage activeImage = this.getActiveEscapeCrystalImage();
			this.activeInfoBox = new EscapeCrystalNotifyInfoBox(ItemID.ESCAPE_CRYSTAL, activeImage, this, this.config);
			this.activeInfoBox.setTooltip(getInfoBoxTooltip());
			infoBoxManager.addInfoBox(this.activeInfoBox);

		} else {
			if (currentInfoBoxes.contains(this.activeInfoBox)) {
				if (this.activeInfoBox.imageId != ItemID.CORRUPTED_ESCAPE_CRYSTAL) {
					this.activeInfoBox.setImage(this.getInactiveEscapeCrystalImage());
					this.activeInfoBox.imageId = ItemID.CORRUPTED_ESCAPE_CRYSTAL;
					this.activeInfoBox.setTooltip(getInfoBoxTooltip());
					infoBoxManager.updateInfoBoxImage(this.activeInfoBox);
				}
				return;
			}

			BufferedImage inactiveImage = this.getInactiveEscapeCrystalImage();
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

	private void deprioritizeEnterOption() {
		MenuEntry[] menuEntries = client.getMenuEntries();

		if (menuEntries.length == 0) return;

		int topEntryIndex = menuEntries.length - 1;
		MenuEntry topEntry = menuEntries[topEntryIndex];

		if (this.validEntrances.isEmpty()) return;

		EscapeCrystalNotifyLocatedEntrance entranceToDeprioritize = null;
		int entryId;
		if (topEntry.getNpc() != null)  {
			entryId = topEntry.getNpc().getId();
		} else {
			entryId = topEntry.getIdentifier();
		}

		for (EscapeCrystalNotifyLocatedEntrance entrance : this.validEntrances) {
			if (entrance.canDeprioritize() && 
				!entrance.isPlayerPastEntrance(this.currentWorldPoint) &&
				entryId == entrance.getTarget().getId()) {
				entranceToDeprioritize = entrance;
				break;
			}
		}

		if (entranceToDeprioritize == null) return;

		MenuEntry[] newEntries = new MenuEntry[menuEntries.length + 1];
		System.arraycopy(menuEntries, 0, newEntries, 0, menuEntries.length);

		String optionText = ColorUtil.wrapWithColorTag("Where's Your Crystal?", Color.MAGENTA);
		MenuEntry escapeCrystalReminderEntry = client.createMenuEntry(0).setType(MenuAction.CANCEL).setOption(optionText).setTarget("");

		newEntries[newEntries.length - 1] = escapeCrystalReminderEntry;

		client.setMenuEntries(newEntries);
	}

	private BufferedImage combineItemImages(BufferedImage... images) {
		BufferedImage backgroundImage = images[0];

		BufferedImage result = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = result.createGraphics();

		graphics.setComposite(AlphaComposite.SrcOver);
		graphics.drawImage(backgroundImage, 0, 0, null);

		for (int i = 1; i < images.length; i++) {
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			graphics.drawImage(images[i], 0, 0, null);
		}

		graphics.dispose();

		return result;
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

	public boolean isAccountTypeEnabled() {
		if (config.requireHardcoreAccountType()) {
			return isHardcoreAccountType();
		}
		return true;
	}

	public boolean isEscapeCrystalInactivityTeleportActive() {
		return escapeCrystalWithPlayer && escapeCrystalActive;
	}

	public int getEscapeCrystalInactivitySeconds() {
		return convertTicksToSeconds(escapeCrystalInactivityTicks);
	}

	public int getExpectedServerInactivitySeconds() {
		return convertTicksToSeconds(expectedServerInactivityTicks);
	}

	public int getExpectedSecondsUntilTeleport() {
		return convertTicksToSeconds(expectedTicksUntilTeleport);
	}

	public boolean isTimeExpired() {
		return expectedTicksUntilTeleport == 0;
	}

	public void resetLocatedEntrance() {
		this.validEntrances.clear();
	}

	public BufferedImage getInactiveEscapeCrystalImage() {
		if (inactiveEscapeCrystalImage == null) {
			inactiveEscapeCrystalImage = itemManager.getImage(ItemID.CORRUPTED_ESCAPE_CRYSTAL);
		}
		return inactiveEscapeCrystalImage;
	}

	public BufferedImage getActiveEscapeCrystalImage() {
		if (activeEscapeCrystalImage == null) {
			activeEscapeCrystalImage = itemManager.getImage(ItemID.ESCAPE_CRYSTAL);
		}
		return activeEscapeCrystalImage;
	}

	public BufferedImage getBankFillerImage() {
		if (bankFillerImage == null) {
			bankFillerImage = itemManager.getImage(ItemID.BANK_FILLER);
		}
		return bankFillerImage;
	}

	public BufferedImage getEntranceOverlayImage() {
		if (entranceOverlayImage == null) {
			entranceOverlayImage = combineItemImages(this.getActiveEscapeCrystalImage(), this.getBankFillerImage());
		}
		return entranceOverlayImage;
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

	public boolean shouldDeprioritizeEntranceEnterOption() {
		boolean enabled = config.deprioritizeEntranceEnterOption();
		boolean active = this.isEscapeCrystalInactivityTeleportActive();
		boolean notHardcore = config.requireHardcoreAccountType() && !this.isHardcoreAccountType();
		boolean atNotifyRegion = this.isAtNotifyRegionId();

		if (!enabled || active || notHardcore || !atNotifyRegion) return false;

		if (this.validEntrances.isEmpty()) return false;

		return this.validEntrances.stream().anyMatch(EscapeCrystalNotifyLocatedEntrance::canDeprioritize);
	}

	public WorldPoint resolvePossiblyInstancedWorldPoint(WorldPoint worldPoint, LocalPoint localPoint) {
		if (client.isInInstancedRegion()) {
			return WorldPoint.fromLocalInstance(client, localPoint);
		}
		return worldPoint;
	}

	public int computeChunkIdFromWorldPoint(WorldPoint worldPoint) {
		int currentTileX = worldPoint.getX();
		int currentTileY = worldPoint.getY();
		final int currentChunkX = currentTileX >> 3;
		final int currentChunkY = currentTileY >> 3;

		return (currentChunkX << 11) | currentChunkY;
	}

	private int convertTicksToSeconds(int ticks) {
		return (int) Math.round(ticks * 0.6);
	}

	private int convertSecondsToTicks(int seconds) {
		return (int) Math.round(seconds * 1.6);
	}
}
