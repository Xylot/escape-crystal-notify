package com.escapecrystalnotify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyAccountTypeTest {
    @Test
    public void hardcoreMainUsesGroupHardcorePolicyWithoutReadingAccountVarbit() throws Exception {
        Fixture main = new Fixture();
        main.worlds.add(WorldType.HARDCORE_MAIN);
        main.worlds.add(WorldType.MEMBERS);
        main.rejectAccountVarbit = true;
        main.refresh();

        Fixture group = new Fixture();
        group.accountVarbit = 5;
        group.refresh();

        assertEquals(EscapeCrystalNotifyAccountType.HARDCORE_MAIN, main.plugin.getAccountType());
        assertTrue(main.plugin.isHardcoreAccountType());
        assertTrue(main.plugin.isAccountTypeEnabled());
        for (String cache : new String[]{"targetRegionIds", "npcEntranceIds", "gameObjectEntranceIds",
            "entranceOnlyRegionIds", "instancedOnlyRegionIds"}) {
            assertEquals(cache, get(group.plugin, cache), get(main.plugin, cache));
        }
        assertFalse("Inferno remains dangerous on Hardcore Main", main.plugin.isRegionSafe(9043));
    }

    @Test
    public void worldChangesRecomputeAccountAndRegionPolicies() throws Exception {
        Fixture f = new Fixture();
        for (int varbit : new int[]{0, 3, 5}) {
            f.accountVarbit = varbit;
            f.worlds.add(WorldType.HARDCORE_MAIN);
            f.refresh();
            assertEquals(EscapeCrystalNotifyAccountType.HARDCORE_MAIN, f.plugin.getAccountType());
            assertTrue(f.plugin.isHardcoreAccountType());
            assertFalse(f.plugin.isRegionSafe(9043));

            f.worlds.clear();
            f.refresh();
            EscapeCrystalNotifyAccountType expected = varbit == 3
                ? EscapeCrystalNotifyAccountType.STANDARD_HARDCORE
                : varbit == 5 ? EscapeCrystalNotifyAccountType.GROUP_HARDCORE
                : EscapeCrystalNotifyAccountType.NON_HARDCORE;
            assertEquals(expected, f.plugin.getAccountType());
            assertEquals(varbit != 0, f.plugin.isHardcoreAccountType());
            assertEquals(varbit != 5, f.plugin.isRegionSafe(9043));
        }
    }

    @Test
    public void developerOverrideStillTakesPrecedenceOnHardcoreMainWorlds() throws Exception {
        Fixture f = new Fixture();
        f.worlds.add(WorldType.HARDCORE_MAIN);
        set(f.plugin, "developerMode", true);
        set(f.plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public TestingAccountType testingAccountTypeOverride() {
                return TestingAccountType.NON_HARDCORE;
            }
        });
        f.refresh();
        assertEquals(EscapeCrystalNotifyAccountType.NON_HARDCORE, f.plugin.getAccountType());
        assertFalse(f.plugin.isHardcoreAccountType());
    }

    @Test
    public void hardcoreMainTestingOverrideAppliesSamePolicyOnOrdinaryWorlds() throws Exception {
        Fixture f = new Fixture();
        f.rejectAccountVarbit = true;
        set(f.plugin, "developerMode", true);
        set(f.plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public TestingAccountType testingAccountTypeOverride() {
                return TestingAccountType.HARDCORE_MAIN;
            }
        });
        f.refresh();
        assertEquals(EscapeCrystalNotifyAccountType.HARDCORE_MAIN, f.plugin.getAccountType());
        assertTrue(f.plugin.isHardcoreAccountType());
        assertTrue(f.plugin.isAccountTypeEnabled());
        assertFalse(f.plugin.isRegionSafe(9043));
        Fixture group = new Fixture();
        group.accountVarbit = 5;
        group.refresh();
        assertEquals(get(group.plugin, "targetRegionIds"), get(f.plugin, "targetRegionIds"));
    }

    @Test
    public void regionPoliciesMatchAcrossAllFilterCombinations() throws Exception {
        Fixture main = new Fixture();
        main.worlds.add(WorldType.HARDCORE_MAIN);
        main.refresh();
        Fixture group = new Fixture();
        group.accountVarbit = 5;
        group.refresh();
        for (int mask = 0; mask < 256; mask++) {
            final int options = mask;
            EscapeCrystalNotifyConfig config = new EscapeCrystalNotifyConfig() {
                @Override public boolean displayBosses() { return (options & 1) != 0; }
                @Override public boolean displayRaids() { return (options & 2) != 0; }
                @Override public boolean displayDungeons() { return (options & 4) != 0; }
                @Override public boolean displayMinigames() { return (options & 8) != 0; }
                @Override public boolean displayTeleportDisabled() { return (options & 16) != 0; }
                @Override public boolean excludeSafeForRegularHardcoreRegions() { return (options & 32) != 0; }
                @Override public boolean excludeZulrahWithEliteDiary() { return (options & 64) != 0; }
                @Override public String includeRegionIds() { return (options & 128) != 0 ? "9043,13107,12345" : ""; }
                @Override public String excludeRegionIds() { return (options & 128) != 0 ? "9043,12582" : ""; }
            };
            for (boolean diary : new boolean[]{false, true}) {
                for (Fixture f : new Fixture[]{main, group}) {
                    set(f.plugin, "config", config);
                    set(f.plugin, "completedWesternEliteDiary", diary);
                    f.plugin.onConfigChanged(null);
                }
                for (String cache : new String[]{"targetRegionIds", "npcEntranceIds", "gameObjectEntranceIds",
                    "entranceOnlyRegionIds", "instancedOnlyRegionIds", "manuallyExcludedRegionIds"}) {
                    assertEquals("options=" + mask + ", diary=" + diary + ", " + cache,
                        get(group.plugin, cache), get(main.plugin, cache));
                }
                for (int region : EscapeCrystalNotifyRegion.getAllRegionIds()) {
                    assertEquals("options=" + mask + ", region=" + region,
                        group.plugin.isRegionSafe(region), main.plugin.isRegionSafe(region));
                }
            }
        }
    }

    @Test
    public void notificationDecisionsMatchAcrossCrystalAndLocationStates() throws Exception {
        Fixture main = new Fixture();
        main.worlds.add(WorldType.HARDCORE_MAIN);
        main.refresh();
        Fixture group = new Fixture();
        group.accountVarbit = 5;
        group.refresh();
        String[] inputs = {"atNotifyRegionId", "enteredNotifyRegionId", "escapeCrystalWithPlayer",
            "escapeCrystalActive", "escapeCrystalLeftClickTeleportEnabled", "inTimeRemainingThreshold"};
        String[] outputs = {"notifyMissing", "notifyInactive", "notifyTimeRemainingThreshold",
            "notifyNonLeftClickTeleport", "inTimeRemainingThreshold", "enteredTimeRemainingThreshold"};
        for (int mask = 0; mask < 256; mask++) {
            final int options = mask;
            for (Fixture f : new Fixture[]{main, group}) {
                set(f.plugin, "config", new EscapeCrystalNotifyConfig() {
                    @Override public boolean requireHardcoreAccountType() { return (options & 64) != 0; }
                });
                for (int i = 0; i < inputs.length; i++) set(f.plugin, inputs[i], (mask & (1 << i)) != 0);
                for (String flag : new String[]{"notifyMissing", "notifyInactive", "notifyTimeRemainingThreshold",
                    "notifyNonLeftClickTeleport"}) set(f.plugin, flag, false);
                set(f.plugin, "timeRemainingThresholdTicks", 10);
                set(f.plugin, "expectedTicksUntilTeleport", (mask & 128) != 0 ? 5 : 20);
                invoke(f.plugin, "computeNotificationMetrics");
            }
            for (String output : outputs) {
                assertEquals("state=" + mask + ", " + output, get(group.plugin, output), get(main.plugin, output));
            }
            assertEquals(group.plugin.isAccountTypeEnabled(), main.plugin.isAccountTypeEnabled());
        }
    }

    @Test
    public void everySafeguardModeTreatsBothAsHardcore() throws Exception {
        for (EscapeCrystalNotifyAccountType account : new EscapeCrystalNotifyAccountType[]{
            EscapeCrystalNotifyAccountType.GROUP_HARDCORE, EscapeCrystalNotifyAccountType.HARDCORE_MAIN}) {
            Fixture f = new Fixture();
            f.accountVarbit = 5;
            if (account == EscapeCrystalNotifyAccountType.HARDCORE_MAIN) f.worlds.add(WorldType.HARDCORE_MAIN);
            f.refresh();
            for (EscapeCrystalNotifyConfig.SafeguardAccountType mode : EscapeCrystalNotifyConfig.SafeguardAccountType.values()) {
                set(f.plugin, "config", new EscapeCrystalNotifyConfig() {
                    @Override public SafeguardAccountType leviathanLogoutSafeguardMode() { return mode; }
                    @Override public SafeguardAccountType doomLogoutSafeguardMode() { return mode; }
                    @Override public SafeguardAccountType leviathanSixHourMode() { return mode; }
                    @Override public SafeguardAccountType doomSixHourMode() { return mode; }
                });
                boolean expected = mode != EscapeCrystalNotifyConfig.SafeguardAccountType.DISABLED;
                assertEquals(expected, f.plugin.isLeviathanSafeguardEnabled());
                assertEquals(expected, f.plugin.isDoomSafeguardEnabled());
                assertEquals(expected, f.plugin.isLeviathanSixHourWarningEnabled());
                assertEquals(expected, f.plugin.isDoomSixHourWarningEnabled());
            }
        }
    }

    private static Object invoke(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static class Fixture {
        final EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
        final EnumSet<WorldType> worlds = EnumSet.noneOf(WorldType.class);
        int accountVarbit;
        boolean rejectAccountVarbit;

        Fixture() throws Exception {
            set(plugin, "config", new EscapeCrystalNotifyConfig() {});
            Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (p, m, a) -> m.getName().equals("getHealthScale") ? -1 : null);
            set(plugin, "client", Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[]{Client.class},
                (p, m, a) -> {
                    switch (m.getName()) {
                        case "getLocalPlayer": return player;
                        case "getWorldType": return worlds;
                        case "getVarbitValue":
                            if ((int) a[0] == Varbits.ACCOUNT_TYPE) {
                                if (rejectAccountVarbit) fail("Hardcore Main must not read the account type varbit");
                                return accountVarbit;
                            }
                            return 0;
                        default: return null;
                    }
                }));
            plugin.onConfigChanged(null);
        }

        void refresh() throws Exception {
            Method method = EscapeCrystalNotifyPlugin.class.getDeclaredMethod("computeAccountTypeMetrics");
            method.setAccessible(true);
            method.invoke(plugin);
        }
    }

    private static Object get(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
