package com.example.simpleludogame.ludoboardui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.simpleludogame.R
import kotlin.math.min

class LudoBoardForeGroundView @JvmOverloads constructor(
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val availableSize = min(width, height).toFloat()
        val cellSize = availableSize / gridSize
        val boardSize = cellSize * gridSize
        val left = (width - boardSize) / 2f
        val top = (height - boardSize) / 2f
        val boardRect = RectF(left, top, left + boardSize, top + boardSize)

        canvas.drawRoundRect(boardRect, cellSize * 0.45f, cellSize * 0.45f, boardBackgroundPaint)

    }
}