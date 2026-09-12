package com.escapecrystalnotify;

import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyClosestEntranceTest {
    private final WorldView view = proxy(WorldView.class, (n, a) -> null);
    private final Player player = proxy(Player.class, (n, a) -> n.equals("getWorldView") ? view
        : n.equals("getWorldLocation") ? new WorldPoint(6400, 6400, 0) : null);
    private final Client client = proxy(Client.class, (n, a) -> n.equals("getLocalPlayer") ? player : null);

    @Test
    public void defaultsOffAndSupportsFluentOptIn() {
        EscapeCrystalNotifyRegionEntrance definition = definition();
        assertFalse(definition.isClosest());
        assertSame(definition, definition.withClosest());
        assertTrue(definition.isClosest());
    }

    @Test
    public void selectsUsingWorldPointDistanceAndPreservesFirstTie() {
        EscapeCrystalNotifyRegionEntrance definition = definition().withClosest();
        EscapeCrystalNotifyLocatedEntrance diagonal = entrance(definition, 4, 4, 0, view);
        EscapeCrystalNotifyLocatedEntrance straight = entrance(definition, 5, 0, 0, view);
        EscapeCrystalNotifyLocatedEntrance tie = entrance(definition, 0, 4, 0, view);
        assertEquals(List.of(diagonal), select(straight, diagonal, tie));
    }

    @Test
    public void excludesCloserCopyOnOtherPlaneOrInOtherWorldView() {
        EscapeCrystalNotifyRegionEntrance definition = definition().withClosest();
        EscapeCrystalNotifyLocatedEntrance upstairs = entrance(definition, 0, 0, 1, view);
        EscapeCrystalNotifyLocatedEntrance otherView = entrance(definition, 0, 0, 0, proxy(WorldView.class, (n, a) -> null));
        EscapeCrystalNotifyLocatedEntrance valid = entrance(definition, 10, 10, 0, view);
        assertEquals(List.of(valid), select(upstairs, otherView, valid));
        assertTrue(select(upstairs, otherView).isEmpty());
    }

    @Test
    public void selectsPerIdAndLeavesUnflaggedEntrancesAlone() {
        EscapeCrystalNotifyRegionEntrance first = definition().withClosest(), second = definition().withClosest();
        second.entranceIds = new int[]{2};
        EscapeCrystalNotifyRegionEntrance normal = definition();
        EscapeCrystalNotifyLocatedEntrance a = entrance(first, 2, 2, 0, view);
        EscapeCrystalNotifyLocatedEntrance b = entrance(second, 8, 8, 0, view);
        EscapeCrystalNotifyLocatedEntrance c = entrance(normal, 0, 0, 1, view);
        EscapeCrystalNotifyLocatedEntrance d = entrance(normal, 9, 9, 0, view);
        assertEquals(List.of(a, b, c, d), select(a, b, c, d));
    }

    @Test
    public void replacesByIdAcrossDefinitionsWithoutMovingOtherEntries() {
        EscapeCrystalNotifyLocatedEntrance far = entrance(definition().withClosest(), 8, 8, 0, view);
        EscapeCrystalNotifyLocatedEntrance normal = entrance(definition(), 6, 6, 0, view);
        EscapeCrystalNotifyLocatedEntrance near = entrance(definition().withClosest(), 1, 1, 0, view);
        assertEquals(List.of(near, normal), select(far, normal, near));
    }

    @Test
    public void invalidCloserCopyDoesNotReplaceEligibleEntrance() {
        EscapeCrystalNotifyRegionEntrance invalid = definition().withClosest();
        invalid.chunkIds = List.of(-1);
        EscapeCrystalNotifyLocatedEntrance near = entrance(invalid, 1, 1, 0, view);
        EscapeCrystalNotifyLocatedEntrance far = entrance(definition().withClosest(), 8, 8, 0, view);
        assertEquals(List.of(far), select(near, far));
    }

    @Test
    public void missingPlayerOnlyRemovesFlaggedEntrances() {
        EscapeCrystalNotifyLocatedEntrance a = entrance(definition().withClosest(), 1, 1, 0, view);
        EscapeCrystalNotifyLocatedEntrance b = entrance(definition(), 2, 2, 0, view);
        assertEquals(List.of(b), selectWithClient(null, a, b));
    }

    @Test
    public void supportsNpcEntranceAndChecksItsPlane() {
        EscapeCrystalNotifyRegionEntrance definition = definition().withClosest();
        for (int plane = 0; plane <= 1; plane++) {
            WorldPoint point = new WorldPoint(6401, 6401, plane);
            NPC npc = proxy(NPC.class, (n, a) -> n.equals("getWorldView") ? view : n.equals("getWorldLocation") ? point : null);
            EscapeCrystalNotifyLocatedEntrance entrance = new EscapeCrystalNotifyLocatedEntrance(
                new EscapeCrystalNotifyRegionEntranceObject(npc), definition, point, 1);
            assertEquals(plane == 0 ? List.of(entrance) : List.of(), select(entrance));
        }
    }

    private EscapeCrystalNotifyRegionEntrance definition() {
        return new EscapeCrystalNotifyRegionEntrance(EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT,
            null, EscapeCrystalNotifyRegionEntranceObjectType.ANY, 1);
    }

    private EscapeCrystalNotifyLocatedEntrance entrance(EscapeCrystalNotifyRegionEntrance definition, int x, int y, int plane, WorldView targetView) {
        WorldPoint point = new WorldPoint(6400 + x, 6400 + y, plane);
        GameObject object = proxy(GameObject.class, (n, a) -> {
            switch (n) {
                case "getWorldView": return targetView;
                case "getWorldLocation": return point;
                case "getPlane": return plane;
                case "getId": return definition.getEntranceIds()[0];
                default: return null;
            }
        });
        EscapeCrystalNotifyRegionEntranceObject target = new EscapeCrystalNotifyRegionEntranceObject(object) {
            @Override public WorldPoint getWorldLocation() {
                return new WorldPoint(3200, 3200, 0);
            }
        };
        return new EscapeCrystalNotifyLocatedEntrance(target, definition, target.getWorldLocation(), definition.getEntranceIds()[0]);
    }

    private List<EscapeCrystalNotifyLocatedEntrance> select(EscapeCrystalNotifyLocatedEntrance... entries) {
        return selectWithClient(client, entries);
    }

    private List<EscapeCrystalNotifyLocatedEntrance> selectWithClient(Client selectedClient, EscapeCrystalNotifyLocatedEntrance... entries) {
        try {
            EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
            set(plugin, "client", selectedClient);
            set(plugin, "atNotifyRegionId", true);
            set(plugin, "possibleEntrances", Map.of(1, Arrays.asList(entries)));
            java.lang.reflect.Method compute = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("computeEntranceObjectMetrics");
            compute.setAccessible(true);
            compute.invoke(plugin);
            return plugin.getValidEntrances();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void set(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private interface Answer { Object get(String name, Object[] args); }
    private static <T> T proxy(Class<T> type, Answer answer) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, m, a) -> {
            Object value = answer.get(m.getName(), a);
            if (value != null) return value;
            if (m.getReturnType() == boolean.class) return false;
            if (m.getReturnType() == int.class) return 0;
            return null;
        }));
    }
}
