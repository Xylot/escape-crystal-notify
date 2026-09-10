package com.escapecrystalnotify;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyGroundCircleTest
{
	@Test
	public void artworkIsReusedUntilAnInputChanges()
	{
		EscapeCrystalNotifyGroundCircle renderer = new EscapeCrystalNotifyGroundCircle();
		BufferedImage ring = renderer.texture(Color.CYAN, 4, 0, null);
		assertSame(ring, renderer.texture(Color.CYAN, 4, 0, null));
		BufferedImage glow = renderer.texture(Color.CYAN, 4, 8, null);
		assertNotSame(ring, glow);
		assertTrue(paintedPixels(glow) > paintedPixels(ring));
		assertSame(glow, renderer.texture(Color.CYAN, 4, 8, null));
		BufferedImage recolored = renderer.texture(Color.MAGENTA, 4, 8, null);
		assertNotSame(glow, recolored);
		assertNotSame(recolored, renderer.texture(Color.MAGENTA, 6, 8, null));
	}

	@Test
	public void optionalImageFillsCenterAndRespectsIndicatorOpacity()
	{
		EscapeCrystalNotifyGroundCircle renderer = new EscapeCrystalNotifyGroundCircle();
		BufferedImage icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		icon.setRGB(0, 0, Color.ORANGE.getRGB());
		BufferedImage ring = renderer.texture(Color.CYAN, 4, 8, null);
		assertEquals(0, ring.getRGB(128, 128));
		BufferedImage withIcon = renderer.texture(Color.CYAN, 4, 8, icon);
		assertEquals(Color.ORANGE.getRGB(), withIcon.getRGB(128, 128));
		BufferedImage faded = renderer.texture(new Color(0, 255, 255, 75), 4, 8, icon);
		assertEquals(75, faded.getRGB(128, 128) >>> 24);
		BufferedImage hidden = renderer.texture(new Color(0, 255, 255, 0), 4, 8, icon);
		assertEquals(0, paintedPixels(hidden));
	}

	private static int paintedPixels(BufferedImage image)
	{
		int count = 0;
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++)
				if ((image.getRGB(x, y) >>> 24) != 0) count++;
		return count;
	}
}
