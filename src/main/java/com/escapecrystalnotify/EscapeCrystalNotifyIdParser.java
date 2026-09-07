package com.escapecrystalnotify;

import java.util.HashSet;
import java.util.Set;

final class EscapeCrystalNotifyIdParser {
    private EscapeCrystalNotifyIdParser() { }

    static Set<Integer> parseIds(String text) {
        Set<Integer> ids = new HashSet<>();
        if (text == null) return ids;
        for (String token : text.split("(?U)[,\\s]+")) {
            try {
                int id = Integer.parseInt(token);
                if (id >= 0) ids.add(id);
            } catch (NumberFormatException ignored) {
                // Ignore incomplete or invalid entries while editing the settings.
            }
        }
        return ids;
    }

}
