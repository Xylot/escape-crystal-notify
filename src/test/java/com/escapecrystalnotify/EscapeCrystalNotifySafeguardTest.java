package com.escapecrystalnotify;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Rectangle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.imageio.ImageIO;
import net.runelite.api.MenuAction;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.escapecrystalnotify.EscapeCrystalNotifyConfig.SafeguardAccountType.*;
import static com.escapecrystalnotify.EscapeCrystalNotifyConfig.GROUP;
import static org.junit.Assert.*;

public class EscapeCrystalNotifySafeguardTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ConfigManager manager;
    private EscapeCrystalNotifyConfig config;
    private EscapeCrystalNotifyPlugin plugin;
    private EscapeCrystalNotifyTextOverlayPanel overlay;

    @Before public void setup() throws Exception {
        // Exercise RuneLite's actual config proxy and writes, with an isolated in-memory profile.
        ScheduledExecutorService executor = (ScheduledExecutorService) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class[]{ScheduledExecutorService.class}, (p, m, a) -> null);
        Constructor<?> constructor = ConfigManager.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        manager = (ConfigManager) constructor.newInstance(null, executor, new EventBus(), null, null, null, null, null);
        Constructor<?> dataConstructor = Class.forName("net.runelite.client.config.ConfigData").getDeclaredConstructor(File.class);
        dataConstructor.setAccessible(true);
        field(ConfigManager.class, "configProfile").set(manager, dataConstructor.newInstance(temporaryFolder.newFile()));
        config = manager.getConfig(EscapeCrystalNotifyConfig.class);
        plugin = new EscapeCrystalNotifyPlugin();
        set("config", config);
        set("configManager", manager);
        set("accountType", EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        set("hardcoreAccountType", true);
        overlay = new EscapeCrystalNotifyTextOverlayPanel(plugin, config);
        set("escapeCrystalNotifyTextOverlayPanel", overlay);
    }

    @Test public void oldSavedModesDoNotOverrideNewDisabledDefaults() throws Exception {
        manager.setConfiguration(GROUP, "leviathanSafeguardMode", ALWAYS);
        manager.setConfiguration(GROUP, "doomSafeguardMode", HC_ONLY);
        assertEquals(DISABLED, config.leviathanLogoutSafeguardMode());
        assertEquals(DISABLED, config.doomLogoutSafeguardMode());
        assertTrue(config.displayLeviathanFixInfo());
        assertTrue(config.displayDoomFixInfo());
        assertFalse(plugin.isLeviathanSafeguardEnabled());
        assertFalse(plugin.isDoomSafeguardEnabled());
        manager.setConfiguration(GROUP, "leviathanLogoutSafeguardMode", ALWAYS);
        manager.setConfiguration(GROUP, "doomLogoutSafeguardMode", HC_ONLY);
        assertTrue(plugin.isLeviathanSafeguardEnabled());
        assertTrue(plugin.isDoomSafeguardEnabled());
        set("accountType", EscapeCrystalNotifyAccountType.NON_HARDCORE);
        set("hardcoreAccountType", false);
        assertTrue(plugin.isLeviathanSafeguardEnabled());
        assertFalse(plugin.isDoomSafeguardEnabled());
    }

    @Test public void sixHourThresholdsAreIndependentAndHonorLoginOverrideAndBounds() throws Exception {
        assertEquals(31500, config.leviathanSixHourWarningTicks());
        assertEquals(31500, config.doomSixHourWarningTicks());
        set("ticksSinceLogin", 31499);
        assertFalse(plugin.isCloseToLeviathanSixHourLogout());
        assertFalse(plugin.isCloseToDoomSixHourLogout());
        set("ticksSinceLogin", 31500);
        assertTrue(plugin.isCloseToLeviathanSixHourLogout());
        assertTrue(plugin.isCloseToDoomSixHourLogout());

        manager.setConfiguration(GROUP, "leviathanSixHourWarningTicks", 32000);
        manager.setConfiguration(GROUP, "doomSixHourWarningTicks", 33000);
        set("ticksSinceLogin", 32000);
        assertTrue(plugin.isCloseToLeviathanSixHourLogout());
        assertFalse(plugin.isCloseToDoomSixHourLogout());
        manager.setConfiguration(GROUP, "ticksSinceLoginOverride", 33000);
        assertTrue(plugin.isCloseToDoomSixHourLogout());
        manager.setConfiguration(GROUP, "ticksSinceLoginOverride", 31999);
        assertFalse(plugin.isCloseToLeviathanSixHourLogout());
        assertFalse(plugin.isCloseToDoomSixHourLogout());
        manager.setConfiguration(GROUP, "ticksSinceLoginOverride", -1);
        assertTrue(plugin.isCloseToLeviathanSixHourLogout());

        // Imported settings can bypass the config UI's range validation.
        manager.setConfiguration(GROUP, "leviathanSixHourWarningTicks", -10);
        manager.setConfiguration(GROUP, "doomSixHourWarningTicks", Integer.MAX_VALUE);
        set("ticksSinceLogin", 0);
        assertTrue(plugin.isCloseToLeviathanSixHourLogout());
        assertFalse(plugin.isCloseToDoomSixHourLogout());
        set("ticksSinceLogin", 35999);
        assertFalse(plugin.isCloseToDoomSixHourLogout());
        set("ticksSinceLogin", 36000);
        assertTrue(plugin.isCloseToDoomSixHourLogout());
    }

    @Test public void eachLobbyWarningIsIndependentOfLogoutModeAndInfoCheckboxes() throws Exception {
        manager.setConfiguration(GROUP, "leviathanSixHourWarningTicks", 32000);
        manager.setConfiguration(GROUP, "doomSixHourWarningTicks", 33000);
        for (String boss : Arrays.asList("Leviathan", "Doom")) {
            lobby(boss);
            assertEquals(DISABLED, boss.equals("Leviathan") ? config.leviathanLogoutSafeguardMode() : config.doomLogoutSafeguardMode());
            manager.setConfiguration(GROUP, "display" + boss + "BugInfo", false);
            manager.setConfiguration(GROUP, "display" + boss + "FixInfo", false);
            manager.setConfiguration(GROUP, "display" + boss + "LogoutSetting", false);
            int threshold = boss.equals("Leviathan") ? 32000 : 33000;
            set("ticksSinceLogin", threshold - 1);
            assertNull(render());
            set("ticksSinceLogin", threshold);
            assertNotNull(render());
            assertTrue(text().contains("approaching the 6-hour"));
            assertFalse(text().contains("Current Logout Setting"));
            assertTrue(overlay.getMenuEntries().isEmpty());
            manager.setConfiguration(GROUP, boss.toLowerCase() + "SixHourMode", DISABLED);
            manager.setConfiguration(GROUP, boss.toLowerCase() + "LogoutSafeguardMode", ALWAYS);
            assertNull(render());
        }
    }

    @Test public void sixHourModesDefaultToHardcoreAndAreIndependentForEachBoss() throws Exception {
        assertEquals(HC_ONLY, config.leviathanSixHourMode());
        assertEquals(HC_ONLY, config.doomSixHourMode());
        for (boolean hardcore : new boolean[]{false, true}) {
            set("hardcoreAccountType", hardcore);
            for (EscapeCrystalNotifyConfig.SafeguardAccountType leviathanMode : values()) {
                manager.setConfiguration(GROUP, "leviathanSixHourMode", leviathanMode);
                for (EscapeCrystalNotifyConfig.SafeguardAccountType doomMode : values()) {
                    manager.setConfiguration(GROUP, "doomSixHourMode", doomMode);
                    assertEquals(leviathanMode == ALWAYS || leviathanMode == HC_ONLY && hardcore,
                        plugin.isLeviathanSixHourWarningEnabled());
                    assertEquals(doomMode == ALWAYS || doomMode == HC_ONLY && hardcore,
                        plugin.isDoomSixHourWarningEnabled());
                    assertFalse(plugin.isLeviathanSafeguardEnabled());
                    assertFalse(plugin.isDoomSafeguardEnabled());
                }
            }
        }
        manager.setConfiguration(GROUP, "leviathanSixHourMode", DISABLED);
        manager.setConfiguration(GROUP, "doomSixHourMode", DISABLED);
        manager.setConfiguration(GROUP, "leviathanLogoutSafeguardMode", ALWAYS);
        manager.setConfiguration(GROUP, "doomLogoutSafeguardMode", ALWAYS);
        assertTrue(plugin.isLeviathanSafeguardEnabled());
        assertTrue(plugin.isDoomSafeguardEnabled());
        assertFalse(plugin.isLeviathanSixHourWarningEnabled());
        assertFalse(plugin.isDoomSixHourWarningEnabled());
    }

    @Test public void disabledModesShowOnlyFixAndConfirmationHidesOnlyThatBoss() throws Exception {
        for (String boss : Arrays.asList("Leviathan", "Doom")) {
            lobby(boss);
            assertNotNull(render());
            String text = text();
            assertTrue(text.contains("BUG FIXED"));
            assertTrue(text.contains("logout bug has been fixed"));
            assertFalse(text.contains("UNAVOIDABLE DEATH"));
            assertFalse(text.contains("Current Logout Setting"));
            assertFalse(text.contains("NOT PROTECTED"));
            assertEquals(1, overlay.getMenuEntries().size());
            OverlayMenuEntry entry = overlay.getMenuEntries().get(0);
            assertEquals("Confirm fix", entry.getOption());
            plugin.onOverlayMenuClicked(new OverlayMenuClicked(entry, overlay));
            assertEquals("false", manager.getConfiguration(GROUP, "display" + boss + "FixInfo"));
            assertNull(render());
            assertTrue(overlay.getMenuEntries().isEmpty());
            if (boss.equals("Leviathan")) assertTrue(config.displayDoomFixInfo());
            manager.setConfiguration(GROUP, "display" + boss + "FixInfo", true);
            assertNotNull(render());
            assertEquals(1, overlay.getMenuEntries().size());
        }
    }

    @Test public void enabledModesAppendFixAndKeepBugAfterConfirmation() throws Exception {
        for (String boss : Arrays.asList("Leviathan", "Doom")) {
            lobby(boss);
            // HC_ONLY is still a non-disabled mode on a non-hardcore account.
            for (EscapeCrystalNotifyConfig.SafeguardAccountType mode : Arrays.asList(ALWAYS, HC_ONLY)) {
                set("accountType", EscapeCrystalNotifyAccountType.NON_HARDCORE);
                set("hardcoreAccountType", false);
                manager.setConfiguration(GROUP, boss.toLowerCase() + "LogoutSafeguardMode", mode);
                manager.setConfiguration(GROUP, "display" + boss + "FixInfo", true);
                assertNotNull(render());
                String text = text();
                assertTrue(text.contains("UNAVOIDABLE DEATH"));
                assertTrue(text.indexOf("logout bug has been fixed") > text.indexOf("UNAVOIDABLE DEATH"));
                assertFalse(text.contains("BUG FIXED")); // Only the existing bug header.
                assertEquals(1, overlay.getMenuEntries().size());
                plugin.onOverlayMenuClicked(new OverlayMenuClicked(overlay.getMenuEntries().get(0), overlay));
                assertNotNull(render());
                assertTrue(text().contains("UNAVOIDABLE DEATH"));
                assertFalse(text().contains("logout bug has been fixed"));
                assertTrue(overlay.getMenuEntries().isEmpty());
                assertEquals(mode, boss.equals("Doom") ? config.doomLogoutSafeguardMode() : config.leviathanLogoutSafeguardMode());
            }
        }
    }

    @Test public void fixCanDisplayIndependentlyOfBugAndLogoutCheckboxes() throws Exception {
        for (String boss : Arrays.asList("Leviathan", "Doom")) {
            lobby(boss);
            manager.setConfiguration(GROUP, "display" + boss + "BugInfo", false);
            manager.setConfiguration(GROUP, "display" + boss + "LogoutSetting", false);
            for (EscapeCrystalNotifyConfig.SafeguardAccountType mode : values()) {
                manager.setConfiguration(GROUP, boss.toLowerCase() + "LogoutSafeguardMode", mode);
                manager.setConfiguration(GROUP, "display" + boss + "FixInfo", true);
                assertNotNull(render());
                assertTrue(text().contains("logout bug has been fixed"));
                assertFalse(text().contains("UNAVOIDABLE DEATH"));
                manager.setConfiguration(GROUP, "display" + boss + "FixInfo", false);
                assertNull(render());
            }
        }
    }

    @Test public void confirmationsCannotAffectOtherPanelsOrBossesOutsideLobby() throws Exception {
        lobby("Leviathan");
        render();
        OverlayMenuEntry entry = overlay.getMenuEntries().get(0);
        plugin.onOverlayMenuClicked(new OverlayMenuClicked(entry, new EscapeCrystalNotifyTextOverlayPanel(plugin, config)));
        assertTrue(config.displayLeviathanFixInfo());
        plugin.onOverlayMenuClicked(new OverlayMenuClicked(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY,
            "Unrelated action", entry.getTarget()), overlay));
        assertTrue(config.displayLeviathanFixInfo());
        lobby("Doom");
        plugin.onOverlayMenuClicked(new OverlayMenuClicked(entry, overlay));
        assertTrue(config.displayLeviathanFixInfo());
        assertTrue(config.displayDoomFixInfo());
        set("atDoomLobby", false);
        assertNull(render());
        assertTrue(overlay.getMenuEntries().isEmpty());
    }

    @Test public void ordinaryRightClickConfirmsWithoutShiftAndDoesNotDuplicateNativeMenu() throws Exception {
        lobby("Leviathan");
        render();
        overlay.setBounds(new Rectangle(100, 100, 235, 200));
        net.runelite.api.Point[] mouse = {new net.runelite.api.Point(120, 120)};
        List<MenuEntry> menu = new ArrayList<>();
        menu.add(menuEntry().setOption("Cancel").setTarget(""));
        set("client", Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Client.class}, (p, m, a) -> {
            if (m.getName().equals("getMouseCanvasPosition")) return mouse[0];
            if (m.getName().equals("createMenuEntry")) {
                MenuEntry entry = menuEntry();
                menu.add((Integer) a[0], entry);
                return entry;
            }
            throw new AssertionError("Unexpected client call: " + m.getName());
        }));
        MenuOpened event = new MenuOpened();
        event.setMenuEntries(menu.toArray(new MenuEntry[0]));
        plugin.onMenuOpened(event);
        assertEquals(2, menu.size());
        MenuEntry confirm = menu.get(1);
        assertEquals("Confirm fix", confirm.getOption());
        assertEquals(MenuAction.RUNELITE_OVERLAY, confirm.getType());
        event.setMenuEntries(menu.toArray(new MenuEntry[0]));
        plugin.onMenuOpened(event);
        assertEquals(2, menu.size());
        confirm.onClick().accept(confirm);
        assertFalse(config.displayLeviathanFixInfo());
        assertTrue(config.displayDoomFixInfo());

        manager.setConfiguration(GROUP, "displayLeviathanFixInfo", true);
        mouse[0] = new net.runelite.api.Point(0, 0);
        menu.remove(1);
        event.setMenuEntries(menu.toArray(new MenuEntry[0]));
        plugin.onMenuOpened(event);
        assertEquals(1, menu.size());
    }

    @Test public void renderNoticePreviews() throws Exception {
        File directory = new File("build/reports/safeguard-previews");
        assertTrue(directory.isDirectory() || directory.mkdirs());
        for (String boss : Arrays.asList("Leviathan", "Doom")) {
            lobby(boss);
            for (EscapeCrystalNotifyConfig.SafeguardAccountType mode : Arrays.asList(DISABLED, ALWAYS)) {
                manager.setConfiguration(GROUP, boss.toLowerCase() + "LogoutSafeguardMode", mode);
                render(); // PanelComponent uses the previous frame's dimensions for its background.
                BufferedImage image = new BufferedImage(260, 650, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                Dimension size;
                try { size = overlay.render(graphics); } finally { graphics.dispose(); }
                assertNotNull(size);
                assertTrue(size.height < image.getHeight());
                ImageIO.write(image.getSubimage(0, 0, size.width, size.height), "png",
                    new File(directory, boss + "-" + mode.name() + ".png"));
            }
        }
    }

    private void lobby(String boss) throws Exception {
        set("atLeviathanLobby", boss.equals("Leviathan"));
        set("atDoomLobby", boss.equals("Doom"));
    }

    private Dimension render() {
        Graphics2D graphics = new BufferedImage(300, 800, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try { return overlay.render(graphics); } finally { graphics.dispose(); }
    }

    private String text() throws Exception {
        PanelComponent panel = (PanelComponent) field(OverlayPanel.class, "panelComponent").get(overlay);
        StringBuilder text = new StringBuilder();
        for (Object line : panel.getChildren()) {
            text.append(field(LineComponent.class, "left").get(line)).append(' ')
                .append(field(LineComponent.class, "right").get(line)).append('\n');
        }
        return text.toString();
    }

    private void set(String name, Object value) throws Exception {
        field(EscapeCrystalNotifyPlugin.class, name).set(plugin, value);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private MenuEntry menuEntry() {
        Map<String, Object> values = new HashMap<>();
        return (MenuEntry) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MenuEntry.class}, (p, m, a) -> {
            if (m.getName().equals("onClick")) {
                if (a == null) return values.get("onClick");
                values.put("onClick", a[0]);
                return p;
            }
            if (m.getName().startsWith("set")) {
                values.put(m.getName().substring(3), a[0]);
                return p;
            }
            return values.get(m.getName().substring(3));
        });
    }
}
