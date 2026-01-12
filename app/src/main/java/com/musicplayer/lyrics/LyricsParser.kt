package com.musicplayer.lyrics

import android.content.Context
import com.musicplayer.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.regex.Pattern

/**
 * 歌词解析器
 * 支持解析标准LRC格式歌词
 */
class LyricsParser(private val context: Context) {

    companion object {
        private const val LRC_LINE_PATTERN = "\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)"
    }

    /**
     * 从URL解析歌词
     */
    suspend fun parseFromUrl(lyricsUrl: String): Result<List<LyricLine>> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(lyricsUrl).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                val lrcText = reader.readText()
                Result.success(parseLrc(lrcText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从本地文件解析歌词
     */
    suspend fun parseFromLocal(filePath: String): Result<List<LyricLine>> = withContext(Dispatchers.IO) {
        try {
            val lrcText = context.assets.open(filePath).bufferedReader().use { it.readText() }
            Result.success(parseLrc(lrcText))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从字符串解析歌词
     */
    fun parseFromString(lrcText: String): List<LyricLine> {
        return parseLrc(lrcText)
    }

    /**
     * 解析LRC格式歌词
     * 格式: [mm:ss.ms]歌词内容
     */
    private fun parseLrc(lrcText: String): List<LyricLine> {
        val lyricLines = mutableListOf<LyricLine>()
        val pattern = Pattern.compile(LRC_LINE_PATTERN)

        lrcText.lines().forEach { line ->
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
                val minutes = matcher.group(1).toLong()
                val seconds = matcher.group(2).toLong()
                val milliseconds = matcher.group(3).toLong()
                val text = matcher.group(4).trim()

                val time = minutes * 60 * 1000 + seconds * 1000 + milliseconds
                if (text.isNotEmpty()) {
                    lyricLines.add(LyricLine(time, text))
                }
            }
        }

        return lyricLines.sortedBy { it.time }
    }

    /**
     * 获取当前应该显示的歌词行索引
     */
    fun getCurrentLineIndex(lyrics: List<LyricLine>, currentPosition: Long): Int {
        if (lyrics.isEmpty()) return -1

        for (i in lyrics.indices) {
            if (currentPosition < lyrics[i].time) {
                return maxOf(0, i - 1)
            }
        }

        return lyrics.size - 1
    }
}
