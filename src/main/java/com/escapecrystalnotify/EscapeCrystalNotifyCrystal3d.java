package com.escapecrystalnotify;

import com.google.common.primitives.Ints;
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
	private volatile boolean settingsDirty = true;
	private boolean besideHead;
	private int requestedSize, bobHeight, radius, halfHeight;
	private double rotationPerSecond;
	private boolean carried, active, notifyLocation;
	private double fillFraction = 1;
	private double inactivityFraction = 1, hitpointsFraction = 1, prayerFraction = 1;
	private EscapeCrystalNotifyConfig.Crystal3dFillMode fillMode = DEFAULT_FILL_MODE;
	private short requestedColor, activeColor, inactiveColor;
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
		requestedColor = carried && active ? activeColor : inactiveColor;
		inactivityFraction = EscapeCrystalNotifyCrystalFill.fraction(carried, active, remainingTicks, totalTicks);
		hitpointsFraction = resourceFraction(Skill.HITPOINTS);
		prayerFraction = resourceFraction(Skill.PRAYER);
		refreshFillFraction();
	}

	private double resourceFraction(Skill skill)
	{
		return EscapeCrystalNotifyCrystalFill.fraction(client.getBoostedSkillLevel(skill), client.getRealSkillLevel(skill));
	}

	private void refreshFillFraction()
	{
		switch (fillMode)
		{
			case INACTIVITY_TIME: fillFraction = inactivityFraction; break;
			case HITPOINTS: fillFraction = hitpointsFraction; break;
			case PRAYER_POINTS: fillFraction = prayerFraction; break;
			default: fillFraction = 1;
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
			clear();
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
			clear();
			return;
		}

		LocalPoint location = player.getLocalLocation();
		WorldView worldView = player.getWorldView();
		if (!inScene(location, worldView))
		{
			clear();
			return;
		}

		if (settingsDirty) refreshSettings();
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
					refreshBounds();
					if (crystal != null) crystal.setModel(model);
					nextModelAttempt = 0;
				}
			}
			if (model == null)
			{
				return;
			}
		}

		if (fillMode != EscapeCrystalNotifyConfig.Crystal3dFillMode.DISABLED)
		{
			if (crystalFill == null) crystalFill = new EscapeCrystalNotifyCrystalFill(model);
			crystalFill.apply(fillFraction);
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
		if (rotationPerSecond > 0)
		{
			rotation = (rotation + elapsed * rotationPerSecond) % FULL_TURN;
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
				Ints.constrainToRange(config.crystal3dSideOffset(), MIN_SIDE_OFFSET, MAX_SIDE_OFFSET), Ints.constrainToRange(config.crystal3dForwardOffset(), MIN_SIDE_OFFSET, MAX_SIDE_OFFSET));
			if (!inScene(side, worldView))
			{
				if (crystal.isActive()) crystal.setActive(false);
				return;
			}
			crystal.setX(side.getX());
			crystal.setY(side.getY());
			lift = player.getLogicalHeight() + Ints.constrainToRange(config.crystal3dSideHeight(), MIN_SIDE_HEIGHT, MAX_SIDE_HEIGHT) + bob;
		}
		else if (config.crystal3dAutomaticClearance())
		{
			int safeLift = clearance.lift(client, player, location, crystal.getZ(), radius, halfHeight,
				Ints.constrainToRange(config.crystal3dClearancePadding(), MIN_PADDING, MAX_PADDING), now, elapsed);
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
			lift = player.getLogicalHeight() + Ints.constrainToRange(config.crystal3dHeight(), MIN_HEIGHT, MAX_HEIGHT) + modelSize / 2 + bob;
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

	void clear()
	{
		hide();
		crystal = null;
		model = null;
		crystalFill = null;
		nextModelAttempt = 0;
		rotation = 0;
		bobPhase = 0;
		invalidateSettings();
	}

	/** Scene/session changes discard tick state too; ordinary resource release preserves it. */
	void reset()
	{
		carried = active = notifyLocation = false;
		fillFraction = inactivityFraction = hitpointsFraction = prayerFraction = 1;
		clear();
	}

	void invalidateSettings()
	{
		settingsDirty = true;
	}

	private void refreshSettings()
	{
		// Clear first so a concurrent config/profile event cannot lose its invalidation.
		settingsDirty = false;
		activeColor = JagexColor.rgbToHSL(config.crystal3dActiveColor().getRGB(), COLOR_BRIGHTNESS);
		inactiveColor = JagexColor.rgbToHSL(config.crystal3dInactiveColor().getRGB(), COLOR_BRIGHTNESS);
		requestedColor = carried && active ? activeColor : inactiveColor;
		fillMode = config.crystal3dFillMode();
		refreshFillFraction();
		besideHead = config.crystal3dDisplayStyle() == EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		requestedSize = Ints.constrainToRange(config.crystal3dSize(), MIN_SIZE, MAX_SIZE);
		bobHeight = Ints.constrainToRange(config.crystal3dBobHeight(), MIN_BOB_HEIGHT, MAX_BOB_HEIGHT);
		if (besideHead)
		{
			requestedSize = Math.max(MIN_SMALL_SIZE, (int) Math.round(requestedSize * SMALL_STYLE_SCALE));
			bobHeight = (int) Math.round(bobHeight * SMALL_STYLE_SCALE);
		}
		int period = Ints.constrainToRange(config.crystal3dSpinSeconds(), MIN_SPIN_SECONDS, MAX_SPIN_SECONDS);
		rotationPerSecond = period == 0 ? 0 : (double) FULL_TURN / period;
		refreshBounds();
	}

	private void refreshBounds()
	{
		// Use the installed size even when a replacement model is still waiting for cache assets.
		radius = (int) Math.ceil(modelSize * MODEL_WIDTH_RATIO / 2 * Math.sqrt(2));
		halfHeight = (int) Math.ceil(modelSize / 2.0) + bobHeight;
	}

	private static boolean inScene(LocalPoint point, WorldView view)
	{
		return point != null && view != null && point.getSceneX() >= 0 && point.getSceneY() >= 0
			&& point.getSceneX() < view.getSizeX() && point.getSceneY() < view.getSizeY();
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
