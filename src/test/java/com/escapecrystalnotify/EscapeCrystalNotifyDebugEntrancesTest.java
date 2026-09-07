package com.escapecrystalnotify;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyDebugEntrancesTest {
    @Test
    public void acceptsWhitespaceAndEmptySeparators() {
        assertEquals(Set.of(123, 456, 789), EscapeCrystalNotifyIdParser.parseIds(" ,123, 456,,\t789\r\n, "));
        assertEquals(Set.of(123, 456), EscapeCrystalNotifyIdParser.parseIds("123\u00a0456"));
        assertTrue(EscapeCrystalNotifyIdParser.parseIds(" ,\t\n,, ").isEmpty());
    }

    @Test
    public void parsesListsAndToleratesInvalidInput() {
        assertEquals(Set.of(123, 456, 789), EscapeCrystalNotifyIdParser.parseIds("123, 456\n789,123, bad,-1,999999999999"));
        assertTrue(EscapeCrystalNotifyIdParser.parseIds("").isEmpty());
        assertTrue(EscapeCrystalNotifyIdParser.parseIds(null).isEmpty());
    }

    @Test
    public void normalDefinitionsAreNotDebugEntrances() {
        EscapeCrystalNotifyRegionEntrance definition = new EscapeCrystalNotifyRegionEntrance(
            EscapeCrystalNotifyRegionEntranceOverlayType.DEPRIORITIZED_WITH_HIGHLIGHT, null,
            EscapeCrystalNotifyRegionEntranceObjectType.GAME_OBJECT, 123);
        assertFalse(definition.isDebug());
        assertFalse(new EscapeCrystalNotifyRegionEntrance(123, false).isDebug());
        assertArrayEquals(new int[]{123}, new EscapeCrystalNotifyRegionEntrance(123, true).getEntranceIds());
    }

}
