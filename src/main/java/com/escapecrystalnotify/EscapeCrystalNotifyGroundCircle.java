package com.escapecrystalnotify;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.RadialGradientPaint;
import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.util.ImageUtil;
import com.escapecrystalnotify.EscapeCrystalNotifyConfig.PlayerCircleDisplay;

final class EscapeCrystalNotifyGroundCircle
{
	private static final int SIZE = 256;
	private static final int HALF_GROUND_SIZE = 96;
	// Texture corners at orientation zero, with the top of the crystal pointing south.
	private static final float[] GROUND_X = {HALF_GROUND_SIZE, -HALF_GROUND_SIZE, -HALF_GROUND_SIZE, HALF_GROUND_SIZE};
	private static final float[] GROUND_Y = {-HALF_GROUND_SIZE, -HALF_GROUND_SIZE, HALF_GROUND_SIZE, HALF_GROUND_SIZE};
	private static final float[] GROUND_HEIGHT = new float[4];
	private static final BufferedImage CRYSTAL = ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-active.png");
	private BufferedImage texture;
	private Color textureColor;
	private int textureWidth, textureGlow;
	private PlayerCircleDisplay textureDisplay;
	private BufferedImage buffer;
	private final Polygon corners = new Polygon(new int[4], new int[4], 4);
	private final EscapeCrystalNotifyPlayerMask playerMask = new EscapeCrystalNotifyPlayerMask();

	void draw(Graphics2D graphics, Client client, LocalPoint location, int plane,
		Color color, int width, int glow, PlayerCircleDisplay display, Player player)
	{
		int height = Perspective.getTileHeight(client, location, plane);
		Perspective.modelToCanvas(client, player.getWorldView(), 4, location.getX(), location.getY(), height,
			player.getCurrentOrientation(), GROUND_X, GROUND_Y, GROUND_HEIGHT, corners.xpoints, corners.ypoints);
		for (int x : corners.xpoints)
			if (x == Integer.MIN_VALUE) return;
		corners.invalidate();
		BufferedImage image = texture(color, width, glow, display);
		Rectangle bounds = corners.getBounds();
		bounds = bounds.intersection(new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(),
			client.getViewportWidth(), client.getViewportHeight()));
		if (bounds.isEmpty()) return;
		if (buffer == null || buffer.getWidth() < bounds.width || buffer.getHeight() < bounds.height)
		{
			buffer = new BufferedImage(Math.max(buffer == null ? 0 : buffer.getWidth(), (bounds.width + 63) / 64 * 64),
				Math.max(buffer == null ? 0 : buffer.getHeight(), (bounds.height + 63) / 64 * 64), BufferedImage.TYPE_INT_ARGB);
		}
		Graphics2D g = buffer.createGraphics();
		try
		{
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0, 0, bounds.width, bounds.height);
			g.setComposite(AlphaComposite.SrcOver);
			g.translate(-bounds.x, -bounds.y);
			// Two clipped triangles map all four image corners onto the ground quadrilateral.
			drawTriangle(g, image, corners, 0);
			drawTriangle(g, image, corners, 2);
			playerMask.erase(g, client, player, location, height, bounds);
		}
		finally { g.dispose(); }
		graphics.drawImage(buffer, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
			0, 0, bounds.width, bounds.height, null);
	}

	BufferedImage texture(Color color, int width, int glow, PlayerCircleDisplay display)
	{
		if (texture != null && color.equals(textureColor) && width == textureWidth
			&& glow == textureGlow && display == textureDisplay) return texture;
		texture = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = texture.createGraphics();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			if (display != PlayerCircleDisplay.CRYSTAL_ONLY)
			{
				int radius = SIZE / 3;
				int left = SIZE / 2 - radius;
				if (glow > 0)
				{
					int spread = width + glow * 3;
					int outerRadius = radius + spread;
					Color clear = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
					Color halo = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() * 2 / 3);
					g.setPaint(new RadialGradientPaint(SIZE / 2f, SIZE / 2f, outerRadius,
						new float[]{0, (radius - spread) / (float) outerRadius, radius / (float) outerRadius, 1},
						new Color[]{clear, clear, halo, clear}));
					g.fillOval(SIZE / 2 - outerRadius, SIZE / 2 - outerRadius, outerRadius * 2, outerRadius * 2);
				}
				g.setColor(color);
				g.setStroke(new BasicStroke(width * 2));
				g.drawOval(left, left, radius * 2, radius * 2);
				}
			if (display != PlayerCircleDisplay.CIRCLE_ONLY)
			{
				int iconHeight = Math.round(SIZE * 0.6f);
				int iconWidth = CRYSTAL.getWidth() * iconHeight / CRYSTAL.getHeight();
				g.setComposite(java.awt.AlphaComposite.SrcOver.derive(color.getAlpha() / 255f));
				g.drawImage(CRYSTAL, (SIZE - iconWidth) / 2, (SIZE - iconHeight) / 2, iconWidth, iconHeight, null);
			}
		}
		finally { g.dispose(); }
		textureColor = color;
		textureWidth = width;
		textureGlow = glow;
		textureDisplay = display;
		return texture;
	}

	static void drawTriangle(Graphics2D graphics, BufferedImage image, Polygon corners, int origin)
	{
		int next = (origin + 1) % 4, previous = (origin + 3) % 4;
		int[] x = corners.xpoints, y = corners.ypoints;
		AffineTransform transform = new AffineTransform(
			(x[next] - x[origin]) / (double) image.getWidth(), (y[next] - y[origin]) / (double) image.getWidth(),
			(x[previous] - x[origin]) / (double) image.getHeight(), (y[previous] - y[origin]) / (double) image.getHeight(),
			x[origin], y[origin]);
		// The opposite triangle starts at the image's bottom-right corner.
		if (origin == 2) transform.quadrantRotate(2, image.getWidth() / 2.0, image.getHeight() / 2.0);
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.clip(new Polygon(new int[]{x[origin], x[next], x[previous]}, new int[]{y[origin], y[next], y[previous]}, 3));
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, transform, null);
		}
		finally { g.dispose(); }
	}
}
