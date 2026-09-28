package io.github.mangi.flymefreeform.gesture

import io.github.mangi.flymefreeform.config.ModulePreferences
import kotlin.math.hypot

/** 左右下角共享的四分之一圆起点区域。 */
internal object CornerTriggerRegion {
    fun radiusPx(rangeDp: Int, density: Float): Float {
        val safeDensity = density.takeIf { it.isFinite() && it > 0f } ?: 1f
        return ModulePreferences.coerceCornerTriggerRangeDp(rangeDp) * safeDensity
    }

    /**
     * 抢断阈值：角区内出现这么一点「向内」位移就视为我方要接管指针流。
     * 要早于系统手势的识别窗口，否则边缘条带里会被系统监视器后手抢走。
     */
    fun claimInwardPx(density: Float): Float {
        val safeDensity = density.takeIf { it.isFinite() && it > 0f } ?: 1f
        return CLAIM_INWARD_DP * safeDensity
    }

    /** 左右镜像的「向内」位移（左角向右、右角向左为正）。 */
    fun inwardDistance(side: CornerSide, downX: Float, x: Float): Float =
        when (side) {
            CornerSide.Left -> x - downX
            CornerSide.Right -> downX - x
        }

    /** 未被选中的角落触摸是否该原样补发回应用：全程位移不超过触控抖动才算一次点击。 */
    fun tapReplayable(maxDisplacement: Float, touchSlop: Float): Boolean =
        maxDisplacement < touchSlop.coerceAtLeast(1f)

    fun detectSide(
        x: Float,
        y: Float,
        displayWidth: Float,
        displayHeight: Float,
        radius: Float,
        leftEnabled: Boolean,
        rightEnabled: Boolean,
    ): CornerSide? {
        val fromBottom = displayHeight - y
        if (fromBottom < 0f || fromBottom > radius) return null
        if (leftEnabled && x >= 0f && hypot(x, fromBottom) <= radius) {
            return CornerSide.Left
        }
        val fromRight = displayWidth - x
        if (rightEnabled && fromRight >= 0f && hypot(fromRight, fromBottom) <= radius) {
            return CornerSide.Right
        }
        return null
    }

    private const val CLAIM_INWARD_DP = 3f
}
