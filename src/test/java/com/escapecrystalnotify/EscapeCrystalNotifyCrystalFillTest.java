package com.escapecrystalnotify;

import java.lang.reflect.Proxy;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyCrystalFillTest
{
	@Test
	public void fractionClampsAndOnlyUsesAnActiveCarriedCrystalsValidTimer()
	{
		assertEquals(0.5, EscapeCrystalNotifyCrystalFill.fraction(true, true, 50, 100), 0);
		assertEquals(0, EscapeCrystalNotifyCrystalFill.fraction(true, true, -1, 100), 0);
		assertEquals(1, EscapeCrystalNotifyCrystalFill.fraction(true, true, 200, 100), 0);
		assertEquals(1, EscapeCrystalNotifyCrystalFill.fraction(true, true, 0, 0), 0);
		assertEquals(1, EscapeCrystalNotifyCrystalFill.fraction(true, false, 0, 100), 0);
		assertEquals(1, EscapeCrystalNotifyCrystalFill.fraction(false, true, 0, 100), 0);
	}

	@Test
	public void drainsTopFirstPreservesHueAndRestoresOriginalShadingFlags()
	{
		int color = Short.toUnsignedInt(JagexColor.packHSL(15, 7, 80));
		int[] c1 = {color, color, 123}, c2 = {color, 0, 456}, c3 = {color, -1, -2};
		float[] y = {-50, 0, 50};
		Model model = (Model) Proxy.newProxyInstance(Model.class.getClassLoader(), new Class<?>[]{Model.class},
			(proxy, method, args) -> {
				switch (method.getName())
				{
					case "getFaceColors1": return c1;
					case "getFaceColors2": return c2;
					case "getFaceColors3": return c3;
					case "getVerticesY": return y;
					case "getVerticesCount": case "getFaceCount": return 3;
					case "getFaceIndices1": return new int[]{0, 0, 0};
					case "getFaceIndices2": return new int[]{1, 1, 1};
					case "getFaceIndices3": return new int[]{2, 2, 2};
					default: throw new AssertionError(method.getName());
				}
			});
		EscapeCrystalNotifyCrystalFill fill = new EscapeCrystalNotifyCrystalFill(model);
		fill.apply(0.5);
		assertTrue((c1[0] & 127) < (c2[0] & 127));
		assertTrue((c2[0] & 127) < (c3[0] & 127));
		assertEquals(color & ~127, c1[0] & ~127);
		assertEquals(123, c1[2]);
		assertEquals(456, c2[2]);
		assertEquals(-2, c3[2]);
		assertArrayEquals(new float[]{-50, 0, 50}, y, 0);
		fill.apply(0);
		assertEquals(16, c1[0] & 127);
		assertEquals(16, c3[0] & 127);
		fill.apply(1);
		assertArrayEquals(new int[]{color, color, 123}, c1);
		assertArrayEquals(new int[]{color, 0, 456}, c2);
		assertArrayEquals(new int[]{color, -1, -2}, c3);
	}
}
