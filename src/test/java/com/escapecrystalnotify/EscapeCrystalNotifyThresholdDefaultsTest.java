package com.escapecrystalnotify;

import java.awt.Container;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyThresholdDefaultsTest {
    @Test public void everyBossRaidAndAliasUsesItsDefaultUnlessOverridden() {
        Map<String, String> saved = new HashMap<>();
        EscapeCrystalNotifyThresholds thresholds = new EscapeCrystalNotifyThresholds(
            saved::get, (key, value) -> saved.put(key, value.toString()), () -> saved);
        for (EscapeCrystalNotifyRegion region : EscapeCrystalNotifyRegion.values()) {
            if (!EscapeCrystalNotifyEncounters.supported(region)) continue;
            int expected = EscapeCrystalNotifyThresholdDefaults.seconds(region);
            assertTrue(region.name(), expected >= EscapeCrystalNotifyThresholds.MIN_SECONDS);
            assertEquals(expected, EscapeCrystalNotifyThresholdDefaults.seconds(
                EscapeCrystalNotifyEncounters.canonical(region)));
            saved.clear();
            assertEquals(expected, thresholds.get(region));
            for (String invalid : new String[]{"", " ", "1", "-2", "2.5", "invalid", "999999999999"}) {
                saved.put(EscapeCrystalNotifyThresholds.key(region), invalid);
                assertEquals(region.name(), expected, thresholds.get(region));
            }
            int override = expected == 9 ? 10 : 9;
            thresholds.set(region, override, saved);
            assertEquals(override, thresholds.get(region));
            for (EscapeCrystalNotifyRegion other : EscapeCrystalNotifyEncounters.all()) {
                if (other == EscapeCrystalNotifyEncounters.canonical(region)) continue;
                assertEquals(EscapeCrystalNotifyThresholdDefaults.seconds(other), thresholds.get(other));
            }
        }
    }

    @Test public void everyEditorAndResetUsesTheEncounterDefault() throws Exception {
        Map<String, String> saved = new HashMap<>();
        EscapeCrystalNotifyThresholds thresholds = new EscapeCrystalNotifyThresholds(
            saved::get, (key, value) -> saved.put(key, value.toString()), () -> saved);
        SwingUtilities.invokeAndWait(() -> {
            EscapeCrystalNotifyPanel panel = new EscapeCrystalNotifyPanel(thresholds, (id, label) -> {});
            try {
                Field field = EscapeCrystalNotifyPanel.class.getDeclaredField("rows");
                field.setAccessible(true);
                Map<?, ?> rows = (Map<?, ?>) field.get(panel);
                for (EscapeCrystalNotifyRegion region : EscapeCrystalNotifyEncounters.all()) {
                    if (!EscapeCrystalNotifyEncounters.hasEntrance(region)) continue;
                    Container row = (Container) rows.get(EscapeCrystalNotifyThresholds.key(region));
                    JSpinner spinner = find(row, JSpinner.class);
                    JButton reset = findReset(row);
                    int expected = EscapeCrystalNotifyThresholdDefaults.seconds(region);
                    assertEquals(expected, spinner.getValue());
                    JFormattedTextField input = ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
                    assertTrue(input.getToolTipText().contains("default: " + expected + ")"));
                    assertEquals("Reset to " + expected + " seconds", reset.getToolTipText());
                    spinner.setValue(expected == 9 ? 10 : 9);
                    reset.doClick();
                    assertEquals(expected, spinner.getValue());
                    assertEquals(expected, thresholds.get(region));
                }
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        });
    }

    private JButton findReset(Container parent) {
        for (java.awt.Component child : parent.getComponents()) {
            if (child instanceof JButton && "Reset".equals(((JButton) child).getText())) return (JButton) child;
            if (child instanceof Container) {
                JButton found = findReset((Container) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private <T> T find(Container parent, Class<T> type) {
        for (java.awt.Component child : parent.getComponents()) {
            if (type.isInstance(child)) return type.cast(child);
            if (child instanceof Container) {
                T found = find((Container) child, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}
