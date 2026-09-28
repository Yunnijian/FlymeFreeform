package io.github.mangi.flymefreeform.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CornerTriggerRegionTest {
    @Test
    fun radiusUsesClampedDpAndDensity() {
        assertEquals(48f, CornerTriggerRegion.radiusPx(8, 2f), 0f)
        assertEquals(168f, CornerTriggerRegion.radiusPx(84, 2f), 0f)
        assertEquals(320f, CornerTriggerRegion.radiusPx(200, 2f), 0f)
    }

    @Test
    fun detectsMirroredQuarterCirclesAndHonorsSideSwitches() {
        assertEquals(
            CornerSide.Left,
            CornerTriggerRegion.detectSide(30f, 1960f, 1000f, 2000f, 50f, true, true),
        )
        assertEquals(
            CornerSide.Right,
            CornerTriggerRegion.detectSide(970f, 1960f, 1000f, 2000f, 50f, true, true),
        )
        assertNull(
            CornerTriggerRegion.detectSide(30f, 1960f, 1000f, 2000f, 50f, false, true),
        )
        assertNull(
            CornerTriggerRegion.detectSide(40f, 1960f, 1000f, 2000f, 50f, true, true),
        )
    }

    @Test
    fun claimThresholdScalesWithDensityAndGuardsInvalidInput() {
        assertEquals(3f, CornerTriggerRegion.claimInwardPx(1f), 0f)
        assertEquals(9f, CornerTriggerRegion.claimInwardPx(3f), 0f)
        assertEquals(3f, CornerTriggerRegion.claimInwardPx(0f), 0f)
        assertEquals(3f, CornerTriggerRegion.claimInwardPx(Float.NaN), 0f)
    }

    @Test
    fun inwardDistanceIsMirroredBetweenSides() {
        // 左角：向右为正；右角：向左为正。反向位移记为负。
        assertEquals(12f, CornerTriggerRegion.inwardDistance(CornerSide.Left, 30f, 42f), 0f)
        assertEquals(-7f, CornerTriggerRegion.inwardDistance(CornerSide.Left, 30f, 23f), 0f)
        assertEquals(9f, CornerTriggerRegion.inwardDistance(CornerSide.Right, 970f, 961f), 0f)
        assertEquals(-5f, CornerTriggerRegion.inwardDistance(CornerSide.Right, 970f, 975f), 0f)
    }

    @Test
    fun tapReplayOnlyForStreamsWithinSlop() {
        // 全程位移不超过触控抖动才算一次点击，值得原样补发。
        assertTrue(CornerTriggerRegion.tapReplayable(0f, 24f))
        assertTrue(CornerTriggerRegion.tapReplayable(23.9f, 24f))
        assertFalse(CornerTriggerRegion.tapReplayable(24f, 24f))
        assertFalse(CornerTriggerRegion.tapReplayable(60f, 24f))
        // 抖动阈值为 0 时按 1px 保底，避免任意微动都被判成点击。
        assertTrue(CornerTriggerRegion.tapReplayable(0.5f, 0f))
        assertFalse(CornerTriggerRegion.tapReplayable(1.5f, 0f))
    }
}
