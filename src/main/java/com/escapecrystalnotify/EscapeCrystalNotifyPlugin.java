package com.escapecrystalnotify;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Escape Crystal Notify"
)
public class EscapeCrystalNotifyPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EscapeCrystalNotifyConfig config;

	@Inject private EscapeCrystalNotifyOverlay escapeCrystalNotifyOverlay;

	@Inject private OverlayManager overlayManager;

	private boolean escapeCrystalWithPlayer = true;
	private boolean escapeCrystalActive = true;
	private int escapeCrystalInactivityTime;
	private boolean escapeCrystalRingOfLifeActive = true;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(escapeCrystalNotifyOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(escapeCrystalNotifyOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		this.escapeCrystalActive = client.getVarbitValue(14838) == 1;
		this.escapeCrystalInactivityTime = client.getVarbitValue(14849);
		this.escapeCrystalRingOfLifeActive = client.getVarbitValue(14857) == 1;

		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (equipmentContainer == null && inventoryContainer == null) {
			this.escapeCrystalWithPlayer = false;
			return;
		}

		if (equipmentContainer.contains(ItemID.ESCAPE_CRYSTAL) || inventoryContainer.contains(ItemID.ESCAPE_CRYSTAL)) {
			this.escapeCrystalWithPlayer = true;
			return;
		}

		this.escapeCrystalWithPlayer = false;
	}

	@Provides
	EscapeCrystalNotifyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EscapeCrystalNotifyConfig.class);
	}

	public boolean isEscapeCrystalActive() {
		return escapeCrystalActive;
	}

	public boolean isEscapeCrystalWithPlayer() {
		return escapeCrystalWithPlayer;
	}

	public boolean isEscapeCrystalRingOfLifeActive() {
		return escapeCrystalRingOfLifeActive;
	}

	public int getEscapeCrystalInactivityTime() {
		return escapeCrystalInactivityTime;
	}
}
