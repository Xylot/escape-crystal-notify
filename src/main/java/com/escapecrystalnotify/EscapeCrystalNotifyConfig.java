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
			name = "Location Filter",
			description = "Filter locations where the reminder is shown",
			closedByDefault = true,
			position = 2
	)
	String displayRegionFilter = "displayRegionFilter";

	@ConfigItem(
			keyName = "displayBosses",
			name = "Enable for Unsafe Bosses",
			description = "Enable the overlays for unsafe bosses",
			section = "displayRegionFilter",
			position = 1
	)
	default boolean displayBosses() { return true; }

	@ConfigItem(
			keyName = "displayRaids",
			name = "Enable in Unsafe Raids",
			description = "Enable the overlays in unsafe raids",
			section = "displayRegionFilter",
			position = 2
	)
	default boolean displayRaids() { return true; }

	@ConfigItem(
			keyName = "displayDungeons",
			name = "Enable in Unsafe Dungeons",
			description = "Enable the overlays when inside unsafe dungeons",
			section = "displayRegionFilter",
			position = 3
	)
	default boolean displayDungeons() { return true; }

	@ConfigItem(
			keyName = "displayMinigames",
			name = "Enable in Unsafe Minigames",
			description = "Enable the overlays when in unsafe minigames",
			section = "displayRegionFilter",
			position = 4
	)
	default boolean displayMinigames() { return true; }

	@ConfigItem(
			keyName = "displayTeleportDisabled",
			name = "Enable in Teleport Disabled Areas",
			description = "Enable the reminder overlay when in areas where your escape crystal cannot teleport you out",
			section = "displayRegionFilter",
			position = 5
	)
	default boolean displayTeleportDisabled() { return true; }

	@ConfigItem(
			keyName = "displayEverywhere",
			name = "Enable Everywhere",
			description = "Enable the overlays at all times",
			section = "displayRegionFilter",
			position = 6
	)
	default boolean displayEverywhere() { return false; }

	@ConfigItem(
			keyName = "excludeZulrahWithEliteDiary",
			name = "Exclude Zulrah With Elite Diary",
			description = "Don't display at Zulrah when you have completed the Western Elite Diary. Note: This exclusion is removed if you die and proc the revival.",
			section = "displayRegionFilter",
			position = 7
	)
	default boolean excludeZulrahWithEliteDiary() { return true; }

	@ConfigItem(
			keyName = "excludeSafeForRegularHardcoreRegions",
			name = "Exclude Safe Regular HC Regions",
			description = "Don't display at regions that are explicitly safe for regular hardcores (Cox, inferno, etc..). Displays will still be enabled when playing on a GHCIM.",
			section = "displayRegionFilter",
			position = 8
	)
	default boolean excludeSafeForRegularHardcoreRegions() { return true; }

	@ConfigItem(
			keyName = "excludeRegionIds",
			name = "Exclude Region IDs",
			description = "A comma separated list of Region IDs to exclude",
			section = "displayRegionFilter",
			position = 9
	)
	default String excludeRegionIds() { return ""; }

	@ConfigItem(
			keyName = "includeRegionIds",
			name = "Include Region IDs",
			description = "A comma separated list of Region IDs to include",
			section = "displayRegionFilter",
			position = 10
	)
	default String includeRegionIds() { return ""; }

	@ConfigSection(
			name = "Entrance Overlay",
			description = "Add a reminder overlay to boss/dungeon entrances and optional deprioritization of the enter menu option",
			closedByDefault = true,
			position = 3
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

	@Alpha
	@ConfigItem(
			keyName = "escapeCrystalDisabledTextColor",
			name = "Escape Crystal Disabled Text Color",
			description = "Color of the Escape Crystal disabled text overlay (Some regions of the game do not allow Escape Crystals)",
			section = "entranceOverlaySettings",
			position = 8
	)
	default Color escapeCrystalDisabledTextColor() { return new Color(255,0,0,255); }

	@ConfigSection(
		name = "On-Screen Widget",
		description = "Settings for the movable and resizable on-screen widget",
		closedByDefault = true,
		position = 4
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
		name = "Info Box",
		description = "Settings for Info Box",
		closedByDefault = true,
		position = 5
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
		name = "Inventory Highlight",
		description = "Settings for inventory & equipment screen",
		closedByDefault = true,
		position = 6
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
		name = "Notification Settings",
		description = "Configure preferences for Runelite notifications",
		closedByDefault = true,
		position = 7
	)
	String notificationSettings = "notificationSettings";

	@ConfigItem(
		keyName = "requireCombatForNotifications",
		name = "Only Send in Combat",
		description = "Only show notifications when in combat or recently in combat",
		section = "notificationSettings",
		position = 1
	)
	default boolean requireCombatForNotifications() { return true; }

	@ConfigItem(
		keyName = "combatGracePeriodSeconds",
		name = "Combat Grace Period",
		description = "How many seconds after combat to enable notifications (0 = only during combat)",
		section = "notificationSettings",
		position = 2
	)
	default int combatGracePeriodSeconds() { return 10; }

	@ConfigItem(
		keyName = "notificationInactivityTimeFormat",
		name = "Time Format",
		description = "Format for displaying the time information shown in your RuneLite notifications",
		section = "notificationSettings",
		position = 3
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
		position = 4
	)
	default boolean notifyInactive() { return true; }

	@ConfigItem(
		keyName = "notifyMissing",
		name = "Missing Crystal",
		description = "Sends a notification when you are not carrying your escape crystal",
		section = "notificationSettings",
		position = 5
	)
	default boolean notifyMissing() { return true; }

	@ConfigItem(
		keyName = "notifyTimeUntilTeleportThreshold",
		name = "Time Remaining Threshold",
		description = "Sends a notification when your escape crystal is about to trigger. Note that this respects the time format you specified above (Ticks vs Seconds). A value of 0 will disable the notification.",
		section = "notificationSettings",
		position = 6
	)
	default int notifyTimeUntilTeleportThreshold() { return 0; }

	@ConfigItem(
		keyName = "notifyNonLeftClickTeleport",
		name = "Non Left Click Teleport",
		description = "Sends a notification when the left click option on your escape crystal is not set to teleport",
		section = "notificationSettings",
		position = 7
	)
	default boolean notifyNonLeftClickTeleport() { return true; }

	@ConfigSection(
		name = "Leviathan Safeguards",
		description = "Configure safeguards to prevent deaths due to Leviathan logout bugs",
		closedByDefault = true,
		position = 8
	)
	String leviathanSafeguardSettings = "leviathanSafeguardSettings";

	@ConfigItem(
			keyName = "leviathanSafeguardMode",
			name = "Mode",
			description = "Controls when Leviathan safeguards are active. Hardcore accounts have this enabled by default.",
			section = "leviathanSafeguardSettings",
			position = 1
	)
	default SafeguardAccountType leviathanSafeguardMode() { return SafeguardAccountType.HC_ONLY; }

	@ConfigItem(
		keyName = "displayLeviathanBugInfo",
		name = "Display Bug Info",
		description = "Display information about Leviathan's logout bug",
		section = "leviathanSafeguardSettings",
		position = 2
	)
	default boolean displayLeviathanBugInfo() { return true; }

	@ConfigItem(
		keyName = "displayLeviathanLogoutSetting",
		name = "Display Logout Setting",
		description = "Display information about the current configured logout setting",
		section = "leviathanSafeguardSettings",
		position = 3
	)
	default boolean displayLeviathanLogoutSetting() { return true; }

	@Alpha
	@ConfigItem(
		keyName = "leviathanLogoutBugHighlightColor",
		name = "6-Hour Entrance Highlight Color",
		description = "Color used to highlight entrances when close to 6-hour logout and logout bug is possible",
		section = "leviathanSafeguardSettings",
		position = 4
	)
	default Color leviathanLogoutBugHighlightColor() { return new Color(173, 216, 230, 75); }

	@ConfigItem(
		keyName = "leviathanLogoutBugMessage",
		name = "6-Hour Warning Message",
		description = "Message displayed when close to 6-hour logout and logout bug is possible. Only shows when 'Display Logout Setting' is enabled.",
		section = "leviathanSafeguardSettings",
		position = 5
	)
	default String leviathanLogoutBugMessage() { return "Relog - Close to 6 hour logout"; }

	@ConfigSection(
		name = "Doom Safeguards",
		description = "Configure safeguards to prevent deaths due to Doom logout bugs",
		closedByDefault = true,
		position = 9
	)
	String doomSafeguardSettings = "doomSafeguardSettings";

	@ConfigItem(
			keyName = "doomSafeguardMode",
			name = "Mode",
			description = "Controls when Doom safeguards are active. Hardcore accounts have this enabled by default.",
			section = "doomSafeguardSettings",
			position = 1
	)
	default SafeguardAccountType doomSafeguardMode() { return SafeguardAccountType.HC_ONLY; }

	@ConfigItem(
		keyName = "displayDoomBugInfo",
		name = "Display Bug Info",
		description = "Display information about Doom's logout bug",
		section = "doomSafeguardSettings",
		position = 2
	)
	default boolean displayDoomBugInfo() { return true; }

	@ConfigItem(
		keyName = "displayDoomLogoutSetting",
		name = "Display Logout Setting",
		description = "Display information about the current configured logout setting",
		section = "doomSafeguardSettings",
		position = 3
	)
	default boolean displayDoomLogoutSetting() { return true; }

	@Alpha
	@ConfigItem(
		keyName = "doomLogoutBugHighlightColor",
		name = "6-Hour Entrance Highlight Color",
		description = "Color used to highlight entrances when close to 6-hour logout and logout bug is possible",
		section = "doomSafeguardSettings",
		position = 4
	)
	default Color doomLogoutBugHighlightColor() { return new Color(0, 142, 80, 75); }

	@ConfigItem(
		keyName = "doomLogoutBugMessage",
		name = "6-Hour Warning Message",
		description = "Message displayed when close to 6-hour logout and logout bug is possible. Only shows when 'Display Logout Setting' is enabled.",
		section = "doomSafeguardSettings",
		position = 5
	)
	default String doomLogoutBugMessage() { return "Relog - Close to 6 hour logout"; }

	@ConfigSection(
		name = "Non-HC Inventory Highlight",
		description = "Settings for minimal inventory highlighting for non-hardcore accounts",
		closedByDefault = true,
		position = 10
	)
	String nonHardcoreInventorySettings = "nonHardcoreInventorySettings";

	@ConfigItem(
		keyName = "enableNonHardcoreInventoryHighlight",
		name = "Enable Non-HC Inventory Highlight",
		description = "Enable minimal inventory highlighting for non-hardcore accounts (no overlay text, just highlight)",
		section = "nonHardcoreInventorySettings",
		position = 1
	)
	default boolean enableNonHardcoreInventoryHighlight() { return true; }

	@ConfigItem(
		keyName = "nonHardcoreInventoryOverlayType",
		name = "Overlay Type",
		description = "Method of highlighting the crystal's inventory & equipment model for non-hardcore accounts",
		section = "nonHardcoreInventorySettings",
		position = 2
	)
	default ModelOverlayType nonHardcoreInventoryOverlayType()
	{
		return ModelOverlayType.ITEM_FILL;
	}

	@Alpha
	@ConfigItem(
		keyName = "nonHardcoreInventoryActiveFillColor",
		name = "Active Fill Color",
		description = "Color of the background fill to draw onto the crystal's inventory model while active for non-hardcore accounts",
		section = "nonHardcoreInventorySettings",
		position = 3
	)
	default Color nonHardcoreInventoryActiveFillColor() { return new Color(50,205,50,75); }

	@Alpha
	@ConfigItem(
		keyName = "nonHardcoreInventoryInactiveFillColor",
		name = "Inactive Fill Color",
		description = "Color of the background fill to draw onto the crystal's inventory model while inactive for non-hardcore accounts",
		section = "nonHardcoreInventorySettings",
		position = 4
	)
	default Color nonHardcoreInventoryInactiveFillColor() { return new Color(205,50,50,75); }

	@ConfigSection(
		name = "Debug",
		description = "Settings for testing and debugging the plugin",
		closedByDefault = true,
		position = 11
	)
	String debugSettings = "debugSettings";

	@ConfigItem(
		keyName = "enableDebugMode",
		name = "Enable Testing Mode",
		description = "Shows a comprehensive overlay with all plugin state information for debugging",
		section = "debugSettings",
		position = 1
	)
	default boolean enableDebugMode() { return false; }

	@ConfigItem(
		keyName = "showPlayerLocationSection",
		name = "Show Player Location Section",
		description = "Display player location information in the testing overlay",
		section = "debugSettings",
		position = 2
	)
	default boolean showPlayerLocationSection() { return true; }

	@ConfigItem(
		keyName = "showEscapeCrystalSection",
		name = "Show Escape Crystal Section",
		description = "Display escape crystal status information in the testing overlay",
		section = "debugSettings",
		position = 3
	)
	default boolean showEscapeCrystalSection() { return true; }

	@ConfigItem(
		keyName = "showAccountInfoSection",
		name = "Show Account Info Section",
		description = "Display account information in the testing overlay",
		section = "debugSettings",
		position = 4
	)
	default boolean showAccountInfoSection() { return true; }

	@ConfigItem(
		keyName = "showPossibleEntrancesSection",
		name = "Show Possible Entrances Section",
		description = "Display possible entrances information in the testing overlay",
		section = "debugSettings",
		position = 5
	)
	default boolean showPossibleEntrancesSection() { return true; }

	@ConfigItem(
		keyName = "showValidEntrancesSection",
		name = "Show Valid Entrances Section",
		description = "Display valid entrances information in the testing overlay",
		section = "debugSettings",
		position = 6
	)
	default boolean showValidEntrancesSection() { return true; }

	@ConfigItem(
		keyName = "showSpecialRegionSection",
		name = "Show Special Region Section",
		description = "Display special region status information in the testing overlay",
		section = "debugSettings",
		position = 7
	)
	default boolean showSpecialRegionSection() { return true; }

	@ConfigItem(
		keyName = "showNotificationSection",
		name = "Show Notification Section",
		description = "Display notification status information in the testing overlay",
		section = "debugSettings",
		position = 8
	)
	default boolean showNotificationSection() { return true; }

	@ConfigItem(
		keyName = "testingAccountTypeOverride",
		name = "Testing Account Type Override",
		description = "Override the detected account type for testing purposes. Set to 'Default' to use the actual account type.",
		section = "debugSettings",
		position = 9
	)
	default TestingAccountType testingAccountTypeOverride() { return TestingAccountType.DEFAULT; }

	@ConfigItem(
		keyName = "ticksSinceLoginOverride",
		name = "Ticks Since Login Override",
		description = "Override the ticks since login for testing logout bug warnings. Set to -1 to use actual value.",
		section = "debugSettings",
		position = 10
	)
	default int ticksSinceLoginOverride() { return -1; }

	enum TestingAccountType {
		DEFAULT ("Default (Actual Account Type)"),
		NON_HARDCORE ("Non-Hardcore"),
		STANDARD_HARDCORE ("Standard Hardcore"),
		GROUP_HARDCORE ("Group Hardcore");

		private final String displayName;

		TestingAccountType(String displayName) {
			this.displayName = displayName;
		}

		public String toString() {
			return this.displayName;
		}
	}

	enum SafeguardAccountType {
		ALWAYS ("All Account Types"),
		HC_ONLY ("Hardcore Only"),
		DISABLED ("Disabled");

		private final String displayName;

		SafeguardAccountType(String displayName) {
			this.displayName = displayName;
		}

		public String toString() {
			return this.displayName;
		}
	}
}

