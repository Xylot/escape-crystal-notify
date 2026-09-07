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

        set(plugin, "config", debugConfig(true));
        plugin.onConfigChanged(null);
        compute.invoke(plugin);
        assertEquals(2, plugin.getValidEntrances().size());

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
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getId": return id;
                    case "getWorldLocation": return new WorldPoint(3200, 3200, 0);
                    default: return null;
                }
            }));
    }
}