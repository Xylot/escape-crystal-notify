package com.escapecrystalnotify;

import java.awt.Color;
import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyPlayerOutlineTest
{
	@Test
	public void colorsFollowCrystalStateIncludingMissingCrystal() throws Exception
	{
		Fixture f = new Fixture();
		assertEquals(Color.CYAN, f.color());
		set(f.plugin, "escapeCrystalActive", false);
		assertEquals(Color.MAGENTA, f.color());
		set(f.plugin, "escapeCrystalActive", true);
		set(f.plugin, "escapeCrystalWithPlayer", false);
		assertEquals(Color.MAGENTA, f.color());
	}

	@Test
	public void independentToggleLocationAndAccountFilters() throws Exception
	{
		Fixture f = new Fixture();
		f.settings.enabled = false;
		assertNull(f.color());
		f.settings.enabled = true;
		f.settings.everywhere = false;
		assertNull(f.color());
		set(f.plugin, "atNotifyRegionId", true);
		assertEquals(Color.CYAN, f.color());
		f.settings.hardcoreOnly = true;
		assertNull(f.color());
		set(f.plugin, "hardcoreAccountType", true);
		assertEquals(Color.CYAN, f.color());
		set(f.plugin, "atNotifyRegionId", false);
		f.settings.everywhere = true;
		assertEquals(Color.CYAN, f.color());
	}

	private static class Settings implements EscapeCrystalNotifyConfig
	{
		boolean enabled = true, everywhere = true, hardcoreOnly;
		Color activeColor = Color.CYAN;
		@Override public boolean enablePlayerOutline() { return enabled; }
		@Override public boolean alwaysDisplayPlayerOutline() { return everywhere; }
		@Override public boolean requireHardcoreAccountType() { return hardcoreOnly; }
		@Override public Color playerOutlineActiveColor() { return activeColor; }
		@Override public Color playerOutlineInactiveColor() { return Color.MAGENTA; }
		@Override public boolean enableInventoryDisplay() { return false; }
		@Override public boolean alwaysDisplayInventory() { return false; }
	}

	private static class Fixture
	{
		final Settings settings = new Settings();
		final EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
		final EscapeCrystalNotifyPlayerOutlineOverlay overlay =
			new EscapeCrystalNotifyPlayerOutlineOverlay(null, plugin, settings, null);
		Fixture() throws Exception
		{
			set(plugin, "config", settings);
		}
		Color color()
		{
			return overlay.outlineColor();
		}
	}

	@Test
	public void readsCurrentSettingsAndAlreadyComputedPluginState() throws Exception
	{
		Fixture f = new Fixture();
		assertEquals(Color.CYAN, f.overlay.outlineColor());
		set(f.plugin, "escapeCrystalActive", false);
		assertEquals(Color.MAGENTA, f.overlay.outlineColor());
		set(f.plugin, "escapeCrystalActive", true);
		f.settings.activeColor = Color.YELLOW;
		assertEquals(Color.YELLOW, f.overlay.outlineColor());
	}

	@Test
	public void disablingTakesEffectWithoutConfigEventOrGameTick() throws Exception
	{
		Fixture f = new Fixture();
		f.settings.enabled = false;
		assertNull(f.overlay.outlineColor());
		assertNull(f.overlay.render(null)); // Disabled rendering never touches the absent Client.
	}

	@Test
	public void outlineUsesAvailableMetricsBeforeFirstTick() throws Exception
	{
		Fixture f = new Fixture();
		assertEquals(Color.CYAN, f.overlay.outlineColor());
		set(f.plugin, "escapeCrystalWithPlayer", false);
		assertEquals(Color.MAGENTA, f.overlay.outlineColor());
	}

	private static void set(Object object, String name, Object value) throws Exception
	{
		Field field = object.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(object, value);
	}
}
