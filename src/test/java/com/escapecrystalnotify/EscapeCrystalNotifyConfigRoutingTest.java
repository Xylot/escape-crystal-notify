package com.escapecrystalnotify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyConfigRoutingTest
{
	@Test
	public void everyDeclaredConfigKeyRoutesToOnlyItsDependentCaches() throws Exception
	{
		Set<String> regionKeys = Set.of("enableDebugMode", "displayBosses", "displayRaids",
			"displayDungeons", "displayMinigames", "displayTeleportDisabled", "excludeZulrahWithEliteDiary",
			"includeRegionIds", "excludeRegionIds", "debugEntranceObjects", "debugEntranceNpcs");
		Set<String> notificationKeys = Set.of("notifyTimeUntilTeleportThreshold",
			"notificationInactivityTimeFormat", "onScreenWidgetInactivityTimeFormat");
		int checked = 0;
		for (Method method : EscapeCrystalNotifyConfig.class.getDeclaredMethods())
		{
			ConfigItem item = method.getAnnotation(ConfigItem.class);
			if (item == null) continue;
			Fixture f = new Fixture();
			f.change(item.keyName());
			assertEquals(item.keyName(), regionKeys.contains(item.keyName()), f.settings.regionReads > 0);
			assertEquals(item.keyName(), notificationKeys.contains(item.keyName()), f.settings.notificationReads > 0);
			checked++;
		}
		assertEquals("Update the audit when config options are added or removed", 117, checked);
	}

	@Test
	public void testingModeRefreshesRegionOverrides() throws Exception
	{
		Fixture f = new Fixture();
		f.change("enableDebugMode");
		assertTrue(f.settings.regionReads > 0);
		assertEquals(0, f.settings.notificationReads);
	}

	@Test
	public void displayChangesDoNotRebuildRegionOrNotificationSettings() throws Exception
	{
		Fixture f = new Fixture();
		for (String key : new String[]{"playerOutlineActiveColor", "enablePlayerOutline",
			"alwaysDisplayPlayerOutline", "requireHardcoreAccountType", "crystal3dSize", "enableCrystal3d",
			"enableTeleportNpc", "teleportNpcType", "debugTeleportNpcOnAnyTeleport", "inventoryActiveFillColor",
			"alwaysDisplayInventory", "enableNonHardcoreInventoryHighlight", "entranceOverlayFillColor"})
		{
			f.change(key);
		}
		assertEquals(0, f.settings.regionReads);
		assertEquals(0, f.settings.notificationReads);
	}

	@Test
	public void dependentSettingsRefreshOnlyTheirOwnCachedValues() throws Exception
	{
		Fixture f = new Fixture();
		f.change("includeRegionIds");
		assertTrue(f.settings.regionReads > 0);
		assertEquals(0, f.settings.notificationReads);
		assertTrue(((java.util.Set<?>) get(f.plugin, "targetRegionIds")).contains(12345));
		int reads = f.settings.regionReads;
		f.change("notifyTimeUntilTeleportThreshold");
		assertTrue(f.settings.notificationReads > 0);
		assertEquals(reads, f.settings.regionReads);
		assertEquals("Your escape crystal will teleport you in 42 ticks!",
			get(f.plugin, "notifyTimeRemainingThresholdMessage"));
	}

	@Test
	public void fullRefreshStillUpdatesAllCachesAndOtherGroupsAreIgnored() throws Exception
	{
		Fixture f = new Fixture();
		ConfigChanged event = new ConfigChanged();
		event.setGroup("anotherPlugin");
		event.setKey("includeRegionIds");
		f.plugin.onConfigChanged(event);
		assertEquals(0, f.settings.regionReads);
		assertEquals(0, f.settings.notificationReads);
		f.plugin.onConfigChanged(null);
		assertTrue(f.settings.regionReads > 0);
		assertTrue(f.settings.notificationReads > 0);
	}

	private static class Settings implements EscapeCrystalNotifyConfig
	{
		int regionReads, notificationReads;
		@Override public boolean displayBosses() { regionReads++; return true; }
		@Override public String includeRegionIds() { return "12345"; }
		@Override public int notifyTimeUntilTeleportThreshold() { notificationReads++; return 42; }
		@Override public InactivityTimeFormat notificationInactivityTimeFormat() { return InactivityTimeFormat.GAME_TICKS; }
	}

	private static class Fixture
	{
		final EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
		final Settings settings = new Settings();
		Fixture() throws Exception
		{
			set(plugin, "config", settings);
		}
		void change(String key)
		{
			ConfigChanged event = new ConfigChanged();
			event.setGroup(EscapeCrystalNotifyConfig.GROUP);
			event.setKey(key);
			plugin.onConfigChanged(event);
		}
	}

	private static Object get(Object target, String name) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static void set(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
