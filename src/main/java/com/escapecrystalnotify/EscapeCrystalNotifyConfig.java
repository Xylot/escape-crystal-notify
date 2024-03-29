package com.escapecrystalnotify;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("escapecrystalnotify")
public interface EscapeCrystalNotifyConfig extends Config
{
	enum InactivityTimeFormat {
		SECONDS ("seconds"),
		GAME_TICKS ("ticks");

		private final String formatName;

		InactivityTimeFormat(String formatName) {
			this.formatName = formatName;
		}

		public String toString() {
			return this.formatName;
		}
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
			keyName = "requireHardcoreAccountType",
			name = "Only display for HCIM/HCGIM",
			description = "Only display when logged in as a HCIM or HCGIM",
			position = 3
	)
	default boolean requireHardcoreAccountType() { return true; }

	@ConfigItem(
			keyName = "displayTimeBeforeTeleport",
			name = "Display Time Left Before Teleport",
			description = "Display the time left before triggering the inactivity teleport",
			position = 4
	)
	default boolean displayTimeBeforeTeleport() { return true; }

	@ConfigItem(
			keyName = "inactivityTimeFormat",
			name = "Inactivity Time Format",
			description = "Format for displaying the time left before triggering the inactivity teleport",
			position = 5
	)
	default InactivityTimeFormat inactivityTimeFormat()
	{
		return InactivityTimeFormat.SECONDS;
	}

	@ConfigSection(
			name = "Location Filter",
			description = "Filter locations where the reminder is shown",
			position = 6
	)
	String displayRegionFilter = "displayRegionFilter";

	@ConfigItem(
			keyName = "displayBosses",
			name = "Display for Bosses",
			description = "Display the reminder for bosses",
			section = "displayRegionFilter",
			position = 7
	)
	default boolean displayBosses() { return true; }

	@ConfigItem(
			keyName = "displayRaids",
			name = "Display for Raids",
			description = "Display the reminder for raids",
			section = "displayRegionFilter",
			position = 8
	)
	default boolean displayRaids() { return true; }

	@ConfigItem(
			keyName = "displayDungeons",
			name = "Display in Dungeons",
			description = "Display the reminder when inside dungeons",
			section = "displayRegionFilter",
			position = 9
	)
	default boolean displayDungeons() { return true; }

	@ConfigItem(
			keyName = "displayMinigames",
			name = "Display in Minigames",
			description = "Display the reminder when in minigames",
			section = "displayRegionFilter",
			position = 10
	)
	default boolean displayMinigames() { return true; }

	@ConfigItem(
			keyName = "displayEverywhere",
			name = "Always display at all times",
			description = "Display the reminder at all times",
			section = "displayRegionFilter",
			position = 11
	)
	default boolean displayEverywhere() { return true; }

	@ConfigItem(
			keyName = "excludeRegionIds",
			name = "Exclude Region IDs",
			description = "A comma separated list of Region IDs to exclude",
			section = "displayRegionFilter",
			position = 12
	)
	default String excludeRegionIds() { return ""; }

	@ConfigItem(
			keyName = "includeRegionIds",
			name = "Include Region IDs",
			description = "A comma separated list of Region IDs to include",
			section = "displayRegionFilter",
			position = 13
	)
	default String includeRegionIds() { return ""; }

	@ConfigSection(
			name = "Notification Settings",
			description = "Configure preferences for Runelite notifications",
			position = 14
	)
	String notificationSettings = "notificationSettings";

	@ConfigItem(
			keyName = "notifyInactive",
			name = "Inactive Crystal",
			description = "Sends a notification when your escape crystal is not enabled",
			section = "notificationSettings",
			position = 15
	)
	default boolean notifyInactive() { return true; }

	@ConfigItem(
			keyName = "notifyMissing",
			name = "Missing Crystal",
			description = "Sends a notification when you are not carrying your escape crystal",
			section = "notificationSettings",
			position = 16
	)
	default boolean notifyMissing() { return true; }

	@ConfigItem(
			keyName = "notifyTimeUntilTeleportThreshold",
			name = "Time Remaining Threshold",
			description = "Sends a notification when your escape crystal is about to trigger. Note that this respects the time format you specified above (Ticks vs Seconds). A value of 0 will disable the notification.",
			section = "notificationSettings",
			position = 17
	)
	default int notifyTimeUntilTeleportThreshold() { return 0; }

	@ConfigItem(
			keyName = "notifyNonLeftClickTeleport",
			name = "Non Left Click Teleport",
			description = "Sends a notification when the left click option on your escape crystal is not set to teleport",
			section = "notificationSettings",
			position = 18
	)
	default boolean notifyNonLeftClickTeleport() { return false; }
}

