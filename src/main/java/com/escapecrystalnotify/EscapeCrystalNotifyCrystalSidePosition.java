package com.escapecrystalnotify;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.Perspective;
import static com.escapecrystalnotify.EscapeCrystalNotifyCrystalDefaults.FULL_TURN;

/** Character-relative offsets in scene units; camera rotation and zoom never move this anchor. */
class EscapeCrystalNotifyCrystalSidePosition
{
	static LocalPoint position(LocalPoint origin, int orientation, int side, int forward)
	{
		// Actor angles: 0 = south, 512 = west, 1024 = north, 1536 = east.
		int angle = orientation & (FULL_TURN - 1);
		double sin = Perspective.SINEF[angle], cos = Perspective.COSINEF[angle];
		int dx = (int) Math.round(-side * cos - forward * sin);
		int dy = (int) Math.round(side * sin - forward * cos);
		return new LocalPoint(origin.getX() + dx, origin.getY() + dy, origin.getWorldView());
	}
}
