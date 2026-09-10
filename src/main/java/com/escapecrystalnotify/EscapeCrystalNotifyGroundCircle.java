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
import net.runelite.api.Point;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.util.ImageUtil;

/** Caches the artwork and masks the projected ground effect beneath the player. */
final class EscapeCrystalNotifyGroundCircle
{
	private static final int SIZE = 256;
	private static final int HALF_GROUND_SIZE = 96;
	private static final BufferedImage CRYSTAL = ImageUtil.loadImageResource(EscapeCrystalNotifyPlugin.class, "/escape-crystal-active.png");
	private BufferedImage texture;
	private Color textureColor;
	private int textureWidth, textureGlow;
	private BufferedImage textureIcon;
	private BufferedImage buffer;
	private final EscapeCrystalNotifyPlayerMask playerMask = new EscapeCrystalNotifyPlayerMask();

	void draw(Graphics2D graphics, Client client, LocalPoint location, int plane,
		Color color, int width, int glow, boolean showImage, Player player)
	{
		int height = Perspective.getTileHeight(client, location, plane);
		int x = location.getX(), y = location.getY(), view = location.getWorldView();
		// Rotate the ground quad so the image's top points forward (actor orientation 0 faces south).
		// Use the current orientation to follow the player's visible turn without rebuilding the texture.
		int orientation = player.getCurrentOrientation();
		int cos = -Math.round(Perspective.COSINEF[orientation] * HALF_GROUND_SIZE);
		int sin = -Math.round(Perspective.SINEF[orientation] * HALF_GROUND_SIZE);
		Point a = Perspective.localToCanvas(client, view, x - cos + sin, y + sin + cos, height);
		Point b = Perspective.localToCanvas(client, view, x + cos + sin, y - sin + cos, height);
		Point c = Perspective.localToCanvas(client, view, x + cos - sin, y - sin - cos, height);
		Point d = Perspective.localToCanvas(client, view, x - cos - sin, y + sin - cos, height);
		if (a == null || b == null || c == null || d == null) return;
		BufferedImage icon = showImage ? CRYSTAL : null;
		BufferedImage image = texture(color, width, glow, icon);
		Rectangle bounds = new Polygon(new int[]{a.getX(), b.getX(), c.getX(), d.getX()},
			new int[]{a.getY(), b.getY(), c.getY(), d.getY()}, 4).getBounds();
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
			drawTriangle(g, image, a, b, d, new AffineTransform(
				(b.getX() - a.getX()) / (double) SIZE, (b.getY() - a.getY()) / (double) SIZE,
				(d.getX() - a.getX()) / (double) SIZE, (d.getY() - a.getY()) / (double) SIZE,
				a.getX(), a.getY()));
			drawTriangle(g, image, b, c, d, new AffineTransform(
				(c.getX() - d.getX()) / (double) SIZE, (c.getY() - d.getY()) / (double) SIZE,
				(c.getX() - b.getX()) / (double) SIZE, (c.getY() - b.getY()) / (double) SIZE,
				b.getX() + d.getX() - c.getX(), b.getY() + d.getY() - c.getY()));
			playerMask.erase(g, client, player, location, height, bounds);
		}
		finally { g.dispose(); }
		graphics.drawImage(buffer, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
			0, 0, bounds.width, bounds.height, null);
	}

	BufferedImage texture(Color color, int width, int glow, BufferedImage icon)
	{
		if (texture != null && color.equals(textureColor) && width == textureWidth
			&& glow == textureGlow && icon == textureIcon) return texture;
		texture = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = texture.createGraphics();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
			if (icon != null)
			{
				int iconHeight = Math.round(SIZE * 0.6f);
				int iconWidth = icon.getWidth() * iconHeight / icon.getHeight();
				g.setComposite(java.awt.AlphaComposite.SrcOver.derive(color.getAlpha() / 255f));
				g.drawImage(icon, (SIZE - iconWidth) / 2, (SIZE - iconHeight) / 2, iconWidth, iconHeight, null);
			}
		}
		finally { g.dispose(); }
		textureColor = color;
		textureWidth = width;
		textureGlow = glow;
		textureIcon = icon;
		return texture;
	}

	private static void drawTriangle(Graphics2D graphics, BufferedImage image, Point a, Point b, Point c, AffineTransform transform)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.clip(new Polygon(new int[]{a.getX(), b.getX(), c.getX()}, new int[]{a.getY(), b.getY(), c.getY()}, 3));
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, transform, null);
		}
		finally { g.dispose(); }
	}
}
