package com.escapecrystalnotify;

import com.google.common.primitives.Doubles;
import net.runelite.api.Model;
import net.runelite.api.JagexColor;
import static com.escapecrystalnotify.EscapeCrystalNotifyCrystalDefaults.*;

class EscapeCrystalNotifyCrystalFill
{
	private final Model model;
	private final int[] base1, base2, base3;
	private final float[] height;
	private final double[] brightness;
	private double previousFraction = 1;

	EscapeCrystalNotifyCrystalFill(Model model)
	{
		this.model = model;
		base1 = model.getFaceColors1().clone();
		base2 = model.getFaceColors2().clone();
		base3 = model.getFaceColors3().clone();
		float[] y = model.getVerticesY();
		height = new float[model.getVerticesCount()];
		brightness = new double[height.length];
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < height.length; i++)
		{
			min = Math.min(min, y[i]);
			max = Math.max(max, y[i]);
		}
		for (int i = 0; i < height.length; i++)
		{
			// Model Y is downward: 0 at the bottom tip, 1 at the top tip.
			height[i] = max == min ? 0.5f : (max - y[i]) / (max - min);
		}
	}

	static double fraction(boolean carried, boolean active, int remainingTicks, int totalTicks)
	{
		return carried && active ? fraction(remainingTicks, totalTicks) : 1;
	}

	static double fraction(int remaining, int maximum)
	{
		return maximum <= 0 ? 1 : Doubles.constrainToRange(remaining / (double) maximum, 0, 1);
	}

	void apply(double fraction)
	{
		fraction = Doubles.constrainToRange(fraction, 0, 1);
		if (fraction == previousFraction) return;
		previousFraction = fraction;
		int[] colors1 = model.getFaceColors1(), colors2 = model.getFaceColors2(), colors3 = model.getFaceColors3();
		if (fraction == 1)
		{
			System.arraycopy(base1, 0, colors1, 0, base1.length);
			System.arraycopy(base2, 0, colors2, 0, base2.length);
			System.arraycopy(base3, 0, colors3, 0, base3.length);
			return;
		}
		int[] face1 = model.getFaceIndices1(), face2 = model.getFaceIndices2(), face3 = model.getFaceIndices3();
		double fillTop = fraction * (1 + FILL_TRANSITION_WIDTH);
		for (int i = 0; i < height.length; i++)
		{
			brightness[i] = EMPTY_BRIGHTNESS + (1 - EMPTY_BRIGHTNESS)
				* Doubles.constrainToRange((fillTop - height[i]) / FILL_TRANSITION_WIDTH, 0, 1);
		}
		for (int i = 0; i < model.getFaceCount(); i++)
		{
			if (base3[i] == -2) continue; // Hidden faces stay hidden.
			boolean flat = base3[i] == -1;
			// Gouraud corner colors give the existing mesh a soft fill transition without changing its shape.
			colors1[i] = shade(base1[i], brightness[face1[i]]);
			colors2[i] = shade(flat ? base1[i] : base2[i], brightness[face2[i]]);
			colors3[i] = shade(flat ? base1[i] : base3[i], brightness[face3[i]]);
		}
	}

	private static int shade(int color, double brightness)
	{
		int lightness = Math.max(MIN_FACE_LIGHTNESS, (int) Math.round((color & JagexColor.LUMINANCE_MAX) * brightness));
		return (color & ~JagexColor.LUMINANCE_MAX) | lightness;
	}
}
