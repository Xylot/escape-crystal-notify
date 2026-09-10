package com.escapecrystalnotify;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class EscapeCrystalNotifyTeleportNpcOverlay extends Overlay
{
	private final Client client;
	private final EscapeCrystalNotifyTeleportNpc npc;
	private final EscapeCrystalNotifyConfig config;

	@Inject
	EscapeCrystalNotifyTeleportNpcOverlay(Client client, EscapeCrystalNotifyTeleportNpc npc,
		EscapeCrystalNotifyConfig config)
	{
		this.client = client;
		this.npc = npc;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		LocalPoint location = npc.getLocation();
		if (location == null) return null;
		String text = config.teleportNpcText();
		if (text == null || text.isEmpty()) return null;
		Point point = Perspective.localToCanvas(client, location, npc.getPlane(), npc.getOverheadHeight());
		if (point != null)
		{
			graphics.setFont(FontManager.getRunescapeBoldFont());
			int width = graphics.getFontMetrics().stringWidth(text);
			OverlayUtil.renderTextLocation(graphics, new Point(point.getX() - width / 2, point.getY()),
				text, Color.YELLOW);
		}
		return null;
	}
}
