package com.escapecrystalnotify;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EscapeCrystalNotifyPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EscapeCrystalNotifyPlugin.class);
		RuneLite.main(args);
	}
}