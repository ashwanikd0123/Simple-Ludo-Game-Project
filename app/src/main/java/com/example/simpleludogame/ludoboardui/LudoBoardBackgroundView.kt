package com.example.simpleludogame.ludoboardui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.simpleludogame.R
import kotlin.math.min

class LudoBoardBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridSize = 15

    private val boardBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ludo_board_background)
        style = Paint.Style.FILL
    }

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ludo_board_stroke)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
    }

    private val tokenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val starDrawable: Drawable? = AppCompatResources.getDrawable(context, R.drawable.ic_ludo_star)

    private val greenColor = ContextCompat.getColor(context, R.color.ludo_green)
    private val yellowColor = ContextCompat.getColor(context, R.color.ludo_yellow)
    private val redColor = ContextCompat.getColor(context, R.color.ludo_red)
    private val blueColor = ContextCompat.getColor(context, R.color.ludo_blue)
    private val neutralCellColor = ContextCompat.getColor(context, R.color.white)
    private val tokenBaseColor = ContextCompat.getColor(context, R.color.ludo_token_base)
    private val starTintColor = ContextCompat.getColor(context, R.color.ludo_star_tint)

    companion object {
        /** 0-based (row, column); 1-based board cell (1, 8). */
        private val yellowStartPointCell = 1 to 8

        /** 0-based (row, column); 1-based (6, 2). */
        private val greenStartPointCell = 6 to 1

        /** 0-based (row, column); 1-based (8, 13). */
        private val blueStartPointCell = 8 to 13

        /** 0-based (row, column); 1-based (13, 7). */
        private val redStartPointCell = 13 to 6
    }

    private var cachedBitmap: android.graphics.Bitmap? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        var side = min(w, h)
        if (side <= 0) {
            side = (resources.displayMetrics.density * 280f).toInt().coerceAtLeast(1)
        }
        setMeasuredDimension(side, side)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            updateBitmap(w, h)
        }
    }

    private fun updateBitmap(w: Int, h: Int) {
        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawToCanvas(canvas, w, h)
        cachedBitmap = bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cachedBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        } ?: run {
            drawToCanvas(canvas, width, height)
        }
    }

    private fun drawToCanvas(canvas: Canvas, width: Int, height: Int) {
        val availableSize = min(width, height).toFloat()
        val cellSize = availableSize / gridSize
        val boardSize = cellSize * gridSize
        val left = (width - boardSize) / 2f
        val top = (height - boardSize) / 2f
        val boardRect = RectF(left, top, left + boardSize, top + boardSize)

        canvas.drawRoundRect(boardRect, cellSize * 0.45f, cellSize * 0.45f, boardBackgroundPaint)

        val tempRect = RectF()
        for (row in 0 until gridSize) {
            for (column in 0 until gridSize) {
                setCellRect(tempRect, left, top, cellSize, row, column)
                cellPaint.color = colorStartPoints(row, column) ?: colorForCell(row, column)
                canvas.drawRect(tempRect, cellPaint)
                canvas.drawRect(tempRect, strokePaint)
            }
        }

        drawHomePane(canvas, left, top, cellSize, startRow = 0, startColumn = 0, paneColor = greenColor)
        drawHomePane(canvas, left, top, cellSize, startRow = 0, startColumn = 9, paneColor = yellowColor)
        drawHomePane(canvas, left, top, cellSize, startRow = 9, startColumn = 0, paneColor = redColor)
        drawHomePane(canvas, left, top, cellSize, startRow = 9, startColumn = 9, paneColor = blueColor)

        drawGoal(canvas, left, top, cellSize)
        drawStarCells(canvas, left, top, cellSize)
    }

    private fun drawHomePane(
        canvas: Canvas,
        left: Float,
        top: Float,
        cellSize: Float,
        startRow: Int,
        startColumn: Int,
        paneColor: Int
    ) {
        val outer = RectF(
            left + startColumn * cellSize,
            top + startRow * cellSize,
            left + (startColumn + 6) * cellSize,
            top + (startRow + 6) * cellSize
        )

        cellPaint.color = paneColor
        canvas.drawRect(outer, cellPaint)
        canvas.drawRect(outer, strokePaint)

        val inset = cellSize * 0.9f
        val inner = RectF(
            outer.left + inset,
            outer.top + inset,
            outer.right - inset,
            outer.bottom - inset
        )

        cellPaint.color = tokenBaseColor
        canvas.drawRoundRect(inner, cellSize * 0.35f, cellSize * 0.35f, cellPaint)
        canvas.drawRoundRect(inner, cellSize * 0.35f, cellSize * 0.35f, strokePaint)

        val tokenRadius = cellSize * 0.42f
        val offset = cellSize * 1.1f
        val centerX = inner.centerX()
        val centerY = inner.centerY()

        tokenPaint.color = paneColor
        
        canvas.drawCircle(centerX - offset, centerY - offset, tokenRadius, tokenPaint)
        canvas.drawCircle(centerX - offset, centerY - offset, tokenRadius, strokePaint)
        
        canvas.drawCircle(centerX + offset, centerY - offset, tokenRadius, tokenPaint)
        canvas.drawCircle(centerX + offset, centerY - offset, tokenRadius, strokePaint)
        
        canvas.drawCircle(centerX - offset, centerY + offset, tokenRadius, tokenPaint)
        canvas.drawCircle(centerX - offset, centerY + offset, tokenRadius, strokePaint)
        
        canvas.drawCircle(centerX + offset, centerY + offset, tokenRadius, tokenPaint)
        canvas.drawCircle(centerX + offset, centerY + offset, tokenRadius, strokePaint)
    }

    private fun drawGoal(canvas: Canvas, left: Float, top: Float, cellSize: Float) {
        val goalLeft = left + 6 * cellSize
        val goalTop = top + 6 * cellSize
        val goalRight = goalLeft + 3 * cellSize
        val goalBottom = goalTop + 3 * cellSize
        val centerX = (goalLeft + goalRight) / 2f
        val centerY = (goalTop + goalBottom) / 2f

        val path = Path()
        
        // Left triangle (Green)
        path.reset()
        path.moveTo(goalLeft, goalTop)
        path.lineTo(goalLeft, goalBottom)
        path.lineTo(centerX, centerY)
        path.close()
        cellPaint.color = greenColor
        canvas.drawPath(path, cellPaint)
        
        // Bottom triangle (Red)
        path.reset()
        path.moveTo(goalLeft, goalBottom)
        path.lineTo(goalRight, goalBottom)
        path.lineTo(centerX, centerY)
        path.close()
        cellPaint.color = redColor
        canvas.drawPath(path, cellPaint)
        
        // Right triangle (Blue)
        path.reset()
        path.moveTo(goalRight, goalTop)
        path.lineTo(goalRight, goalBottom)
        path.lineTo(centerX, centerY)
        path.close()
        cellPaint.color = blueColor
        canvas.drawPath(path, cellPaint)
        
        // Top triangle (Yellow)
        path.reset()
        path.moveTo(goalLeft, goalTop)
        path.lineTo(goalRight, goalTop)
        path.lineTo(centerX, centerY)
        path.close()
        cellPaint.color = yellowColor
        canvas.drawPath(path, cellPaint)

        canvas.drawRect(goalLeft, goalTop, goalRight, goalBottom, strokePaint)
        canvas.drawLine(goalLeft, goalTop, goalRight, goalBottom, strokePaint)
        canvas.drawLine(goalRight, goalTop, goalLeft, goalBottom, strokePaint)
    }

    private fun drawStarCells(canvas: Canvas, left: Float, top: Float, cellSize: Float) {
        val starBounds = listOf(
            2 to 6,
            8 to 2,
            6 to 12,
            12 to 8
        )

        val tempRect = RectF()
        starBounds.forEach { (row, column) ->
            setCellRect(tempRect, left, top, cellSize, row, column)
            val star = starDrawable ?: return@forEach
            DrawableCompat.setTint(star, starTintColor)
            val inset = (cellSize * 0.18f).toInt()
            star.setBounds(
                tempRect.left.toInt() + inset,
                tempRect.top.toInt() + inset,
                tempRect.right.toInt() - inset,
                tempRect.bottom.toInt() - inset
            )
            star.draw(canvas)
        }
    }

    private fun colorStartPoints(row: Int, column: Int): Int? {
        return when (row to column) {
            yellowStartPointCell -> yellowColor
            greenStartPointCell -> greenColor
            blueStartPointCell -> blueColor
            redStartPointCell -> redColor
            else -> null
        }
    }

    private fun colorForCell(row: Int, column: Int): Int {
        return when {
            row == 7 && column in 1..5 -> greenColor
            column == 7 && row in 1..5 -> yellowColor
            column == 7 && row in 9..13 -> redColor
            row == 7 && column in 9..13 -> blueColor
            else -> neutralCellColor
        }
    }

    private fun setCellRect(
        rect: RectF,
        left: Float,
        top: Float,
        cellSize: Float,
        row: Int,
        column: Int
    ) {
        val cellLeft = left + column * cellSize
        val cellTop = top + row * cellSize
        rect.set(cellLeft, cellTop, cellLeft + cellSize, cellTop + cellSize)
    }

}