package com.escapecrystalnotify;

import java.awt.Shape;
import static com.escapecrystalnotify.EscapeCrystalNotifyCrystalDefaults.*;
import java.util.function.IntFunction;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

class EscapeCrystalNotifyCrystalClearance
{
	private double reservedPixels;
	private long holdUntil;
	private int previousLift = -1;

	int lift(Client client, Player player, LocalPoint location, int groundZ, int radius,
		int halfHeight, int padding, long now, double elapsed)
	{
		int logicalHeight = player.getLogicalHeight();
		Point head = Perspective.localToCanvas(client, location.getWorldView(), location.getX(),
			location.getY(), groundZ - logicalHeight - HEAD_ANCHOR_OFFSET);
		if (head == null)
		{
			return -1;
		}
		boolean hint = client.getHintArrowPlayer() == player;
		WorldPoint hintPoint = client.getHintArrowPoint();
		if (!hint && hintPoint != null)
		{
			WorldPoint playerPoint = player.getWorldLocation();
			hint = playerPoint != null && hintPoint.getPlane() == playerPoint.getPlane()
				&& Math.abs(hintPoint.getX() - playerPoint.getX()) <= HINT_DISTANCE_TILES
				&& Math.abs(hintPoint.getY() - playerPoint.getY()) <= HINT_DISTANCE_TILES;
		}
		String text = player.getOverheadText();
		int pixels = overheadPixels(player.getSkullIcon() != -1, player.getOverheadIcon() != null,
			player.getHealthRatio() >= 0, hint, text != null && !text.isEmpty());
		int held = reserve(pixels, now, elapsed);
		int targetBottom = head.getY() - held - padding;
		// Also clear parts of the rendered actor model extending above the usual logical head height.
		Shape actorHull = player.getConvexHull();
		if (actorHull != null)
		{
			targetBottom = Math.min(targetBottom, actorHull.getBounds().y - padding);
		}
		int viewportTop = client.getViewportYOffset() + VIEWPORT_EDGE_MARGIN;
		int viewportBottom = client.getViewportYOffset() + client.getViewportHeight() - VIEWPORT_EDGE_MARGIN;
		int viewportLeft = client.getViewportXOffset() + VIEWPORT_EDGE_MARGIN;
		int viewportRight = client.getViewportXOffset() + client.getViewportWidth() - VIEWPORT_EDGE_MARGIN;
		// Include the entire rotational envelope and both extremes of the bob, not only the bottom tip.
		return solve(logicalHeight + halfHeight, targetBottom,
			viewportLeft, viewportTop, viewportRight, viewportBottom,
			centerLift -> bounds(client, location, groundZ - centerLift, radius, halfHeight));
	}

	static int overheadPixels(boolean skull, boolean prayer, boolean health, boolean hint, boolean chat)
	{
		// Public Actor API exposes health presence, not the full bar stack or final sprite rectangles.
		// Empty slots must not lift the crystal. The grace period below handles prayer flicking.
		// Hitsplats sit below this head anchor; custom name overlays can use the padding setting.
		int pixels = 0;
		if (health) pixels += HEALTH_PIXELS; // Space for stacked standard health/status bars.
		if (skull) pixels += SKULL_PIXELS;
		if (prayer) pixels += PRAYER_PIXELS;
		if (hint) pixels += HINT_PIXELS; // Player-target and nearby coordinate hint arrows, including blinking.
		if (chat) pixels += CHAT_PIXELS; // Text height plus standard wave/scroll effects.
		return pixels;
	}

	int reserve(int requested, long now, double elapsed)
	{
		if (requested >= reservedPixels)
		{
			reservedPixels = requested;
			holdUntil = now + CLEARANCE_HOLD_NANOS;
		}
		else if (now >= holdUntil)
		{
			reservedPixels = Math.max(requested, reservedPixels - Math.min(elapsed, MAX_FRAME_SECONDS) * CLEARANCE_SETTLE_PIXELS_PER_SECOND);
		}
		return (int) Math.ceil(reservedPixels);
	}

	void clear()
	{
		reservedPixels = 0;
		holdUntil = 0;
		previousLift = -1;
	}

	/** Returns -1 if lifting cannot safely clear the overhead band inside the viewport. */
	int solve(int minimum, int targetBottom, int left, int top, int right, int bottom,
		IntFunction<Bounds> project)
	{
		if (targetBottom <= top || right <= left || bottom <= top || minimum > MAX_CLEARANCE_LIFT)
		{
			return previousLift = -1;
		}
		int high = Math.max(minimum, previousLift);
		Bounds bounds = project.apply(high);
		if (bounds == null && high != minimum)
		{
			high = minimum;
			bounds = project.apply(high);
		}
		int low = high;
		int step = CLEARANCE_SEARCH_STEP;
		while (bounds != null && bounds.bottom > targetBottom && high < MAX_CLEARANCE_LIFT)
		{
			low = high;
			high = Math.min(MAX_CLEARANCE_LIFT, high + step);
			step *= 2;
			bounds = project.apply(high);
		}
		if (bounds == null || bounds.bottom > targetBottom)
		{
			return previousLift = -1;
		}
		if (low == high)
		{
			// Validate against this frame's projection, then check downward to avoid retaining excess padding.
			// An unchanged scene needs just the previous height and the unit immediately below it.
			step = 1;
			while (low > minimum)
			{
				low = Math.max(minimum, low - step);
				Bounds candidate = project.apply(low);
				if (candidate == null) return previousLift = -1;
				if (candidate.bottom > targetBottom) break;
				high = low;
				bounds = candidate;
				step *= 2;
			}
		}
		while (high - low > 1)
		{
			int mid = (high + low) / 2;
			Bounds candidate = project.apply(mid);
			if (candidate == null) return previousLift = -1;
			if (candidate.bottom > targetBottom) low = mid;
			else
			{
				high = mid;
				bounds = candidate;
			}
		}
		if (bounds.top < top || bounds.bottom > bottom || bounds.left < left || bounds.right > right)
		{
			return previousLift = -1;
		}
		return previousLift = high;
	}

	static Bounds bounds(Client client, LocalPoint location, int centerZ, int radius, int halfHeight)
	{
		int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE, bottom = Integer.MIN_VALUE;
		for (int x = -1; x <= HINT_DISTANCE_TILES; x += 2)
		{
			for (int y = -1; y <= HINT_DISTANCE_TILES; y += 2)
			{
				for (int z = -1; z <= HINT_DISTANCE_TILES; z += 2)
				{
					Point point = Perspective.localToCanvas(client, location.getWorldView(),
						location.getX() + x * radius, location.getY() + y * radius, centerZ + z * halfHeight);
					if (point == null) return null;
					left = Math.min(left, point.getX());
					right = Math.max(right, point.getX());
					top = Math.min(top, point.getY());
					bottom = Math.max(bottom, point.getY());
				}
			}
		}
		return new Bounds(left, top, right, bottom);
	}

	static class Bounds
	{
		final int left, top, right, bottom;

		Bounds(int left, int top, int right, int bottom)
		{
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}
	}
}
