package com.escapecrystalnotify;

import java.util.function.IntFunction;
import org.junit.Test;
import static org.junit.Assert.*;

public class EscapeCrystalNotifyCrystalClearanceTest
{
	@Test
	public void reservesOnlyPresentOverheadsWithoutEmptySlots()
	{
		int base = EscapeCrystalNotifyCrystalClearance.overheadPixels(false, false, false, false, false);
		assertEquals(0, base);
		int all = EscapeCrystalNotifyCrystalClearance.overheadPixels(true, true, true, true, true);
		assertEquals(28 + 28 + 32 + 32 + 40, all);
		assertEquals(28, EscapeCrystalNotifyCrystalClearance.overheadPixels(false, true, false, false, false));
		assertTrue(all > EscapeCrystalNotifyCrystalClearance.overheadPixels(true, true, true, false, false));
	}

	@Test
	public void holdsClearanceThroughFlickingThenLowersGradually()
	{
		EscapeCrystalNotifyCrystalClearance clearance = new EscapeCrystalNotifyCrystalClearance();
		assertEquals(100, clearance.reserve(100, 0, 0));
		assertEquals(100, clearance.reserve(48, 600_000_000L, 0.02));
		assertEquals(100, clearance.reserve(100, 1_200_000_000L, 0.02));
		assertEquals(100, clearance.reserve(48, 3_000_000_000L, 0.02));
		assertEquals(96, clearance.reserve(48, 3_300_000_000L, 0.1));
		assertEquals(140, clearance.reserve(140, 3_400_000_000L, 0.1));
		clearance.clear();
		assertEquals(48, clearance.reserve(48, 3_500_000_000L, 0));
	}

	@Test
	public void findsClearanceAcrossZoomAndPitchScales()
	{
		for (double pixelsPerUnit : new double[]{0.15, 0.4, 1.0, 1.8})
		{
			IntFunction<EscapeCrystalNotifyCrystalClearance.Bounds> projection = lift ->
				new EscapeCrystalNotifyCrystalClearance.Bounds(200,
					(int) Math.floor(800 - (lift + 80) * pixelsPerUnit), 260,
					(int) Math.ceil(800 - (lift - 80) * pixelsPerUnit));
			int result = new EscapeCrystalNotifyCrystalClearance().solve(100, 650, 0, 0, 1000, 1000, projection);
			assertTrue("scale " + pixelsPerUnit, result > 0);
			assertTrue(projection.apply(result).bottom <= 650);
			assertTrue(projection.apply(result).top >= 0);
		}
	}

	@Test
	public void hidesWhenClippedUnprojectableOrLiftingCannotClear()
	{
		EscapeCrystalNotifyCrystalClearance clearance = new EscapeCrystalNotifyCrystalClearance();
		assertEquals(-1, clearance.solve(100, 300, 0, 0, 500, 500, lift -> null));
		assertEquals(-1, clearance.solve(100, 0, 0, 0, 500, 500,
			lift -> { throw new AssertionError("No room, so projection should not run"); }));
		assertEquals(-1, clearance.solve(100, 300, 0, 0, 500, 500,
			lift -> new EscapeCrystalNotifyCrystalClearance.Bounds(10, 350, 50, 400)));
		assertEquals(-1, clearance.solve(100, 300, 0, 0, 500, 500,
			lift -> new EscapeCrystalNotifyCrystalClearance.Bounds(10, -10, 50, 100)));
		assertEquals(-1, clearance.solve(100, 300, 0, 0, 500, 500,
			lift -> new EscapeCrystalNotifyCrystalClearance.Bounds(-10, 10, 50, 100)));
	}

	@Test
	public void reusesHeightWithTwoProjectionsAndStillSettlesAndRechecksClipping()
	{
		EscapeCrystalNotifyCrystalClearance clearance = new EscapeCrystalNotifyCrystalClearance();
		int[] calls = {0};
		IntFunction<EscapeCrystalNotifyCrystalClearance.Bounds> projection = lift -> {
			calls[0]++;
			return new EscapeCrystalNotifyCrystalClearance.Bounds(200, 700 - lift, 260, 800 - lift);
		};
		assertEquals(400, clearance.solve(100, 400, 0, 0, 1000, 1000, projection));
		int coldCalls = calls[0];
		calls[0] = 0;
		for (int i = 0; i < 100; i++)
		{
			assertEquals(400, clearance.solve(100, 400, 0, 0, 1000, 1000, projection));
		}
		assertEquals(200, calls[0]);
		assertTrue(coldCalls > 2);
		System.out.println("Clearance projection batches: cold=" + coldCalls + ", unchanged frame=2");
		assertEquals(300, clearance.solve(100, 500, 0, 0, 1000, 1000, projection));
		assertEquals(500, clearance.solve(100, 300, 0, 0, 1000, 1000, projection));
		assertEquals(-1, clearance.solve(100, 300, 0, 250, 1000, 1000, projection));
		assertEquals(500, clearance.solve(100, 300, 0, 0, 1000, 1000, projection));
	}

	@Test
	public void cachedSearchMatchesFreshSearchAcrossChangingProjectionAndMinimum()
	{
		EscapeCrystalNotifyCrystalClearance cached = new EscapeCrystalNotifyCrystalClearance();
		java.util.Random random = new java.util.Random(42);
		for (int i = 0; i < 500; i++)
		{
			double scale = 0.1 + random.nextDouble() * 2;
			int minimum = 100 + random.nextInt(300);
			int target = 100 + random.nextInt(700);
			IntFunction<EscapeCrystalNotifyCrystalClearance.Bounds> projection = lift ->
				new EscapeCrystalNotifyCrystalClearance.Bounds(200,
					(int) Math.floor(800 - (lift + 80) * scale), 260,
					(int) Math.ceil(800 - (lift - 80) * scale));
			int fresh = new EscapeCrystalNotifyCrystalClearance().solve(minimum, target, 0, 0, 1000, 1000, projection);
			assertEquals("case " + i, fresh, cached.solve(minimum, target, 0, 0, 1000, 1000, projection));
		}
	}
}
