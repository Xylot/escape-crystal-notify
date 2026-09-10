package com.escapecrystalnotify;

import java.awt.Color;
import net.runelite.api.Perspective;
import static com.escapecrystalnotify.EscapeCrystalNotifyConfig.Crystal3dDisplayStyle;
import static com.escapecrystalnotify.EscapeCrystalNotifyConfig.Crystal3dFillMode;

final class EscapeCrystalNotifyCrystalDefaults
{
	private EscapeCrystalNotifyCrystalDefaults() {}

	// User-facing defaults.
	static final boolean DEFAULT_ENABLED = false;
	static final boolean DEFAULT_SECTION_CLOSED = true;
	static final boolean DEFAULT_DISPLAY_EVERYWHERE = false;
	static final Crystal3dDisplayStyle DEFAULT_DISPLAY_STYLE = Crystal3dDisplayStyle.ABOVE_HEAD;
	static final Color DEFAULT_ACTIVE_COLOR = new Color(50, 205, 50);
	static final Color DEFAULT_INACTIVE_COLOR = new Color(205, 50, 50);
	static final Crystal3dFillMode DEFAULT_FILL_MODE = Crystal3dFillMode.INACTIVITY_TIME;
	static final int DEFAULT_SIZE = 100;
	static final int DEFAULT_HEIGHT = 60;
	static final int DEFAULT_SPIN_SECONDS = 6;
	static final int DEFAULT_BOB_HEIGHT = 8;
	static final boolean DEFAULT_AUTOMATIC_CLEARANCE = true;
	static final int DEFAULT_CLEARANCE_PADDING = 4;
	static final int DEFAULT_SIDE_OFFSET = 56;
	static final int DEFAULT_FORWARD_OFFSET = 0;
	static final int DEFAULT_SIDE_HEIGHT = 20;

	// Config limits.
	static final int MIN_SIZE = 25, MAX_SIZE = 200;
	static final int MIN_HEIGHT = 0, MAX_HEIGHT = 300;
	static final int MIN_SPIN_SECONDS = 0, MAX_SPIN_SECONDS = 30;
	static final int MIN_BOB_HEIGHT = 0, MAX_BOB_HEIGHT = 30;
	static final int MIN_PADDING = 0, MAX_PADDING = 100;
	static final int MIN_SIDE_OFFSET = -Perspective.LOCAL_TILE_SIZE, MAX_SIDE_OFFSET = Perspective.LOCAL_TILE_SIZE;
	static final int MIN_SIDE_HEIGHT = -100, MAX_SIDE_HEIGHT = 300;

	// Model and motion tuning. Full-turn angle units and tile size come from RuneLite.
	static final double SMALL_STYLE_SCALE = 0.4;
	static final float MODEL_WIDTH_RATIO = 0.4f;
	static final double COLOR_BRIGHTNESS = 1.0;
	static final long MODEL_RETRY_NANOS = 1_000_000_000L;
	static final double MAX_FRAME_SECONDS = 0.1;
	static final double BOB_PERIOD_SECONDS = 3;
	static final int FULL_TURN = Perspective.SINEF.length;

	// Overhead clearance tuning.
	static final long CLEARANCE_HOLD_NANOS = 2_000_000_000L;
	static final int MAX_CLEARANCE_LIFT = 2048;
	static final int VIEWPORT_EDGE_MARGIN = 4;
	static final int HEAD_ANCHOR_OFFSET = 15;
	static final int HEALTH_PIXELS = 32, SKULL_PIXELS = 28, PRAYER_PIXELS = 28, HINT_PIXELS = 32, CHAT_PIXELS = 40;
	static final int HINT_DISTANCE_TILES = 1;
	static final int CLEARANCE_SETTLE_PIXELS_PER_SECOND = 40;
	static final int CLEARANCE_SEARCH_STEP = 32;

	// Fill shading tuning.
	static final double EMPTY_BRIGHTNESS = 0.2;
	static final double FILL_TRANSITION_WIDTH = 0.4;
	static final int MIN_FACE_LIGHTNESS = 2;
}
