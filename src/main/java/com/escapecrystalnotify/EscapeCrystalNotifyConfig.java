package com.escapecrystalnotify;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("escapecrystalnotify")
public interface EscapeCrystalNotifyConfig extends Config
{
	enum InactivityTimeFormat {
		SECONDS ("Seconds"),
		SECONDS_MMSS ("Seconds (MM:SS)"),
		GAME_TICKS ("Ticks");

		private final String formatName;

		InactivityTimeFormat(String formatName) {
			this.formatName = formatName;
		}

		public String toString() {
			return this.formatName;
		}
	}

	enum OverlayDisplayType {
		REMAINING_TIME ("Remaining Time"),
		CURRENT_SETTING ("Current Setting"),
		DISABLED ("Disabled");

		private final String formatName;

		OverlayDisplayType(String formatName) {
			this.formatName = formatName;
		}

		public String toString() {
			return this.formatName;
		}
	}

	enum ModelOverlayType {
		ITEM_FILL ("Item Fill"),
		BACKGROUND_FILL ("Background Fill"),
		DISABLED ("Disabled");

		private final String formatName;

		ModelOverlayType(String formatName) {
			this.formatName = formatName;
		}

		public String toString() {
			return this.formatName;
		}
	}

	@ConfigItem(
			keyName = "requireHardcoreAccountType",
			name = "Only display for HCIM/HCGIM",
			description = "Only display when logged in as a HCIM or HCGIM",
			position = 1
	)
	default boolean requireHardcoreAccountType() { return true; }

	@ConfigSection(
			name = "On-Screen Widget Display Settings",
			description = "Settings for the movable and resizable on-screen widget",
			position = 2
	)
	String onScreenWidgetSettings = "onScreenWidgetSettings";

	@ConfigItem(
			keyName = "enableOnScreenWidget",
			name = "Enable On-Screen Widget",
			description = "Display the movable and resizable on-screen widget",
			section = "onScreenWidgetSettings",
			position = 1
	)
	default boolean enableOnScreenWidget() { return true; }

	@ConfigItem(
			keyName = "alwaysDisplayOnScreenWidget",
			name = "Display Everywhere",
			description = "Always enable the on-screen display regardless of location",
			section = "onScreenWidgetSettings",
			position = 2
	)
	default boolean alwaysDisplayOnScreenWidget() { return false; }

	@ConfigItem(
			keyName = "onlyDisplayInactiveOnScreenWidget",
			name = "Only Show When Inactive",
			description = "Only enable the on-screen display when your crystal is inactive",
			section = "onScreenWidgetSettings",
			position = 3
	)
	default boolean onlyDisplayInactiveOnScreenWidget() { return false; }

	@ConfigItem(
			keyName = "displayDisabledLeftClickTeleportText",
			name = "Display Left Click Disabled",
			description = "Display text indicating the the crystal is not set to be left click teleport",
			section = "onScreenWidgetSettings",
			position = 4
	)
	default boolean displayDisabledLeftClickTeleportText() { return true; }

	@ConfigItem(
			keyName = "onScreenWidgetDisplayFormat",
			name = "Display Format",
			description = "Type of information shown on the on-screen widget",
			section = "onScreenWidgetSettings",
			position = 5
	)
	default OverlayDisplayType onScreenWidgetDisplayFormat()
	{
		return OverlayDisplayType.REMAINING_TIME;
	}

	@ConfigItem(
			keyName = "onScreenWidgetInactivityTimeFormat",
			name = "Time Format",
			description = "Format for displaying the time information shown on the on-screen widget",
			section = "onScreenWidgetSettings",
			position = 6
	)
	default InactivityTimeFormat onScreenWidgetInactivityTimeFormat()
	{
		return InactivityTimeFormat.SECONDS;
	}

	@ConfigItem(
			keyName = "activeCrystalWidgetScale",
			name = "Active Widget Size",
			description = "The size of the active crystal widget",
			section = "onScreenWidgetSettings",
			position = 7
	)
	default int activeCrystalWidgetScale()
	{
		return 4;
	}

	@ConfigItem(
			keyName = "inactiveCrystalWidgetScale",
			name = "Inactive Widget Size",
			description = "The size of the inactive crystal widget",
			section = "onScreenWidgetSettings",
			position = 8
	)
	default int inactiveCrystalWidgetScale()
	{
		return 4;
	}

	@ConfigItem(
			keyName = "onScreenWidgetTimeExpiredText",
			name = "Time Expired Text",
			description = "Text to draw onto the crystal's inventory model when the inactivity time has been reached",
			section = "onScreenWidgetSettings",
			position = 9
	)
	default String onScreenWidgetTimeExpiredText() { return "Tele"; }

	@ConfigSection(
			name = "Info Box Display Settings",
			description = "Settings for Info Box",
			position = 3
	)
	String infoBoxDisplaySettings = "infoBoxDisplaySettings";

	@ConfigItem(
			keyName = "enableInfoBox",
			name = "Enable Info Box",
			description = "Enable the info box display",
			section = "infoBoxDisplaySettings",
			position = 1
	)
	default boolean enableInfoBox() { return true; }

	@ConfigItem(
			keyName = "alwaysDisplayInfoBox",
			name = "Display Everywhere",
			description = "Always enable the info box display regardless of location",
			section = "infoBoxDisplaySettings",
			position = 2
	)
	default boolean alwaysDisplayInfoBox() { return true; }

	@ConfigItem(
			keyName = "infoBoxDisplayFormat",
			name = "Display Format",
			description = "Type of information shown on the info box",
			section = "infoBoxDisplaySettings",
			position = 3
	)
	default OverlayDisplayType infoBoxDisplayFormat()
	{
		return OverlayDisplayType.REMAINING_TIME;
	}

	@ConfigItem(
			keyName = "infoBoxInactivityTimeFormat",
			name = "Time Format",
			description = "Format for displaying the time left before triggering the inactivity teleport on the info box",
			section = "infoBoxDisplaySettings",
			position = 4
	)
	default InactivityTimeFormat infoBoxInactivityTimeFormat()
	{
		return InactivityTimeFormat.SECONDS;
	}

	@ConfigItem(
			keyName = "infoBoxTimeExpiredText",
			name = "Time Expired Text",
			description = "Text to draw onto the crystal's inventory model when the inactivity time has been reached",
			section = "infoBoxDisplaySettings",
			position = 5
	)
	default String infoBoxTimeExpiredText() { return "Tele"; }

	@ConfigItem(
			keyName = "infoBoxMissingCrystalText",
			name = "Missing Crystal Text",
			description = "Text to draw onto the crystal's inventory model when you are missing an Escape Crystal",
			section = "infoBoxDisplaySettings",
			position = 6
	)
	default String infoBoxMissingCrystalText() { return "Missing"; }

	@ConfigSection(
			name = "Inventory Display Settings",
			description = "Settings for inventory & equipment screen",
			position = 4
	)
	String inventoryDisplaySettings = "inventoryDisplaySettings";

	@ConfigItem(
			keyName = "enableInventoryDisplay",
			name = "Enable Inventory Display",
			description = "Enable the Inventory display",
			section = "inventoryDisplaySettings",
			position = 1
	)
	default boolean enableInventoryDisplay() { return true; }

	@ConfigItem(
			keyName = "alwaysDisplayInventory",
			name = "Display Everywhere",
			description = "Always enable the inventory display regardless of location",
			section = "inventoryDisplaySettings",
			position = 2
	)
	default boolean alwaysDisplayInventory() { return true; }

	@ConfigItem(
			keyName = "inventoryDisplayFormat",
			name = "Display Format",
			description = "Type of information shown on the inventory model",
			section = "inventoryDisplaySettings",
			position = 3
	)
	default OverlayDisplayType inventoryDisplayFormat()
	{
		return OverlayDisplayType.REMAINING_TIME;
	}

	@ConfigItem(
			keyName = "inventoryTimeFormat",
			name = "Time Format",
			description = "Format for displaying the time left before triggering the inactivity teleport in the inventory & equipment menus",
			section = "inventoryDisplaySettings",
			position = 4
	)
	default InactivityTimeFormat inventoryInactivityTimeFormat()
	{
		return InactivityTimeFormat.SECONDS;
	}

	@ConfigItem(
			keyName = "inventoryOverlayType",
			name = "Overlay Type",
			description = "Method of highlighting the crystal's inventory & equipment model",
			section = "inventoryDisplaySettings",
			position = 5
	)
	default ModelOverlayType inventoryOverlayType()
	{
		return ModelOverlayType.ITEM_FILL;
	}

	@Alpha
	@ConfigItem(
			keyName = "inventoryActiveFillColor",
			name = "Active Fill Color",
			description = "Color of the background fill to draw onto the crystal's inventory model while active",
			section = "inventoryDisplaySettings",
			position = 6
	)
	default Color inventoryActiveFillColor() { return new Color(50,205,50,75); }

	@Alpha
	@ConfigItem(
			keyName = "inventoryInactiveFillColor",
			name = "Inactive Fill Color",
			description = "Color of the background fill to draw onto the crystal's inventory model while inactive",
			section = "inventoryDisplaySettings",
			position = 7
	)
	default Color inventoryInactiveFillColor() { return new Color(205,50,50,75); }

	@ConfigItem(
			keyName = "inventoryActiveText",
			name = "Active Crystal Text",
			description = "Text to draw onto the crystal's inventory model while active",
			section = "inventoryDisplaySettings",
			position = 8
	)
	default String inventoryActiveText() { return "Active"; }

	@Alpha
	@ConfigItem(
			keyName = "inventoryActiveTextColor",
			name = "Active Text Color",
			description = "Color of the text to draw onto the crystal's inventory model while active",
			section = "inventoryDisplaySettings",
			position = 9
	)
	default Color inventoryActiveTextColor() { return Color.GREEN; }

	@ConfigItem(
			keyName = "inventoryInactiveText",
			name = "Inactive Crystal Text",
			description = "Text to draw onto the crystal's inventory model while inactive",
			section = "inventoryDisplaySettings",
			position = 10
	)
	default String inventoryInactiveText() { return "Inactive"; }

	@Alpha
	@ConfigItem(
			keyName = "inventoryInactiveTextColor",
			name = "Inactive Text Color",
			description = "Color of the text to draw onto the crystal's inventory model while inactive",
			section = "inventoryDisplaySettings",
			position = 11
	)
	default Color inventoryInactiveTextColor() { return Color.RED; }

	@ConfigItem(
			keyName = "inventoryTimeExpiredText",
			name = "Time Expired Text",
			description = "Text to draw onto the crystal's inventory model when the inactivity time has been reached",
			section = "inventoryDisplaySettings",
			position = 12
	)
	default String inventoryTimeExpiredText() { return "Tele"; }

	@ConfigSection(
			name = "Entrance Overlay & Menu Swap",
			description = "Add a reminder overlay to boss/dungeon entrances and optional deprioritization of the enter menu option",
			position = 5
	)
	String entranceOverlaySettings = "entranceOverlaySettings";

	@ConfigItem(
			keyName = "deprioritizeEntranceEnterOption",
			name = "Deprioritize Entrance Enter Option",
			description = "Deprioritize the enter menu option when you do not have an active Escape Crystal",
			section = "entranceOverlaySettings",
			position = 1
	)
	default boolean deprioritizeEntranceEnterOption() { return true; }

	@ConfigItem(
			keyName = "displayEntranceOverlay",
			name = "Display Entrance Overlay",
			description = "Display the reminder overlay for entrances to dangerous regions when you do not have an active Escape Crystal",
			section = "entranceOverlaySettings",
			position = 2
	)
	default boolean displayEntranceOverlay() { return true; }

	@Alpha
	@ConfigItem(
			keyName = "entranceOverlayFillColor",
			name = "Entrance Overlay Fill Color",
			description = "Fill color of the entrance reminder overlay for deprioritized entrances",
			section = "entranceOverlaySettings",
			position = 3
	)
	default Color entranceOverlayFillColor() { return new Color(205,50,50,75); }

	@Alpha
	@ConfigItem(
			keyName = "prioritizedEntranceOverlayFillColor",
			name = "Prioritized Entrance Overlay Fill Color",
			description = "Fill color of the entrance reminder overlay for prioritized entrances",
			section = "entranceOverlaySettings",
			position = 4
	)
	default Color prioritizedEntranceOverlayFillColor() { return new Color(255,140,0,75); }

	@ConfigItem(
			keyName = "entranceOverlayImageScale",
			name = "Image Scale",
			description = "The scale of the inactive Escape Crystal image",
			section = "entranceOverlaySettings",
			position = 5
	)
	default double entranceOverlayImageScale()
	{
		return 1.0;
	}

	@ConfigItem(
		keyName = "deprioritizedMenuText",
		name = "Deprioritized Menu Text",
		description = "Text to display in the deprioritized menu option for entrances",
		section = "entranceOverlaySettings",
		position = 6
	)
	default String deprioritizedMenuText() { return "Where's Your Crystal?"; }

	@Alpha
	@ConfigItem(
		keyName = "deprioritizedMenuTextColor",
		name = "Deprioritized Menu Text Color",
		description = "Color of the deprioritized menu option text",
		section = "entranceOverlaySettings",
		position = 7
	)
	default Color deprioritizedMenuTextColor() { return new Color(255,106,213,255); }

	@ConfigSection(
			name = "Location Filter",
			description = "Filter locations where the reminder is shown",
			position = 6
	)
	String displayRegionFilter = "displayRegionFilter";

	@ConfigItem(
			keyName = "displayBosses",
			name = "Display for Unsafe Bosses",
			description = "Display the overlays for unsafe bosses",
			section = "displayRegionFilter",
			position = 1
	)
	default boolean displayBosses() { return true; }

	@ConfigItem(
			keyName = "displayRaids",
			name = "Display in Unsafe Raids",
			description = "Display the overlays in unsafe raids",
			section = "displayRegionFilter",
			position = 2
	)
	default boolean displayRaids() { return true; }

	@ConfigItem(
			keyName = "displayDungeons",
			name = "Display in Unsafe Dungeons",
			description = "Display the overlays when inside unsafe dungeons",
			section = "displayRegionFilter",
			position = 3
	)
	default boolean displayDungeons() { return true; }

	@ConfigItem(
			keyName = "displayMinigames",
			name = "Display in Unsafe Minigames",
			description = "Display the overlays when in unsafe minigames",
			section = "displayRegionFilter",
			position = 4
	)
	default boolean displayMinigames() { return true; }

	@ConfigItem(
			keyName = "displayEverywhere",
			name = "Always display at all times",
			description = "Display the overlays at all times",
			section = "displayRegionFilter",
			position = 5
	)
	default boolean displayEverywhere() { return false; }

	@ConfigItem(
			keyName = "excludeZulrahWithEliteDiary",
			name = "Exclude Zulrah With Elite Diary",
			description = "Don't display at Zulrah when you have completed the Western Elite Diary",
			section = "displayRegionFilter",
			position = 6
	)
	default boolean excludeZulrahWithEliteDiary() { return true; }

	@ConfigItem(
			keyName = "excludeRegionIds",
			name = "Exclude Region IDs",
			description = "A comma separated list of Region IDs to exclude",
			section = "displayRegionFilter",
			position = 7
	)
	default String excludeRegionIds() { return ""; }

	@ConfigItem(
			keyName = "includeRegionIds",
			name = "Include Region IDs",
			description = "A comma separated list of Region IDs to include",
			section = "displayRegionFilter",
			position = 8
	)
	default String includeRegionIds() { return ""; }

	@ConfigSection(
			name = "Notification Settings",
			description = "Configure preferences for Runelite notifications",
			position = 7
	)
	String notificationSettings = "notificationSettings";

	@ConfigItem(
			keyName = "notificationInactivityTimeFormat",
			name = "Time Format",
			description = "Format for displaying the time information shown in your RuneLite notifications",
			section = "notificationSettings",
			position = 1
	)
	default InactivityTimeFormat notificationInactivityTimeFormat()
	{
		return InactivityTimeFormat.SECONDS;
	}

	@ConfigItem(
			keyName = "notifyInactive",
			name = "Inactive Crystal",
			description = "Sends a notification when your escape crystal is not enabled",
			section = "notificationSettings",
			position = 2
	)
	default boolean notifyInactive() { return true; }

	@ConfigItem(
			keyName = "notifyMissing",
			name = "Missing Crystal",
			description = "Sends a notification when you are not carrying your escape crystal",
			section = "notificationSettings",
			position = 3
	)
	default boolean notifyMissing() { return true; }

	@ConfigItem(
			keyName = "notifyTimeUntilTeleportThreshold",
			name = "Time Remaining Threshold",
			description = "Sends a notification when your escape crystal is about to trigger. Note that this respects the time format you specified above (Ticks vs Seconds). A value of 0 will disable the notification.",
			section = "notificationSettings",
			position = 4
	)
	default int notifyTimeUntilTeleportThreshold() { return 0; }

	@ConfigItem(
			keyName = "notifyNonLeftClickTeleport",
			name = "Non Left Click Teleport",
			description = "Sends a notification when the left click option on your escape crystal is not set to teleport",
			section = "notificationSettings",
			position = 5
	)
	default boolean notifyNonLeftClickTeleport() { return true; }

	@ConfigSection(
			name = "Leviathan Safeguards",
			description = "Configure safeguards to prevent deaths due to Leviathan logout bugs",
			position = 8
	)
	String leviathanSafeguardSettings = "leviathanSafeguardSettings";

	@ConfigItem(
			keyName = "disableLeviathanSafeguardPanelPopup",
			name = "Disable Panel Popup",
			description = "Disables the panel that appears when near Leviathan's entry boat",
			section = "leviathanSafeguardSettings",
			position = 1
	)
	default boolean disableLeviathanSafeguardPanelPopup() { return false; }

	@ConfigItem(
			keyName = "hideLeviathanBugInfoText",
			name = "Hide Bug Info Text",
			description = "Hides the information about Leviathan's logout bug",
			section = "leviathanSafeguardSettings",
			position = 2
	)
	default boolean hideLeviathanBugInfoText() { return false; }

	@ConfigItem(
			keyName = "deprioritizeLeviathanLogout",
			name = "Deprioritize Logout in Leviathan Arena",
			description = "Deprioritizes the logout menu entry while inside Leviathan's arena to disallow accidental logouts",
			section = "leviathanSafeguardSettings",
			position = 3
	)
	default boolean deprioritizeLeviathanLogout() { return false; }

	@ConfigItem(
			keyName = "hideLeviathanLogoutSettingText",
			name = "Hide Current Logout Setting Text",
			description = "Hides the information about the current configured logout setting",
			section = "leviathanSafeguardSettings",
			position = 4
	)
	default boolean hideLeviathanLogoutSettingText() { return false; }

	@ConfigItem(
			keyName = "warnSixHourLogout",
			name = "Warn When Close to a 6-Hour Logout",
			description = "Warns you when you are close to getting 6-hour logged",
			section = "leviathanSafeguardSettings",
			position = 5
	)
	default boolean warnLeviathanLogoutTimer() { return true; }

	@ConfigItem(
			keyName = "hideLeviathanSettingsInstructionText",
			name = "Hide Bottom Instruction Info",
			description = "Hides the text at the bottom of the panel that explains how to enable/disable the Leviathan safeguard features",
			section = "leviathanSafeguardSettings",
			position = 6
	)
	default boolean hideLeviathanSettingsInstructionText() { return false; }

	@ConfigSection(
			name = "Doom Safeguards",
			description = "Configure safeguards to prevent deaths due to Doom logout bugs",
			position = 9
	)
	String doomSafeguardSettings = "doomSafeguardSettings";

	@ConfigItem(
			keyName = "disableDoomSafeguardPanelPopup",
			name = "Disable Panel Popup",
			description = "Disables the panel that appears when near Doom's arena",
			section = "doomSafeguardSettings",
			position = 1
	)
	default boolean disableDoomSafeguardPanelPopup() { return false; }

	@ConfigItem(
			keyName = "hideDoomBugInfoText",
			name = "Hide Bug Info Text",
			description = "Hides the information about Doom's logout bug",
			section = "doomSafeguardSettings",
			position = 2
	)
	default boolean hideDoomBugInfoText() { return false; }

	@ConfigItem(
			keyName = "deprioritizeDoomLogout",
			name = "Deprioritize Logout in Doom's Arena",
			description = "Deprioritizes the logout menu entry while inside Doom's arena to disallow accidental logouts. The logout is ENABLED between floors.",
			section = "doomSafeguardSettings",
			position = 3
	)
	default boolean deprioritizeDoomLogout() { return false; }

	@ConfigItem(
			keyName = "hideDoomLogoutSettingText",
			name = "Hide Current Logout Setting Text",
			description = "Hides the information about the current configured logout setting",
			section = "doomSafeguardSettings",
			position = 4
	)
	default boolean hideDoomLogoutSettingText() { return false; }

	@ConfigItem(
			keyName = "warnDoomSixHourLogout",
			name = "Warn When Close to a 6-Hour Logout",
			description = "Warns you when you are close to getting 6-hour logged",
			section = "doomSafeguardSettings",
			position = 5
	)
	default boolean warnDoomLogoutTimer() { return true; }

	@ConfigItem(
			keyName = "hideDoomSettingsInstructionText",
			name = "Hide Bottom Instruction Info",
			description = "Hides the text at the bottom of the panel that explains how to enable/disable the Doom safeguard features",
			section = "doomSafeguardSettings",
			position = 6
	)
	default boolean hideDoomSettingsInstructionText() { return false; }
}

