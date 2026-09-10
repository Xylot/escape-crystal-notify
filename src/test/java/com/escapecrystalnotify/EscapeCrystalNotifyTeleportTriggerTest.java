package com.escapecrystalnotify;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ItemContainerChanged;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyTeleportTriggerTest
{
	@Test
	public void startsHiddenThenRevealsAfterContainerPacketsOnClientTick()
	{
		for (boolean inventoryFirst : new boolean[]{true, false})
		{
			Fixture f = new Fixture(1, 0);
			if (!inventoryFirst) f.animate(714);
			f.inventoryCount = 0;
			f.inventoryChanged();
			if (inventoryFirst) f.animate(714);
			assertEquals(1, f.npc.spawns);
			assertFalse(f.npc.visible);
			f.plugin.onClientTick(null);
			assertTrue(f.npc.visible);
			assertEquals(1, f.npc.spawns);
			assertEquals(120, f.npc.endCycle);
		}
	}

	@Test
	public void equippingCrystalDoesNotRevealNpc()
	{
		Fixture f = new Fixture(1, 0);
		f.animate(714);
		f.inventoryCount = 0;
		f.inventoryChanged();
		f.equipmentCount = 1;
		f.plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.EQUIPMENT.getId(), f.equipment));
		f.plugin.onClientTick(null);
		assertFalse(f.npc.visible);
		f.equipmentCount = 0;
		f.plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.EQUIPMENT.getId(), f.equipment));
		f.plugin.onClientTick(null);
		assertTrue(f.npc.visible);
	}

	@Test
	public void missingContainersAreNotConsumptionAndIdleTicksDoNotRescan()
	{
		Fixture f = new Fixture(1, 0);
		int reads = f.containerReads;
		for (int i = 0; i < 100; i++) f.plugin.onClientTick(null);
		assertEquals(reads, f.containerReads);
		f.animate(714);
		f.containersAvailable = false;
		f.inventoryChanged();
		f.plugin.onClientTick(null);
		assertFalse(f.npc.visible);
		f.containersAvailable = true;
		f.inventoryCount = 0;
		f.inventoryChanged();
		f.plugin.onClientTick(null);
		assertFalse(f.npc.visible);
	}

	@Test
	public void debugRevealRequiresBothDebugSettingsAndFeatureEnabled()
	{
		for (int flags = 0; flags < 8; flags++)
		{
			Fixture f = new Fixture(0, 0);
			f.settings.enabled = (flags & 1) != 0;
			f.settings.debug = (flags & 2) != 0;
			f.settings.anyTeleport = (flags & 4) != 0;
			f.animate(714);
			assertEquals((flags & 1) != 0 ? 1 : 0, f.npc.spawns);
			assertEquals(flags == 7, f.npc.visible);
		}
	}

	@Test
	public void durationIsLoadedOnceAndAnimationEndRemovesNpc()
	{
		Fixture f = new Fixture(1, 0);
		f.animate(714);
		assertTrue(f.npc.active);
		f.animate(-1);
		assertFalse(f.npc.active);
		f.cycle = 200;
		f.animate(714);
		assertEquals(1, f.animationLoads);
		assertEquals(320, f.npc.endCycle);
	}

	@Test
	public void otherPlayersAndUnrelatedAnimationsDoNotSpawn()
	{
		Fixture f = new Fixture(1, 0);
		AnimationChanged other = new AnimationChanged();
		other.setActor(stub(Player.class, (name, args) -> { throw new AssertionError(name); }));
		f.plugin.onAnimationChanged(other);
		f.animate(123);
		assertEquals(0, f.npc.spawns);
	}
	private static class Settings implements EscapeCrystalNotifyConfig
	{
		boolean enabled = true;
		boolean debug, anyTeleport;
		@Override public boolean enableTeleportNpc() { return enabled; }
		@Override public boolean enableDebugMode() { return debug; }
		@Override public boolean debugTeleportNpcOnAnyTeleport() { return anyTeleport; }
	}

	private static class RecordingNpc extends EscapeCrystalNotifyTeleportNpc
	{
		int spawns, endCycle;
		boolean active, visible;
		Player player;
		RecordingNpc() { super(null, null); }
		@Override void spawnHidden(Player player, int endCycle) { this.player = player; this.endCycle = endCycle; active = true; visible = false; spawns++; }
		@Override void tick() {}
		@Override void show() { visible = active; }
		@Override void reset() { active = visible = false; }
	}

	private static class Fixture
	{
		int inventoryCount, equipmentCount, containerReads, tick, cycle, animationLoads, animation = -1;
		boolean containersAvailable = true;
		WorldPoint location = new WorldPoint(3200, 3200, 0);
		final Settings settings = new Settings();
		final RecordingNpc npc = new RecordingNpc();
		final Player player = stub(Player.class, (name, args) -> {
			switch (name) {
				case "getAnimation": return animation;
				case "getWorldLocation": return location;
				default: throw new AssertionError(name);
			}
		});
		final ItemContainer inventory = stub(ItemContainer.class, (name, args) -> inventoryCount);
		final ItemContainer equipment = stub(ItemContainer.class, (name, args) -> equipmentCount);
		final Client client = stub(Client.class, (name, args) -> {
			switch (name) {
				case "getGameState": return GameState.LOGGED_IN;
				case "getLocalPlayer": return player;
				case "getTickCount": return tick;
				case "getGameCycle": return cycle;
				case "loadAnimation":
					animationLoads++;
					return stub(Animation.class, (method, ignored) -> {
						if (method.equals("isMayaAnim")) return false;
						if (method.equals("getFrameLengths")) return new int[]{30, 90};
						throw new AssertionError(method);
					});
				case "getItemContainer":
					containerReads++;
					return !containersAvailable ? null : args[0] == InventoryID.INVENTORY ? inventory : equipment;
				default: throw new AssertionError(name);
			}
		});
		final EscapeCrystalNotifyPlugin plugin = new EscapeCrystalNotifyPlugin();
		Fixture(int inventoryCount, int equipmentCount)
		{
			this.inventoryCount = inventoryCount;
			this.equipmentCount = equipmentCount;
			try
			{
				set(plugin, "client", client);
				set(plugin, "config", settings);
				set(plugin, "teleportNpc", npc);
			}
			catch (Exception e) { throw new AssertionError(e); }
			inventoryChanged();
		}
		void animate(int id)
		{
			animation = id;
			AnimationChanged event = new AnimationChanged();
			event.setActor(player);
			plugin.onAnimationChanged(event);
		}
		void inventoryChanged()
		{
			plugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), inventory));
		}
	}

	private static <T> T stub(Class<T> type, BiFunction<String, Object[], Object> answer)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
			(proxy, method, args) -> answer.apply(method.getName(), args)));
	}

	private static void set(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
