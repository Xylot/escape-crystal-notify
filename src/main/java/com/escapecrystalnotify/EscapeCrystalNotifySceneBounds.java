package com.escapecrystalnotify;

import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

final class EscapeCrystalNotifySceneBounds
{
	private EscapeCrystalNotifySceneBounds() {}

	static boolean contains(LocalPoint point, WorldView view)
	{
		return point != null && view != null && point.getSceneX() >= 0 && point.getSceneY() >= 0
			&& point.getSceneX() < view.getSizeX() && point.getSceneY() < view.getSizeY();
	}
}
