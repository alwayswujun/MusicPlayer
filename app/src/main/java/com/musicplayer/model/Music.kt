package com.musicplayer.model

/**
 * 音乐数据模型
 */
data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val url: String, // 本地路径或在线URL
    val coverUrl: String? = null,
    val duration: Long = 0, // 毫秒
    val lyricsUrl: String? = null, // 歌词URL或本地路径
    val isLocal: Boolean = false // 是否为本地音乐
)

/**
 * 歌词行
 */
data class LyricLine(
    val time: Long, // 时间戳（毫秒）
    val text: String // 歌词文本
)
