package com.escapecrystalnotify;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;

/** Reads through to RuneLite so overlay and menu checks always use the active profile. */
final class EscapeCrystalNotifyThresholds {
    static final String PREFIX = "maximumSeconds_";
    static final int MIN_SECONDS = 2;
    private final Function<String, String> read;
    private final BiConsumer<String, Integer> write;
    private final Supplier<Object> profile;

    @Inject
    EscapeCrystalNotifyThresholds(ConfigManager configManager) {
        this(key -> configManager.getConfiguration(EscapeCrystalNotifyConfig.GROUP, key),
            (key, value) -> configManager.setConfiguration(EscapeCrystalNotifyConfig.GROUP, key, value), configManager::getProfile);
    }

    EscapeCrystalNotifyThresholds(Function<String, String> read, BiConsumer<String, Integer> write, Supplier<Object> profile) {
        this.read = read;
        this.write = write;
        this.profile = profile;
    }

    Object profile() { return profile.get(); }

    int get(EscapeCrystalNotifyRegion encounter) {
        String saved = read.apply(key(encounter));
        if (saved == null || saved.trim().isEmpty()) return EscapeCrystalNotifyThresholdDefaults.seconds(encounter);
        try {
            return parseSeconds(saved);
        } catch (NumberFormatException e) {
            return EscapeCrystalNotifyThresholdDefaults.seconds(encounter);
        }
    }

    void set(EscapeCrystalNotifyRegion encounter, int seconds, Object editedProfile) {
        if (seconds >= MIN_SECONDS && editedProfile == profile()) {
            write.accept(key(encounter), seconds);
        }
    }

    static int parseSeconds(String text) {
        if (text == null || !text.trim().matches("[0-9]+")) throw new NumberFormatException();
        int seconds = Integer.parseInt(text.trim());
        if (seconds < MIN_SECONDS) throw new NumberFormatException();
        return seconds;
    }

    static String key(EscapeCrystalNotifyRegion encounter) {
        return PREFIX + EscapeCrystalNotifyEncounters.canonical(encounter).name();
    }
}
