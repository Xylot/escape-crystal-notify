package com.escapecrystalnotify;

import java.awt.Container;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import javax.swing.*;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.components.IconTextField;
import org.junit.Test;
import static com.escapecrystalnotify.EscapeCrystalNotifyRegion.*;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyThresholdsTest {
    private Map<String, String> profile = new HashMap<>();
    private final EscapeCrystalNotifyThresholds thresholds = new EscapeCrystalNotifyThresholds(
        key -> profile.get(key), (key, value) -> profile.put(key, value.toString()), () -> profile);

    @Test public void rightClickAddsOneActionForTheHoveredEntranceAndOpensItsPrompt() throws Exception {
        List<EscapeCrystalNotifyRegion> opened = new ArrayList<>();
        EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin() {
            @Override void openThresholdInput(EscapeCrystalNotifyRegion encounter) { opened.add(encounter); }
        };
        set(plugin, "config", new EscapeCrystalNotifyConfig() {});
        EscapeCrystalNotifyLocatedEntrance entrance = entrance(BOSS_ARAXXOR);
        plugin.getPossibleEntrances().put(12850, Arrays.asList(entrance, entrance));
        MenuEntry enter = menuEntry(entrance.getTarget().getId()).setType(MenuAction.GAME_OBJECT_FIRST_OPTION)
            .setParam0(10).setParam1(20).setWorldViewId(WorldView.TOPLEVEL);
        MenuEntry examine = menuEntry(entrance.getTarget().getId()).setType(MenuAction.EXAMINE_OBJECT)
            .setParam0(10).setParam1(20).setWorldViewId(WorldView.TOPLEVEL);
        MenuEntry[][] menu = {new MenuEntry[]{menuEntry(0).setType(MenuAction.CANCEL), examine, enter}};
        installClient(plugin, menu, EnumSet.noneOf(WorldType.class));
        MenuOpened event = new MenuOpened();
        event.setMenuEntries(menu[0]);
        plugin.onMenuOpened(event);
        assertEquals(3, menu[0].length); // Possible entrances alone must not expose the editor.
        plugin.getValidEntrances().addAll(Arrays.asList(entrance, entrance));
        plugin.onMenuOpened(event);
        assertEquals(4, menu[0].length);
        assertSame(enter, menu[0][3]);
        assertEquals("Set crystal maximum", menu[0][1].getOption());
        assertTrue(menu[0][1].getTarget().contains("Araxxor"));
        assertEquals(MenuAction.RUNELITE, menu[0][1].getType());
        menu[0][1].onClick().accept(menu[0][1]);
        assertEquals(Arrays.asList(BOSS_ARAXXOR), opened);
    }

    @Test public void rightClickMatchesNpcDefinitionIdInsteadOfMenuIndex() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        EscapeCrystalNotifyLocatedEntrance entrance = entrance(BOSS_NIGHTMARE_ENTRANCE);
        int id = entrance.getTarget().getId();
        NPC npc = (NPC) Proxy.newProxyInstance(NPC.class.getClassLoader(), new Class[]{NPC.class},
            (p,m,a) -> m.getName().equals("getId") ? id : null);
        entrance.target = new EscapeCrystalNotifyRegionEntranceObject(npc);
        plugin.getValidEntrances().add(entrance);
        MenuEntry npcEntry = (MenuEntry) Proxy.newProxyInstance(MenuEntry.class.getClassLoader(), new Class[]{MenuEntry.class},
            (p,m,a) -> m.getName().equals("getNpc") ? npc : 42);
        MenuEntry[][] menu = {new MenuEntry[]{npcEntry}};
        installClient(plugin, menu, EnumSet.noneOf(WorldType.class));
        MenuOpened event = new MenuOpened();
        event.setMenuEntries(menu[0]);
        plugin.onMenuOpened(event);
        assertEquals(2, menu[0].length);
        assertEquals("Set crystal maximum", menu[0][1].getOption());
        assertTrue(menu[0][1].getTarget().contains("Nightmare"));
    }

    @Test public void chatboxInputRejectsInvalidValuesAndDoesNotSaveToAnotherProfile() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        assertTrue(plugin.saveThresholdInput(BOSS_ARAXXOR, "7", profile));
        assertEquals(7, thresholds.get(BOSS_ARAXXOR));
        for (String invalid : Arrays.asList("", "0", "1", "-1", "1.5", "word", "999999999999")) {
            assertFalse(plugin.saveThresholdInput(BOSS_ARAXXOR, invalid, profile));
            assertEquals(7, thresholds.get(BOSS_ARAXXOR));
        }
        assertTrue(plugin.saveThresholdInput(BOSS_ARAXXOR, "2", profile));
        assertEquals(2, thresholds.get(BOSS_ARAXXOR));
        thresholds.set(BOSS_ARAXXOR, 1, profile);
        assertEquals(2, thresholds.get(BOSS_ARAXXOR));
        Object oldProfile = profile;
        profile = new HashMap<>();
        assertTrue(plugin.saveThresholdInput(BOSS_ARAXXOR, "9", oldProfile));
        assertTrue(profile.isEmpty());
        assertEquals(4, thresholds.get(BOSS_ARAXXOR));
    }

    @Test public void profilesPersistIndependentlyAndRejectStaleEdits() {
        assertEquals(4, thresholds.get(BOSS_ARAXXOR));
        for (String invalid : Arrays.asList("", "-1", "0", "1", "2.5", "bad", "999999999999")) {
            profile.put("maximumSeconds_BOSS_ARAXXOR", invalid);
            assertEquals(4, thresholds.get(BOSS_ARAXXOR));
        }
        Map<String, String> first = profile;
        thresholds.set(BOSS_ARAXXOR, 8, first);
        assertEquals(8, thresholds.get(BOSS_ARAXXOR));
        assertEquals(4, thresholds.get(BOSS_AMOXLIATL));
        profile = new HashMap<>();
        thresholds.set(BOSS_ARAXXOR, 9, first);
        assertTrue(profile.isEmpty());
        assertEquals(4, thresholds.get(BOSS_ARAXXOR));
        thresholds.set(BOSS_ARAXXOR, 3, profile);
        assertEquals(3, thresholds.get(BOSS_ARAXXOR));
        profile = first;
        assertEquals(8, thresholds.get(BOSS_ARAXXOR));
        thresholds.set(BOSS_CORRUPTED_GAUNTLET, 6, profile);
        assertEquals(6, thresholds.get(BOSS_GAUNTLET_LOBBY));
    }

    @Test public void catalogGroupsEntrancesAndCoversEveryBossAndRaid() {
        for (EscapeCrystalNotifyRegion region : EscapeCrystalNotifyRegion.values()) {
            if (!EscapeCrystalNotifyEncounters.supported(region)) continue;
            assertTrue(EscapeCrystalNotifyEncounters.all().contains(EscapeCrystalNotifyEncounters.canonical(region)));
            assertNotEquals(net.runelite.api.gameval.ItemID.TOB_TELEPORT, EscapeCrystalNotifyEncounters.icon(region));
            if (region.getRegionEntrance() != null) {
                assertSame(EscapeCrystalNotifyEncounters.canonical(region), EscapeCrystalNotifyEncounters.forEntrance(region.getRegionEntrance()));
            }
        }
        assertSame(BOSS_GAUNTLET, EscapeCrystalNotifyEncounters.forEntrance(BOSS_GAUNTLET_LOBBY.getRegionEntrance()));
        assertSame(RAIDS_TOMBS_OF_AMASCUT, EscapeCrystalNotifyEncounters.canonical(RAIDS_OSMUMTENS_BURIAL_CHAMBER));
        assertFalse(EscapeCrystalNotifyEncounters.hasEntrance(BOSS_BARROWS));
        assertFalse(EscapeCrystalNotifyEncounters.hasEntrance(BOSS_MYSTERIOUS_FIGURE));
        assertNull(EscapeCrystalNotifyEncounters.forEntrance(new EscapeCrystalNotifyRegionEntrance(123, true)));
    }

    @Test public void panelCommitsIntegersAndRefreshesWithoutWriting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<Integer> icons = new ArrayList<>();
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(thresholds, (id, label) -> icons.add(id));
            assertEquals(EscapeCrystalNotifyEncounters.all().size(), icons.size());
            JSpinner spinner = findSpinner(panel);
            JFormattedTextField input = ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
            input.setText("9");
            input.postActionEvent();
            assertEquals(9, spinner.getValue());
            assertEquals(1, profile.size());
            for (String invalid : Arrays.asList("", "0", "1", "-1", "2.5", "oops", "999999999999")) {
                input.setText(invalid);
                input.postActionEvent();
                assertEquals("9", input.getText());
            }
            input.setText("2");
            input.postActionEvent();
            assertEquals(2, spinner.getValue());
            assertNull(spinner.getPreviousValue());
            input.setText("7");
            for (java.awt.event.FocusListener listener : input.getFocusListeners()) {
                listener.focusLost(new java.awt.event.FocusEvent(input, java.awt.event.FocusEvent.FOCUS_LOST));
            }
            assertEquals(7, spinner.getValue());
            Map<String, String> first = profile;
            profile = new HashMap<>();
            input.setText("12");
            input.postActionEvent();
            assertTrue(profile.isEmpty());
            assertEquals(4, spinner.getValue());
            profile = first;
            panel.refresh();
            assertEquals(7, spinner.getValue());
            assertEquals(1, profile.size());
        });
    }

    @Test public void comparesRoundedSettingNotCountdownAndPreservesInactiveWarnings() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        EscapeCrystalNotifyLocatedEntrance entrance = entrance(BOSS_ARAXXOR);
        plugin.getValidEntrances().add(entrance);
        for (int ticks : new int[]{5, 6, 7}) {
            set(plugin, "escapeCrystalInactivityTicks", ticks);
            assertEquals(0, plugin.getExceededMaximumSeconds(entrance));
        }
        set(plugin, "escapeCrystalInactivityTicks", 8); // 4.8 seconds displays as 5.
        set(plugin, "expectedTicksUntilTeleport", 0);
        assertEquals(4, plugin.getExceededMaximumSeconds(entrance));
        assertNotNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        thresholds.set(BOSS_ARAXXOR, 5, profile);
        assertEquals(0, plugin.getExceededMaximumSeconds(entrance));
        set(plugin, "escapeCrystalActive", false);
        assertEquals(0, plugin.getExceededMaximumSeconds(entrance));
        assertNotNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        set(plugin, "escapeCrystalActive", true);
        set(plugin, "escapeCrystalWithPlayer", false);
        assertEquals(0, plugin.getExceededMaximumSeconds(entrance));
        assertNotNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        set(plugin, "escapeCrystalWithPlayer", true);
        assertEquals(0, plugin.getExceededMaximumSeconds(entrance(BOSS_MYSTERIOUS_FIGURE)));
        EscapeCrystalNotifyLocatedEntrance debug = entrance(BOSS_ARAXXOR);
        debug.definition = new EscapeCrystalNotifyRegionEntrance(debug.getTarget().getId(), true);
        assertEquals(0, plugin.getExceededMaximumSeconds(debug));
    }

    @Test public void targetedRefreshPreservesOtherDraftsAndResetRespectsProfiles() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(thresholds, (id, label) -> {});
            List<JSpinner> spinners = components(panel, JSpinner.class);
            JSpinner first = spinners.get(0);
            JFormattedTextField draft = ((JSpinner.NumberEditor) spinners.get(1).getEditor()).getTextField();
            draft.setText("12");
            thresholds.set(BOSS_ABYSSAL_SIRE, 9, profile);
            panel.refresh(EscapeCrystalNotifyThresholds.key(BOSS_ABYSSAL_SIRE));
            assertEquals(9, first.getValue());
            assertEquals("12", draft.getText());
            assertEquals(4, spinners.get(1).getValue());

            JButton reset = components(panel, JButton.class).stream()
                .filter(button -> "Reset".equals(button.getText())).findFirst().get();
            reset.doClick();
            assertEquals(4, first.getValue());
            assertEquals(4, thresholds.get(BOSS_ABYSSAL_SIRE));
            assertEquals("12", draft.getText());

            profile = new HashMap<>();
            thresholds.set(BOSS_ABYSSAL_SIRE, 7, profile);
            reset.doClick(); // A stale panel must not reset the newly selected profile.
            assertEquals(7, first.getValue());
            assertEquals(7, thresholds.get(BOSS_ABYSSAL_SIRE));
            assertEquals("4", draft.getText());
        });
    }

    @Test public void searchFiltersRowsAndSectionsWithoutChangingSettings() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(thresholds, (id, label) -> {});
            IconTextField search = components(panel, IconTextField.class).get(0);
            List<JSpinner> spinners = components(panel, JSpinner.class);
            List<JLabel> labels = components(panel, JLabel.class);
            JLabel bosses = labels.stream().filter(label -> "Bosses".equals(label.getText())).findFirst().get();
            JLabel raids = labels.stream().filter(label -> "Raids".equals(label.getText())).findFirst().get();
            JLabel empty = labels.stream().filter(label -> "No matching encounters".equals(label.getText())).findFirst().get();
            search.setText("  aRaXxOr  ");
            assertEquals(1, spinners.stream().filter(spinner -> spinner.getParent().getParent().getParent().isVisible()).count());
            assertTrue(bosses.isVisible());
            assertFalse(raids.isVisible());
            search.setText("tombs");
            assertFalse(bosses.isVisible());
            assertTrue(raids.isVisible());
            search.setText("no such encounter");
            assertTrue(empty.isVisible());
            assertFalse(bosses.isVisible());
            assertFalse(raids.isVisible());
            JButton clear = components(search, JButton.class).stream()
                .filter(button -> "×".equals(button.getText())).findFirst().get();
            clear.doClick();
            assertEquals("", search.getText());
            assertFalse(empty.isVisible());
            assertEquals(spinners.size(), spinners.stream().filter(spinner -> spinner.getParent().getParent().getParent().isVisible()).count());
            assertTrue(profile.isEmpty());
        });
    }

    @Test public void nearbySectionReusesEditorsAndPreservesSearchAndDrafts() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<Integer> loadedIcons = new ArrayList<>();
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(thresholds, (id, label) -> loadedIcons.add(id));
            List<JSpinner> originalSpinners = components(panel, JSpinner.class);
            JSpinner boss = originalSpinners.get(0); // Abyssal Sire is the first alphabetically.
            Container bossRow = boss.getParent().getParent().getParent();
            Container bossSection = bossRow.getParent();
            JLabel raidName = components(panel, JLabel.class).stream()
                .filter(label -> label.getText() != null && label.getText().contains(">Tombs of Amascut<"))
                .findFirst().get();
            Container raidRow = raidName.getParent().getParent();
            Container raidSection = raidRow.getParent();
            JFormattedTextField draft = ((JSpinner.NumberEditor) boss.getEditor()).getTextField();
            draft.setText("12");
            panel.setNearbyBosses(Set.of(BOSS_ABYSSAL_SIRE, RAIDS_TOMBS_OF_AMASCUT));
            assertNotSame(bossSection, bossRow.getParent());
            assertSame(bossRow.getParent(), raidRow.getParent());
            assertEquals(2, bossRow.getParent().getComponentCount());
            assertSame(boss, findSpinner(panel));
            assertEquals(originalSpinners.size(), components(panel, JSpinner.class).size());
            assertEquals(EscapeCrystalNotifyEncounters.all().size(), loadedIcons.size());
            assertEquals("12", draft.getText());
            assertTrue(profile.isEmpty());

            IconTextField search = components(panel, IconTextField.class).get(0);
            search.setText("tombs");
            assertFalse(bossRow.isVisible());
            assertTrue(raidRow.isVisible());
            panel.setNearbyBosses(Set.of(BOSS_ABYSSAL_SIRE));
            assertSame(raidSection, raidRow.getParent());
            assertTrue(raidRow.isVisible());
            assertFalse(bossRow.isVisible());
            panel.setNearbyBosses(Collections.emptySet());
            search.setText("");
            assertSame(bossSection, bossRow.getParent());
            assertEquals(originalSpinners, components(panel, JSpinner.class));
            assertEquals("12", draft.getText());
            assertTrue(components(panel, JLabel.class).stream()
                .anyMatch(label -> "No supported entrances nearby".equals(label.getText()) && label.isVisible()));
            assertTrue(profile.isEmpty());
        });
    }

    @Test public void unsupportedEncountersHaveNoEditorAndRefreshPreservesTheirSavedSettings() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            thresholds.set(BOSS_BARROWS, 9, profile);
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(thresholds, (id, label) -> {});
            List<JLabel> unavailable = new ArrayList<>();
            for (JLabel label : components(panel, JLabel.class)) {
                if (label.getText() != null && label.getText().contains("Entrance warning unavailable")) {
                    unavailable.add(label);
                    Container row = label.getParent().getParent();
                    assertTrue(components(row, JSpinner.class).isEmpty());
                    assertTrue(components(row, JButton.class).isEmpty());
                }
            }
            long editable = EscapeCrystalNotifyEncounters.all().stream()
                .filter(EscapeCrystalNotifyEncounters::hasEntrance).count();
            assertEquals(editable, components(panel, JSpinner.class).size());
            assertEquals(EscapeCrystalNotifyEncounters.all().size() - editable, unavailable.size());
            panel.refresh(EscapeCrystalNotifyThresholds.key(BOSS_BARROWS));
            panel.refresh();
            assertEquals(9, thresholds.get(BOSS_BARROWS));
            assertEquals(1, profile.size());
        });
    }

    @Test public void resetSavesOnceAndItsRefreshPreservesANewDraft() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> reads = new ArrayList<>();
            List<Integer> writes = new ArrayList<>();
            EscapeCrystalNotifyThresholds settings = new EscapeCrystalNotifyThresholds(key -> {
                reads.add(key);
                return profile.get(key);
            }, (key, value) -> {
                writes.add(value);
                profile.put(key, value.toString());
            }, () -> profile);
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(settings, (id, label) -> {});
            JSpinner spinner = findSpinner(panel);
            JFormattedTextField input = ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
            spinner.setValue(9);
            reads.clear();
            writes.clear();
            JButton reset = components(panel, JButton.class).stream()
                .filter(button -> "Reset".equals(button.getText())).findFirst().get();
            reset.doClick();
            assertEquals(Arrays.asList(4), writes);
            assertTrue(reads.isEmpty()); // Reset uses the editor's save path without rereading the row.
            input.setText("12");
            String key = EscapeCrystalNotifyThresholds.key(BOSS_ABYSSAL_SIRE);
            panel.refresh(key); // The config event for Reset arrives after another edit began.
            assertEquals(Arrays.asList(key), reads);
            assertEquals("12", input.getText());
            reset.doClick(); // Reset must also discard a draft when the saved value is already 4.
            assertEquals("4", input.getText());
            assertEquals(Arrays.asList(4), writes);
            input.setText("8");
            profile = new HashMap<>();
            panel.refresh(key);
            assertEquals("4", input.getText()); // A profile switch forces refresh even if both saved values are 4.
            assertTrue(profile.isEmpty());
        });
    }

    @Test public void configEventsIgnoreOtherPluginsButStillRecomputeOwnSettings() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        int[] reads = {0};
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean displayBosses() { reads[0]++; return true; }
        });
        ConfigChanged event = new ConfigChanged();
        event.setGroup("menuentryswapper");
        event.setKey("item_1");
        plugin.onConfigChanged(event);
        assertEquals(0, reads[0]);
        event.setGroup(EscapeCrystalNotifyConfig.GROUP);
        event.setKey("displayBosses");
        plugin.onConfigChanged(event);
        assertTrue(reads[0] > 0);
    }

    @Test public void menuUsesHoveredBossAndKeepsLogoutPriority() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        List<String> reads = new ArrayList<>();
        set(plugin, "thresholds", new EscapeCrystalNotifyThresholds(key -> {
            reads.add(key);
            return profile.get(key);
        }, (key, value) -> profile.put(key, value.toString()), () -> profile));
        EscapeCrystalNotifyLocatedEntrance araxxor = entrance(BOSS_ARAXXOR);
        EscapeCrystalNotifyLocatedEntrance amoxliatl = entrance(BOSS_AMOXLIATL);
        plugin.getValidEntrances().addAll(Arrays.asList(araxxor, amoxliatl));
        thresholds.set(BOSS_AMOXLIATL, 10, profile);
        MenuEntry[][] menu = {new MenuEntry[]{menuEntry(amoxliatl.getTarget().getId())}};
        installClient(plugin, menu, EnumSet.noneOf(WorldType.class));
        plugin.onPostMenuSort(new PostMenuSort());
        assertEquals(1, menu[0].length);
        assertEquals(Arrays.asList("maximumSeconds_BOSS_AMOXLIATL"), reads);
        reads.clear();
        menu[0] = new MenuEntry[]{menuEntry(araxxor.getTarget().getId())};
        plugin.onPostMenuSort(new PostMenuSort());
        assertEquals(2, menu[0].length);
        assertTrue(menu[0][1].getOption().contains("Crystal setting too high (6s > 4s)"));
        assertTrue(menu[0][1].getOption().contains("ff8c00"));
        assertEquals(Arrays.asList("maximumSeconds_BOSS_ARAXXOR"), reads);
        reads.clear();
        menu[0] = new MenuEntry[]{menuEntry(0)};
        plugin.onPostMenuSort(new PostMenuSort());
        assertEquals(1, menu[0].length);
        assertTrue(reads.isEmpty());
        for (MenuEntry matchingId : Arrays.asList(
            menuEntry(araxxor.getTarget().getId()).setParam0(11),
            menuEntry(araxxor.getTarget().getId()).setWorldViewId(2),
            menuEntry(araxxor.getTarget().getId()).setType(MenuAction.WALK))) {
            menu[0] = new MenuEntry[]{matchingId};
            plugin.onPostMenuSort(new PostMenuSort());
            assertEquals(2, menu[0].length);
            assertTrue(menu[0][1].getOption().contains("Crystal setting too high (6s > 4s)"));
            assertEquals(Arrays.asList("maximumSeconds_BOSS_ARAXXOR"), reads);
            reads.clear();
        }
        for (String field : Arrays.asList("escapeCrystalActive", "escapeCrystalWithPlayer")) {
            set(plugin, field, false);
            menu[0] = new MenuEntry[]{menuEntry(araxxor.getTarget().getId())};
            plugin.onPostMenuSort(new PostMenuSort());
            assertTrue(menu[0][1].getOption().contains("Where's Your Crystal?"));
            assertTrue(reads.isEmpty());
            set(plugin, field, true);
        }
        EscapeCrystalNotifyLocatedEntrance doom = entrance(BOSS_DOOM_OF_MOKHAIOTL);
        plugin.getValidEntrances().clear();
        plugin.getValidEntrances().add(doom);
        set(plugin, "atDoomLobby", true);
        set(plugin, "ticksSinceLogin", 31500);
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public SafeguardAccountType doomSixHourMode() { return SafeguardAccountType.HC_ONLY; }
        });
        menu[0] = new MenuEntry[]{menuEntry(doom.getTarget().getId())};
        plugin.onPostMenuSort(new PostMenuSort());
        assertTrue(menu[0][1].getOption().contains("Relog"));
        assertTrue(reads.isEmpty());
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean deprioritizeEntranceEnterOption() { return false; }
            @Override public SafeguardAccountType doomSixHourMode() { return SafeguardAccountType.HC_ONLY; }
        });
        menu[0] = new MenuEntry[]{menuEntry(doom.getTarget().getId())};
        plugin.onPostMenuSort(new PostMenuSort());
        assertTrue(menu[0][1].getOption().contains("Relog"));
        assertTrue(reads.isEmpty());
    }

    @Test public void entranceLogoutWarningsUseEachBossThreshold() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean deprioritizeEntranceEnterOption() { return false; }
            @Override public SafeguardAccountType leviathanSixHourMode() { return SafeguardAccountType.ALWAYS; }
            @Override public SafeguardAccountType doomSixHourMode() { return SafeguardAccountType.ALWAYS; }
            @Override public int leviathanSixHourWarningTicks() { return 32000; }
            @Override public int doomSixHourWarningTicks() { return 33000; }
        });
        for (EscapeCrystalNotifyRegion region : Arrays.asList(BOSS_THE_LEVIATHAN_ENTRANCE, BOSS_DOOM_OF_MOKHAIOTL)) {
            boolean leviathan = region == BOSS_THE_LEVIATHAN_ENTRANCE;
            set(plugin, "atLeviathanLobby", leviathan);
            set(plugin, "atDoomLobby", !leviathan);
            EscapeCrystalNotifyLocatedEntrance entrance = entrance(region);
            int threshold = leviathan ? 32000 : 33000;
            set(plugin, "ticksSinceLogin", threshold - 1);
            assertNull(plugin.getEntranceMenuWarning(entrance));
            set(plugin, "ticksSinceLogin", threshold);
            assertTrue(plugin.getEntranceMenuWarning(entrance).contains("Relog"));
        }
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean deprioritizeEntranceEnterOption() { return false; }
            @Override public SafeguardAccountType leviathanLogoutSafeguardMode() { return SafeguardAccountType.ALWAYS; }
            @Override public SafeguardAccountType doomLogoutSafeguardMode() { return SafeguardAccountType.ALWAYS; }
            @Override public SafeguardAccountType leviathanSixHourMode() { return SafeguardAccountType.DISABLED; }
            @Override public SafeguardAccountType doomSixHourMode() { return SafeguardAccountType.DISABLED; }
        });
        for (EscapeCrystalNotifyRegion region : Arrays.asList(BOSS_THE_LEVIATHAN_ENTRANCE, BOSS_DOOM_OF_MOKHAIOTL)) {
            set(plugin, "atLeviathanLobby", region == BOSS_THE_LEVIATHAN_ENTRANCE);
            set(plugin, "atDoomLobby", region == BOSS_DOOM_OF_MOKHAIOTL);
            assertNull(plugin.getEntranceMenuWarning(entrance(region)));
        }
    }

    @Test public void respectsPvpAccountRegionAndHighlightOnlyEntrances() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        plugin.getValidEntrances().add(entrance(BOSS_ARTIO));
        assertNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        plugin.getValidEntrances().clear();
        plugin.getValidEntrances().add(entrance(BOSS_ARAXXOR));
        installClient(plugin, new MenuEntry[][]{new MenuEntry[0]}, EnumSet.of(WorldType.PVP));
        assertNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        installClient(plugin, new MenuEntry[][]{new MenuEntry[0]}, EnumSet.noneOf(WorldType.class));
        set(plugin, "hardcoreAccountType", false);
        assertNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        set(plugin, "hardcoreAccountType", true);
        set(plugin, "atNotifyRegionId", false);
        assertNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
    }

    @Test public void overlayPaintsOrangeOnlyAboveMaximum() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        List<String> reads = new ArrayList<>();
        set(plugin, "thresholds", new EscapeCrystalNotifyThresholds(key -> {
            reads.add(key);
            return profile.get(key);
        }, (key, value) -> profile.put(key, value.toString()), () -> profile));
        plugin.getValidEntrances().add(entrance(BOSS_ARAXXOR));
        set(plugin, "entranceOverlayImage", new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        EscapeCrystalNotifyRegionEntranceOverlay overlay = new EscapeCrystalNotifyRegionEntranceOverlay(plugin, new EscapeCrystalNotifyConfig() {});
        BufferedImage image = new BufferedImage(400, 250, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        overlay.render(graphics);
        graphics.dispose();
        assertEquals(Arrays.asList("maximumSeconds_BOSS_ARAXXOR"), reads);
        java.awt.Color fill = new java.awt.Color(image.getRGB(15, 15), true);
        assertEquals(255, fill.getRed());
        assertEquals(75, fill.getAlpha());
        assertTrue(Math.abs(140 - fill.getGreen()) <= 2); // Alpha compositing rounds channels.
        EscapeCrystalNotifyConfig hidden = new EscapeCrystalNotifyConfig() {
            @Override public boolean displayEntranceOverlay() { return false; }
        };
        BufferedImage hiddenImage = new BufferedImage(400, 250, BufferedImage.TYPE_INT_ARGB);
        graphics = hiddenImage.createGraphics();
        new EscapeCrystalNotifyRegionEntranceOverlay(plugin, hidden).render(graphics);
        graphics.dispose();
        assertEquals(0, hiddenImage.getRGB(15, 15));
        assertNotNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        set(plugin, "config", new EscapeCrystalNotifyConfig() {
            @Override public boolean deprioritizeEntranceEnterOption() { return false; }
        });
        assertNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        assertEquals(4, plugin.getExceededMaximumSeconds(plugin.getValidEntrances().get(0)));
        thresholds.set(BOSS_ARAXXOR, 6, profile);
        BufferedImage safe = new BufferedImage(400, 250, BufferedImage.TYPE_INT_ARGB);
        graphics = safe.createGraphics();
        overlay.render(graphics);
        graphics.dispose();
        assertEquals(0, safe.getRGB(15, 15));
    }

    @Test public void safeEntranceHighlightsWithoutRegionNotifications() throws Exception {
        EscapeCrystalNotifyPlugin plugin = plugin();
        set(plugin, "accountType", EscapeCrystalNotifyAccountType.STANDARD_HARDCORE);
        plugin.onConfigChanged(null);
        set(plugin, "atNotifyRegionId", false);
        set(plugin, "currentRegionId", 12582);
        set(plugin, "regionLocationRequirementsMet", true);
        set(plugin, "currentWorldPoint", new WorldPoint(3176, 2477, 0));
        set(plugin, "entranceOverlayImage", new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        plugin.getValidEntrances().add(entrance(BOSS_SHELLBANE_GRYPHON_ENTRANCE));
        EscapeCrystalNotifyRegionEntranceOverlay overlay = new EscapeCrystalNotifyRegionEntranceOverlay(plugin, new EscapeCrystalNotifyConfig() {});
        BufferedImage image = new BufferedImage(400, 250, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        overlay.render(graphics);
        graphics.dispose();
        assertFalse(plugin.isAtNotifyRegionId());
        assertNotEquals(0, image.getRGB(15, 15));
        assertNotNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
        assertEquals(BOSS_SHELLBANE_GRYPHON, EscapeCrystalNotifyEncounters.forEntrance(
            plugin.getValidEntrances().get(0).getDefinition()));

        set(plugin, "escapeCrystalWithPlayer", true);
        set(plugin, "escapeCrystalActive", true);
        thresholds.set(BOSS_SHELLBANE_GRYPHON, 6, profile);
        BufferedImage ready = new BufferedImage(400, 250, BufferedImage.TYPE_INT_ARGB);
        graphics = ready.createGraphics();
        overlay.render(graphics);
        graphics.dispose();
        assertEquals(0, ready.getRGB(15, 15));
        assertNull(plugin.getEntranceMenuWarning(plugin.getValidEntrances().get(0)));
    }

    private EscapeCrystalNotifyPlugin plugin() throws Exception {
        EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
        set(plugin, "config", new EscapeCrystalNotifyConfig() {});
        set(plugin, "thresholds", thresholds);
        set(plugin, "hardcoreAccountType", true);
        set(plugin, "atNotifyRegionId", true);
        set(plugin, "escapeCrystalInactivityTicks", 10);
        set(plugin, "currentWorldPoint", new WorldPoint(3200, 3200, 0));
        installClient(plugin, new MenuEntry[][]{new MenuEntry[0]}, EnumSet.noneOf(WorldType.class));
        return plugin;
    }

    private EscapeCrystalNotifyLocatedEntrance entrance(EscapeCrystalNotifyRegion region) {
        EscapeCrystalNotifyRegionEntrance definition = region.getRegionEntrance();
        int id = definition.getEntranceIds()[0];
        GameObject target = (GameObject) Proxy.newProxyInstance(GameObject.class.getClassLoader(), new Class[]{GameObject.class},
            (p, m, a) -> {
                switch (m.getName()) {
                    case "getId": return id;
                    case "getWorldLocation": return new WorldPoint(3200, 3200, 0);
                    case "getLocalLocation": return new net.runelite.api.coords.LocalPoint(10 * 128, 20 * 128);
                    case "getSceneMinLocation": return new net.runelite.api.Point(10, 20);
                    case "getConvexHull": return new Rectangle(10, 10, 20, 20);
                    case "getCanvasTextLocation": return new net.runelite.api.Point(200, 100);
                    default: return null;
                }
            });
        return new EscapeCrystalNotifyLocatedEntrance(new EscapeCrystalNotifyRegionEntranceObject(target), definition, new WorldPoint(3200, 3200, 0), id);
    }

    private void installClient(EscapeCrystalNotifyPlugin plugin, MenuEntry[][] menu, EnumSet<WorldType> worlds) throws Exception {
        set(plugin, "client", Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[]{Client.class}, (p, m, a) -> {
            switch (m.getName()) {
                case "getWorldType": return worlds;
                case "getMenuEntries": return menu[0];
                case "setMenuEntries": menu[0] = (MenuEntry[]) a[0]; return null;
                case "createMenuEntry":
                    MenuEntry created = menuEntry(0);
                    List<MenuEntry> entries = new ArrayList<>(Arrays.asList(menu[0]));
                    entries.add((Integer) a[0], created);
                    menu[0] = entries.toArray(new MenuEntry[0]);
                    return created;
                default: return null;
            }
        }));
    }

    private MenuEntry menuEntry(int id) {
        Map<String, Object> values = new HashMap<>();
        values.put("Identifier", id);
        values.put("Type", MenuAction.GAME_OBJECT_FIRST_OPTION);
        values.put("Param0", 10);
        values.put("Param1", 20);
        values.put("WorldViewId", WorldView.TOPLEVEL);
        return (MenuEntry) Proxy.newProxyInstance(MenuEntry.class.getClassLoader(), new Class[]{MenuEntry.class}, (p, m, a) -> {
            if (m.getName().equals("onClick")) {
                if (a == null) return values.get("onClick");
                values.put("onClick", a[0]);
                return p;
            }
            if (m.getName().startsWith("set")) { values.put(m.getName().substring(3), a[0]); return p; }
            return values.get(m.getName().substring(3));
        });
    }

    private JSpinner findSpinner(Container container) {
        return components(container, JSpinner.class).stream().findFirst().orElse(null);
    }

    private <T> List<T> components(Container container, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (java.awt.Component child : container.getComponents()) {
            if (type.isInstance(child)) found.add(type.cast(child));
            if (child instanceof Container) found.addAll(components((Container) child, type));
        }
        return found;
    }

    private void set(Object target, String name, Object value) throws Exception {
        Field field = EscapeCrystalNotifyPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}

