package com.escapecrystalnotify;

import java.awt.Color;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.ItemID;
import static com.escapecrystalnotify.EscapeCrystalNotifyCrystalDefaults.*;

class EscapeCrystalNotifyCrystal3d
{
	private final Client client;
	private final EscapeCrystalNotifyConfig config;
	private RuneLiteObject crystal;
	private Model model;
	private EscapeCrystalNotifyCrystalFill crystalFill;
	private int modelSize;
	private short modelColor;
	private long lastFrame;
	private long nextModelAttempt;
	private double rotation;
	private double bobPhase;
	private boolean carried, active, notifyLocation;
	private double inactivityFraction = 1, hitpointsFraction = 1, prayerFraction = 1;
	private Color convertedFrom;
	private short convertedColor;
	private final EscapeCrystalNotifyCrystalClearance clearance = new EscapeCrystalNotifyCrystalClearance();

	@Inject
	EscapeCrystalNotifyCrystal3d(Client client, EscapeCrystalNotifyConfig config)
	{
		this.client = client;
		this.config = config;
	}

	void updateState(boolean carried, boolean active, boolean notifyLocation, int remainingTicks, int totalTicks)
	{
		this.carried = carried;
		this.active = active;
		this.notifyLocation = notifyLocation;
		inactivityFraction = EscapeCrystalNotifyCrystalFill.fraction(carried, active, remainingTicks, totalTicks);
		hitpointsFraction = resourceFraction(Skill.HITPOINTS);
		prayerFraction = resourceFraction(Skill.PRAYER);
	}

	private double resourceFraction(Skill skill)
	{
		return EscapeCrystalNotifyCrystalFill.fraction(client.getBoostedSkillLevel(skill), client.getRealSkillLevel(skill));
	}

	private double fillFraction(EscapeCrystalNotifyConfig.Crystal3dFillMode mode)
	{
		switch (mode)
		{
			case INACTIVITY_TIME: return inactivityFraction;
			case HITPOINTS: return hitpointsFraction;
			case PRAYER_POINTS: return prayerFraction;
			default: return 1;
		}
	}

	void update()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			reset();
			return;
		}
		if (!config.enableCrystal3d())
		{
			reset();
			return;
		}
		if (!config.crystal3dDisplayEverywhere() && !notifyLocation)
		{
			hide();
			return;
		}
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			reset();
			return;
		}

		LocalPoint location = player.getLocalLocation();
		WorldView worldView = player.getWorldView();
		if (!EscapeCrystalNotifySceneBounds.contains(location, worldView))
		{
			reset();
			return;
		}

		boolean besideHead = config.crystal3dDisplayStyle() == EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		int requestedSize = config.crystal3dSize();
		int bobHeight = config.crystal3dBobHeight();
		if (besideHead)
		{
			requestedSize = (int) Math.round(requestedSize * SMALL_STYLE_SCALE);
			bobHeight = (int) Math.round(bobHeight * SMALL_STYLE_SCALE);
		}
		Color color = carried && active ? config.crystal3dActiveColor() : config.crystal3dInactiveColor();
		if (!color.equals(convertedFrom))
		{
			convertedColor = JagexColor.rgbToHSL(color.getRGB(), COLOR_BRIGHTNESS);
			convertedFrom = color;
		}
		short requestedColor = convertedColor;
		long now = System.nanoTime();
		if (model == null || modelSize != requestedSize || modelColor != requestedColor)
		{
			// Cache assets can be temporarily unavailable; avoid trying to load them every frame.
			if (now >= nextModelAttempt)
			{
				Model replacement = buildModel(requestedSize, requestedColor);
				if (replacement == null)
				{
					nextModelAttempt = now + MODEL_RETRY_NANOS;
				}
				else
				{
					model = replacement;
					crystalFill = null;
					modelSize = requestedSize;
					modelColor = requestedColor;
					if (crystal != null) crystal.setModel(model);
					nextModelAttempt = 0;
				}
			}
			if (model == null)
			{
				return;
			}
		}

		EscapeCrystalNotifyConfig.Crystal3dFillMode fillMode = config.crystal3dFillMode();
		if (fillMode != EscapeCrystalNotifyConfig.Crystal3dFillMode.DISABLED)
		{
			if (crystalFill == null) crystalFill = new EscapeCrystalNotifyCrystalFill(model);
			crystalFill.apply(fillFraction(fillMode));
		}
		else if (crystalFill != null)
		{
			crystalFill.apply(1);
		}

		if (crystal == null)
		{
			crystal = client.createRuneLiteObject();
			crystal.setModel(model);
		}
		// setLocation also sets terrain height, and re-registers when moving between world views.
		crystal.setLocation(location, worldView.getPlane());
		double elapsed = lastFrame == 0 ? 0 : Math.min((now - lastFrame) / 1_000_000_000.0, MAX_FRAME_SECONDS);
		lastFrame = now;
		int period = config.crystal3dSpinSeconds();
		if (period > 0)
		{
			rotation = (rotation + elapsed * FULL_TURN / period) % FULL_TURN;
		}
		int bob = 0;
		if (bobHeight > 0)
		{
			bobPhase = (bobPhase + elapsed * Math.PI * 2 / BOB_PERIOD_SECONDS) % (Math.PI * 2);
			bob = (int) Math.round(Math.sin(bobPhase) * bobHeight);
		}
		int lift;
		if (besideHead)
		{
			clearance.clear();
			// Use the rendered facing direction and the player's ground height, not the camera or offset tile.
			LocalPoint side = EscapeCrystalNotifyCrystalSidePosition.position(location, player.getCurrentOrientation(),
				config.crystal3dSideOffset(), config.crystal3dForwardOffset());
			if (!EscapeCrystalNotifySceneBounds.contains(side, worldView))
			{
				if (crystal.isActive()) crystal.setActive(false);
				return;
			}
			crystal.setX(side.getX());
			crystal.setY(side.getY());
			lift = player.getLogicalHeight() + config.crystal3dSideHeight() + bob;
		}
		else if (config.crystal3dAutomaticClearance())
		{
			// Use the installed size while a replacement model is waiting for cache assets.
			int radius = (int) Math.ceil(modelSize * MODEL_WIDTH_RATIO / 2 * Math.sqrt(2));
			int halfHeight = (int) Math.ceil(modelSize / 2.0) + bobHeight;
			int safeLift = clearance.lift(client, player, location, crystal.getZ(), radius, halfHeight,
				config.crystal3dClearancePadding(), now, elapsed);
			if (safeLift < 0)
			{
				// Give game overheads priority when the camera leaves no safe room for the crystal.
				if (crystal.isActive()) crystal.setActive(false);
				return;
			}
			lift = safeLift + bob;
		}
		else
		{
			clearance.clear();
			// Negative Z is upwards; the centered model needs half its height above the manual gap.
			lift = player.getLogicalHeight() + config.crystal3dHeight() + modelSize / 2 + bob;
		}
		crystal.setZ(crystal.getZ() - lift);
		crystal.setOrientation((int) rotation);
		if (!crystal.isActive())
		{
			crystal.setActive(true);
		}
	}

	private void hide()
	{
		if (crystal != null && crystal.isActive()) crystal.setActive(false);
		lastFrame = 0;
		clearance.clear();
	}

	/** Release scene resources; display metrics are refreshed by the next game tick. */
	void reset()
	{
		hide();
		crystal = null;
		model = null;
		crystalFill = null;
		nextModelAttempt = 0;
		rotation = 0;
		bobPhase = 0;
	}

	private Model buildModel(int size, short color)
	{
		int modelId = client.getItemDefinition(ItemID.TOB_TELEPORT).getInventoryModel();
		ModelData source = client.loadModelData(modelId);
		if (source == null)
		{
			return null;
		}
		// All edited arrays must be private: loadModelData shares its arrays with the game cache.
		ModelData data = source.shallowCopy().cloneVertices().cloneColors();
		normalizeAxis(data.getVerticesX(), data.getVerticesCount(), size * MODEL_WIDTH_RATIO);
		normalizeAxis(data.getVerticesY(), data.getVerticesCount(), size);
		normalizeAxis(data.getVerticesZ(), data.getVerticesCount(), size * MODEL_WIDTH_RATIO);
		Arrays.fill(data.getFaceColors(), color);
		if (data.getFaceTextures() != null)
		{
			// Untextured models have no texture array; RuneLite's cloneTextures requires one.
			data.cloneTextures();
			Arrays.fill(data.getFaceTextures(), (short) -1);
		}
		if (data.getFaceTransparencies() != null)
		{
			data.cloneTransparencies();
			Arrays.fill(data.getFaceTransparencies(), (byte) 0);
		}
		return data.light();
	}

	static void normalizeAxis(float[] vertices, int count, float extent)
	{
		if (count == 0)
		{
			return;
		}
		float min = vertices[0];
		float max = min;
		for (int i = 1; i < count; i++)
		{
			min = Math.min(min, vertices[i]);
			max = Math.max(max, vertices[i]);
		}
		float center = (min + max) / 2;
		float scale = max == min ? 0 : extent / (max - min);
		for (int i = 0; i < count; i++)
		{
			vertices[i] = (vertices[i] - center) * scale;
		}
	}
}
