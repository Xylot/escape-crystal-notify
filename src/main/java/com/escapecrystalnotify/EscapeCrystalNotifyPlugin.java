package com.escapecrystalnotify;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

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
	private boolean escapeCrystalRingOfLifeActive = true;
	private int escapeCrystalInactivityTicks;
	private int clientInactivityTicks;
	private int expectedServerInactivityTicks = 0;
	private int expectedTicksUntilTeleport;

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
		this.escapeCrystalInactivityTicks = client.getVarbitValue(14849);
		this.escapeCrystalRingOfLifeActive = client.getVarbitValue(14857) == 1;

		int currentClientInactivityTicks = Math.min(client.getKeyboardIdleTicks(), client.getMouseIdleTicks());

		if (currentClientInactivityTicks > this.clientInactivityTicks) {
			this.expectedServerInactivityTicks += 1;
		}
		else {
			this.expectedServerInactivityTicks = 0;
		}

		this.clientInactivityTicks = currentClientInactivityTicks;
		this.expectedTicksUntilTeleport = this.escapeCrystalInactivityTicks - this.expectedServerInactivityTicks;

		if (this.expectedTicksUntilTeleport < 0) {
			this.expectedTicksUntilTeleport = 0;
		}

		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (equipmentContainer == null && inventoryContainer == null) {
			this.escapeCrystalWithPlayer = false;
			return;
		}

		boolean escapeCrystalEquipped = equipmentContainer != null && equipmentContainer.contains(ItemID.ESCAPE_CRYSTAL);
		boolean escapeCrystalInInventory = inventoryContainer != null && inventoryContainer.contains(ItemID.ESCAPE_CRYSTAL);

		if (escapeCrystalEquipped || escapeCrystalInInventory) {
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

	public boolean isEscapeCrystalInactivityTeleportActive() {
		return escapeCrystalWithPlayer && escapeCrystalActive;
	}

	public boolean isEscapeCrystalRingOfLifeActive() {
		return escapeCrystalRingOfLifeActive;
	}

	public int getEscapeCrystalInactivityTicks() {
		return escapeCrystalInactivityTicks;
	}

	public int getEscapeCrystalInactivitySeconds() {
		return convertTicksToSeconds(escapeCrystalInactivityTicks);
	}

	public int getClientInactivityTicks() {
		return clientInactivityTicks;
	}

	public int getExpectedServerInactivityTicks() {
		return expectedServerInactivityTicks;
	}

	public int getExpectedServerInactivitySeconds() {
		return convertTicksToSeconds(expectedServerInactivityTicks);
	}

	public int getExpectedTicksUntilTeleport() {
		return expectedTicksUntilTeleport;
	}

	public int getExpectedSecondsUntilTeleport() {
		return convertTicksToSeconds(expectedTicksUntilTeleport);
	}

	private int convertTicksToSeconds(int ticks) {
		return (int) Math.round(ticks * 0.6);
	}


}
