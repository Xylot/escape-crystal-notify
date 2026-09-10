package com.escapecrystalnotify;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import net.runelite.api.Model;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyPlayerMaskTest
{
	@Test
	public void visibleFacesAreRemovedButGapsBetweenLimbsRemain()
	{
		BufferedImage image = masked(new int[]{10, 10, 30, 60, 60, 80},
			new int[]{10, 50, 50, 10, 50, 50}, new int[]{0, 3}, new int[]{1, 4}, new int[]{2, 5}, null);
		assertEquals(0, image.getRGB(15, 40));
		assertEquals(0, image.getRGB(65, 40));
		assertEquals("The effect should show between separate model faces", Color.CYAN.getRGB(), image.getRGB(45, 40));
	}

	@Test
	public void invisibleFacesDoNotRemoveTheEffect()
	{
		int[] x = {10, 10, 30}, y = {10, 50, 50};
		assertUnmasked(masked(x, y, new int[]{0}, new int[]{2}, new int[]{1}, null)); // Back face.
		assertUnmasked(masked(x, y, new int[]{0}, new int[]{1}, new int[]{2}, new byte[]{(byte) 254}));
		assertUnmasked(masked(x, y, new int[]{0}, new int[]{1}, new int[]{2}, new byte[]{(byte) 255}));
		assertUnmasked(masked(new int[]{Integer.MIN_VALUE, 10, 30}, y,
			new int[]{0}, new int[]{1}, new int[]{2}, null)); // Clipped by the near plane.
		assertUnmasked(masked(new int[]{110, 110, 130}, y,
			new int[]{0}, new int[]{1}, new int[]{2}, null)); // Outside the effect buffer.
	}

	private static BufferedImage masked(int[] x, int[] y, int[] a, int[] b, int[] c, byte[] transparency)
	{
		Model model = (Model) Proxy.newProxyInstance(Model.class.getClassLoader(), new Class<?>[]{Model.class},
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "getFaceCount": return a.length;
					case "getFaceIndices1": return a;
					case "getFaceIndices2": return b;
					case "getFaceIndices3": return c;
					case "getFaceTransparencies": return transparency;
					default: throw new AssertionError(method.getName());
				}
			});
		BufferedImage image = new BufferedImage(100, 60, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setColor(Color.CYAN);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			new EscapeCrystalNotifyPlayerMask().eraseFaces(graphics, model, x, y, new Rectangle(0, 0, 100, 60));
		}
		finally { graphics.dispose(); }
		return image;
	}

	private static void assertUnmasked(BufferedImage image)
	{
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++)
				assertEquals(Color.CYAN.getRGB(), image.getRGB(x, y));
	}
}
