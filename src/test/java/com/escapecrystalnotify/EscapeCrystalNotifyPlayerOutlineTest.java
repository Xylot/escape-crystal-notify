package com.escapecrystalnotify;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Player;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyPlayerOutlineTest
{
	@Test
	public void groundCircleRendersRingWithoutBodyRendererAndPreservesGraphicsState() throws Exception
	{
		Fixture f = new Fixture();
		f.settings.style = EscapeCrystalNotifyConfig.PlayerOutlineStyle.GROUND_CIRCLE;
		WorldView view = stub(WorldView.class, (name, args) -> {
			switch (name) {
				case "getId": return WorldView.TOPLEVEL;
				case "isTopLevel": return true;
				case "getPlane": return 0;
				case "getSizeX": case "getSizeY": return 104;
				case "getTileSettings": return new byte[4][104][104];
				case "getTileHeights": return new int[4][105][105];
				default: throw new AssertionError(name);
			}
		});
		LocalPoint location = new LocalPoint(6400, 6400);
		Model[] playerModel = {null};
		Player player = stub(Player.class, (name, args) -> {
			if (name.equals("getLocalLocation")) return location;
			if (name.equals("getWorldView")) return view;
			if (name.equals("getModel")) return playerModel[0];
			if (name.equals("getCurrentOrientation")) return 0;
			throw new AssertionError(name);
		});
		Client client = stub(Client.class, (name, args) -> {
			switch (name) {
				case "getLocalPlayer": return player;
				case "getWorldView": return view;
				case "isGpu": return false;
				case "getCameraX": return 6400;
				case "getCameraY": return 5400;
				case "getCameraZ": return -1200;
				case "getCameraPitch": return 2048;
				case "getCameraYaw": case "getViewportXOffset": case "getViewportYOffset": return 0;
				case "getViewportWidth": return 1000;
				case "getViewportHeight": return 800;
				case "getScale": return 512;
				default: throw new AssertionError(name);
			}
		});
		EscapeCrystalNotifyPlayerOutlineOverlay overlay = new EscapeCrystalNotifyPlayerOutlineOverlay(client, f.plugin, f.settings, null);
		BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		BasicStroke originalStroke = new BasicStroke(2);
		try {
			g.setColor(Color.RED);
			g.setStroke(originalStroke);
			overlay.render(g);
			assertEquals(Color.RED, g.getColor());
			assertSame(originalStroke, g.getStroke());
			int colored = 0;
			for (int y = 0; y < image.getHeight(); y++)
				for (int x = 0; x < image.getWidth(); x++)
					if (image.getRGB(x, y) == Color.CYAN.getRGB()) colored++;
			assertTrue("Circle should render in the active color", colored > 0);
			net.runelite.api.Point center = Perspective.localToCanvas(client, location, 0);
			assertNotNull(center);
			assertEquals("The ring must not fill the player's tile", 0, image.getRGB(center.getX(), center.getY()));
			f.settings.image = true;
			f.settings.glow = 8;
			g.setComposite(java.awt.AlphaComposite.Clear);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setComposite(java.awt.AlphaComposite.SrcOver);
			overlay.render(g);
			int centerPixels = 0;
			for (int y = center.getY() - 5; y <= center.getY() + 5; y++)
				for (int x = center.getX() - 5; x <= center.getX() + 5; x++)
					if ((image.getRGB(x, y) >>> 24) != 0) centerPixels++;
			assertTrue("The crystal image should be projected inside the ring", centerPixels > 0);
			int[] unmasked = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
			float[] modelX = {-40, 40, 0}, modelY = {-80, -80, 60}, modelZ = {0, 0, 0};
			int[] screenX = new int[3], screenY = new int[3];
			Perspective.modelToCanvas(client, view, 3, location.getX(), location.getY(), 0, 0,
				modelX, modelZ, modelY, screenX, screenY);
			int direction = (screenX[1] - screenX[0]) * (screenY[2] - screenY[0])
				- (screenY[1] - screenY[0]) * (screenX[2] - screenX[0]);
			playerModel[0] = stub(Model.class, (name, args) -> {
				switch (name) {
					case "getVerticesCount": return 3;
					case "getVerticesX": return modelX;
					case "getVerticesY": return modelY;
					case "getVerticesZ": return modelZ;
					case "getFaceCount": return 1;
					case "getFaceIndices1": return new int[]{0};
					case "getFaceIndices2": return new int[]{direction < 0 ? 1 : 2};
					case "getFaceIndices3": return new int[]{direction < 0 ? 2 : 1};
					case "getFaceTransparencies": return null;
					default: throw new AssertionError(name);
				}
			});
			java.awt.Polygon body = new java.awt.Polygon(screenX, screenY, 3);
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			overlay.render(g);
			int maskedPixels = 0, outsidePixels = 0;
			for (int y = 0; y < image.getHeight(); y++)
				for (int x = 0; x < image.getWidth(); x++)
				{
					if (body.contains(x - 1, y - 1, 3, 3))
					{
						assertEquals("Masking must preserve the underlying game/overlay pixels", Color.DARK_GRAY.getRGB(), image.getRGB(x, y));
						if ((unmasked[y * image.getWidth() + x] >>> 24) != 0) maskedPixels++;
					}
					else if (image.getRGB(x, y) != Color.DARK_GRAY.getRGB()) outsidePixels++;
				}
			assertTrue("The player's model should obscure part of the ground effect", maskedPixels > 0);
			assertTrue("Unobstructed parts of the ring must remain visible", outsidePixels > 0);
			assertNull("Clipping must not leak to other overlays", g.getClip());
			javax.imageio.ImageIO.write(image, "png", new java.io.File("build/ground-circle-preview.png"));
			playerModel[0] = null;
			g.setComposite(java.awt.AlphaComposite.Clear);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setComposite(java.awt.AlphaComposite.SrcOver);
			overlay.render(g);
			assertArrayEquals("Reusing the buffer must not retain the previous model mask", unmasked,
				image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
		}
		finally { g.dispose(); }
	}

	@Test
	public void bodyOutlineIsDefaultAndTransparentCircleSkipsPlayerAccess() throws Exception
	{
		assertEquals(EscapeCrystalNotifyConfig.PlayerOutlineStyle.BODY_OUTLINE,
			new EscapeCrystalNotifyConfig() {}.playerOutlineStyle());
		Fixture f = new Fixture();
		f.settings.style = EscapeCrystalNotifyConfig.PlayerOutlineStyle.GROUND_CIRCLE;
		f.settings.activeColor = new Color(0, 0, 0, 0);
		assertNull(f.overlay.render(null));
	}

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
		PlayerOutlineStyle style = PlayerOutlineStyle.BODY_OUTLINE;
		boolean image;
		int glow;
		@Override public PlayerOutlineStyle playerOutlineStyle() { return style; }
		@Override public boolean groundCircleImage() { return image; }
		@Override public int groundCircleGlow() { return glow; }
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

	private static <T> T stub(Class<T> type, BiFunction<String, Object[], Object> answer)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
			(proxy, method, args) -> answer.apply(method.getName(), args)));
	}

	private static void set(Object object, String name, Object value) throws Exception
	{
		Field field = object.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(object, value);
	}
}
