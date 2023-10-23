package com.escapecrystalnotify;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("escapecrystalnotify")
public interface EscapeCrystalNotifyConfig extends Config
{
	@ConfigItem(
			keyName = "scale",
			name = "Scale",
			description = "The scale of the escape crystal image"
	)
	default int scale()
	{
		return 1;
	}
}
