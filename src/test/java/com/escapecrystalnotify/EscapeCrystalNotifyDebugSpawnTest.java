package com.escapecrystalnotify;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyDebugSpawnTest {
    @Test
    public void ignoresKnownIdsSpawnedOutsideTheirEntranceArea() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        int objectId = EscapeCrystalNotifyRegion.BOSS_ARAXXOR.getRegionEntrance().getEntranceIds()[0];
        int npcId = EscapeCrystalNotifyRegion.BOSS_NIGHTMARE_ENTRANCE.getRegionEntrance().getEntranceIds()[0];
        set(plugin, "gameObjectEntranceIds", java.util.Set.of(objectId));
        set(plugin, "npcEntranceIds", java.util.Set.of(npcId));
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, objectId)));
        plugin.onDecorativeObjectSpawned(event(DecorativeObjectSpawned.class, target(DecorativeObject.class, objectId)));
        plugin.onWallObjectSpawned(event(WallObjectSpawned.class, target(WallObject.class, objectId)));
        plugin.onNpcSpawned(new NpcSpawned(target(NPC.class, npcId)));
        computeEntrances(plugin, new WorldPoint(3200, 3200, 0));
        plugin.onConfigChanged(null);
        assertTrue(plugin.getPossibleEntrances().isEmpty());
        assertTrue(plugin.getValidEntrances().isEmpty());
    }

    @Test
    public void resolvesShellbaneEntranceOutsideTheUndergroundArena() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        EscapeCrystalNotifyRegion region = EscapeCrystalNotifyRegion.BOSS_SHELLBANE_GRYPHON_ENTRANCE;
        int id = region.getRegionEntrance().getEntranceIds()[0];
        WorldPoint location = new WorldPoint(3176, 2477, 0);
        assertEquals(12582, location.getRegionID());
        assertTrue(EscapeCrystalNotifyRegion.getRegionIdsFromRegions(java.util.List.of(region)).contains(location.getRegionID()));
        set(plugin, "gameObjectEntranceIds", java.util.Set.of(id));
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, id, location)));
        assertEquals(1, plugin.getPossibleEntrances().get(12582).size());
        computeEntrances(plugin, location, false);
        assertFalse(plugin.isAtNotifyRegionId());
        assertTrue(plugin.isAtEntranceLocation());
        assertEquals(1, plugin.getValidEntrances().size());
        assertSame(region.getRegionEntrance(), plugin.getValidEntrances().get(0).getDefinition());
        assertEquals(java.util.Set.of(EscapeCrystalNotifyRegion.BOSS_SHELLBANE_GRYPHON), plugin.getNearbyBosses());
        java.util.Set<EscapeCrystalNotifyRegion> nearby = plugin.getNearbyBosses();
        computeEntrances(plugin, location, false);
        assertSame(nearby, plugin.getNearbyBosses()); // Unchanged ticks don't publish another panel update.
        plugin.onConfigChanged(null);
        computeEntrances(plugin, new WorldPoint(3200, 3200, 0), false);
        assertTrue(plugin.getValidEntrances().isEmpty());
        assertTrue(plugin.getNearbyBosses().isEmpty());
        computeEntrances(plugin, location, false);
        assertEquals(1, plugin.getValidEntrances().size());
        GameStateChanged loading = new GameStateChanged();
        loading.setGameState(GameState.LOADING);
        plugin.onGameStateChanged(loading);
        assertTrue(plugin.getPossibleEntrances().isEmpty());
        assertTrue(plugin.getValidEntrances().isEmpty());
        assertTrue(plugin.getNearbyBosses().isEmpty());
    }

    @Test
    public void chunkLookupMatchesTheEntranceIdAndType() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        EscapeCrystalNotifyRegionEntrance definition = EscapeCrystalNotifyRegion.BOSS_ARAXXOR.getRegionEntrance();
        int chunkId = definition.getChunkIds().get(0);
        WorldPoint location = new WorldPoint((chunkId >> 11) * 8, (chunkId & 2047) * 8, 0);
        int id = definition.getEntranceIds()[0];
        int otherId = EscapeCrystalNotifyRegion.BOSS_AMOXLIATL.getRegionEntrance().getEntranceIds()[0];
        set(plugin, "gameObjectEntranceIds", java.util.Set.of(id, otherId));
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, otherId, location)));
        assertTrue(plugin.getPossibleEntrances().isEmpty());
        assertNull(EscapeCrystalNotifyRegion.findEntrance(id, location, EscapeCrystalNotifyRegionEntranceObjectType.NPC));
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, id, location)));
        computeEntrances(plugin, location);
        assertEquals(1, plugin.getValidEntrances().size());
        assertSame(definition, plugin.getValidEntrances().get(0).getDefinition());
    }

    private void computeEntrances(EscapeCrystalNotifyPlugin plugin, WorldPoint location) throws Exception {
        computeEntrances(plugin, location, true);
    }

    private void computeEntrances(EscapeCrystalNotifyPlugin plugin, WorldPoint location, boolean notify) throws Exception {
        set(plugin, "atNotifyRegionId", notify);
        set(plugin, "currentRegionId", location.getRegionID());
        set(plugin, "currentPlaneId", location.getPlane());
        set(plugin, "currentChunkId", EscapeCrystalNotifyLocatedEntrance.computeChunkIdFromWorldPoint(location));
        set(plugin, "currentWorldPoint", location);
        java.lang.reflect.Method requirements = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("meetsRegionLocationRequirements");
        requirements.setAccessible(true);
        set(plugin, "regionLocationRequirementsMet", requirements.invoke(plugin));
        java.lang.reflect.Method compute = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("computeEntranceObjectMetrics");
        compute.setAccessible(true);
        compute.invoke(plugin);
    }

    @Test
    public void shellbaneNotifiesOnlyInArenaAndHonorsEntranceExclusions() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        WorldPoint entrance = new WorldPoint(3176, 2477, 0);
        java.lang.reflect.Method check = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("checkAtNotifyLocation");
        check.setAccessible(true);
        computeEntrances(plugin, entrance, false);
        assertFalse((boolean) check.invoke(plugin));
        assertTrue(plugin.isAtEntranceLocation());

        computeEntrances(plugin, new WorldPoint((12682 >> 8) * 64, (12682 & 255) * 64, 0), false);
        assertTrue((boolean) check.invoke(plugin));

        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean displayBosses() { return false; }
        });
        plugin.onConfigChanged(null);
        computeEntrances(plugin, entrance, false);
        assertFalse(plugin.isAtEntranceLocation());

        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public String excludeRegionIds() { return "12582"; }
        });
        plugin.onConfigChanged(null);
        assertFalse(plugin.isAtEntranceLocation());

        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public String includeRegionIds() { return "12582"; }
        });
        plugin.onConfigChanged(null);
        assertTrue((boolean) check.invoke(plugin)); // Explicit user overrides still work.
    }

    @Test
    public void entranceOnlyRegionsKeepDestinationDeathTypeFiltering() {
        java.util.List<EscapeCrystalNotifyRegionType> bosses = java.util.List.of(EscapeCrystalNotifyRegionType.BOSSES);
        java.util.List<EscapeCrystalNotifyRegionDeathType> unsafe = java.util.List.of(EscapeCrystalNotifyRegionDeathType.UNSAFE);
        assertFalse(EscapeCrystalNotifyRegion.getRegionIdsFromTypes(bosses, unsafe).contains(12582));
        assertTrue(EscapeCrystalNotifyRegion.getRegionIdsFromTypes(bosses, unsafe).contains(12682));
        assertTrue(EscapeCrystalNotifyRegion.getEntranceOnlyRegionIdsFromTypes(bosses, unsafe).contains(12582));
        assertTrue(EscapeCrystalNotifyRegion.getEntranceOnlyRegionIdsFromTypes(bosses, java.util.List.of()).isEmpty());
        assertTrue(EscapeCrystalNotifyRegion.getEntranceIdsFromTypes(
            java.util.List.of(EscapeCrystalNotifyRegionEntranceObjectType.GAME_OBJECT), unsafe).contains(58439));
        assertFalse(EscapeCrystalNotifyRegion.getEntranceIdsFromTypes(
            java.util.List.of(EscapeCrystalNotifyRegionEntranceObjectType.GAME_OBJECT), java.util.List.of()).contains(58439));
    }

    @Test
    public void nearbyEncountersGroupEntrancesAndClearOnLeavingOrLoading() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        EscapeCrystalNotifyRegion region = EscapeCrystalNotifyRegion.RAIDS_TOMBS_OF_AMASCUT_ENTRANCE;
        EscapeCrystalNotifyRegionEntrance definition = region.getRegionEntrance();
        int chunk = definition.getChunkIds().get(0);
        WorldPoint location = new WorldPoint((chunk >> 11) * 8, (chunk & 2047) * 8, 0);
        int id = definition.getEntranceIds()[0];
        set(plugin, "gameObjectEntranceIds", java.util.Set.of(id));
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, id, location)));
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, id, location)));
        computeEntrances(plugin, location);
        assertEquals(2, plugin.getValidEntrances().size());
        assertEquals(java.util.Set.of(EscapeCrystalNotifyRegion.RAIDS_TOMBS_OF_AMASCUT), plugin.getNearbyBosses());
        set(plugin, "atNotifyRegionId", false);
        java.lang.reflect.Method compute = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("computeEntranceObjectMetrics");
        compute.setAccessible(true);
        compute.invoke(plugin);
        assertTrue(plugin.getNearbyBosses().isEmpty());
        assertTrue(plugin.getValidEntrances().isEmpty());
        computeEntrances(plugin, location);
        GameStateChanged loading = new GameStateChanged();
        loading.setGameState(GameState.LOADING);
        plugin.onGameStateChanged(loading);
        assertTrue(plugin.getNearbyBosses().isEmpty());
        assertTrue(plugin.getValidEntrances().isEmpty());
    }

    @Test
    public void indexedLookupResolvesEveryConfiguredEntranceIdAndLocation() {
        for (EscapeCrystalNotifyRegion region : EscapeCrystalNotifyRegion.values()) {
            EscapeCrystalNotifyRegionEntrance definition = region.getRegionEntrance();
            if (definition == null) continue;
            java.util.List<WorldPoint> locations = new java.util.ArrayList<>();
            if (definition.getChunkIds() != null) {
                for (int chunk : definition.getChunkIds()) {
                    locations.add(new WorldPoint((chunk >> 11) * 8, (chunk & 2047) * 8, 0));
                }
            } else {
                for (int regionId : region.getRegionIds()) {
                    locations.add(new WorldPoint((regionId >> 8) * 64, (regionId & 255) * 64, 0));
                }
            }
            for (int id : definition.getEntranceIds()) {
                for (WorldPoint location : locations) {
                    assertSame(region.name() + " ID " + id + " at " + location, definition,
                        EscapeCrystalNotifyRegion.findEntrance(id, location, definition.getObjectType()));
                }
                assertNull(EscapeCrystalNotifyRegion.findEntrance(id, new WorldPoint(0, 0, 0), definition.getObjectType()));
            }
        }
        assertNull(EscapeCrystalNotifyRegion.findEntrance(-1, new WorldPoint(3200, 3200, 0), EscapeCrystalNotifyRegionEntranceObjectType.GAME_OBJECT));
    }

    @Test
    public void tracksAndDespawnsEachDebugTargetTypeWithoutAChunkDefinition() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(true);
        GameObject object = target(GameObject.class, 123);
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, object));
        assertDebugEntrance(plugin, 123);
        plugin.onGameObjectDespawned(event(GameObjectDespawned.class, object));
        assertTrue(plugin.getPossibleEntrances().isEmpty());

        DecorativeObject decoration = target(DecorativeObject.class, 123);
        plugin.onDecorativeObjectSpawned(event(DecorativeObjectSpawned.class, decoration));
        assertDebugEntrance(plugin, 123);
        plugin.onDecorativeObjectDespawned(event(DecorativeObjectDespawned.class, decoration));
        assertTrue(plugin.getPossibleEntrances().isEmpty());

        WallObject wall = target(WallObject.class, 123);
        plugin.onWallObjectSpawned(event(WallObjectSpawned.class, wall));
        assertDebugEntrance(plugin, 123);
        plugin.onWallObjectDespawned(event(WallObjectDespawned.class, wall));
        assertTrue(plugin.getPossibleEntrances().isEmpty());

        NPC npc = target(NPC.class, 456);
        plugin.onNpcSpawned(new NpcSpawned(npc));
        assertDebugEntrance(plugin, 456);
        plugin.onNpcDespawned(new NpcDespawned(npc));
        assertTrue(plugin.getPossibleEntrances().isEmpty());
    }

    @Test
    public void debugEntriesAreTrackedWhileDisabledAndBecomeValidWhenEnabled() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        GameObject object = target(GameObject.class, 123);
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, object));
        assertDebugEntrance(plugin, 123);
        NPC npc = target(NPC.class, 456);
        plugin.onNpcSpawned(new NpcSpawned(npc));
        set(plugin, "atNotifyRegionId", true);
        set(plugin, "currentWorldPoint", new WorldPoint(3200, 3200, 0));
        java.lang.reflect.Method compute = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("computeEntranceObjectMetrics");
        compute.setAccessible(true);
        compute.invoke(plugin);
        assertTrue(plugin.getValidEntrances().isEmpty());
        assertTrue(plugin.getNearbyBosses().isEmpty());

        set(plugin, "config", debugConfig(true));
        plugin.onConfigChanged(null);
        compute.invoke(plugin);
        assertEquals(2, plugin.getValidEntrances().size());
        assertTrue(plugin.getNearbyBosses().isEmpty()); // Unmapped debug targets have no boss settings row.

        set(plugin, "config", debugConfig(false));
        plugin.onConfigChanged(null);
        assertEquals(2, plugin.getPossibleEntrances().values().iterator().next().size());
        assertTrue(plugin.getValidEntrances().isEmpty());
        compute.invoke(plugin);
        assertTrue(plugin.getValidEntrances().isEmpty());
        plugin.onGameObjectDespawned(event(GameObjectDespawned.class, object));
        plugin.onNpcDespawned(new NpcDespawned(npc));
        assertTrue(plugin.getPossibleEntrances().isEmpty());
    }

    @Test
    public void ignoresIdsFromTheWrongField() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(false);
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, 456)));
        plugin.onNpcSpawned(new NpcSpawned(target(NPC.class, 123)));
        assertTrue(plugin.getPossibleEntrances().isEmpty());
    }

    @Test
    public void configChangesReplaceCachedDebugIds() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(true);
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean enableDebugMode() { return true; }
            @Override public String debugEntranceObjects() { return " 789, "; }
            @Override public String debugEntranceNpcs() { return " 987, "; }
        });
        plugin.onConfigChanged(null);
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, target(GameObject.class, 123)));
        plugin.onNpcSpawned(new NpcSpawned(target(NPC.class, 456)));
        assertTrue(plugin.getPossibleEntrances().isEmpty());
        GameObject object = target(GameObject.class, 789);
        plugin.onGameObjectSpawned(event(GameObjectSpawned.class, object));
        assertDebugEntrance(plugin, 789);
        plugin.onGameObjectDespawned(event(GameObjectDespawned.class, object));
        plugin.onNpcSpawned(new NpcSpawned(target(NPC.class, 987)));
        assertDebugEntrance(plugin, 987);

        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean enableDebugMode() { return true; }
        });
        plugin.onConfigChanged(null);
        assertTrue(plugin.getPossibleEntrances().values().stream().allMatch(java.util.List::isEmpty));
    }

    private <T> T event(Class<T> type, Object target) throws Exception {
        T event = type.getDeclaredConstructor().newInstance();
        for (java.lang.reflect.Method method : type.getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1
                && method.getParameterTypes()[0].isInstance(target)) {
                method.invoke(event, target);
                return event;
            }
        }
        throw new IllegalArgumentException("Missing target setter on " + type.getName());
    }

    private void assertDebugEntrance(EscapeCrystalNotifyPlugin plugin, int id) {
        assertEquals(1, plugin.getPossibleEntrances().size());
        assertEquals(1, plugin.getPossibleEntrances().values().iterator().next().size());
        EscapeCrystalNotifyLocatedEntrance entrance = plugin.getPossibleEntrances().values().iterator().next().get(0);
        assertEquals(id, entrance.getInitialTargetId());
        assertTrue(entrance.getDefinition().isDebug());
        assertTrue(entrance.canHighlight());
        assertTrue(entrance.canDeprioritize());
    }

    private EscapeCrystalNotifyPlugin plugin(boolean enabled) throws Exception {
        EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
        set(plugin, "config", debugConfig(enabled));
        set(plugin, "client", Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[]{Client.class},
            (proxy, method, args) -> method.getName().equals("isInInstancedRegion") ? false : null));
        set(plugin, "gameObjectEntranceIds", new HashSet<Integer>());
        set(plugin, "npcEntranceIds", new HashSet<Integer>());
        set(plugin, "accountType", EscapeCrystalNotifyAccountType.NON_HARDCORE);
        plugin.onConfigChanged(null);
        return plugin;
    }

    private EscapeCrystalNotifyConfig debugConfig(boolean enabled) {
        return new EscapeCrystalNotifyConfig() {
            @Override public boolean enableDebugMode() { return enabled; }
            @Override public String debugEntranceObjects() { return " 123, "; }
            @Override public String debugEntranceNpcs() { return " 456, "; }
        };
    }

    private void set(EscapeCrystalNotifyPlugin plugin, String name, Object value) throws Exception {
        Field field = EscapeCrystalNotifyPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }

    private <T> T target(Class<T> type, int id) {
        return target(type, id, new WorldPoint(3200, 3200, 0));
    }

    private <T> T target(Class<T> type, int id, WorldPoint location) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getId": return id;
                    case "getWorldLocation": return location;
                    default: return null;
                }
            }));
    }
}
