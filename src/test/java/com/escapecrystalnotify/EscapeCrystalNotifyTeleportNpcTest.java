package com.escapecrystalnotify;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyTeleportNpcTest
{
	@Test
	public void hiddenNpcAnimatesAndRevealDoesNotRestartIt()
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, 120);
		RuneLiteObject actor = f.registered.iterator().next();
		assertNull(actor.getModel());
		assertNull(f.npc.getLocation());
		actor.tick(3);
		f.npc.show();
		actor.getModel();
		assertEquals(1, f.renderedFrame);
		assertNotNull(f.npc.getLocation());
		assertEquals(1, f.registered.size());
	}

	@Test
	public void hiddenNpcExpiresWithoutEverBeingRevealed()
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, 120);
		assertEquals(1, f.registered.size());
		f.cycle = 120;
		f.npc.tick();
		f.npc.show();
		assertTrue(f.registered.isEmpty());
		assertNull(f.npc.getLocation());
	}

	@Test
	public void missingAssetsSkipAppearanceWithoutRetryingEachTick()
	{
		Fixture f = new Fixture();
		f.modelAvailable = false;
		f.npc.spawnHidden(f.player, 120);
		f.modelAvailable = true;
		for (int i = 0; i < 100; i++) f.npc.tick();
		assertTrue(f.registered.isEmpty());
		assertEquals(1, f.modelLoads);
		f.npc.spawnHidden(f.player, 120);
		assertEquals(1, f.registered.size());
		assertTrue(f.colorsCloned);
	}

	@Test
	public void reusesOneModelAcrossExpiryAndSceneChangesAndReleasesItOnShutdown() throws Exception
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		Model original = f.registered.iterator().next().getBaseModel();
		f.animation = -1; f.cycle += 120;
		f.npc.tick();
		assertTrue(f.registered.isEmpty());
		f.animation = 714;
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertSame(original, f.registered.iterator().next().getBaseModel());
		f.changeGameState(GameState.LOADING);
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(1, f.modelLoads);
		assertEquals(1, f.boundsCalculations);
		f.config.type = EscapeCrystalNotifyTeleportNpcType.WISE_OLD_MAN;
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(2, f.modelLoads);
		f.config.type = EscapeCrystalNotifyTeleportNpcType.DEATH;
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(3, f.modelLoads); // Only the most recent type is retained.
		f.npc.shutDown();
		assertTrue(f.registered.isEmpty());
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(4, f.modelLoads);
		assertEquals(4, f.boundsCalculations);
	}

	@Test
	public void idleTicksDoNotReadSettingsOrPlayer()
	{
		Fixture f = new Fixture();
		for (int i = 0; i < 100; i++) f.npc.tick();
		assertEquals(0, f.config.enabledReads);
		assertEquals(0, f.playerReads);
		assertEquals(0, f.modelLoads);
	}

	@Test
	public void defaultsToDeathAndUsesSelectedNpcAndAnimationOnNextSpawn()
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(net.runelite.api.gameval.NpcID.HALLOWEEN_DEATH, f.requestedNpcId);
		assertEquals(net.runelite.api.gameval.AnimationID.HUMAN_SCYTHE_SWEEP,
			f.registered.iterator().next().getAnimationController().getAnimation().getId());
		f.config.type = EscapeCrystalNotifyTeleportNpcType.WISE_OLD_MAN;
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(net.runelite.api.gameval.NpcID.WISE_OLD_MAN, f.requestedNpcId);
		assertEquals(net.runelite.api.gameval.AnimationID.EMOTE_CHEER, f.requestedAnimationId);
		assertEquals(1, f.registered.size());
	}

	@Test
	public void deathEntranceAdvancesFramesThenIdlesUntilPlayerTeleportEnds()
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.npc.tick();
		RuneLiteObject actor = f.registered.iterator().next();
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.HUMAN_SCYTHE_SWEEP, f.renderedAnimationId);
		assertEquals(0, f.renderedFrame);
		actor.tick(3);
		actor.getModel();
		assertEquals(1, f.renderedFrame);
		actor.tick(4);
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.HUMAN_READY_SCYTHE, f.renderedAnimationId);
		assertEquals(0, f.renderedFrame);
		actor.tick(7);
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.HUMAN_READY_SCYTHE, f.renderedAnimationId);
		assertTrue(actor.isActive());
		assertEquals(1, f.modelLoads);
		f.animation = -1; f.cycle += 120;
		f.npc.tick();
		assertFalse(actor.isActive());
	}

	@Test
	public void mercenarySpecsTowardPlayerThenHoldsGodswordUntilTeleportEnds()
	{
		Fixture f = new Fixture();
		f.config.type = EscapeCrystalNotifyTeleportNpcType.AGS_MERCENARY;
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.npc.tick();
		assertEquals(8213, f.requestedNpcId);
		RuneLiteObject actor = f.registered.iterator().next();
		assertEquals(f.location.dy(128), f.npc.getLocation());
		assertEquals(0, actor.getOrientation()); // South, toward the player.
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.AGS_SPECIAL_PLAYER, f.renderedAnimationId);
		actor.tick(7);
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.DH_SWORD_UPDATE_READY, f.renderedAnimationId);
		actor.tick(7);
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.DH_SWORD_UPDATE_READY, f.renderedAnimationId);
		assertEquals(1, f.modelLoads);
		assertTrue(actor.isActive());
		f.animation = -1; f.cycle += 120;
		f.npc.tick();
		assertFalse(actor.isActive());
	}

	@Test
	public void wiseOldManContinuesCheeringWithoutRecreatingActor()
	{
		Fixture f = new Fixture();
		f.config.type = EscapeCrystalNotifyTeleportNpcType.WISE_OLD_MAN;
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.npc.tick();
		RuneLiteObject actor = f.registered.iterator().next();
		actor.tick(7);
		actor.getModel();
		assertEquals(net.runelite.api.gameval.AnimationID.EMOTE_CHEER, f.renderedAnimationId);
		assertEquals(0, f.renderedFrame);
		assertTrue(actor.isActive());
	}

	@Test
	public void deadlineExpiresOnExactClientCycleWithoutPollingPlayer()
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.npc.tick();
		assertEquals(1, f.registered.size());
		assertEquals(230, f.npc.getOverheadHeight());
		f.cycle = 119;
		f.npc.tick();
		assertEquals(1, f.registered.size());
		assertEquals(0, f.playerReads);
		f.cycle = 120;
		f.npc.tick(); // The game-tick counter has not advanced.
		assertTrue(f.registered.isEmpty());
		f.animation = 714;
		f.npc.tick();
		assertTrue(f.registered.isEmpty());
	}

	@Test
	public void realNpcStopsAtDeadlineOrSceneLoading() throws Exception
	{
		for (int reason = 0; reason < 2; reason++)
		{
			Fixture f = new Fixture();
			f.npc.spawnHidden(f.player, f.cycle + 120);
			f.npc.show();
			f.arrive();
			if (reason == 0) f.cycle += 120;
			if (reason == 1) f.changeGameState(GameState.LOADING);
			f.npc.tick();
			assertTrue(f.registered.isEmpty());
			f.npc.tick();
			assertTrue(f.registered.isEmpty());
		}
	}

	@Test
	public void duplicateTriggersReplaceTheActorAndDisablingRemovesIt()
	{
		Fixture f = new Fixture();
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		RuneLiteObject original = f.registered.iterator().next();
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		assertFalse(original.isActive());
		f.arrive();
		assertEquals(1, f.registered.size());
		f.config.enabled = false;
		f.npc.tick();
		assertTrue(f.registered.isEmpty());
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.config.enabled = true;
		f.npc.tick();
		assertTrue(f.registered.isEmpty());
	}

	@Test
	public void logoutHopSceneChangesAndShutdownCleanUp() throws Exception
	{
		for (GameState state : new GameState[]{GameState.LOGIN_SCREEN, GameState.HOPPING,
			GameState.CONNECTION_LOST, GameState.LOADING})
		{
			Fixture f = new Fixture();
			f.npc.spawnHidden(f.player, f.cycle + 120);
			f.npc.show();
			f.arrive();
			f.changeGameState(state);
			assertTrue(f.registered.isEmpty());
			f.npc.tick();
			assertTrue(f.registered.isEmpty());
			f.npc.spawnHidden(f.player, f.cycle + 120);
			f.npc.show();
			f.npc.reset();
			f.npc.tick();
			assertTrue(f.registered.isEmpty());
		}
	}

	@Test
	public void sceneEdgeFallsBackSouth()
	{
		Fixture f = new Fixture();
		f.location = new LocalPoint(6400, 103 * 128 + 64);
		f.npc.spawnHidden(f.player, f.cycle + 120);
		f.npc.show();
		f.arrive();
		assertEquals(f.location.dy(-128), f.npc.getLocation());
		assertEquals(1024, f.registered.iterator().next().getOrientation());
		f.cycle += 120;
		f.npc.tick();
		assertTrue(f.registered.isEmpty());
	}

	private static class Settings implements EscapeCrystalNotifyConfig
	{
		boolean enabled = true;
		int enabledReads;
		EscapeCrystalNotifyTeleportNpcType type = EscapeCrystalNotifyConfig.super.teleportNpcType();
		@Override public boolean enableTeleportNpc() { enabledReads++; return enabled; }
		@Override public EscapeCrystalNotifyTeleportNpcType teleportNpcType() { return type; }
	}

	private static class Fixture
	{
		final Settings config = new Settings();
		final Set<RuneLiteObject> registered = new HashSet<>();
		LocalPoint location = new LocalPoint(6400, 6400);
		GameState state = GameState.LOGGED_IN;
		int plane;
		int requestedNpcId, requestedAnimationId;
		int missingAnimationId = -1;
		int animationLoads, renderedAnimationId, renderedFrame;
		int modelLoads, boundsCalculations, playerReads;
		int tick, cycle, animation = 714;
		boolean modelAvailable = true, colorsCloned;
		final WorldView view = stub(WorldView.class, (name, args) -> {
			switch (name)
			{
				case "getId": return WorldView.TOPLEVEL;
				case "getSizeX": case "getSizeY": return 104;
				case "getPlane": return plane;
				case "getTileSettings": return new byte[4][104][104];
				case "getTileHeights": return new int[4][105][105];
				default: throw new AssertionError(name);
			}
		});
		final Player player = stub(Player.class, (name, args) -> {
			switch (name)
			{
				case "getWorldView": return view;
				case "getLocalLocation": return location;
				case "getAnimation": return animation;
				case "getWorldLocation": return new net.runelite.api.coords.WorldPoint(location.getSceneX(), location.getSceneY(), plane);
				case "getName": return "Tester";
				default: throw new AssertionError(name);
			}
		});
		final Model model = stub(Model.class, (name, args) -> {
			if (name.equals("calculateBoundsCylinder")) { boundsCalculations++; return null; }
			if (name.equals("getModelHeight")) return 200;
			throw new AssertionError(name);
		});
		final NPCComposition definition = stub(NPCComposition.class, (name, args) -> {
			switch (name)
			{
				case "getModels": return new int[]{123};
				case "getColorToReplace": return new short[]{1};
				case "getColorToReplaceWith": return new short[]{2};
				default: throw new AssertionError(name);
			}
		});
		final Client client = stub(Client.class, this::callClient);
		final EscapeCrystalNotifyTeleportNpc npc = new EscapeCrystalNotifyTeleportNpc(client, config);

		void arrive()
		{
			npc.tick();
		}

		void nextGameTick() { tick++; cycle += 30; npc.tick(); }

		void changeGameState(GameState state) throws Exception
		{
			EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
			java.lang.reflect.Field npcField = EscapeCrystalNotifyPlugin.class.getDeclaredField("teleportNpc");
			npcField.setAccessible(true);
			npcField.set(plugin, npc);
			java.lang.reflect.Field crystalField = EscapeCrystalNotifyPlugin.class.getDeclaredField("crystal3d");
			crystalField.setAccessible(true);
			crystalField.set(plugin, new EscapeCrystalNotifyCrystal3d(client, config));
			net.runelite.api.events.GameStateChanged event = new net.runelite.api.events.GameStateChanged();
			event.setGameState(state);
			plugin.onGameStateChanged(event);
		}

		Object callClient(String name, Object[] args)
		{
			switch (name)
			{
				case "getGameState": return state;
				case "getTickCount": return tick;
				case "getGameCycle": return cycle;
				case "getLocalPlayer": playerReads++; return player;
				case "getWorldView": return view;
				case "getNpcDefinition": requestedNpcId = (int) args[0]; return definition;
				case "loadAnimation":
					animationLoads++;
					requestedAnimationId = (int) args[0];
					return requestedAnimationId == missingAnimationId ? null : animationData(requestedAnimationId);
				case "getAnimationInterpolationFilter": return null;
				case "applyTransformations":
					assertSame(model, args[0]);
					renderedAnimationId = ((Animation) args[1]).getId();
					renderedFrame = (int) args[2];
					return model;
				case "loadModelData": modelLoads++; return modelAvailable ? modelData() : null;
				case "mergeModels": return modelData();
				case "createRuneLiteObject": return new RuneLiteObject(client);
				case "registerRuneLiteObject": registered.add((RuneLiteObject) args[0]); return null;
				case "removeRuneLiteObject": registered.remove(args[0]); return null;
				case "isRuneLiteObjectRegistered": return registered.contains(args[0]);
				default: throw new AssertionError(name);
			}
		}

		Animation animationData(int id)
		{
			return stub(Animation.class, (name, args) -> {
				switch (name)
				{
					case "getId": return id;
					case "isMayaAnim": return false;
					case "getFrameLengths": return new int[]{2, 2, 2};
					case "getFrameStep": case "getDuration": return 3;
					default: throw new AssertionError(name);
				}
			});
		}

		ModelData modelData()
		{
			ModelData[] data = new ModelData[1];
			data[0] = stub(ModelData.class, (name, args) -> {
				switch (name)
				{
					case "shallowCopy": return data[0];
					case "cloneColors": colorsCloned = true; return data[0];
					case "recolor": assertTrue(colorsCloned); return data[0];
					case "light": return model;
					default: throw new AssertionError(name);
				}
			});
			return data[0];
		}
	}

	private static <T> T stub(Class<T> type, BiFunction<String, Object[], Object> answer)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
			(proxy, method, args) -> answer.apply(method.getName(), args)));
	}
}
