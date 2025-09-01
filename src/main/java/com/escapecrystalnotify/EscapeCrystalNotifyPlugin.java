package com.escapecrystalnotify;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
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
	private static final int ESCAPE_CRYSTAL_ACTIVE_VARBIT = VarbitID.TELEPORT_CRYSTAL_AFK_MODE;
	private static final int ESCAPE_CRYSTAL_INACTIVITY_TICKS_VARBIT = VarbitID.TELEPORT_CRYSTAL_AFK_DELAY;
	private static final int ESCAPE_CRYSTAL_RING_OF_LIFE_ACTIVE_VARBIT = VarbitID.TELEPORT_CRYSTAL_ROL;
	private static final int ITEMS_STORED_VARBIT = VarbitID.HOLDING_INVENTORY_LOCATION;
	private static final int STANDARD_HARDCORE_ACCOUNT_TYPE_VARBIT_VALUE = 3;
	private static final int GROUP_HARDCORE_ACCOUNT_TYPE_VARBIT_VALUE = 5;
	private static final int ZULRAH_REVIVE_VARBIT = VarbitID.ZULRAH_REVIVE;
	private static final int ZULRAH_ENTRANCE_REGION_ID = 8751;
	private static final int YAMA_REGION_ID = 6045;
	private static final List<Integer> LEVIATHAN_LOBBY_CHUNK_IDS = List.of(525092, 525093, 527139, 527140, 527141, 529188);
	private static final List<Integer> DOOM_LOBBY_CHUNK_IDS = List.of(335016, 335017, 335018, 337064, 337065, 337066);
	private static final List<Integer> DOOM_BURROW_HOLE_IDS = List.of(ObjectID.DOM_DESCEND_HOLE, ObjectID.DOM_DESCEND_HOLE_UNIQUE);
	private static final List<Integer> DOOM_NPC_IDS = List.of(NpcID.DOM_BOSS, NpcID.DOM_BOSS_SHIELDED, NpcID.DOM_BOSS_BURROWED);
	private static final List<Integer> HYDRA_ENTRANCE_IDS = List.of(34553, 34554);
	private static final HashSet<Integer> ENTRANCE_CLEAR_REQUIRED_IDS = new HashSet<>(List.of(ObjectID.INFERNO_ENTRANCE, ObjectID.TZHAAR_FIGHTCAVE_WALL_ENTRANCE));
	private static final HashSet<Integer> NPC_ENTRANCE_FORCE_CLEAR_IDS = new HashSet<>(List.of(NpcID.NIGHTMARE_ENTRY_READY, NpcID.NIGHTMARE_ENTRY_OPEN, NpcID.NIGHTMARE_ENTRY_CLOSED_01, NpcID.NIGHTMARE_ENTRY_CLOSED_02, NpcID.NIGHTMARE_ENTRY_CLOSED_03, NpcID.VOICE_OF_YAMA_3OP));
	private static final HashSet<Integer> NPC_ENTRANCE_AUTO_RECHECK_ON_LOAD_REGION_IDS = new HashSet<>(List.of(6045, 15256));
	private static final HashSet<Integer> NPC_ENTRANCE_AUTO_RECHECK_ON_LOAD_NPC_IDS = new HashSet<>(List.of(NpcID.NIGHTMARE_ENTRY_READY, NpcID.NIGHTMARE_ENTRY_OPEN, NpcID.NIGHTMARE_ENTRY_CLOSED_01, NpcID.NIGHTMARE_ENTRY_CLOSED_02, NpcID.NIGHTMARE_ENTRY_CLOSED_03, NpcID.YAMA_THRONE_OCCUPIED));
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
	private EscapeCrystalNotifyTeleportDisabledPanel escapeCrystalNotifyTeleportDisabledPanel;

	@Inject
	private EscapeCrystalNotifyRegionEntranceOverlay escapeCrystalNotifyRegionEntranceOverlay;

	@Inject
	private EscapeCrystalNotifyTestingOverlay escapeCrystalNotifyTestingOverlay;

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

	@Getter
	private Instant lastCombatTime;

	private boolean ready;
	private boolean recheckLocalNpcs = false;
	private boolean notifyMissing = false;
	private boolean notifyInactive = false;
	private boolean notifyTimeRemainingThreshold = false;
	private boolean notifyNonLeftClickTeleport = false;
	private String notifyTimeRemainingThresholdMessage;
	@Getter
	private EscapeCrystalNotifyAccountType accountType = EscapeCrystalNotifyAccountType.NON_HARDCORE;
	@Getter
	private boolean hardcoreAccountType = false;
	@Getter
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
	@Getter
	private boolean atLeviathanRegionId = false;
	@Getter
	private boolean atLeviathanLobby = false;
	@Getter
	private boolean atDoomRegionId = false;
	@Getter
	private boolean atDoomLobby = false;
	@Getter
	private boolean doomFloorCleared = false;
	@Getter
	private boolean inTzhaarEntranceRegion = false;
	@Getter
	private boolean atTeleportDisabledRegion = false;
	@Getter
	private int timeRemainingThresholdTicks;
	@Getter
	private boolean inTimeRemainingThreshold = false;
	@Getter
	private boolean previouslyInTimeRemainingThreshold = false;
	@Getter
	private boolean enteredTimeRemainingThreshold = false;
	@Getter
	private int currentPlaneId;
	@Getter
	private int currentChunkId;
	@Getter
	private Map<Integer, List<EscapeCrystalNotifyLocatedEntrance>> possibleEntrances = new HashMap<>();
	@Getter
	private boolean testingModeEnabled = false;
	private int previousRegionId;
	private boolean enteredNotifyRegionId = false;
	@Getter
	private boolean atNotifyRegionId = false;
	private boolean previouslyAtNotifyRegionId = false;
	@Getter
	private boolean atNotifyRegionEntrance = false;
	@Getter
	private List<EscapeCrystalNotifyLocatedEntrance> validEntrances = new ArrayList<>();
	private Set<Integer> targetRegionIds;
	private final List<Integer> excludedRegionIds = EscapeCrystalNotifyRegionChunkExclusions.getAllExcludedRegionIds();
	private final List<Integer> excludedChunkIds = EscapeCrystalNotifyRegionChunkExclusions.getAllExcludedChunkIds();
	private final Map<Integer, Integer> planeRequirements = EscapeCrystalNotifyRegionPlaneRequirements.getRegionPlaneMap();
	private final Map<Integer, List<Integer>> chunkRequirements = EscapeCrystalNotifyRegion.getRegionChunkRequirementsMap();
	private final Map<Integer, EscapeCrystalNotifyRegionEntrance> chunkEntranceMap = EscapeCrystalNotifyRegion.getChunkEntranceMap();
	private final Set<Integer> allEntranceIds = new HashSet<>(EscapeCrystalNotifyRegion.getAllEntranceIds());
	private final Set<Integer> leviathanRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_THE_LEVIATHAN.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> doomRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_DOOM_OF_MOKHAIOTL.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> infernoEntranceRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_INFERNO_ENTRANCE.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> fightCavesEntranceRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_TZHAAR_FIGHT_CAVES_ENTRANCE.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> whispererEntranceRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_THE_WHISPERER.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> hydraEntranceRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_HYDRA.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> zulrahRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_ZULRAH.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> zulrahEntranceRegionIds = new HashSet<>(Arrays.stream(EscapeCrystalNotifyRegion.BOSS_ZULRAH_ENTRANCE.getRegionIds()).boxed().collect(Collectors.toList()));
	private final Set<Integer> teleportDisabledRegionIds = new HashSet<>();
	private Set<Integer> tzhaarEntranceRegionIds = new HashSet<>();
	private Set<Integer> logoutBugRegionIds = new HashSet<>();
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

		this.tzhaarEntranceRegionIds.addAll(fightCavesEntranceRegionIds);
		this.tzhaarEntranceRegionIds.addAll(infernoEntranceRegionIds);

		this.possibleEntrances.clear();

		overlayManager.add(escapeCrystalNotifyOverlayActive);
		overlayManager.add(escapeCrystalNotifyOverlayInactive);
		overlayManager.add(escapeCrystalNotifyInventoryOverlay);
		overlayManager.add(escapeCrystalNotifyTextOverlayPanel);
		overlayManager.add(escapeCrystalNotifyTeleportDisabledPanel);
		overlayManager.add(escapeCrystalNotifyRegionEntranceOverlay);
		overlayManager.add(escapeCrystalNotifyTestingOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.possibleEntrances.clear();
		overlayManager.remove(escapeCrystalNotifyOverlayActive);
		overlayManager.remove(escapeCrystalNotifyOverlayInactive);
		overlayManager.remove(escapeCrystalNotifyInventoryOverlay);
		overlayManager.remove(escapeCrystalNotifyTextOverlayPanel);
		overlayManager.remove(escapeCrystalNotifyTeleportDisabledPanel);
		overlayManager.remove(escapeCrystalNotifyRegionEntranceOverlay);
		overlayManager.remove(escapeCrystalNotifyTestingOverlay);
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

			if (ENTRANCE_CLEAR_REQUIRED_IDS.contains(spawnedObjectId)) {
				clearPossibleEntranceId(spawnedObjectId);
			}

			possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
					new EscapeCrystalNotifyLocatedEntrance(
							new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
							this.chunkEntranceMap.get(locatedChunkId),
							locatedWorldPoint,
							spawnedObjectId
					)
			);
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

			if (ENTRANCE_CLEAR_REQUIRED_IDS.contains(despawnedObject.getId())) {
				clearPossibleEntranceId(despawnedObject.getId());
			}

			if (entrances != null) {
				entrances.removeIf(entrance ->
					entrance.getTarget().getId() == despawnedObject.getId());

				if (entrances.isEmpty()) {
					possibleEntrances.remove(regionId);
				}
			}
		}

		// Special handling for Leviathan boat despawning as a different ID
		else if (despawnedObject.getId() == net.runelite.api.gameval.ObjectID.DT2_SCAR_BOAT_ISLAND) {
			possibleEntrances.remove(8292);
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
									locatedWorldPoint,
									spawnedNpcId
							)
					);
				}
			} else if (spawnedNpcId == NpcID.WHISPERER_SPAWN) {
				for (int regionId : this.whispererEntranceRegionIds) {
					possibleEntrances.computeIfAbsent(regionId, k -> new ArrayList<>()).add(
							new EscapeCrystalNotifyLocatedEntrance(
									new EscapeCrystalNotifyRegionEntranceObject(spawnedNpc),
									EscapeCrystalNotifyRegion.BOSS_THE_WHISPERER.getRegionEntrance(),
									locatedWorldPoint,
									spawnedNpcId
							)
					);
				}
			} else if (spawnedNpcId == NpcID.DT2_PURSUER_HIDEOUT_COMBAT) {
				this.clearPossibleEntranceId(ObjectID.DT2_HIDEOUT_ALTAR_OP);
			} else {
				possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
						new EscapeCrystalNotifyLocatedEntrance(
								new EscapeCrystalNotifyRegionEntranceObject(spawnedNpc),
								this.chunkEntranceMap.get(locatedChunkId),
								locatedWorldPoint,
								spawnedNpcId
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

		if (NPC_ENTRANCE_FORCE_CLEAR_IDS.contains(despawnedNpc.getId())) {
			clearPossibleEntranceId(despawnedNpc.getId());
			return;
		}

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
		NPC changedNpc = npc.getNpc();

		if (changedNpc.getId() == NpcID.WHISPERER || changedNpc.getId() == NpcID.WHISPERER_QUEST) {
			this.clearPossibleChangedEntranceId(NpcID.WHISPERER_SPAWN);
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
							locatedWorldPoint,
							spawnedObject.getId()
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
									locatedWorldPoint,
									spawnedObject.getId()
							)
					);
				}
			} else {
				possibleEntrances.computeIfAbsent(locatedWorldPoint.getRegionID(), k -> new ArrayList<>()).add(
						new EscapeCrystalNotifyLocatedEntrance(
								new EscapeCrystalNotifyRegionEntranceObject(spawnedObject),
								this.chunkEntranceMap.get(locatedChunkId),
								locatedWorldPoint,
								spawnedObject.getId()
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
				lastCombatTime = null;
				break;
			case LOGGED_IN:
				if (ready)
				{
					loginTime = Instant.now();
					ticksSinceLogin = 0;
					lastCombatTime = null;
					ready = false;
				}
				break;
			case LOGIN_SCREEN:
            case LOADING:
                this.possibleEntrances.clear();

				if (NPC_ENTRANCE_AUTO_RECHECK_ON_LOAD_REGION_IDS.contains(this.currentRegionId)) {
					this.recheckLocalNpcs = true;
				}

				break;
        }
	}

	private EscapeCrystalNotifyAccountType determineAccountType() {
		if (config.testingAccountTypeOverride() != EscapeCrystalNotifyConfig.TestingAccountType.DEFAULT) {
			switch (config.testingAccountTypeOverride()) {
				case NON_HARDCORE: return EscapeCrystalNotifyAccountType.NON_HARDCORE;
				case STANDARD_HARDCORE: return EscapeCrystalNotifyAccountType.STANDARD_HARDCORE;
				case GROUP_HARDCORE: return EscapeCrystalNotifyAccountType.GROUP_HARDCORE;
				default: break;
			}
		}

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
		this.inTzhaarEntranceRegion = this.tzhaarEntranceRegionIds.contains(this.currentRegionId);
		this.atTeleportDisabledRegion = this.isRegionTeleportDisabled(this.currentRegionId);

		this.recheckLocalNpcs();

		this.computeEntranceObjectMetrics();
	}

	private void computeWorldPointMetrics() {
		this.currentWorldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		this.currentRegionId = this.currentWorldPoint.getRegionID();
		this.currentPlaneId = this.currentWorldPoint.getPlane();
		this.currentChunkId = this.computeChunkIdFromWorldPoint(this.currentWorldPoint);
	}

	private void computeEntranceObjectMetrics() {
		if (this.currentRegionId == YAMA_REGION_ID) {
			this.clearPossibleChangedEntranceId(NpcID.YAMA_THRONE_OCCUPIED);
		}

		if (!this.atNotifyRegionId && !this.inTzhaarEntranceRegion) {
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

		if (this.currentRegionId == ZULRAH_ENTRANCE_REGION_ID) this.updateZulrahRegionsInSet();

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

		if (client.getLocalPlayer().getHealthScale() != -1) {
			this.lastCombatTime = Instant.now();
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

	private Set<Integer> getTargetRegionIdsFromConfig(EscapeCrystalNotifyAccountType accountType) {
		ArrayList<EscapeCrystalNotifyRegionType> targetRegions = new ArrayList<>();

		if (config.displayBosses()) targetRegions.add(EscapeCrystalNotifyRegionType.BOSSES);
		if (config.displayRaids()) targetRegions.add(EscapeCrystalNotifyRegionType.RAIDS);
		if (config.displayDungeons()) targetRegions.add(EscapeCrystalNotifyRegionType.DUNGEONS);
		if (config.displayMinigames()) targetRegions.add(EscapeCrystalNotifyRegionType.MINIGAMES);
		if (config.displayTeleportDisabled()) targetRegions.add(EscapeCrystalNotifyRegionType.TELEPORT_DISABLED);

		Set<Integer> regionIds = new HashSet<>(EscapeCrystalNotifyRegion.getRegionIdsFromTypes(targetRegions, getTargetDeathTypes(accountType)));
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
		if (config.requireCombatForNotifications() && this.getTimeSinceLastCombat() > config.combatGracePeriodSeconds()) {
			return;
		}

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

		String optionText = ColorUtil.wrapWithColorTag(config.deprioritizedMenuText(), config.deprioritizedMenuTextColor());
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

	public boolean isTeleportDisabledPanelEnabled() {
		return this.atTeleportDisabledRegion && config.displayTeleportDisabled();
	}

	public boolean isCloseToSixHourLogout() {
		return ticksSinceLogin >= SIX_HOUR_LOG_WARNING_THRESHOLD_TICKS;
	}

	public boolean hasDiedAtZulrah() {
		return client.getVarbitValue(ZULRAH_REVIVE_VARBIT) == 1;
	}

	public boolean isQuestCompleted(Quest quest) {
		if (client != null && quest != null) {
			return quest.getState(client) == QuestState.FINISHED;
		}
		return false;
	}

	public boolean isRegionTeleportDisabled(int regionId) {
		for (EscapeCrystalNotifyRegion region : EscapeCrystalNotifyRegion.values()) {
			if (region.getRegionType() == EscapeCrystalNotifyRegionType.TELEPORT_DISABLED) {
				if (Arrays.stream(region.getRegionIds()).anyMatch(id -> id == regionId)) {
					if (region.getQuestNotCompleted() != null) {
						return !this.isQuestCompleted(region.getQuestNotCompleted());
					}
					return true;
				}
			}
		}
		return false;
	}

	private void updateZulrahRegionsInSet() {
		if (config.excludeZulrahWithEliteDiary() && this.completedWesternEliteDiary) {
			if (hasDiedAtZulrah()) {
				this.targetRegionIds.addAll(this.zulrahRegionIds);
				this.targetRegionIds.addAll(this.zulrahEntranceRegionIds);
			} else {
				this.targetRegionIds.removeAll(this.zulrahRegionIds);
				this.targetRegionIds.removeAll(this.zulrahEntranceRegionIds);
			}
		}
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

	public void toggleTestingMode() {
		this.testingModeEnabled = !this.testingModeEnabled;
		log.info("Testing mode " + (this.testingModeEnabled ? "enabled" : "disabled"));
	}

	public WorldPoint resolvePossiblyInstancedWorldPoint(WorldPoint worldPoint, LocalPoint localPoint) {
		if (client.isInInstancedRegion()) {
			return WorldPoint.fromLocalInstance(client, localPoint);
		}
		return worldPoint;
	}

	public void clearPossibleEntranceId(int entranceId) {
		for (int regionId : this.possibleEntrances.keySet()) {
			List<EscapeCrystalNotifyLocatedEntrance> entrances = this.possibleEntrances.get(regionId);

			if (entrances != null) {
				entrances.removeIf(entrance -> entrance.getTarget().getId() == entranceId);

				if (entrances.isEmpty()) {
					this.possibleEntrances.remove(regionId);
				}
			}
		}
	}

	public void clearPossibleChangedEntranceId(int entranceId) {
		for (int regionId : this.possibleEntrances.keySet()) {
			List<EscapeCrystalNotifyLocatedEntrance> entrances = this.possibleEntrances.get(regionId);

			if (entrances != null) {
				entrances.removeIf(entrance -> entrance.getInitialTargetId() == entranceId && entrance.hasChangedTargetId());

				if (entrances.isEmpty()) {
					this.possibleEntrances.remove(regionId);
				}
			}
		}
	}

	public void recheckLocalNpcs() {
		if (!this.recheckLocalNpcs) return;

		this.recheckLocalNpcs = false;

		for (NPC npc : client.getTopLevelWorldView().npcs()) {
			if (npc != null && NPC_ENTRANCE_AUTO_RECHECK_ON_LOAD_NPC_IDS.contains(npc.getId())) {
				onNpcSpawned(new NpcSpawned(npc));
			}
		}
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

	private int getTimeSinceLastCombat() {
		if (this.lastCombatTime == null) {
			return 0;
		}

		long timeSinceCombat = System.currentTimeMillis() - this.lastCombatTime.toEpochMilli();	
		return (int) Math.round(timeSinceCombat / 1000.0);
	}
}
