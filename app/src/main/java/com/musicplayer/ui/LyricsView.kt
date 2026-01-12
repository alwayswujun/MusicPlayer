package com.musicplayer.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.musicplayer.model.LyricLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 歌词滚动视图
 * 支持歌词同步滚动和高亮显示
 */
class LyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 歌词数据
    private var lyrics: List<LyricLine> = emptyList()

    // 当前播放位置（毫秒）
    private var currentPosition: Long = 0L

    // 当前高亮行索引
    private var currentLineIndex: Int = -1

    // 画笔
    private val normalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 文字大小
    private var normalTextSize: Float = 40f
    private var highlightTextSize: Float = 48f

    // 行间距
    private var lineSpacing: Float = 80f

    // 垂直偏移量（用于平滑滚动）
    private var offsetY: Float = 0f

    // 目标偏移量
    private var targetOffsetY: Float = 0f

    init {
        initPaints()
    }

    /**
     * 初始化画笔
     */
    private fun initPaints() {
        // 普通文本画笔
        normalTextPaint.apply {
            color = Color.GRAY
            textSize = normalTextSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // 高亮文本画笔
        highlightTextPaint.apply {
            color = Color.WHITE
            textSize = highlightTextSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(20f, 0f, 0f, Color.argb(100, 100, 100, 100))
        }
    }

    /**
     * 设置歌词
     */
    fun setLyrics(lyricLines: List<LyricLine>) {
        lyrics = lyricLines
        currentLineIndex = -1
        offsetY = 0f
        targetOffsetY = 0f
        invalidate()
    }

    /**
     * 更新当前播放位置
     */
    fun updatePosition(position: Long) {
        currentPosition = position

        // 计算当前应该高亮的行
        var newLineIndex = -1
        for (i in lyrics.indices) {
            if (position < lyrics[i].time) {
                newLineIndex = maxOf(0, i - 1)
                break
            }
        }

        // 如果是最后一行
        if (newLineIndex == -1 && lyrics.isNotEmpty()) {
            newLineIndex = lyrics.size - 1
        }

        // 如果行发生变化，更新偏移量
        if (newLineIndex != currentLineIndex && newLineIndex >= 0) {
            currentLineIndex = newLineIndex
            targetOffsetY = currentLineIndex * lineSpacing
            invalidate()
        }

        // 平滑滚动动画
        if (kotlin.math.abs(offsetY - targetOffsetY) > 0.5f) {
            offsetY += (targetOffsetY - offsetY) * 0.1f
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (lyrics.isEmpty()) {
            // 没有歌词时显示提示
            highlightTextPaint.color = Color.GRAY
            canvas.drawText(
                "暂无歌词",
                width / 2f,
                height / 2f,
                highlightTextPaint
            )
            return
        }

        // 计算Y坐标起始位置（屏幕中心）
        val centerY = height / 2f

        // 绘制每一行歌词
        lyrics.forEachIndexed { index, lyricLine ->
            val y = centerY - offsetY + index * lineSpacing

            // 只绘制可见范围内的歌词
            if (y < -lineSpacing || y > height + lineSpacing) {
                return@forEachIndexed
            }

            val paint = if (index == currentLineIndex) {
                highlightTextPaint
            } else {
                normalTextPaint
            }

            canvas.drawText(lyricLine.text, width / 2f, y, paint)
        }
    }

    /**
     * 重置歌词视图
     */
    fun reset() {
        lyrics = emptyList()
        currentPosition = 0L
        currentLineIndex = -1
        offsetY = 0f
        targetOffsetY = 0f
        invalidate()
    }

    /**
     * 设置普通文本颜色
     */
    fun setNormalTextColor(color: Int) {
        normalTextPaint.color = color
        invalidate()
    }

    /**
     * 设置高亮文本颜色
     */
    fun setHighlightTextColor(color: Int) {
        highlightTextPaint.color = color
        invalidate()
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(normalSize: Float, highlightSize: Float) {
        normalTextSize = normalSize
        highlightTextSize = highlightSize
        normalTextPaint.textSize = normalTextSize
        highlightTextPaint.textSize = highlightTextSize
        invalidate()
    }

    /**
     * 设置行间距
     */
    fun setLineSpacing(spacing: Float) {
        lineSpacing = spacing
        invalidate()
    }
}
