package com.escapecrystalnotify;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("escapecrystalnotify")
public interface EscapeCrystalNotifyConfig extends Config
{
	enum InactivityTimeFormat {
		SECONDS,
		GAME_TICKS,
	}

	@ConfigItem(
			keyName = "activeCrystalScale",
			name = "Active Crystal Scale",
			description = "The size of the active crystal image",
			position = 1
	)
	default int activeCrystalScale()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "inactiveCrystalScale",
			name = "Inactive Crystal Scale",
			description = "The size of the inactive crystal image",
			position = 2
	)
	default int inactiveCrystalScale()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "displayTimeBeforeTeleport",
			name = "Display Time Left Before Teleport",
			description = "Display the time left before triggering the inactivity teleport",
			position = 3
	)
	default boolean displayTimeBeforeTeleport() { return true; }

	@ConfigItem(
			keyName = "inactivityTimeFormat",
			name = "Inactivity Time Format",
			description = "Format for displaying the time left before triggering the inactivity teleport",
			position = 4
	)
	default InactivityTimeFormat inactivityTimeFormat()
	{
		return InactivityTimeFormat.SECONDS;
	}
}
