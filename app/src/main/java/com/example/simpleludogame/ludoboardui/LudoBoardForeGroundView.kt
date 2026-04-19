package com.example.simpleludogame.ludoboardui

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import com.example.simpleludogame.R
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.Cell
import com.example.simpleludogame.game.gamemodel.ludomodel.cell.inHouseCell
import com.example.simpleludogame.game.gamemodel.ludomodel.pawn.Pawn
import com.example.simpleludogame.game.gamemodel.ludomodel.player.PlayerColors
import kotlin.math.min

class LudoBoardForeGroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val PAWN_MOVE_ANIMATION_DURATION_MS = 300L
        const val PULSE_ANIMATION_DURATION_MS = 600L
    }

    private val gridSize = 15

    private val greenPawnDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pawn_green)
    private val yellowPawnDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pawn_yellow)
    private val redPawnDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pawn_red)
    private val bluePawnDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pawn_blue)

    /** Slot index 0–3 inside the colored home pane; stable for each pawn once assigned. */
    var pawnInHouseCellCoord = mutableMapOf<Pawn, Int>()
    var pawnCellMap = mutableMapOf<Pawn, Cell>()
    var cellPawnsMap = mutableMapOf<Cell, MutableList<Pawn>>()

    /** Invoked when the user taps a pawn (after ACTION_UP). */
    var onPawnClickedObserver: ((Pawn) -> Unit)? = null

    private var moveAnimator: ValueAnimator? = null
    private var animatingPawn: Pawn? = null
    private var animStartX = 0f
    private var animStartY = 0f
    private var animEndX = 0f
    private var animEndY = 0f
    private var animFraction = 1f

    private var pulseAnimator: ValueAnimator? = null
    private var pulseScale = 1f
    private var selectablePawns: List<Pawn> = emptyList()

    init {
        setBackgroundColor(Color.TRANSPARENT)
        isClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        var side = min(w, h)
        if (side <= 0) {
            side = (resources.displayMetrics.density * 280f).toInt().coerceAtLeast(1)
        }
        setMeasuredDimension(side, side)
    }

    override fun onDetachedFromWindow() {
        moveAnimator?.cancel()
        moveAnimator = null
        animatingPawn = null
        pulseAnimator?.cancel()
        pulseAnimator = null
        super.onDetachedFromWindow()
    }
    
    fun setSelectablePawns(selectablePawns: List<Pawn>) {
        this.selectablePawns = selectablePawns
        animateSelectedPawns()
    }
    
    fun animateSelectedPawns() {
        pulseAnimator?.cancel()
        if (selectablePawns.isEmpty()) {
            pulseScale = 1f
            invalidate()
            return
        }

        pulseAnimator = ValueAnimator.ofFloat(1f, 1.5f).apply {
            duration = PULSE_ANIMATION_DURATION_MS
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                pulseScale = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun updatePawn(pawn: Pawn) {
        val newCell = pawn.getCell() ?: return
        if (width == 0 || height == 0) {
            post { updatePawn(pawn) }
            return
        }
        val metrics = boardMetrics() ?: return

        if (pawn !in pawnCellMap) {
            if (isHouse(newCell)) {
                val slot = allocateHomeSlot(pawn.player)
                pawnInHouseCellCoord[pawn] = slot
            }
            pawnCellMap[pawn] = newCell
            cellPawnsMap.getOrPut(newCell) { mutableListOf() }.add(pawn)
            invalidate()
            return
        }

        val oldCell = pawnCellMap[pawn]!!
        if (oldCell == newCell) {
            invalidate()
            return
        }

        val from = positionOf(pawn, metrics)
            ?: return

        cellPawnsMap[oldCell]?.remove(pawn)
        if (cellPawnsMap[oldCell]?.isEmpty() == true) {
            cellPawnsMap.remove(oldCell)
        }

        pawnCellMap[pawn] = newCell
        cellPawnsMap.getOrPut(newCell) { mutableListOf() }.add(pawn)

        if (isHouse(newCell) && pawn !in pawnInHouseCellCoord) {
            pawnInHouseCellCoord[pawn] = allocateHomeSlot(pawn.player)
        }

        val to = positionOf(pawn, metrics) ?: return

        if (from.first == to.first && from.second == to.second) {
            invalidate()
            return
        }

        moveAnimator?.cancel()
        moveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = PAWN_MOVE_ANIMATION_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animFraction = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (animatingPawn == pawn) {
                        animatingPawn = null
                    }
                    invalidate()
                }
            })
            animatingPawn = pawn
            animStartX = from.first
            animStartY = from.second
            animEndX = to.first
            animEndY = to.second
            animFraction = 0f
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val metrics = boardMetrics() ?: return
        val baseHalf = metrics.cellSize * 0.7f

        val drawOrder = pawnCellMap.keys.sortedBy { p ->
            if (p == animatingPawn) 1 else 0
        }

        for (pawn in drawOrder) {
            val (cx, cy) = drawPosition(pawn, metrics) ?: continue
            val d = drawableForPlayer(pawn.player) ?: continue
            
            val currentHalf = if (pawn in selectablePawns) {
                baseHalf * pulseScale
            } else {
                baseHalf
            }
            
            val l = (cx - currentHalf).toInt()
            val t = (cy - currentHalf).toInt()
            val r = (cx + currentHalf).toInt()
            val b = (cy + currentHalf).toInt()
            
            d.setBounds(l, t, r, b)
            d.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val metrics = boardMetrics() ?: return super.onTouchEvent(event)
            val hitRadius = metrics.cellSize * 0.5f
            val x = event.x
            val y = event.y
            for (pawn in pawnCellMap.keys.reversed()) {
                if (!selectablePawns.contains(pawn)) {
                    continue
                }

                val pos = drawPosition(pawn, metrics) ?: continue
                val dx = x - pos.first
                val dy = y - pos.second
                if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                    onPawnClickedObserver?.invoke(pawn)
                    performClick()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun boardMetrics(): BoardMetrics? {
        if (width == 0 || height == 0) return null
        val availableSize = min(width, height).toFloat()
        val cellSize = availableSize / gridSize
        val boardSize = cellSize * gridSize
        val left = (width - boardSize) / 2f
        val top = (height - boardSize) / 2f
        return BoardMetrics(left, top, cellSize)
    }

    private fun drawPosition(pawn: Pawn, metrics: BoardMetrics): Pair<Float, Float>? {
        if (pawn == animatingPawn && moveAnimator?.isRunning == true) {
            val t = animFraction
            return animStartX + (animEndX - animStartX) * t to
                animStartY + (animEndY - animStartY) * t
        }
        return positionOf(pawn, metrics)
    }

    private fun isHouse(cell: Cell): Boolean =
        cell === inHouseCell || (cell.row == -1 && cell.col == -1)

    private fun positionOf(pawn: Pawn, metrics: BoardMetrics): Pair<Float, Float>? {
        val cell = pawnCellMap[pawn] ?: return null
        if (isHouse(cell)) {
            val slot = pawnInHouseCellCoord.getOrPut(pawn) { allocateHomeSlot(pawn.player) }
            return homeCircleCenter(pawn.player, slot, metrics)
        }
        val list = cellPawnsMap[cell] ?: return null
        val idx = list.indexOf(pawn).takeIf { it >= 0 } ?: 0
        val baseX = metrics.left + (cell.col + 0.5f) * metrics.cellSize
        val baseY = metrics.top + (cell.row + 0.5f) * metrics.cellSize
        val (ox, oy) = stackOffset(list.size, idx, metrics.cellSize)
        return baseX + ox to baseY + oy
    }

    private fun allocateHomeSlot(color: PlayerColors): Int {
        val used = mutableSetOf<Int>()
        cellPawnsMap[inHouseCell]?.filter { it.player == color }?.forEach { p ->
            pawnInHouseCellCoord[p]?.let { used.add(it) }
        }
        for (i in 0..3) {
            if (i !in used) return i
        }
        return 0
    }

    private fun homeCircleCenter(
        color: PlayerColors,
        slotIndex: Int,
        metrics: BoardMetrics
    ): Pair<Float, Float> {
        val (startRow, startColumn) = paneOrigin(color)
        val left = metrics.left
        val top = metrics.top
        val cellSize = metrics.cellSize
        val outer = RectF(
            left + startColumn * cellSize,
            top + startRow * cellSize,
            left + (startColumn + 6) * cellSize,
            top + (startRow + 6) * cellSize
        )
        val inset = cellSize * 0.9f
        val inner = RectF(
            outer.left + inset,
            outer.top + inset,
            outer.right - inset,
            outer.bottom - inset
        )
        val offset = cellSize * 1.1f
        val centerX = inner.centerX()
        val centerY = inner.centerY()
        val centers = listOf(
            centerX - offset to centerY - offset,
            centerX + offset to centerY - offset,
            centerX - offset to centerY + offset,
            centerX + offset to centerY + offset
        )
        val idx = slotIndex.coerceIn(0, 3)
        return centers[idx]
    }

    private fun paneOrigin(color: PlayerColors): Pair<Int, Int> = when (color) {
        PlayerColors.GREEN -> 0 to 0
        PlayerColors.YELLOW -> 0 to 9
        PlayerColors.RED -> 9 to 0
        PlayerColors.BLUE -> 9 to 9
    }

    private fun stackOffset(count: Int, index: Int, cellSize: Float): Pair<Float, Float> {
        val d = cellSize * 0.22f
        return when (count) {
            1 -> 0f to 0f
            2 -> if (index == 0) -d to 0f else d to 0f
            3 -> when (index) {
                0 -> 0f to -d
                1 -> -d * 0.9f to d * 0.55f
                else -> d * 0.9f to d * 0.55f
            }
            else -> when (index % 4) {
                0 -> -d to -d
                1 -> d to -d
                2 -> -d to d
                else -> d to d
            }
        }
    }

    private fun drawableForPlayer(color: PlayerColors): Drawable? = when (color) {
        PlayerColors.GREEN -> greenPawnDrawable
        PlayerColors.YELLOW -> yellowPawnDrawable
        PlayerColors.RED -> redPawnDrawable
        PlayerColors.BLUE -> bluePawnDrawable
    }

    private data class BoardMetrics(
        val left: Float,
        val top: Float,
        val cellSize: Float
    )
}
