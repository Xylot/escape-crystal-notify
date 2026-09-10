package com.escapecrystalnotify;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class EscapeCrystalNotifyPlayerOutlineOverlay extends Overlay
{
	private final EscapeCrystalNotifyGroundCircle groundCircle = new EscapeCrystalNotifyGroundCircle();
	private final Client client;
	private final EscapeCrystalNotifyPlugin plugin;
	private final EscapeCrystalNotifyConfig config;
	private final ModelOutlineRenderer outlines;

	@Inject
	EscapeCrystalNotifyPlayerOutlineOverlay(Client client, EscapeCrystalNotifyPlugin plugin,
		EscapeCrystalNotifyConfig config, ModelOutlineRenderer outlines)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.outlines = outlines;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	Color outlineColor()
	{
		if (!config.enablePlayerOutline() || !plugin.isAccountTypeEnabled()
			|| (!config.alwaysDisplayPlayerOutline() && !plugin.isAtNotifyRegionId())) return null;
		return plugin.isEscapeCrystalInactivityTeleportActive()
			? config.playerOutlineActiveColor() : config.playerOutlineInactiveColor();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Color color = outlineColor();
		if (color == null || color.getAlpha() == 0) return null;
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			if (config.playerOutlineStyle() == EscapeCrystalNotifyConfig.PlayerOutlineStyle.GROUND_CIRCLE)
			{
				drawGroundCircle(graphics, player, color);
			}
			else
			{
				outlines.drawOutline(player, config.playerOutlineWidth(), color, config.playerOutlineFeather());
			}
		}
		return null;
	}

	private void drawGroundCircle(Graphics2D graphics, Player player, Color color)
	{
		LocalPoint location = player.getLocalLocation();
		if (location == null || player.getWorldView() == null) return;
		groundCircle.draw(graphics, client, location, player.getWorldView().getPlane(), color,
			config.playerOutlineWidth(), config.groundCircleGlow(), config.groundCircleImage(), player);
	}
}
