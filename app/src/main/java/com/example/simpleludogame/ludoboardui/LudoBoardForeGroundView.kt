package com.example.simpleludogame.ludoboardui

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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

    private var cachedMetrics: BoardMetrics? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val availableSize = min(w, h).toFloat()
            val cellSize = availableSize / gridSize
            val boardSize = cellSize * gridSize
            val left = (w - boardSize) / 2f
            val top = (h - boardSize) / 2f
            cachedMetrics = BoardMetrics(left, top, cellSize)
        }
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

        pulseAnimator = ValueAnimator.ofFloat(1f, 1.25f).apply {
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
        val metrics = cachedMetrics ?: return

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

        if (!setPositionOf(pawn, metrics, tempPoint)) return
        val fromX = tempPoint[0]
        val fromY = tempPoint[1]

        cellPawnsMap[oldCell]?.remove(pawn)
        if (cellPawnsMap[oldCell]?.isEmpty() == true) {
            cellPawnsMap.remove(oldCell)
        }

        pawnCellMap[pawn] = newCell
        cellPawnsMap.getOrPut(newCell) { mutableListOf() }.add(pawn)

        if (isHouse(newCell) && pawn !in pawnInHouseCellCoord) {
            pawnInHouseCellCoord[pawn] = allocateHomeSlot(pawn.player)
        }

        if (!setPositionOf(pawn, metrics, tempPoint)) return
        val toX = tempPoint[0]
        val toY = tempPoint[1]

        if (fromX == toX && fromY == toY) {
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
            animStartX = fromX
            animStartY = fromY
            animEndX = toX
            animEndY = toY
            animFraction = 0f
            start()
        }
    }

    private val tempPoint = FloatArray(2)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val metrics = cachedMetrics ?: return
        val baseHalf = metrics.cellSize * 0.7f

        // Draw non-animating pawns first
        for (pawn in pawnCellMap.keys) {
            if (pawn == animatingPawn) continue
            drawPawn(canvas, pawn, metrics, baseHalf)
        }
        
        // Draw animating pawn on top
        animatingPawn?.let {
            drawPawn(canvas, it, metrics, baseHalf)
        }
    }

    private fun drawPawn(canvas: Canvas, pawn: Pawn, metrics: BoardMetrics, baseHalf: Float) {
        if (!setDrawPosition(pawn, metrics, tempPoint)) return
        val cx = tempPoint[0]
        val cy = tempPoint[1]
        
        val d = drawableForPlayer(pawn.player) ?: return
        
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val metrics = cachedMetrics ?: return super.onTouchEvent(event)
            val hitRadius = metrics.cellSize * 0.5f
            val x = event.x
            val y = event.y
            
            // Iterate backwards to pick the one on top
            val keys = pawnCellMap.keys.toList().reversed()
            for (pawn in keys) {
                if (!selectablePawns.contains(pawn)) {
                    continue
                }

                if (!setDrawPosition(pawn, metrics, tempPoint)) continue
                val px = tempPoint[0]
                val py = tempPoint[1]
                
                val dx = x - px
                val dy = y - py
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

    private fun setDrawPosition(pawn: Pawn, metrics: BoardMetrics, outPoint: FloatArray): Boolean {
        if (pawn == animatingPawn && moveAnimator?.isRunning == true) {
            val t = animFraction
            outPoint[0] = animStartX + (animEndX - animStartX) * t
            outPoint[1] = animStartY + (animEndY - animStartY) * t
            return true
        }
        return setPositionOf(pawn, metrics, outPoint)
    }

    private fun isHouse(cell: Cell): Boolean =
        cell === inHouseCell || (cell.row == -1 && cell.col == -1)

    private fun setPositionOf(pawn: Pawn, metrics: BoardMetrics, outPoint: FloatArray): Boolean {
        val cell = pawnCellMap[pawn] ?: return false
        if (isHouse(cell)) {
            val slot = pawnInHouseCellCoord.getOrPut(pawn) { allocateHomeSlot(pawn.player) }
            return setHomeCircleCenter(pawn.player, slot, metrics, outPoint)
        }
        val list = cellPawnsMap[cell] ?: return false
        val idx = list.indexOf(pawn).takeIf { it >= 0 } ?: 0
        val baseX = metrics.left + (cell.col + 0.5f) * metrics.cellSize
        val baseY = metrics.top + (cell.row + 0.5f) * metrics.cellSize
        
        val d = metrics.cellSize * 0.22f
        val ox: Float
        val oy: Float
        when (list.size) {
            1 -> { ox = 0f; oy = 0f }
            2 -> { 
                ox = if (idx == 0) -d else d
                oy = 0f
            }
            3 -> when (idx) {
                0 -> { ox = 0f; oy = -d }
                1 -> { ox = -d * 0.9f; oy = d * 0.55f }
                else -> { ox = d * 0.9f; oy = d * 0.55f }
            }
            else -> {
                ox = if (idx % 2 == 0) -d else d
                oy = if (idx < 2) -d else d
            }
        }
        outPoint[0] = baseX + ox
        outPoint[1] = baseY + oy
        return true
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

    private fun setHomeCircleCenter(
        color: PlayerColors,
        slotIndex: Int,
        metrics: BoardMetrics,
        outPoint: FloatArray
    ): Boolean {
        val (startRow, startColumn) = paneOrigin(color)
        val left = metrics.left
        val top = metrics.top
        val cellSize = metrics.cellSize
        
        val innerCenterX = left + startColumn * cellSize + 3 * cellSize
        val innerCenterY = top + startRow * cellSize + 3 * cellSize
        
        val offset = cellSize * 1.1f
        val idx = slotIndex.coerceIn(0, 3)
        
        outPoint[0] = when (idx) {
            0, 2 -> innerCenterX - offset
            else -> innerCenterX + offset
        }
        outPoint[1] = when (idx) {
            0, 1 -> innerCenterY - offset
            else -> innerCenterY + offset
        }
        return true
    }

    private fun paneOrigin(color: PlayerColors): Pair<Int, Int> = when (color) {
        PlayerColors.GREEN -> 0 to 0
        PlayerColors.YELLOW -> 0 to 9
        PlayerColors.RED -> 9 to 0
        PlayerColors.BLUE -> 9 to 9
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
