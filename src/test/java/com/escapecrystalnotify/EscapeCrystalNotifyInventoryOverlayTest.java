package com.escapecrystalnotify;

import com.google.common.cache.Cache;
import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyInventoryOverlayTest
{
	@Test
	public void highlightSettingsMatchPreviousBehaviorAcrossAllCombinations()
	{
		for (int flags = 0; flags < 128; flags++)
		{
			final int state = flags;
			for (EscapeCrystalNotifyConfig.ModelOverlayType mainType : EscapeCrystalNotifyConfig.ModelOverlayType.values())
			for (EscapeCrystalNotifyConfig.ModelOverlayType nonHcType : EscapeCrystalNotifyConfig.ModelOverlayType.values())
			{
				EscapeCrystalNotifyConfig settings = new EscapeCrystalNotifyConfig() {
					@Override public boolean enableInventoryDisplay() { return (state & 1) != 0; }
					@Override public boolean enableNonHardcoreInventoryHighlight() { return (state & 4) != 0; }
					@Override public boolean alwaysDisplayInventory() { return (state & 32) != 0; }
					@Override public ModelOverlayType inventoryOverlayType() { return mainType; }
					@Override public ModelOverlayType nonHardcoreInventoryOverlayType() { return nonHcType; }
					@Override public Color inventoryActiveFillColor() { return Color.GREEN; }
					@Override public Color inventoryInactiveFillColor() { return Color.RED; }
					@Override public Color nonHardcoreInventoryActiveFillColor() { return Color.BLUE; }
					@Override public Color nonHardcoreInventoryInactiveFillColor() { return Color.ORANGE; }
					@Override public String inventoryActiveText() { return ""; }
					@Override public String inventoryInactiveText() { return ""; }
				};
				EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin() {
					@Override public boolean isAccountTypeEnabled() { return (state & 2) != 0; }
					@Override public boolean isHardcoreAccountType() { return (state & 8) != 0; }
					@Override public boolean isAtNotifyRegionId() { return (state & 16) != 0; }
					@Override public boolean isEscapeCrystalActive() { return (state & 64) != 0; }
					@Override public String getItemModelDisplayText(EscapeCrystalNotifyConfig.OverlayDisplayType format,
						EscapeCrystalNotifyConfig.InactivityTimeFormat timeFormat, String expiredText) { return ""; }
				};
				EscapeCrystalNotifyInventoryOverlay overlay = new EscapeCrystalNotifyInventoryOverlay(null, plugin, settings) {
					@Override Image getCrystalFillImage(Color color) {
						BufferedImage sprite = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = sprite.createGraphics();
						try { g.setColor(color); g.fillRect(0, 0, 8, 8); }
						finally { g.dispose(); }
						return sprite;
					}
				};
				// Preserve the original conditions and main-display precedence as the oracle.
				boolean inRegion = (state & 16) != 0 || (state & 32) != 0;
				boolean main = (state & 1) != 0 && inRegion && (state & 2) != 0;
				boolean nonHc = (state & 4) != 0 && (state & 8) == 0 && inRegion;
				boolean active = (state & 64) != 0;
				EscapeCrystalNotifyConfig.ModelOverlayType type = main ? mainType : nonHcType;
				Color color = main ? (active ? Color.GREEN : Color.RED) : (active ? Color.BLUE : Color.ORANGE);
				boolean draws = (main || nonHc) && type != EscapeCrystalNotifyConfig.ModelOverlayType.DISABLED;
				BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				try { overlay.renderItemOverlay(graphics, ItemID.TOB_TELEPORT, item(10, 10)); }
				finally { graphics.dispose(); }
				String scenario = "flags=" + state + ", main=" + mainType + ", nonHC=" + nonHcType;
				assertEquals(scenario, draws ? color.getRGB() : 0, image.getRGB(11, 11));
				assertEquals(scenario, draws && type == EscapeCrystalNotifyConfig.ModelOverlayType.BACKGROUND_FILL
					? color.getRGB() : 0, image.getRGB(30, 30));
				assertEquals(scenario, 0, countColor(image, new Rectangle(0, 42, 64, 22), null));
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void cachedTintsKeepDistinctBlueAndAlphaBits() throws Exception
	{
		EscapeCrystalNotifyInventoryOverlay overlay = new EscapeCrystalNotifyInventoryOverlay(null, null, null);
		Field field = overlay.getClass().getDeclaredField("fillCache");
		field.setAccessible(true);
		Cache<Integer, Image> cache = (Cache<Integer, Image>) field.get(overlay);
		Color first = new Color(50, 205, 0, 75);
		Color second = new Color(50, 205, 1, 75);
		Color third = new Color(50, 205, 0, 76);
		// These different blues collided under the previous getRGB() | getAlpha() key.
		assertEquals(first.getRGB() | first.getAlpha(), second.getRGB() | second.getAlpha());
		Image a = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Image b = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Image c = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		cache.put(first.getRGB(), a);
		cache.put(second.getRGB(), b);
		cache.put(third.getRGB(), c);
		assertSame(a, overlay.getCrystalFillImage(first));
		assertSame(b, overlay.getCrystalFillImage(second));
		assertSame(c, overlay.getCrystalFillImage(third));
		assertSame(a, overlay.getCrystalFillImage(first));
	}

	@Test
	public void invisibleHighlightsAndEmptyLabelsDoNotLoadSpritesOrDraw()
	{
		Settings settings = new Settings();
		settings.text = "";
		EscapeCrystalNotifyInventoryOverlay overlay = overlay(settings);
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			overlay.renderItemOverlay(graphics, ItemID.TOB_TELEPORT, item(10, 10));
			assertEquals(0, countColor(image, new Rectangle(0, 0, 100, 100), null));
			settings.text = "Hidden";
			settings.textColor = new Color(255, 0, 0, 0);
			overlay.renderItemOverlay(graphics, ItemID.TOB_TELEPORT, item(10, 10));
			assertEquals(0, countColor(image, new Rectangle(0, 0, 100, 100), null));
		}
		finally { graphics.dispose(); }
	}

	@Test
	public void reusedTextUpdatesPositionAndColorBetweenItems()
	{
		Settings settings = new Settings();
		EscapeCrystalNotifyInventoryOverlay overlay = overlay(settings);
		BufferedImage image = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			overlay.renderItemOverlay(graphics, ItemID.TOB_TELEPORT, item(10, 10));
			settings.text = "Next";
			settings.textColor = Color.BLUE;
			overlay.renderItemOverlay(graphics, ItemID.TOB_TELEPORT, item(80, 60));
			assertTrue(countColor(image, new Rectangle(0, 25, 70, 25), Color.RED) > 0);
			assertEquals(0, countColor(image, new Rectangle(0, 25, 70, 25), Color.BLUE));
			assertTrue(countColor(image, new Rectangle(75, 75, 80, 25), Color.BLUE) > 0);
		}
		finally { graphics.dispose(); }
	}

	private static class Settings implements EscapeCrystalNotifyConfig
	{
		String text = "Active";
		Color textColor = Color.RED;
		@Override public boolean alwaysDisplayInventory() { return true; }
		@Override public boolean enableInventoryDisplay() { return true; }
		@Override public Color inventoryActiveFillColor() { return new Color(0, 0, 0, 0); }
		@Override public ModelOverlayType inventoryOverlayType() { return ModelOverlayType.ITEM_FILL; }
		@Override public String inventoryActiveText() { return text; }
		@Override public Color inventoryActiveTextColor() { return textColor; }
	}

	private static EscapeCrystalNotifyInventoryOverlay overlay(Settings settings)
	{
		EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin() {
			@Override public boolean isAccountTypeEnabled() { return true; }
			@Override public boolean isEscapeCrystalActive() { return true; }
			@Override public String getItemModelDisplayText(EscapeCrystalNotifyConfig.OverlayDisplayType format,
				EscapeCrystalNotifyConfig.InactivityTimeFormat timeFormat, String expiredText) { return ""; }
		};
		// A visible item fill would dereference this null ItemManager.
		return new EscapeCrystalNotifyInventoryOverlay(null, plugin, settings);
	}

	private static WidgetItem item(int x, int y)
	{
		return new WidgetItem(ItemID.TOB_TELEPORT, 1, new Rectangle(x, y, 32, 32), null, null);
	}

	private static int countColor(BufferedImage image, Rectangle bounds, Color color)
	{
		int count = 0;
		for (int y = bounds.y; y < bounds.y + bounds.height; y++)
			for (int x = bounds.x; x < bounds.x + bounds.width; x++)
				if (color == null ? (image.getRGB(x, y) >>> 24) != 0 : image.getRGB(x, y) == color.getRGB()) count++;
		return count;
	}

	@Test
	public void otherItemsReturnBeforeReadingConfigOrPlayer()
	{
		EscapeCrystalNotifyInventoryOverlay overlay = new EscapeCrystalNotifyInventoryOverlay(null, null, null);
		overlay.renderItemOverlay(null, 0, null);
	}
}
