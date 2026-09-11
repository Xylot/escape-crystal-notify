package com.escapecrystalnotify;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.escapecrystalnotify.EscapeCrystalNotifyConfig.PlayerCircleDisplay.*;

public class EscapeCrystalNotifyGroundCircleTest
{
	@Test
	public void triangleMappingPreservesAllImageCornersWhenSkewedAndRotated()
	{
		BufferedImage artwork = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D paint = artwork.createGraphics();
		Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
		try
		{
			paint.setColor(colors[0]); paint.fillRect(0, 0, 32, 16);
			paint.setColor(colors[1]); paint.fillRect(32, 0, 32, 16);
			paint.setColor(colors[2]); paint.fillRect(32, 16, 32, 16);
			paint.setColor(colors[3]); paint.fillRect(0, 16, 32, 16);
		}
		finally { paint.dispose(); }
		int[] x = {40, 140, 160, 20}, y = {20, 40, 160, 140};
		for (int rotation = 0; rotation < 4; rotation++)
		{
			Polygon corners = new Polygon();
			for (int i = 0; i < 4; i++) corners.addPoint(x[(i + rotation) % 4], y[(i + rotation) % 4]);
			BufferedImage output = new BufferedImage(180, 180, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = output.createGraphics();
			try
			{
				EscapeCrystalNotifyGroundCircle.drawTriangle(g, artwork, corners, 0);
				EscapeCrystalNotifyGroundCircle.drawTriangle(g, artwork, corners, 2);
				assertNull("Triangle clipping must stay private", g.getClip());
			}
			finally { g.dispose(); }
			for (int corner = 0; corner < 4; corner++)
			{
				int next = (corner + 1) % 4, previous = (corner + 3) % 4;
				// A point 1/8 along both edges lies safely inside this image quadrant.
				int sampleX = (6 * corners.xpoints[corner] + corners.xpoints[next] + corners.xpoints[previous]) / 8;
				int sampleY = (6 * corners.ypoints[corner] + corners.ypoints[next] + corners.ypoints[previous]) / 8;
				assertEquals("Image corner " + corner + " at rotation " + rotation,
					colors[corner].getRGB(), output.getRGB(sampleX, sampleY));
			}
		}
	}

	@Test
	public void artworkIsReusedUntilAnInputChanges()
	{
		EscapeCrystalNotifyGroundCircle renderer = new EscapeCrystalNotifyGroundCircle();
		BufferedImage ring = renderer.texture(Color.CYAN, 4, 0, CIRCLE_ONLY);
		assertSame(ring, renderer.texture(Color.CYAN, 4, 0, CIRCLE_ONLY));
		BufferedImage glow = renderer.texture(Color.CYAN, 4, 8, CIRCLE_ONLY);
		assertNotSame(ring, glow);
		assertTrue(paintedPixels(glow) > paintedPixels(ring));
		assertSame(glow, renderer.texture(Color.CYAN, 4, 8, CIRCLE_ONLY));
		BufferedImage recolored = renderer.texture(Color.MAGENTA, 4, 8, CIRCLE_ONLY);
		assertNotSame(glow, recolored);
		assertNotSame(recolored, renderer.texture(Color.MAGENTA, 6, 8, CIRCLE_ONLY));
	}

	@Test
	public void optionalImageFillsCenterAndRespectsIndicatorOpacity()
	{
		EscapeCrystalNotifyGroundCircle renderer = new EscapeCrystalNotifyGroundCircle();
		BufferedImage ring = renderer.texture(Color.CYAN, 4, 8, CIRCLE_ONLY);
		assertEquals(0, ring.getRGB(128, 128));
		BufferedImage withIcon = renderer.texture(Color.CYAN, 4, 8, CIRCLE_AND_CRYSTAL);
		assertTrue((withIcon.getRGB(128, 128) >>> 24) > 0);
		BufferedImage faded = renderer.texture(new Color(0, 255, 255, 75), 4, 8, CIRCLE_AND_CRYSTAL);
		assertEquals(75, faded.getRGB(128, 128) >>> 24);
		BufferedImage hidden = renderer.texture(new Color(0, 255, 255, 0), 4, 8, CIRCLE_AND_CRYSTAL);
		assertEquals(0, paintedPixels(hidden));
	}

	@Test
	public void crystalOnlyRemovesRingAndGlowAndModeChangesRefreshTheTexture()
	{
		assertEquals(CIRCLE_AND_CRYSTAL, new EscapeCrystalNotifyConfig() {}.playerCircleDisplay());
		EscapeCrystalNotifyGroundCircle renderer = new EscapeCrystalNotifyGroundCircle();
		BufferedImage both = renderer.texture(Color.CYAN, 4, 8, CIRCLE_AND_CRYSTAL);
		BufferedImage crystal = renderer.texture(Color.CYAN, 4, 8, CRYSTAL_ONLY);
		assertNotSame(both, crystal);
		assertSame(crystal, renderer.texture(Color.CYAN, 4, 8, CRYSTAL_ONLY));
		assertTrue(paintedPixels(crystal) > 0);
		assertTrue(paintedPixels(both) > paintedPixels(crystal));
		for (int y = 0; y < crystal.getHeight(); y++)
			for (int x = 0; x < crystal.getWidth(); x++)
				if (Math.hypot(x - 128, y - 128) > 82)
					assertEquals("No ring or glow may remain around the crystal", 0, crystal.getRGB(x, y));
		BufferedImage circle = renderer.texture(Color.CYAN, 4, 8, CIRCLE_ONLY);
		assertEquals(0, circle.getRGB(128, 128));
		assertNotEquals(0, circle.getRGB(213, 128));
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
