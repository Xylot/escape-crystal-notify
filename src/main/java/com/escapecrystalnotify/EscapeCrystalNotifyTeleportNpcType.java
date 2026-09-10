package com.escapecrystalnotify;

import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;

public enum EscapeCrystalNotifyTeleportNpcType
{
	DEATH("Death", NpcID.HALLOWEEN_DEATH, AnimationID.HUMAN_SCYTHE_SWEEP, AnimationID.HUMAN_READY_SCYTHE),
	WISE_OLD_MAN("Wise Old Man", NpcID.WISE_OLD_MAN, AnimationID.EMOTE_CHEER, -1),
	AGS_MERCENARY("Mercenary", NpcID.MYQ4_MELEE_MERCENARY_VISIBLE,
		AnimationID.AGS_SPECIAL_PLAYER, AnimationID.DH_SWORD_UPDATE_READY);

	private final String displayName;
	private final int npcId;
	private final int animationId;
	private final int idleAnimationId;

	EscapeCrystalNotifyTeleportNpcType(String displayName, int npcId, int animationId, int idleAnimationId)
	{
		this.displayName = displayName;
		this.npcId = npcId;
		this.animationId = animationId;
		this.idleAnimationId = idleAnimationId;
	}

	int getNpcId() { return npcId; }
	int getAnimationId() { return animationId; }
	int getIdleAnimationId() { return idleAnimationId; }

	@Override
	public String toString() { return displayName; }
}
