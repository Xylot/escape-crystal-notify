package com.escapecrystalnotify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyRegionPolicyTest {
    private static final WorldPoint SHELLBANE_ENTRANCE = new WorldPoint(3176, 2477, 0);
    private static final WorldPoint SHELLBANE_ARENA = region(12682, 0);

    @Test
    public void safeApproachDoesNotConsumeArenaEntryNotificationAndClearsWarningsOnReturn() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        set(plugin, "escapeCrystalWithPlayer", false);
        set(plugin, "escapeCrystalActive", false);
        at(plugin, SHELLBANE_ENTRANCE);
        invoke(plugin, "computeNotificationMetrics");
        assertFalse(flag(plugin, "notifyMissing"));
        assertFalse(flag(plugin, "notifyInactive"));
        assertTrue(plugin.isAtEntranceLocation());

        at(plugin, SHELLBANE_ARENA);
        invoke(plugin, "computeNotificationMetrics");
        assertTrue(flag(plugin, "enteredNotifyRegionId"));
        assertTrue(flag(plugin, "notifyMissing"));
        assertTrue(flag(plugin, "notifyInactive"));

        at(plugin, SHELLBANE_ARENA);
        assertFalse(flag(plugin, "enteredNotifyRegionId"));
        at(plugin, SHELLBANE_ENTRANCE);
        set(plugin, "notifyTimeRemainingThreshold", true);
        set(plugin, "notifyNonLeftClickTeleport", true);
        invoke(plugin, "computeNotificationMetrics");
        assertFalse(flag(plugin, "notifyMissing"));
        assertFalse(flag(plugin, "notifyInactive"));
        assertFalse(flag(plugin, "notifyTimeRemainingThreshold"));
        assertFalse(flag(plugin, "notifyNonLeftClickTeleport"));
    }

    @Test
    public void existingChunkAndPlaneRestrictionsStillGateNotificationsAndEntrances() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        at(plugin, chunk(785665)); // King Black Dragon entrance: restricted to two chunks.
        assertTrue(plugin.isAtNotifyRegionId());
        assertTrue(plugin.isAtEntranceLocation());
        at(plugin, region(12192, 0));
        assertFalse(plugin.isAtNotifyRegionId());
        assertFalse(plugin.isAtEntranceLocation());

        at(plugin, region(5525, 0)); // Perilous Moons: ground floor only.
        assertTrue(plugin.isAtNotifyRegionId());
        at(plugin, region(5525, 1));
        assertFalse(plugin.isAtNotifyRegionId());
        assertFalse(plugin.isAtEntranceLocation());

        at(plugin, chunk(822449)); // Excluded safe chunk in Lumbridge basement.
        assertFalse(plugin.isAtNotifyRegionId());
        assertFalse(plugin.isAtEntranceLocation());
    }

    @Test
    public void allAccountTypesKeepSafeEntranceSeparateAndInfernoStillRequiresGroupHardcore() throws Exception {
        for (EscapeCrystalNotifyAccountType account : EscapeCrystalNotifyAccountType.values()) {
            EscapeCrystalNotifyPlugin plugin = plugin(account);
            at(plugin, SHELLBANE_ENTRANCE);
            assertFalse(account.name(), plugin.isAtNotifyRegionId());
            assertTrue(account.name(), plugin.isAtEntranceLocation());
            at(plugin, SHELLBANE_ARENA);
            assertTrue(account.name(), plugin.isAtNotifyRegionId());
            at(plugin, region(9043, 0));
            assertEquals(account.name(), account == EscapeCrystalNotifyAccountType.GROUP_HARDCORE,
                plugin.isAtNotifyRegionId());
        }
    }

    @Test
    public void explicitOverridesAndExclusionsRetainTheirPrecedence() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean displayEverywhere() { return true; }
            @Override public boolean displayBosses() { return false; }
        });
        plugin.onConfigChanged(null);
        at(plugin, SHELLBANE_ENTRANCE);
        assertTrue(plugin.isAtNotifyRegionId());
        assertTrue(plugin.isAtEntranceLocation());

        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean displayEverywhere() { return true; }
            @Override public String includeRegionIds() { return "12582"; }
            @Override public String excludeRegionIds() { return "12582"; }
        });
        plugin.onConfigChanged(null);
        at(plugin, SHELLBANE_ENTRANCE);
        assertFalse(plugin.isAtNotifyRegionId());
        assertFalse(plugin.isAtEntranceLocation());

        set(plugin, "config", new EscapeCrystalNotifyConfig() {});
        plugin.onConfigChanged(null);
        at(plugin, SHELLBANE_ENTRANCE);
        assertFalse(plugin.isAtNotifyRegionId());
        assertTrue(plugin.isAtEntranceLocation());
    }

    @Test
    public void crystalUsesNotifyRegionsIncludingGlobalOverrideButNotEntranceOnlyAreas() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        at(plugin, SHELLBANE_ENTRANCE);
        assertFalse(crystalNotifyLocation(plugin));
        at(plugin, SHELLBANE_ARENA);
        assertTrue(crystalNotifyLocation(plugin));
        at(plugin, chunk(785665));
        assertTrue(crystalNotifyLocation(plugin));
        at(plugin, region(12192, 0));
        assertFalse(crystalNotifyLocation(plugin));
        at(plugin, region(5525, 1));
        assertFalse(crystalNotifyLocation(plugin));

        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean displayEverywhere() { return true; }
            @Override public boolean displayBosses() { return false; }
        });
        plugin.onConfigChanged(null);
        at(plugin, SHELLBANE_ENTRANCE);
        assertTrue(plugin.isAtNotifyRegionId());
        assertTrue(crystalNotifyLocation(plugin));
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean displayEverywhere() { return true; }
            @Override public String excludeRegionIds() { return "12582"; }
        });
        plugin.onConfigChanged(null);
        at(plugin, SHELLBANE_ENTRANCE);
        assertFalse(crystalNotifyLocation(plugin));
    }

    @Test
    public void crystalReceivesExistingTickMetricsAndFramesOnlyRender() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin(EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        int[] calls = {0, 0};
        set(plugin, "crystal3d", new EscapeCrystalNotifyCrystal3d(null, null) {
            @Override void updateState(boolean carried, boolean active, boolean notify, int remaining, int total) {
                calls[0]++;
                assertTrue(carried);
                assertTrue(active);
                assertTrue(notify);
                assertEquals(25, remaining);
                assertEquals(100, total);
            }
            @Override void update() { calls[1]++; }
        });
        set(plugin, "escapeCrystalWithPlayer", true);
        set(plugin, "escapeCrystalActive", true);
        set(plugin, "atNotifyRegionId", true);
        set(plugin, "expectedTicksUntilTeleport", 25);
        set(plugin, "escapeCrystalInactivityTicks", 100);
        plugin.updateCrystal3dState();
        for (int i = 0; i < 100; i++) plugin.onBeforeRender(null);
        assertEquals(1, calls[0]);
        assertEquals(100, calls[1]);
    }

    private static boolean crystalNotifyLocation(EscapeCrystalNotifyPlugin plugin) throws Exception {
        boolean[] notifyLocation = {false};
        set(plugin, "crystal3d", new EscapeCrystalNotifyCrystal3d(null, null) {
            @Override void updateState(boolean carried, boolean active, boolean notify, int remaining, int total) {
                notifyLocation[0] = notify;
            }
        });
        plugin.updateCrystal3dState();
        return notifyLocation[0];
    }

    private static EscapeCrystalNotifyPlugin plugin(EscapeCrystalNotifyAccountType account) throws Exception {
        EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
        set(plugin, "config", new EscapeCrystalNotifyConfig() {});
        set(plugin, "accountType", account);
        set(plugin, "hardcoreAccountType", account != EscapeCrystalNotifyAccountType.NON_HARDCORE);
        Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
            (p, m, a) -> m.getName().equals("getHealthScale") ? -1 : null);
        set(plugin, "client", Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[]{Client.class},
            (p, m, a) -> m.getName().equals("getLocalPlayer") ? player : null));
        plugin.onConfigChanged(null);
        return plugin;
    }

    // Supply world coordinates directly, then exercise the production location and notification policies.
    private static void at(EscapeCrystalNotifyPlugin plugin, WorldPoint point) throws Exception {
        boolean previous = plugin.isAtNotifyRegionId();
        set(plugin, "currentWorldPoint", point);
        set(plugin, "currentRegionId", point.getRegionID());
        set(plugin, "currentChunkId", EscapeCrystalNotifyLocatedEntrance.computeChunkIdFromWorldPoint(point));
        set(plugin, "currentPlaneId", point.getPlane());
        set(plugin, "regionLocationRequirementsMet", invoke(plugin, "meetsRegionLocationRequirements"));
        set(plugin, "atSafeRegionId", plugin.isRegionSafe(point.getRegionID()));
        boolean notify = (boolean) invoke(plugin, "checkAtNotifyLocation");
        set(plugin, "atNotifyRegionId", notify);
        set(plugin, "enteredNotifyRegionId", !previous && notify);
        invoke(plugin, "computeEntranceObjectMetrics");
    }

    private static WorldPoint region(int id, int plane) {
        return new WorldPoint((id >> 8) * 64, (id & 255) * 64, plane);
    }

    private static WorldPoint chunk(int id) {
        return new WorldPoint((id >> 11) * 8, (id & 2047) * 8, 0);
    }

    private static Object invoke(EscapeCrystalNotifyPlugin plugin, String name) throws Exception {
        Method method = EscapeCrystalNotifyPlugin.class.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(plugin);
    }

    private static boolean flag(EscapeCrystalNotifyPlugin plugin, String name) throws Exception {
        return field(name).getBoolean(plugin);
    }

    private static void set(EscapeCrystalNotifyPlugin plugin, String name, Object value) throws Exception {
        field(name).set(plugin, value);
    }

    private static Field field(String name) throws Exception {
        Field field = EscapeCrystalNotifyPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
