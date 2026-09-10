package com.escapecrystalnotify;

import java.lang.reflect.Proxy;
import java.awt.Color;
import net.runelite.api.Skill;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HeadIcon;
import net.runelite.api.ItemComposition;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Player;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.*;

public class EscapeCrystalNotifyCrystal3dTest
{
	@Test
	public void centersAndSizesMeshWithoutTouchingUnusedVertices()
	{
		float[] vertices = {10, 30, 50, 999};
		EscapeCrystalNotifyCrystal3d.normalizeAxis(vertices, 3, 100);
		assertArrayEquals(new float[]{-50, 0, 50, 999}, vertices, 0.001f);
		float[] flat = {5, 5};
		EscapeCrystalNotifyCrystal3d.normalizeAxis(flat, 2, 100);
		assertArrayEquals(new float[]{0, 0}, flat, 0.001f);
	}

	@Test
	public void followsPlayerAndReusesModelUntilAppearanceChanges()
	{
		Fixture f = new Fixture();
		f.update(true, true);
		RuneLiteObject object = f.object;
		assertTrue(object.isActive());
		assertEquals(f.location.getX(), object.getX());
		assertEquals(-310, object.getZ()); // ground 0 - head 200 - gap 60 - half-model 50
		assertEquals(1, f.loads);

		f.location = new LocalPoint(6500, 6600);
		f.plane = 1;
		f.update(true, true);
		assertSame(object, f.object);
		assertEquals(6500, object.getX());
		assertEquals(6600, object.getY());
		assertEquals(1, object.getLevel());
		assertEquals(1, f.loads);

		f.config.size = 200;
		f.update(true, true);
		assertEquals(2, f.loads);
		assertEquals(-360, object.getZ());
	}

	@Test
	public void usesConfiguredActiveAndInactiveColorsIncludingMissingCrystal()
	{
		Fixture f = new Fixture();
		f.update(true, true);
		short active = f.colors[0];
		f.update(true, false);
		short inactive = f.colors[0];
		assertNotEquals(active, inactive);
		f.update(false, false);
		assertEquals(inactive, f.colors[0]);
		assertEquals(2, f.loads);
		f.config.inactiveColor = Color.BLUE;
		f.update(false, false);
		assertNotEquals(inactive, f.colors[0]);
		f.update(true, true);
		assertEquals(active, f.colors[0]);
	}
	@Test
	public void disablingLogoutAndShutdownRemoveObjectAndAllowReenable()
	{
		Fixture f = new Fixture();
		f.config.enabled = false;
		f.update(true, true);
		assertEquals(0, f.loads);
		f.config.enabled = true;
		f.update(true, true);
		RuneLiteObject first = f.object;
		f.config.enabled = false;
		f.update(true, true);
		assertFalse(first.isActive());
		f.config.enabled = true;
		f.update(true, true);
		assertNotSame(first, f.object);
		assertEquals(1, f.registered.size());
		f.gameState = GameState.LOADING;
		f.update(true, true);
		assertTrue(f.registered.isEmpty());
		f.gameState = GameState.LOGGED_IN;
		f.update(true, true);
		f.renderer.clear();
		f.renderer.clear();
		assertTrue(f.registered.isEmpty());
	}

	@Test
	public void unavailableModelDoesNotSpawnOrRetryEveryFrame()
	{
		Fixture f = new Fixture();
		f.modelAvailable = false;
		f.update(true, true);
		f.update(true, true);
		assertEquals(1, f.loads);
		assertTrue(f.registered.isEmpty());
		f.renderer.clear();
		f.modelAvailable = true;
		f.update(true, true);
		assertTrue(f.object.isActive());
	}

	@Test
	public void pendingAppearanceReloadKeepsExistingCrystalFollowingPlayer()
	{
		Fixture f = new Fixture();
		f.update(true, true);
		f.config.size = 200;
		f.modelAvailable = false;
		f.location = new LocalPoint(6500, 6600);
		f.update(true, true);
		assertEquals(6500, f.object.getX());
		assertEquals(-310, f.object.getZ()); // Retain the old model's height until replacement is ready.
		f.location = new LocalPoint(6600, 6700);
		f.update(true, true);
		assertEquals(6600, f.object.getX());
		assertEquals(2, f.loads);
	}

	@Test
	public void untexturedOpaqueModelRendersWithoutOptionalArrays()
	{
		Fixture f = new Fixture();
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertNull(f.litTextures);
		assertNull(f.litTransparencies);
	}

	@Test
	public void optionalArraysAreClonedBeforeRemovingTexturesAndTransparency()
	{
		Fixture f = new Fixture();
		f.textures = new short[]{12, -1};
		f.transparencies = new byte[]{32, 64};
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertArrayEquals(new short[]{12, -1}, f.textures);
		assertArrayEquals(new byte[]{32, 64}, f.transparencies);
		assertArrayEquals(new short[]{-1, -1}, f.litTextures);
		assertArrayEquals(new byte[]{0, 0}, f.litTransparencies);
	}

	@Test
	public void automaticClearanceRaisesCrystalAndHandlesStackedOverheads()
	{
		Fixture f = new Fixture();
		f.config.automatic = true;
		f.update(true, true);
		assertTrue(f.object.isActive());
		int initialZ = f.object.getZ();
		assertTrue("Idle automatic placement should be closer than the old manual default", initialZ > -310);
		assertTrue(initialZ < -250);
		f.prayer = HeadIcon.MELEE;
		f.skull = 0;
		f.health = 20;
		f.hintPlayer = f.player;
		f.chat = "Test overhead chat";
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertTrue(f.object.getZ() < initialZ);
		f.config.automatic = false;
		f.update(true, true);
		assertEquals(-310, f.object.getZ());
	}

	@Test
	public void automaticClearanceHidesInSmallViewportAndReappearsWhenSpaceReturns()
	{
		Fixture f = new Fixture();
		f.config.automatic = true;
		f.update(true, true);
		RuneLiteObject original = f.object;
		f.viewportHeight = 100;
		f.update(true, true);
		assertFalse(original.isActive());
		f.viewportHeight = 1000;
		f.update(true, true);
		assertSame(original, f.object);
		assertTrue(original.isActive());
		assertEquals(1, f.loads);
	}

	@Test
	public void coordinateHintNearPlayerAlsoGetsClearance()
	{
		Fixture f = new Fixture();
		f.config.automatic = true;
		f.update(true, true);
		int initialZ = f.object.getZ();
		f.hintPoint = new WorldPoint(3201, 3200, 0);
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertTrue(f.object.getZ() < initialZ);
	}

	@Test
	public void clearsActorModelExtendingAboveTheNormalOverheadBand()
	{
		Fixture f = new Fixture();
		f.config.automatic = true;
		f.update(true, true);
		int initialZ = f.object.getZ();
		f.actorHull = new java.awt.Rectangle(470, 250, 100, 350);
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertTrue(f.object.getZ() < initialZ);
	}

	@Test
	public void manualHeightDoesNotAddAnotherGapToAutomaticPlacement()
	{
		Fixture f = new Fixture();
		f.config.automatic = true;
		f.update(true, true);
		int initialZ = f.object.getZ();
		f.config.manualHeight = 300;
		f.update(true, true);
		assertEquals(initialZ, f.object.getZ());
		f.config.automatic = false;
		f.update(true, true);
		assertEquals(-550, f.object.getZ());
	}

	@Test
	public void sideStyleIsSmallerAtHeadHeightAndSwitchesBackLive()
	{
		Fixture f = new Fixture();
		f.update(true, true);
		assertEquals(100, f.litSize);
		RuneLiteObject original = f.object;
		f.config.style = EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertSame(original, f.object);
		assertEquals(40, f.litSize);
		assertEquals(-220, f.object.getZ());
		assertTrue(f.object.getX() > f.location.getX());
		f.config.style = EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.ABOVE_HEAD;
		f.update(true, true);
		assertEquals(100, f.litSize);
		assertEquals(f.location.getX(), f.object.getX());
		assertEquals(f.location.getY(), f.object.getY());
		assertEquals(-310, f.object.getZ());
	}

	@Test
	public void sideStyleAnchorStaysFixedAsCameraTurns()
	{
		Fixture f = new Fixture();
		f.config.style = EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		for (int yaw : new int[]{0, 2048, 4096, 8192, 12288})
		{
			f.cameraYaw = yaw;
			f.update(true, true);
			assertTrue("yaw " + yaw, f.object.isActive());
			assertEquals(6456, f.object.getX());
			assertEquals(6400, f.object.getY());
			assertEquals(-220, f.object.getZ());
		}
		assertEquals(1, f.loads);
	}

	@Test
	public void sideOffsetsAndHeightUpdateLiveWithoutRebuildingTheModel()
	{
		Fixture f = new Fixture();
		f.config.style = EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		f.update(true, true);
		f.config.sideOffset = -40;
		f.config.forwardOffset = 24;
		f.config.sideHeight = 80;
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertEquals(6360, f.object.getX());
		assertEquals(6424, f.object.getY());
		assertEquals(-280, f.object.getZ());
		assertEquals(1, f.loads);
	}

	@Test
	public void sideAnchorTurnsWithTheCharactersRenderedFacingDirection()
	{
		Fixture f = new Fixture();
		f.config.style = EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		int[][] cases = {{0, 6344, 6400}, {512, 6400, 6456}, {1024, 6456, 6400}, {1536, 6400, 6344}};
		for (int[] direction : cases)
		{
			f.orientation = direction[0];
			f.update(true, true);
			assertEquals(direction[1], f.object.getX());
			assertEquals(direction[2], f.object.getY());
		}
		f.location = new LocalPoint(6500, 6600);
		f.update(true, true);
		assertEquals(6500, f.object.getX());
		assertEquals(6544, f.object.getY());
		assertEquals(1, f.loads);
	}

	@Test
	public void sideOffsetPreservesWorldViewAndSupportsForwardAndBackward()
	{
		LocalPoint origin = new LocalPoint(6400, 6400, 42);
		LocalPoint rightAndForward = EscapeCrystalNotifyCrystalSidePosition.position(origin, 1536, 40, 24);
		assertEquals(new LocalPoint(6424, 6360, 42), rightAndForward);
		LocalPoint leftAndBack = EscapeCrystalNotifyCrystalSidePosition.position(origin, 1536, -40, -24);
		assertEquals(new LocalPoint(6376, 6440, 42), leftAndBack);
	}

	@Test
	public void sideAnchorOutsideSceneHidesAndReturnsWhenPlayerMovesBack()
	{
		Fixture f = new Fixture();
		f.config.style = EscapeCrystalNotifyConfig.Crystal3dDisplayStyle.SMALL_NEXT_TO_HEAD;
		f.location = new LocalPoint(10, 10);
		f.orientation = 0;
		f.update(true, true);
		assertFalse(f.object.isActive());
		f.location = new LocalPoint(6400, 6400);
		f.update(true, true);
		assertTrue(f.object.isActive());
	}

	@Test
	public void displayEverywhereOverridesNotifyRegionsIndependentlyOfInventory()
	{
		Fixture f = new Fixture();
		f.config.everywhere = false;
		f.update(true, true);
		assertEquals(0, f.loads);
		f.notifyLocation = true;
		f.update(true, true);
		RuneLiteObject original = f.object;
		assertTrue(original.isActive());
		f.notifyLocation = false;
		f.update(true, true);
		assertFalse(original.isActive());
		f.config.everywhere = true;
		f.config.inventoryEverywhere = false;
		f.update(true, true);
		assertTrue(f.object.isActive());
		assertSame(original, f.object);
		assertEquals(1, f.loads);
		f.config.everywhere = false;
		f.update(true, true);
		assertFalse(f.object.isActive());
	}

	@Test
	public void countdownUpdatesAndRefillsWithoutReloadingTheMesh()
	{
		Fixture f = new Fixture();
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.INACTIVITY_TIME;
		f.totalTicks = 100;
		f.remainingTicks = 100;
		f.update(true, true);
		int original = f.lit1[0];
		f.remainingTicks = 50;
		f.update(true, true);
		assertTrue((f.lit1[0] & 127) < (original & 127));
		f.remainingTicks = 100;
		f.update(true, true);
		assertEquals(original, f.lit1[0]);
		f.remainingTicks = 0;
		f.update(true, true);
		assertTrue((f.lit1[0] & 127) < (original & 127));
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.DISABLED;
		f.update(true, true);
		assertEquals(original, f.lit1[0]);
		assertEquals(1, f.loads);
	}

	@Test
	public void derivedSettingsStayCachedAcrossFramesAndRefreshOnInvalidation()
	{
		Fixture f = new Fixture();
		f.update(true, true);
		int reads = f.config.sizeReads;
		for (int i = 0; i < 100; i++) f.renderer.update();
		assertEquals(reads, f.config.sizeReads);
		assertEquals(1, f.loads);
		f.config.size = 150;
		f.renderer.invalidateSettings();
		f.renderer.update();
		assertEquals(reads + 1, f.config.sizeReads);
		assertEquals(150, f.litSize);
		assertEquals(2, f.loads);
	}

	@Test
	public void temporaryHidingKeepsMeshAndAppliesLatestTimerWhenReturning()
	{
		Fixture f = new Fixture();
		f.config.everywhere = false;
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.INACTIVITY_TIME;
		f.notifyLocation = true;
		f.totalTicks = f.remainingTicks = 100;
		f.update(true, true);
		RuneLiteObject original = f.object;
		int fullColor = f.lit1[0];
		f.notifyLocation = false;
		f.renderer.updateState(true, true, false, 50, 100);
		for (int i = 0; i < 100; i++) f.renderer.update();
		assertFalse(original.isActive());
		assertEquals(fullColor, f.lit1[0]);
		f.notifyLocation = true;
		f.remainingTicks = 50;
		f.update(true, true);
		assertSame(original, f.object);
		assertTrue(original.isActive());
		assertTrue((f.lit1[0] & 127) < (fullColor & 127));
		assertEquals(1, f.loads);
		f.renderer.clear();
		f.update(true, true);
		assertNotSame(original, f.object);
		assertEquals(2, f.loads);
	}

	@Test
	public void lookupTableOffsetsMatchTrigonometryWithinOneSceneUnit()
	{
		LocalPoint origin = new LocalPoint(6400, 6400, 42);
		for (int orientation = 0; orientation < 2048; orientation++)
		{
			double angle = orientation * Math.PI / 1024;
			int x = 6400 + (int) Math.round(-128 * Math.cos(angle) + 93 * Math.sin(angle));
			int y = 6400 + (int) Math.round(128 * Math.sin(angle) + 93 * Math.cos(angle));
			LocalPoint actual = EscapeCrystalNotifyCrystalSidePosition.position(origin, orientation, 128, -93);
			assertTrue(Math.abs(x - actual.getX()) <= 1);
			assertTrue(Math.abs(y - actual.getY()) <= 1);
		}
	}

	@Test
	public void framesReuseTickColorAndFillWhilePositionRemainsLive()
	{
		Fixture f = new Fixture();
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.INACTIVITY_TIME;
		f.remainingTicks = f.totalTicks = 100;
		f.update(true, true);
		int reads = f.config.colorReads;
		int full = f.lit1[0];
		f.renderer.updateState(true, true, true, 50, 100);
		f.location = new LocalPoint(6500, 6600);
		for (int i = 0; i < 100; i++) f.renderer.update();
		assertEquals(reads, f.config.colorReads);
		assertEquals(1, f.loads);
		assertEquals(6500, f.object.getX());
		assertEquals(6600, f.object.getY());
		assertTrue((f.lit1[0] & 127) < (full & 127));
		f.renderer.updateState(true, false, true, 0, 100);
		f.renderer.update();
		assertEquals(reads, f.config.colorReads);
		assertEquals(2, f.loads);
		assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]); // Inactive restores full shading.
	}

	@Test
	public void configAndModelChangesUseLatestTickWithoutWaitingForAnotherTick()
	{
		Fixture f = new Fixture();
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.INACTIVITY_TIME;
		f.remainingTicks = 50;
		f.totalTicks = 100;
		f.update(true, true);
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.DISABLED;
		f.renderer.invalidateSettings();
		f.renderer.update();
		assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]);
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.INACTIVITY_TIME;
		f.config.activeColor = Color.RED;
		f.config.size = 150;
		f.renderer.invalidateSettings();
		f.renderer.update();
		short red = f.colors[0];
		assertEquals(150, f.litSize);
		assertEquals(2, f.loads);
		assertTrue((f.lit1[0] & 127) < (red & 127));
		f.config.activeColor = EscapeCrystalNotifyCrystalDefaults.DEFAULT_ACTIVE_COLOR;
		f.renderer.invalidateSettings();
		f.renderer.update();
		assertNotEquals(red, f.colors[0]);
		assertTrue((f.lit1[0] & 127) < (f.colors[0] & 127));
	}

	@Test
	public void disablingPreservesTickStateButSceneChangesDiscardIt()
	{
		Fixture f = new Fixture();
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.INACTIVITY_TIME;
		f.config.everywhere = false;
		f.notifyLocation = true;
		f.remainingTicks = 25;
		f.totalTicks = 100;
		f.update(true, true);
		int partial = f.lit1[0];
		f.config.enabled = false;
		f.renderer.update();
		f.config.enabled = true;
		f.renderer.update();
		assertTrue(f.object.isActive());
		assertEquals(partial, f.lit1[0]);
		f.gameState = GameState.LOADING;
		f.renderer.update();
		f.gameState = GameState.LOGGED_IN;
		f.renderer.update();
		assertTrue(f.registered.isEmpty()); // No stale notify-region state before the next game tick.
		f.config.everywhere = true;
		f.renderer.update();
		assertTrue(f.object.isActive());
		assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]); // Preview has no stale countdown.
		assertNotEquals(partial & ~127, f.lit1[0] & ~127); // Missing crystals use the inactive color.
	}

	@Test
	public void clearanceReadsHeightOnceAndWorldLocationOnlyForCoordinateHints()
	{
		Fixture f = new Fixture();
		f.config.automatic = true;
		f.update(true, true);
		assertEquals(1, f.logicalHeightReads);
		assertEquals(0, f.worldLocationReads);
		f.hintPoint = new WorldPoint(3201, 3200, 0);
		f.renderer.update();
		assertEquals(2, f.logicalHeightReads);
		assertEquals(1, f.worldLocationReads);
		f.hintPlayer = f.player;
		f.renderer.update();
		assertEquals(3, f.logicalHeightReads);
		assertEquals(1, f.worldLocationReads);
	}

	@Test
	public void resourceModesUseTickSnapshotsAndSwitchWithoutReloading()
	{
		Fixture f = new Fixture();
		f.hitpoints = 25;
		f.maxHitpoints = 100;
		f.prayerPoints = 75;
		f.maxPrayerPoints = 100;
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.HITPOINTS;
		f.update(true, true);
		int hpColor = f.lit1[0];
		int reads = f.skillReads;
		f.hitpoints = 100; // Client values can change; rendering keeps the last tick snapshot.
		for (int i = 0; i < 100; i++) f.renderer.update();
		assertEquals(hpColor, f.lit1[0]);
		assertEquals(reads, f.skillReads);
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.PRAYER_POINTS;
		f.renderer.invalidateSettings();
		f.renderer.update();
		assertTrue((f.lit1[0] & 127) > (hpColor & 127));
		assertEquals(reads, f.skillReads);
		assertEquals(1, f.loads);
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.HITPOINTS;
		f.renderer.invalidateSettings();
		f.renderer.update();
		assertEquals(hpColor, f.lit1[0]);
		f.renderer.updateState(true, true, true, 0, 100);
		f.renderer.update();
		assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]);
		assertEquals(reads + 4, f.skillReads);
	}

	@Test
	public void resourceFillClampsBoostsAndDisabledRestoresBothStatusColors()
	{
		for (EscapeCrystalNotifyConfig.Crystal3dFillMode mode : new EscapeCrystalNotifyConfig.Crystal3dFillMode[]{
			EscapeCrystalNotifyConfig.Crystal3dFillMode.HITPOINTS, EscapeCrystalNotifyConfig.Crystal3dFillMode.PRAYER_POINTS})
		{
			Fixture f = new Fixture();
			f.config.fillMode = mode;
			f.hitpoints = f.prayerPoints = 120;
			f.maxHitpoints = f.maxPrayerPoints = 99;
			f.update(true, true);
			assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]);
			for (boolean active : new boolean[]{true, false})
			{
				f.config.fillMode = mode;
				f.hitpoints = f.prayerPoints = 0;
				f.update(true, active);
				assertTrue((f.lit1[0] & 127) < (f.colors[0] & 127));
				f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.DISABLED;
				f.renderer.invalidateSettings();
				f.renderer.update();
				assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]);
				Color expected = active ? f.config.activeColor : f.config.inactiveColor;
				assertEquals(net.runelite.api.JagexColor.rgbToHSL(expected.getRGB(),
					EscapeCrystalNotifyCrystalDefaults.COLOR_BRIGHTNESS), f.colors[0]);
			}
		}
	}

	@Test
	public void unavailableResourcesAndNewSessionsUseFullInactiveAppearance()
	{
		Fixture f = new Fixture();
		f.config.fillMode = EscapeCrystalNotifyConfig.Crystal3dFillMode.HITPOINTS;
		f.maxHitpoints = 0;
		f.update(false, false);
		assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]);
		f.maxHitpoints = 100;
		f.hitpoints = 0;
		f.update(false, false);
		assertTrue((f.lit1[0] & 127) < (f.colors[0] & 127));
		f.renderer.reset();
		f.renderer.update();
		assertEquals(Short.toUnsignedInt(f.colors[0]), f.lit1[0]);
	}

	@Test
	public void featureConfigFollowsInventoryAndExposesColorsAndAllFillModes() throws Exception
	{
		Class<EscapeCrystalNotifyConfig> type = EscapeCrystalNotifyConfig.class;
		net.runelite.client.config.ConfigSection crystal = type.getField("crystal3dSettings")
			.getAnnotation(net.runelite.client.config.ConfigSection.class);
		int inventoryPosition = type.getField("inventoryDisplaySettings")
			.getAnnotation(net.runelite.client.config.ConfigSection.class).position();
		assertEquals("3D Crystal", crystal.name());
		assertEquals(inventoryPosition + 1, crystal.position());
		assertEquals(Color.class, type.getMethod("crystal3dActiveColor").getReturnType());
		assertEquals(Color.class, type.getMethod("crystal3dInactiveColor").getReturnType());
		assertEquals(4, EscapeCrystalNotifyConfig.Crystal3dFillMode.values().length);
		java.util.Set<Integer> positions = new java.util.HashSet<>();
		for (java.lang.reflect.Field field : type.getFields())
		{
			net.runelite.client.config.ConfigSection section = field.getAnnotation(net.runelite.client.config.ConfigSection.class);
			if (section != null) assertTrue("Duplicate section position: " + section.name(), positions.add(section.position()));
		}
	}

	private static class Settings implements EscapeCrystalNotifyConfig
	{
		boolean enabled = true;
		boolean everywhere = true;
		boolean inventoryEverywhere = true;
		Crystal3dFillMode fillMode = Crystal3dFillMode.DISABLED;
		int size = 100;
		int sizeReads;
		int colorReads;
		boolean automatic;
		int manualHeight = 60;
		int sideOffset = 56;
		int forwardOffset;
		int sideHeight = 20;
		Crystal3dDisplayStyle style = Crystal3dDisplayStyle.ABOVE_HEAD;
		Color activeColor = EscapeCrystalNotifyCrystalDefaults.DEFAULT_ACTIVE_COLOR, inactiveColor = EscapeCrystalNotifyCrystalDefaults.DEFAULT_INACTIVE_COLOR;
		@Override public boolean enableCrystal3d() { return enabled; }
		@Override public boolean crystal3dDisplayEverywhere() { return everywhere; }
		@Override public boolean alwaysDisplayInventory() { return inventoryEverywhere; }
		@Override public Crystal3dFillMode crystal3dFillMode() { return fillMode; }
		@Override public Crystal3dDisplayStyle crystal3dDisplayStyle() { return style; }
		@Override public int crystal3dSize() { sizeReads++; return size; }
		@Override public int crystal3dBobHeight() { return 0; }
		@Override public int crystal3dHeight() { return manualHeight; }
		@Override public int crystal3dSideOffset() { return sideOffset; }
		@Override public int crystal3dForwardOffset() { return forwardOffset; }
		@Override public int crystal3dSideHeight() { return sideHeight; }
		@Override public boolean crystal3dAutomaticClearance() { return automatic; }
		@Override public Color crystal3dActiveColor() { colorReads++; return activeColor; }
		@Override public Color crystal3dInactiveColor() { colorReads++; return inactiveColor; }
	}

	/** Small API doubles keep lifecycle tests independent of a running game client. */
	private static class Fixture
	{
		final Settings config = new Settings();
		final Set<RuneLiteObject> registered = new HashSet<>();
		final byte[][][] tiles = new byte[4][104][104];
		final int[][][] heights = new int[4][105][105];
		final short[] colors = new short[8];
		short[] textures;
		byte[] transparencies;
		short[] litTextures;
		byte[] litTransparencies;
		LocalPoint location = new LocalPoint(6400, 6400);
		GameState gameState = GameState.LOGGED_IN;
		int plane;
		int loads;
		int litSize;
		int cameraYaw;
		int orientation = 1024;
		int logicalHeightReads, worldLocationReads;
		boolean notifyLocation;
		int remainingTicks;
		int hitpoints = 99, maxHitpoints = 99, prayerPoints = 99, maxPrayerPoints = 99, skillReads;
		int totalTicks;
		boolean modelAvailable = true;
		int viewportHeight = 1000;
		int skull = -1;
		int health = -1;
		HeadIcon prayer;
		String chat;
		Player hintPlayer;
		WorldPoint hintPoint;
		java.awt.Shape actorHull;
		RuneLiteObject object;
		final int[] lit1 = new int[2], lit2 = new int[2], lit3 = new int[2];
		final Model model = stub(Model.class, (name, args) -> {
			switch (name)
			{
				case "getFaceColors1": return lit1;
				case "getFaceColors2": return lit2;
				case "getFaceColors3": return lit3;
				case "getVerticesY": return new float[]{-50, 50};
				case "getVerticesCount": case "getFaceCount": return 2;
				case "getFaceIndices1": return new int[]{0, 0};
				case "getFaceIndices2": return new int[]{1, 1};
				case "getFaceIndices3": return new int[]{1, 0};
				default: throw new AssertionError(name);
			}
		});
		final WorldView worldView = stub(WorldView.class, (name, args) -> {
			switch (name)
			{
				case "getSizeX": case "getSizeY": return 104;
				case "getPlane": return plane;
				case "getId": return WorldView.TOPLEVEL;
				case "getTileSettings": return tiles;
				case "getTileHeights": return heights;
				default: throw new AssertionError(name);
			}
		});
		final Player player = stub(Player.class, (name, args) -> {
			switch (name)
			{
				case "getLocalLocation": return location;
				case "getWorldView": return worldView;
				case "getLogicalHeight": logicalHeightReads++; return 200;
				case "getCurrentOrientation": return orientation;
				case "getSkullIcon": return skull;
				case "getOverheadIcon": return prayer;
				case "getHealthRatio": return health;
				case "getOverheadText": return chat;
				case "getConvexHull": return actorHull;
				case "getWorldLocation": worldLocationReads++; return new WorldPoint(3200, 3200, plane);
				default: throw new AssertionError(name);
			}
		});
		final Client client = stub(Client.class, this::callClient);
		final EscapeCrystalNotifyCrystal3d renderer = new EscapeCrystalNotifyCrystal3d(client, config);

		void update(boolean carried, boolean active)
		{
			// Most tests edit config fields directly; mirror the config event before rendering.
			renderer.invalidateSettings();
			renderer.updateState(carried, active, notifyLocation, remainingTicks, totalTicks);
			renderer.update();
		}

		Object callClient(String name, Object[] args)
		{
			switch (name)
			{
				case "getLocalPlayer": return player;
				case "getGameState": return gameState;
				case "getBoostedSkillLevel":
					skillReads++;
					return args[0] == Skill.HITPOINTS ? hitpoints : prayerPoints;
				case "getRealSkillLevel":
					skillReads++;
					return args[0] == Skill.HITPOINTS ? maxHitpoints : maxPrayerPoints;
				case "getWorldView": return worldView;
				case "getHintArrowPlayer": return hintPlayer;
				case "getHintArrowPoint": return hintPoint;
				case "getViewportYOffset": return 30;
				case "getViewportXOffset": return 20;
				case "getViewportHeight": return viewportHeight;
				case "getViewportWidth": return 1000;
				case "isGpu": return false;
				case "getCameraX": return 6400 + (int) Math.round(1000 * Math.sin(cameraYaw * Math.PI / 8192));
				case "getCameraY": return 6400 - (int) Math.round(1000 * Math.cos(cameraYaw * Math.PI / 8192));
				case "getCameraZ": return -1200;
				case "getCameraPitch": return 2048;
				case "getCameraYaw": return cameraYaw;
				case "getScale": return 512;
				case "getItemDefinition": return stub(ItemComposition.class, (n, a) -> 123);
				case "loadModelData":
					loads++;
					return modelAvailable ? modelData() : null;
				case "createRuneLiteObject": object = new RuneLiteObject(client); return object;
				case "registerRuneLiteObject": registered.add((RuneLiteObject) args[0]); return null;
				case "removeRuneLiteObject": registered.remove(args[0]); return null;
				case "isRuneLiteObjectRegistered": return registered.contains(args[0]);
				default: throw new AssertionError(name);
			}
		}

		ModelData modelData()
		{
			ModelData[] self = new ModelData[1];
			short[][] modelTextures = {textures};
			byte[][] modelTransparencies = {transparencies};
			float[] x = {-10, 10}, y = {-10, 10}, z = {-10, 10};
			self[0] = stub(ModelData.class, (name, args) -> {
				switch (name)
				{
					case "shallowCopy": case "cloneVertices": case "cloneColors": return self[0];
					case "cloneTextures":
						// Match the real client's null-intolerant clone rather than silently accepting it.
						modelTextures[0] = modelTextures[0].clone();
						return self[0];
					case "cloneTransparencies":
						modelTransparencies[0] = modelTransparencies[0].clone();
						return self[0];
					case "getVerticesX": return x;
					case "getVerticesY": return y;
					case "getVerticesZ": return z;
					case "getVerticesCount": return 2;
					case "getFaceColors": return colors;
					case "getFaceTextures": return modelTextures[0];
					case "getFaceTransparencies": return modelTransparencies[0];
					case "light":
						java.util.Arrays.fill(lit1, Short.toUnsignedInt(colors[0]));
						java.util.Arrays.fill(lit2, Short.toUnsignedInt(colors[0]));
						java.util.Arrays.fill(lit3, Short.toUnsignedInt(colors[0]));
						litSize = Math.round(y[1] - y[0]);
						litTextures = modelTextures[0];
						litTransparencies = modelTransparencies[0];
						return model;
					default: throw new AssertionError(name);
				}
			});
			return self[0];
		}
	}

	private static <T> T stub(Class<T> type, BiFunction<String, Object[], Object> answer)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
			(proxy, method, args) -> answer.apply(method.getName(), args)));
	}
}
