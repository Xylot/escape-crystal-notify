package com.escapecrystalnotify;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;

/** Removes visible player faces from the ground effect's private drawing buffer. */
final class EscapeCrystalNotifyPlayerMask
{
	private int[] screenX = new int[0], screenY = new int[0];
	private final Polygon triangle = new Polygon(new int[3], new int[3], 3);

	void erase(Graphics2D graphics, Client client, Player player, LocalPoint location, int height, Rectangle bounds)
	{
		Model model = player.getModel();
		if (model == null) return;
		int count = model.getVerticesCount();
		if (screenX.length < count)
		{
			screenX = new int[count];
			screenY = new int[count];
		}
		Perspective.modelToCanvas(client, player.getWorldView(), count, location.getX(), location.getY(),
			height, player.getCurrentOrientation(), model.getVerticesX(), model.getVerticesZ(), model.getVerticesY(), screenX, screenY);
		eraseFaces(graphics, model, screenX, screenY, bounds);
	}

	void eraseFaces(Graphics2D graphics, Model model, int[] x, int[] y, Rectangle bounds)
	{
		int[] first = model.getFaceIndices1(), second = model.getFaceIndices2(), third = model.getFaceIndices3();
		byte[] transparency = model.getFaceTransparencies();
		graphics.setComposite(AlphaComposite.Clear);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		for (int face = 0; face < model.getFaceCount(); face++)
		{
			if (transparency != null && (transparency[face] & 255) >= 254) continue;
			int a = first[face], b = second[face], c = third[face];
			if (x[a] == Integer.MIN_VALUE || x[b] == Integer.MIN_VALUE || x[c] == Integer.MIN_VALUE) continue;
			long direction = (long) (x[b] - x[a]) * (y[c] - y[a]) - (long) (y[b] - y[a]) * (x[c] - x[a]);
			if (direction >= 0) continue;
			int left = Math.min(x[a], Math.min(x[b], x[c])), right = Math.max(x[a], Math.max(x[b], x[c]));
			int top = Math.min(y[a], Math.min(y[b], y[c])), bottom = Math.max(y[a], Math.max(y[b], y[c]));
			if (!bounds.intersects(left, top, right - left, bottom - top)) continue;
			triangle.xpoints[0] = x[a]; triangle.ypoints[0] = y[a];
			triangle.xpoints[1] = x[b]; triangle.ypoints[1] = y[b];
			triangle.xpoints[2] = x[c]; triangle.ypoints[2] = y[c];
			triangle.invalidate();
			graphics.fill(triangle);
		}
	}
}
