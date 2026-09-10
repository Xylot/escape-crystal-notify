package com.escapecrystalnotify;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Animation;
import net.runelite.api.AnimationController;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

/** A temporary, client-only NPC with overhead text supplied by its overlay. */
@Singleton
class EscapeCrystalNotifyTeleportNpc
{
	private final Client client;
	private final EscapeCrystalNotifyConfig config;
	private RuneLiteObject npc;
	private int plane, endCycle;
	private boolean visible;
	private int overheadHeight;
	private Model cachedModel;
	private EscapeCrystalNotifyTeleportNpcType cachedType;

	@Inject
	EscapeCrystalNotifyTeleportNpc(Client client, EscapeCrystalNotifyConfig config)
	{
		this.client = client;
		this.config = config;
	}

	/** Start the NPC animation immediately, with rendering hidden until show() is called. */
	void spawnHidden(Player player, int endCycle)
	{
		reset();
		WorldView view = player.getWorldView();
		if (!config.enableTeleportNpc() || view == null) return;
		plane = view.getPlane();
		this.endCycle = endCycle;
		spawnNpc(player.getLocalLocation(), view);
	}

	void show()
	{
		visible = npc != null;
	}

	void tick()
	{
		if (npc != null && (client.getGameCycle() >= endCycle || !config.enableTeleportNpc())) reset();
	}

	private void spawnNpc(LocalPoint playerLocation, WorldView view)
	{
		LocalPoint spawn = playerLocation.dy(128);
		if (!EscapeCrystalNotifySceneBounds.contains(spawn, view)) spawn = playerLocation.dy(-128);
		if (!EscapeCrystalNotifySceneBounds.contains(spawn, view)) return;
		EscapeCrystalNotifyTeleportNpcType type = config.teleportNpcType();
		Model model = getModel(type);
		if (model == null) return;
		npc = createAnimatedNpc(type, model);
		if (npc == null) return;
		npc.setLocation(spawn, plane);
		npc.setOrientation(spawn.getY() > playerLocation.getY() ? 0 : 1024);
		npc.setActive(true);
	}

	private Model getModel(EscapeCrystalNotifyTeleportNpcType type)
	{
		if (cachedModel == null || cachedType != type)
		{
			cachedModel = buildModel(type);
			cachedType = type;
			if (cachedModel == null) return null;
			cachedModel.calculateBoundsCylinder();
			overheadHeight = Math.max(100, cachedModel.getModelHeight()) + 30;
		}
		return cachedModel;
	}

	private RuneLiteObject createAnimatedNpc(EscapeCrystalNotifyTeleportNpcType type, Model model)
	{
		Animation animation = client.loadAnimation(type.getAnimationId());
		Animation idle = type.getIdleAnimationId() == -1 ? null : client.loadAnimation(type.getIdleAnimationId());
		if (animation == null || (type.getIdleAnimationId() != -1 && idle == null)) return null;
		RuneLiteObject actor = new RuneLiteObject(client)
		{
			@Override
			public Model getModel()
			{
				return visible ? super.getModel() : null;
			}
		};
		actor.setModel(model);
		actor.setAnimation(animation);
		actor.getAnimationController().setOnFinished(controller -> {
			if (idle != null) controller.setAnimation(idle);
			else controller.loop();
			controller.setOnFinished(AnimationController::loop);
		});
		return actor;
	}

	private Model buildModel(EscapeCrystalNotifyTeleportNpcType type)
	{
		NPCComposition definition = client.getNpcDefinition(type.getNpcId());
		if (definition == null) return null;
		int[] ids = definition.getModels();
		if (ids == null || ids.length == 0) return null;
		ModelData[] parts = new ModelData[ids.length];
		for (int i = 0; i < ids.length; i++)
		{
			parts[i] = client.loadModelData(ids[i]);
			if (parts[i] == null) return null;
		}
		ModelData merged = client.mergeModels(parts);
		if (merged == null) return null;
		ModelData data = merged.shallowCopy().cloneColors();
		short[] from = definition.getColorToReplace();
		short[] to = definition.getColorToReplaceWith();
		if (from != null && to != null && from.length == to.length)
		{
			for (int i = 0; i < from.length; i++) data.recolor(from[i], to[i]);
		}
		return data.light();
	}

	LocalPoint getLocation()
	{
		return visible && npc != null && config.enableTeleportNpc() ? npc.getLocation() : null;
	}

	int getPlane() { return plane; }
	int getOverheadHeight() { return overheadHeight; }

	void reset()
	{
		if (npc != null) npc.setActive(false);
		npc = null;
		visible = false;
	}

	/** Release retained cache assets when the plugin stops; ordinary despawns only reset the actor. */
	void shutDown()
	{
		reset();
		cachedModel = null;
		cachedType = null;
		overheadHeight = 0;
	}
}
